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

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@Transactional
class FluxoDeCaixaControllerTest extends TesteDeIntegracao {

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
    @DisplayName("a janela padrao cobre 6 meses para tras e 6 para frente, mais o mes atual")
    void deveUsarAJanelaPadrao() throws Exception {
        mockMvc.perform(get("/api/fluxo-caixa").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.meses", hasSize(13)));
    }

    @Test
    @DisplayName("o fluxo mistura passado realizado e futuro previsto, acumulando o saldo")
    void deveAcumularPassadoEFuturo() throws Exception {
        LocalDate hoje = LocalDate.now();
        LocalDate mesPassado = hoje.minusMonths(1).withDayOfMonth(10);
        LocalDate mesQueVem = hoje.plusMonths(1).withDayOfMonth(10);

        criar("Salario", "5000.00", "RECEITA", mesPassado, mesPassado);
        criar("Aluguel", "4200.00", "DESPESA", mesPassado, mesPassado);
        criar("Salario futuro", "5000.00", "RECEITA", mesQueVem, null);
        criar("Aluguel futuro", "3100.00", "DESPESA", mesQueVem, null);

        String resposta = mockMvc.perform(get("/api/fluxo-caixa")
                        .param("mesesPassados", "1").param("mesesFuturos", "1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.meses", hasSize(3)))
                .andExpect(jsonPath("$.saldoAnterior", is(0)))
                .andExpect(jsonPath("$.meses[0].entradas", is(5000.00)))
                .andExpect(jsonPath("$.meses[0].saidas", is(4200.00)))
                .andExpect(jsonPath("$.meses[0].saldoDoMes", is(800.00)))
                .andExpect(jsonPath("$.meses[0].saldoAcumulado", is(800.00)))
                .andExpect(jsonPath("$.meses[1].saldoAcumulado", is(800.00)))
                .andExpect(jsonPath("$.meses[2].saldoDoMes", is(1900.00)))
                .andExpect(jsonPath("$.meses[2].saldoAcumulado", is(2700.00)))
                .andReturn().getResponse().getContentAsString();

        objectMapper.readTree(resposta);
    }

    @Test
    @DisplayName("o que ficou fora da janela vira saldo anterior, e nao some da conta")
    void deveTrazerOSaldoAnteriorAJanela() throws Exception {
        LocalDate antigo = LocalDate.now().minusMonths(5).withDayOfMonth(10);
        criar("Heranca", "10000.00", "RECEITA", antigo, antigo);

        mockMvc.perform(get("/api/fluxo-caixa")
                        .param("mesesPassados", "1").param("mesesFuturos", "1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.saldoAnterior", is(10000.00)))
                .andExpect(jsonPath("$.meses[0].saldoAcumulado", is(10000.00)));
    }

    @Test
    @DisplayName("mes deficitario aparece com saldo negativo")
    void deveMostrarSaldoNegativo() throws Exception {
        LocalDate mesQueVem = LocalDate.now().plusMonths(1).withDayOfMonth(10);
        criar("Compra grande", "1500.00", "DESPESA", mesQueVem, null);

        mockMvc.perform(get("/api/fluxo-caixa")
                        .param("mesesPassados", "0").param("mesesFuturos", "1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.meses[1].saldoDoMes", is(-1500.00)))
                .andExpect(jsonPath("$.meses[1].saldoAcumulado", is(-1500.00)));
    }

    @Test
    @DisplayName("janela fora da faixa responde 422")
    void deveRecusarJanelaGrandeDemais() throws Exception {
        mockMvc.perform(get("/api/fluxo-caixa").param("mesesFuturos", "36")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnprocessableEntity());

        mockMvc.perform(get("/api/fluxo-caixa").param("mesesPassados", "-1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @DisplayName("o fluxo nao enxerga lancamento de outro usuario")
    void deveIsolarPorUsuario() throws Exception {
        LocalDate hoje = LocalDate.now();
        criar("Salario", "5000.00", "RECEITA", hoje.withDayOfMonth(1), hoje.withDayOfMonth(1));

        String outro = mockMvc.perform(post("/api/auth/registrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Bruno\",\"email\":\"bruno@clareza.dev\","
                                + "\"senha\":\"senha-secreta\"}"))
                .andReturn().getResponse().getContentAsString();
        String tokenDoBruno = objectMapper.readTree(outro).get("token").asText();

        mockMvc.perform(get("/api/fluxo-caixa").header("Authorization", "Bearer " + tokenDoBruno))
                .andExpect(jsonPath("$.saldoAnterior", is(0)))
                .andExpect(jsonPath("$.meses[6].entradas", is(0)));
    }

    @Test
    @DisplayName("a rota exige autenticacao")
    void deveExigirAutenticacao() throws Exception {
        mockMvc.perform(get("/api/fluxo-caixa")).andExpect(status().isUnauthorized());
    }

    private void criar(String descricao, String valor, String tipo,
                       LocalDate dataPrevista, LocalDate dataEfetivacao) throws Exception {
        String efetivacao = dataEfetivacao == null
                ? ""
                : String.format(",\"dataEfetivacao\":\"%s\"", dataEfetivacao);
        String corpo = String.format(
                "{\"contaId\":%d,\"categoriaId\":%d,\"descricao\":\"%s\",\"valor\":%s,"
                        + "\"tipo\":\"%s\",\"dataPrevista\":\"%s\"%s}",
                contaId, categoriaId, descricao, valor, tipo, dataPrevista, efetivacao);
        mockMvc.perform(post("/api/transacoes")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo))
                .andExpect(status().isCreated());
    }
}
