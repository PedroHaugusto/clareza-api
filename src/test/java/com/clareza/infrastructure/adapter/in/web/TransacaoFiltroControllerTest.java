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
class TransacaoFiltroControllerTest extends TesteDeIntegracao {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String token;
    private Long contaId;
    private Long outraContaId;
    private Long categoriaId;
    private Long outraCategoriaId;

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
        outraContaId = objectMapper.readTree(contas).get(1).get("id").asLong();

        String categorias = mockMvc.perform(get("/api/categorias")
                        .header("Authorization", "Bearer " + token))
                .andReturn().getResponse().getContentAsString();
        categoriaId = objectMapper.readTree(categorias).get(0).get("id").asLong();
        outraCategoriaId = objectMapper.readTree(categorias).get(1).get("id").asLong();

        criar("Conta de luz", "150.00", "DESPESA", hoje().plusDays(5), contaId, categoriaId);
        criar("Salario", "5000.00", "RECEITA", hoje().plusDays(10), contaId, categoriaId);
        criar("Luz do escritorio", "90.00", "DESPESA", hoje().plusDays(60),
                outraContaId, outraCategoriaId);
        criar("Antiga", "10.00", "DESPESA", hoje().minusMonths(2), contaId, categoriaId);
    }

    @Test
    @DisplayName("sem filtro devolve tudo do usuario")
    void deveListarTudoSemFiltro() throws Exception {
        mockMvc.perform(get("/api/transacoes").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(4)));
    }

    @Test
    @DisplayName("filtro por tipo separa receitas de despesas")
    void deveFiltrarPorTipo() throws Exception {
        mockMvc.perform(get("/api/transacoes").param("tipo", "RECEITA")
                        .header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].descricao", is("Salario")));

        mockMvc.perform(get("/api/transacoes").param("tipo", "DESPESA")
                        .header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$", hasSize(3)));
    }

    @Test
    @DisplayName("proximos 30 dias corta o que esta longe e o que ja passou")
    void deveFiltrarPorProximos30Dias() throws Exception {
        mockMvc.perform(get("/api/transacoes").param("periodo", "PROXIMOS_30_DIAS")
                        .header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].descricao", contains("Salario", "Conta de luz")));
    }

    @Test
    @DisplayName("proximos 90 dias alcanca o lancamento mais distante")
    void deveFiltrarPorProximos90Dias() throws Exception {
        mockMvc.perform(get("/api/transacoes").param("periodo", "PROXIMOS_90_DIAS")
                        .header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$", hasSize(3)));
    }

    @Test
    @DisplayName("busca por texto ignora maiusculas e acha no meio da descricao")
    void deveFiltrarPorBusca() throws Exception {
        mockMvc.perform(get("/api/transacoes").param("busca", "LUZ")
                        .header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    @DisplayName("filtros por conta e por categoria")
    void deveFiltrarPorContaECategoria() throws Exception {
        mockMvc.perform(get("/api/transacoes").param("contaId", outraContaId.toString())
                        .header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].descricao", is("Luz do escritorio")));

        mockMvc.perform(get("/api/transacoes").param("categoriaId", outraCategoriaId.toString())
                        .header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @DisplayName("os filtros se combinam, estreitando o resultado")
    void deveCombinarFiltros() throws Exception {
        mockMvc.perform(get("/api/transacoes")
                        .param("tipo", "DESPESA")
                        .param("periodo", "PROXIMOS_90_DIAS")
                        .param("busca", "luz")
                        .header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$", hasSize(2)));

        mockMvc.perform(get("/api/transacoes")
                        .param("tipo", "DESPESA")
                        .param("periodo", "PROXIMOS_30_DIAS")
                        .param("busca", "luz")
                        .header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].descricao", is("Conta de luz")));
    }

    @Test
    @DisplayName("confirmar marca a data de efetivacao e muda o status")
    void deveConfirmarLancamento() throws Exception {
        Long id = criar("Internet", "99.90", "DESPESA", hoje(), contaId, categoriaId);

        mockMvc.perform(patch("/api/transacoes/" + id + "/confirmar")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("CONFIRMADA")))
                .andExpect(jsonPath("$.dataEfetivacao", is(hoje().toString())));
    }

    @Test
    @DisplayName("confirmar de novo responde 422")
    void deveRecusarConfirmacaoRepetida() throws Exception {
        Long id = criar("Internet", "99.90", "DESPESA", hoje(), contaId, categoriaId);

        mockMvc.perform(patch("/api/transacoes/" + id + "/confirmar")
                .header("Authorization", "Bearer " + token)).andExpect(status().isOk());

        mockMvc.perform(patch("/api/transacoes/" + id + "/confirmar")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @DisplayName("conta e categoria com lancamentos respondem 422, e nao 500")
    void deveRecusarExclusaoDeContaOuCategoriaEmUso() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .delete("/api/contas/" + contaId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.mensagem", is("Esta conta tem lancamentos e nao pode ser excluida")));

        String propria = mockMvc.perform(post("/api/categorias")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Pets\",\"tipo\":\"DESPESA\",\"corHex\":\"#AD1457\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Long categoriaPropria = objectMapper.readTree(propria).get("id").asLong();

        criar("Racao", "80.00", "DESPESA", hoje(), contaId, categoriaPropria);

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .delete("/api/categorias/" + categoriaPropria)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.mensagem", is("Esta categoria tem lancamentos e nao pode ser excluida")));
    }

    private Long criar(String descricao, String valor, String tipo, LocalDate dataPrevista,
                       Long conta, Long categoria) throws Exception {
        String corpo = String.format(
                "{\"contaId\":%d,\"categoriaId\":%d,\"descricao\":\"%s\",\"valor\":%s,"
                        + "\"tipo\":\"%s\",\"dataPrevista\":\"%s\"}",
                conta, categoria, descricao, valor, tipo, dataPrevista);
        String resposta = mockMvc.perform(post("/api/transacoes")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(resposta).get("id").asLong();
    }
}
