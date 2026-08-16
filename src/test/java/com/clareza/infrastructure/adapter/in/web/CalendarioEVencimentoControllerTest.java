package com.clareza.infrastructure.adapter.in.web;

import com.clareza.TesteDeIntegracao;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@Transactional
class CalendarioEVencimentoControllerTest extends TesteDeIntegracao {

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
    @DisplayName("calendario agrupa por dia e devolve os totais do mes")
    void deveMontarOCalendarioDoMes() throws Exception {
        LocalDate base = LocalDate.of(2026, 8, 10);
        criar("Aluguel", "1200.00", "DESPESA", base);
        criar("Mercado", "89.90", "DESPESA", base);
        criar("Salario", "5000.00", "RECEITA", LocalDate.of(2026, 8, 5));

        mockMvc.perform(get("/api/calendario").param("mes", "8").param("ano", "2026")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mes", is(8)))
                .andExpect(jsonPath("$.ano", is(2026)))
                .andExpect(jsonPath("$.totalReceitas", is(5000.00)))
                .andExpect(jsonPath("$.totalDespesas", is(1289.90)))
                .andExpect(jsonPath("$.saldoDoMes", is(3710.10)))
                .andExpect(jsonPath("$.dias", hasSize(2)))
                .andExpect(jsonPath("$.dias[*].data", contains("2026-08-05", "2026-08-10")))
                .andExpect(jsonPath("$.dias[1].transacoes", hasSize(2)))
                .andExpect(jsonPath("$.dias[1].saldoDoDia", is(-1289.90)));
    }

    @Test
    @DisplayName("lancamento de outro mes nao entra no calendario consultado")
    void deveIgnorarOutrosMeses() throws Exception {
        criar("Do mes", "10.00", "DESPESA", LocalDate.of(2026, 8, 10));
        criar("Do mes seguinte", "99.00", "DESPESA", LocalDate.of(2026, 9, 10));

        mockMvc.perform(get("/api/calendario").param("mes", "8").param("ano", "2026")
                        .header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.dias", hasSize(1)))
                .andExpect(jsonPath("$.totalDespesas", is(10.00)));
    }

    @Test
    @DisplayName("sem mes e ano o calendario usa o mes corrente")
    void deveUsarOMesAtualPorPadrao() throws Exception {
        LocalDate hoje = hoje();

        mockMvc.perform(get("/api/calendario").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mes", is(hoje.getMonthValue())))
                .andExpect(jsonPath("$.ano", is(hoje.getYear())));
    }

    @Test
    @DisplayName("mes fora da faixa responde 422")
    void deveRecusarMesInvalido() throws Exception {
        mockMvc.perform(get("/api/calendario").param("mes", "13").param("ano", "2026")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @DisplayName("vencimentos trazem os proximos 14 dias e tambem o que ja venceu")
    void deveListarVencimentosIncluindoAtrasados() throws Exception {
        LocalDate hoje = hoje();
        criar("Atrasada", "50.00", "DESPESA", hoje.minusDays(5));
        criar("Vence hoje", "60.00", "DESPESA", hoje);
        criar("Vence em 14 dias", "70.00", "DESPESA", hoje.plusDays(14));
        criar("Vence em 15 dias", "80.00", "DESPESA", hoje.plusDays(15));

        mockMvc.perform(get("/api/vencimentos").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[*].descricao",
                        contains("Atrasada", "Vence hoje", "Vence em 14 dias")))
                .andExpect(jsonPath("$[0].status", is("ATRASADA")));
    }

    @Test
    @DisplayName("o que ja foi confirmado sai da lista de vencimentos")
    void naoDeveListarOQueJaFoiConfirmado() throws Exception {
        LocalDate hoje = hoje();
        Long id = criar("Ja paga", "50.00", "DESPESA", hoje.plusDays(2));
        criar("A pagar", "60.00", "DESPESA", hoje.plusDays(3));

        mockMvc.perform(patch("/api/transacoes/" + id + "/confirmar")
                .header("Authorization", "Bearer " + token)).andExpect(status().isOk());

        mockMvc.perform(get("/api/vencimentos").header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].descricao", is("A pagar")));
    }

    @Test
    @DisplayName("calendario e vencimentos so mostram dados do proprio usuario")
    void deveIsolarPorUsuario() throws Exception {
        criar("Aluguel", "1200.00", "DESPESA", hoje().plusDays(2));

        String outro = mockMvc.perform(post("/api/auth/registrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Bruno\",\"email\":\"bruno@clareza.dev\","
                                + "\"senha\":\"senha-secreta\"}"))
                .andReturn().getResponse().getContentAsString();
        String tokenDoBruno = objectMapper.readTree(outro).get("token").asText();

        mockMvc.perform(get("/api/vencimentos").header("Authorization", "Bearer " + tokenDoBruno))
                .andExpect(jsonPath("$", hasSize(0)));

        mockMvc.perform(get("/api/calendario").header("Authorization", "Bearer " + tokenDoBruno))
                .andExpect(jsonPath("$.dias", hasSize(0)))
                .andExpect(jsonPath("$.totalDespesas", is(0)));
    }

    @Test
    @DisplayName("as duas rotas exigem autenticacao")
    void deveExigirAutenticacao() throws Exception {
        mockMvc.perform(get("/api/calendario")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/vencimentos")).andExpect(status().isUnauthorized());
    }

    private Long criar(String descricao, String valor, String tipo, LocalDate data) throws Exception {
        String corpo = String.format(
                "{\"contaId\":%d,\"categoriaId\":%d,\"descricao\":\"%s\",\"valor\":%s,"
                        + "\"tipo\":\"%s\",\"dataPrevista\":\"%s\"}",
                contaId, categoriaId, descricao, valor, tipo, data);
        String resposta = mockMvc.perform(post("/api/transacoes")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(resposta).get("id").asLong();
    }
}
