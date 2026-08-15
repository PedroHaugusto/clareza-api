package com.clareza.infrastructure.adapter.in.web;

import com.clareza.TesteDeIntegracao;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@Transactional
class TransacaoParceladaControllerTest extends TesteDeIntegracao {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String token;
    private Long contaId;
    private Long categoriaId;

    @BeforeEach
    void prepararCenario() throws Exception {
        String registro = mockMvc.perform(post("/api/auth/registrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Ana\",\"email\":\"ana@clareza.dev\","
                                + "\"senha\":\"senha-secreta\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        token = objectMapper.readTree(registro).get("token").asText();

        String contas = mockMvc.perform(get("/api/contas").header("Authorization", "Bearer " + token))
                .andReturn().getResponse().getContentAsString();
        contaId = objectMapper.readTree(contas).get(0).get("id").asLong();

        String categorias = mockMvc.perform(get("/api/categorias")
                        .header("Authorization", "Bearer " + token))
                .andReturn().getResponse().getContentAsString();
        categoriaId = objectMapper.readTree(categorias).get(0).get("id").asLong();
    }

    @Test
    @DisplayName("1200 em 3 vezes gera tres lancamentos de 400 no mesmo grupo")
    void deveGerarAsParcelas() throws Exception {
        String resposta = mockMvc.perform(post("/api/transacoes/parcelada")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo("Geladeira", "1200.00", 3, LocalDate.now().plusDays(10))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[*].numeroParcela", contains(1, 2, 3)))
                .andExpect(jsonPath("$[*].totalParcelas", contains(3, 3, 3)))
                .andExpect(jsonPath("$[0].valor", is(400.00)))
                .andReturn().getResponse().getContentAsString();

        JsonNode json = objectMapper.readTree(resposta);
        String grupo = json.get(0).get("grupoParcelamentoId").asText();
        assertThat(grupo).isNotEmpty();
        assertThat(json.get(1).get("grupoParcelamentoId").asText()).isEqualTo(grupo);
        assertThat(json.get(2).get("grupoParcelamentoId").asText()).isEqualTo(grupo);
    }

    @Test
    @DisplayName("a sobra de centavos cai na ultima parcela e a soma fecha com o total")
    void deveAbsorverCentavosNaUltimaParcela() throws Exception {
        String resposta = mockMvc.perform(post("/api/transacoes/parcelada")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo("Curso", "100.00", 3, LocalDate.now())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$[0].valor", is(33.33)))
                .andExpect(jsonPath("$[1].valor", is(33.33)))
                .andExpect(jsonPath("$[2].valor", is(33.34)))
                .andReturn().getResponse().getContentAsString();

        BigDecimal soma = BigDecimal.ZERO;
        for (JsonNode parcela : objectMapper.readTree(resposta)) {
            soma = soma.add(parcela.get("valor").decimalValue());
        }
        assertThat(soma).isEqualByComparingTo(new BigDecimal("100.00"));
    }

    @Test
    @DisplayName("as parcelas caem de mes em mes")
    void deveAvancarUmMesPorParcela() throws Exception {
        LocalDate primeira = LocalDate.of(2026, 1, 31);

        mockMvc.perform(post("/api/transacoes/parcelada")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo("Geladeira", "300.00", 3, primeira)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$[*].dataPrevista",
                        contains("2026-01-31", "2026-02-28", "2026-03-31")));
    }

    @Test
    @DisplayName("as parcelas entram na listagem normal e podem ser filtradas")
    void asParcelasDevemAparecerNaListagem() throws Exception {
        mockMvc.perform(post("/api/transacoes/parcelada")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo("Geladeira", "1200.00", 3, LocalDate.now())))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/transacoes").header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$", hasSize(3)));

        mockMvc.perform(get("/api/transacoes").param("busca", "gelad")
                        .header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$", hasSize(3)));
    }

    @Test
    @DisplayName("uma parcela so responde 400")
    void deveRecusarParcelamentoDeUmaParcela() throws Exception {
        mockMvc.perform(post("/api/transacoes/parcelada")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo("Geladeira", "1200.00", 1, LocalDate.now())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos[0].campo", is("totalParcelas")));
    }

    @Test
    @DisplayName("valor baixo demais para a quantidade de parcelas responde 422")
    void deveRecusarValorBaixoDemais() throws Exception {
        mockMvc.perform(post("/api/transacoes/parcelada")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo("Bala", "0.02", 3, LocalDate.now())))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @DisplayName("sem token nao se parcela nada")
    void deveExigirAutenticacao() throws Exception {
        mockMvc.perform(post("/api/transacoes/parcelada")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo("Geladeira", "1200.00", 3, LocalDate.now())))
                .andExpect(status().isUnauthorized());
    }

    private String corpo(String descricao, String valorTotal, int totalParcelas, LocalDate primeira) {
        return String.format(
                "{\"contaId\":%d,\"categoriaId\":%d,\"descricao\":\"%s\",\"valorTotal\":%s,"
                        + "\"tipo\":\"DESPESA\",\"dataDaPrimeiraParcela\":\"%s\",\"totalParcelas\":%d}",
                contaId, categoriaId, descricao, valorTotal, primeira, totalParcelas);
    }
}
