package com.clareza.infrastructure.adapter.in.web;

import com.clareza.TesteDeIntegracao;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@Transactional
class TransacaoControllerTest extends TesteDeIntegracao {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("lancamento criado nasce previsto e aparece na listagem")
    void deveCriarEListar() throws Exception {
        Contexto contexto = prepararContexto("ana@clareza.dev");

        mockMvc.perform(post("/api/transacoes")
                        .header("Authorization", "Bearer " + contexto.token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo(contexto, "Conta de luz", "150.00",
                                hoje().plusDays(5), null)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.descricao", is("Conta de luz")))
                .andExpect(jsonPath("$.valor", is(150.00)))
                .andExpect(jsonPath("$.status", is("PREVISTA")))
                .andExpect(jsonPath("$.dataEfetivacao").doesNotExist());

        mockMvc.perform(get("/api/transacoes").header("Authorization", "Bearer " + contexto.token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @DisplayName("lancamento vencido e devolvido como ATRASADA sem nada ter rodado no banco")
    void deveDerivarStatusAtrasado() throws Exception {
        Contexto contexto = prepararContexto("ana@clareza.dev");

        mockMvc.perform(post("/api/transacoes")
                        .header("Authorization", "Bearer " + contexto.token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo(contexto, "Aluguel atrasado", "1200.00",
                                hoje().minusDays(3), null)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status", is("ATRASADA")));
    }

    @Test
    @DisplayName("informar data de efetivacao lanca ja confirmado")
    void deveCriarConfirmada() throws Exception {
        Contexto contexto = prepararContexto("ana@clareza.dev");

        mockMvc.perform(post("/api/transacoes")
                        .header("Authorization", "Bearer " + contexto.token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo(contexto, "Salario", "5000.00",
                                hoje().minusDays(2), hoje().minusDays(2))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status", is("CONFIRMADA")));
    }

    @Test
    @DisplayName("valor zero ou negativo responde 400 apontando o campo")
    void deveRecusarValorNaoPositivo() throws Exception {
        Contexto contexto = prepararContexto("ana@clareza.dev");

        mockMvc.perform(post("/api/transacoes")
                        .header("Authorization", "Bearer " + contexto.token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo(contexto, "Errado", "-50.00", hoje(), null)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos[0].campo", is("valor")));
    }

    @Test
    @DisplayName("usar conta de outro usuario responde 404")
    void deveRecusarContaDeOutroUsuario() throws Exception {
        Contexto daAna = prepararContexto("ana@clareza.dev");
        Contexto doBruno = prepararContexto("bruno@clareza.dev");

        String corpoComContaAlheia = String.format(
                "{\"contaId\":%d,\"categoriaId\":%d,\"descricao\":\"Invasao\","
                        + "\"valor\":10.00,\"tipo\":\"DESPESA\",\"dataPrevista\":\"%s\"}",
                daAna.contaId, doBruno.categoriaId, hoje());

        mockMvc.perform(post("/api/transacoes")
                        .header("Authorization", "Bearer " + doBruno.token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoComContaAlheia))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("edicao altera os dados e mantem o id")
    void deveEditar() throws Exception {
        Contexto contexto = prepararContexto("ana@clareza.dev");
        Long id = criarTransacao(contexto, "Conta de luz", "150.00");

        mockMvc.perform(put("/api/transacoes/" + id)
                        .header("Authorization", "Bearer " + contexto.token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo(contexto, "Conta de luz revisada", "180.50",
                                hoje().plusDays(5), null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(id.intValue())))
                .andExpect(jsonPath("$.descricao", is("Conta de luz revisada")))
                .andExpect(jsonPath("$.valor", is(180.50)));
    }

    @Test
    @DisplayName("lancamento e excluido e some da listagem")
    void deveExcluir() throws Exception {
        Contexto contexto = prepararContexto("ana@clareza.dev");
        Long id = criarTransacao(contexto, "Conta de luz", "150.00");

        mockMvc.perform(delete("/api/transacoes/" + id)
                        .header("Authorization", "Bearer " + contexto.token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/transacoes").header("Authorization", "Bearer " + contexto.token))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("os lancamentos de um usuario sao invisiveis para outro")
    void deveIsolarTransacoesEntreUsuarios() throws Exception {
        Contexto daAna = prepararContexto("ana@clareza.dev");
        Contexto doBruno = prepararContexto("bruno@clareza.dev");

        Long id = criarTransacao(daAna, "Conta de luz", "150.00");

        mockMvc.perform(get("/api/transacoes").header("Authorization", "Bearer " + doBruno.token))
                .andExpect(jsonPath("$", hasSize(0)));

        mockMvc.perform(delete("/api/transacoes/" + id)
                        .header("Authorization", "Bearer " + doBruno.token))
                .andExpect(status().isNotFound());

        mockMvc.perform(put("/api/transacoes/" + id)
                        .header("Authorization", "Bearer " + doBruno.token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo(doBruno, "Sequestrada", "1.00", hoje(), null)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("sem token nao se acessa lancamento algum")
    void deveExigirAutenticacao() throws Exception {
        mockMvc.perform(get("/api/transacoes")).andExpect(status().isUnauthorized());
    }

    private Contexto prepararContexto(String email) throws Exception {
        String corpoRegistro = String.format(
                "{\"nome\":\"Teste\",\"email\":\"%s\",\"senha\":\"senha-secreta\"}", email);
        String respostaRegistro = mockMvc.perform(post("/api/auth/registrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoRegistro))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String token = objectMapper.readTree(respostaRegistro).get("token").asText();

        String contas = mockMvc.perform(get("/api/contas").header("Authorization", "Bearer " + token))
                .andReturn().getResponse().getContentAsString();
        Long contaId = objectMapper.readTree(contas).get(0).get("id").asLong();

        String categorias = mockMvc.perform(get("/api/categorias")
                        .header("Authorization", "Bearer " + token))
                .andReturn().getResponse().getContentAsString();
        Long categoriaId = objectMapper.readTree(categorias).get(0).get("id").asLong();

        return new Contexto(token, contaId, categoriaId);
    }

    private Long criarTransacao(Contexto contexto, String descricao, String valor) throws Exception {
        String resposta = mockMvc.perform(post("/api/transacoes")
                        .header("Authorization", "Bearer " + contexto.token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo(contexto, descricao, valor, hoje().plusDays(5), null)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        JsonNode json = objectMapper.readTree(resposta);
        return json.get("id").asLong();
    }

    private String corpo(Contexto contexto, String descricao, String valor,
                         LocalDate dataPrevista, LocalDate dataEfetivacao) {
        String efetivacao = dataEfetivacao == null
                ? ""
                : String.format(",\"dataEfetivacao\":\"%s\"", dataEfetivacao);
        return String.format(
                "{\"contaId\":%d,\"categoriaId\":%d,\"descricao\":\"%s\",\"valor\":%s,"
                        + "\"tipo\":\"DESPESA\",\"dataPrevista\":\"%s\"%s}",
                contexto.contaId, contexto.categoriaId, descricao, valor, dataPrevista, efetivacao);
    }

    private static class Contexto {
        private final String token;
        private final Long contaId;
        private final Long categoriaId;

        private Contexto(String token, Long contaId, Long categoriaId) {
            this.token = token;
            this.contaId = contaId;
            this.categoriaId = categoriaId;
        }
    }
}
