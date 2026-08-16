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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@Transactional
class PrevisaoControllerTest extends TesteDeIntegracao {

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
    @DisplayName("preferencia nova ja vem com os 10 por cento padrao, sem precisar gravar")
    void deveDevolverAPreferenciaPadrao() throws Exception {
        mockMvc.perform(get("/api/preferencia-cenario").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.percentualAjusteReceita", is(10)))
                .andExpect(jsonPath("$.percentualAjusteDespesa", is(10)));
    }

    @Test
    @DisplayName("preferencia salva e devolvida nas consultas seguintes")
    void deveSalvarEReleAPreferencia() throws Exception {
        mockMvc.perform(put("/api/preferencia-cenario")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"percentualAjusteReceita\":20,\"percentualAjusteDespesa\":5}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.percentualAjusteReceita", is(20)));

        mockMvc.perform(get("/api/preferencia-cenario").header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.percentualAjusteReceita", is(20)))
                .andExpect(jsonPath("$.percentualAjusteDespesa", is(5)));
    }

    @Test
    @DisplayName("percentual acima de 100 responde 400")
    void deveRecusarPercentualForaDaFaixa() throws Exception {
        mockMvc.perform(put("/api/preferencia-cenario")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"percentualAjusteReceita\":150,\"percentualAjusteDespesa\":10}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos[0].campo", is("percentualAjusteReceita")));
    }

    @Test
    @DisplayName("previsao padrao cobre 6 meses no cenario provavel")
    void deveProjetarSeisMesesPorPadrao() throws Exception {
        mockMvc.perform(get("/api/previsao").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cenario", is("PROVAVEL")))
                .andExpect(jsonPath("$.meses", hasSize(6)))
                .andExpect(jsonPath("$.percentualAjusteReceita", is(10)));
    }

    @Test
    @DisplayName("horizonte de 12 meses e aceito")
    void deveAceitarDozeMeses() throws Exception {
        mockMvc.perform(get("/api/previsao").param("meses", "12")
                        .header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.meses", hasSize(12)));
    }

    @Test
    @DisplayName("horizonte invalido responde 422")
    void deveRecusarHorizonteInvalido() throws Exception {
        mockMvc.perform(get("/api/previsao").param("meses", "3")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @DisplayName("os tres cenarios projetam saldos diferentes sobre os mesmos lancamentos")
    void osCenariosDevemDivergir() throws Exception {
        LocalDate mesQueVem = hoje().plusMonths(1).withDayOfMonth(5);
        criar("Salario", "5000.00", "RECEITA", mesQueVem);
        criar("Aluguel", "2000.00", "DESPESA", mesQueVem);

        mockMvc.perform(get("/api/previsao").param("cenario", "PROVAVEL")
                        .header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.meses[0].totalReceitasPrevistas", is(5000.00)))
                .andExpect(jsonPath("$.meses[0].totalDespesasPrevistas", is(2000.00)));

        mockMvc.perform(get("/api/previsao").param("cenario", "OTIMISTA")
                        .header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.meses[0].totalReceitasPrevistas", is(5500.00)))
                .andExpect(jsonPath("$.meses[0].totalDespesasPrevistas", is(1800.00)));

        mockMvc.perform(get("/api/previsao").param("cenario", "PESSIMISTA")
                        .header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.meses[0].totalReceitasPrevistas", is(4500.00)))
                .andExpect(jsonPath("$.meses[0].totalDespesasPrevistas", is(2200.00)));
    }

    @Test
    @DisplayName("os percentuais da query prevalecem sobre a preferencia salva, sem grava-los")
    void oQueryParamDeveVencerAPreferenciaSemPersistir() throws Exception {
        mockMvc.perform(put("/api/preferencia-cenario")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"percentualAjusteReceita\":10,\"percentualAjusteDespesa\":10}"))
                .andExpect(status().isOk());

        LocalDate mesQueVem = hoje().plusMonths(1).withDayOfMonth(5);
        criar("Salario", "5000.00", "RECEITA", mesQueVem);

        mockMvc.perform(get("/api/previsao")
                        .param("cenario", "OTIMISTA")
                        .param("ajusteReceita", "50")
                        .header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.percentualAjusteReceita", is(50)))
                .andExpect(jsonPath("$.meses[0].totalReceitasPrevistas", is(7500.00)));

        mockMvc.perform(get("/api/preferencia-cenario").header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.percentualAjusteReceita", is(10)));
    }

    @Test
    @DisplayName("a preferencia salva vale quando a query nao traz percentuais")
    void aPreferenciaSalvaDeveValerSemQueryParams() throws Exception {
        mockMvc.perform(put("/api/preferencia-cenario")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"percentualAjusteReceita\":30,\"percentualAjusteDespesa\":30}"))
                .andExpect(status().isOk());

        LocalDate mesQueVem = hoje().plusMonths(1).withDayOfMonth(5);
        criar("Salario", "1000.00", "RECEITA", mesQueVem);

        mockMvc.perform(get("/api/previsao").param("cenario", "PESSIMISTA")
                        .header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.percentualAjusteReceita", is(30)))
                .andExpect(jsonPath("$.meses[0].totalReceitasPrevistas", is(700.00)));
    }

    @Test
    @DisplayName("o saldo projetado de um mes abre o mes seguinte")
    void deveEncadearOsSaldos() throws Exception {
        LocalDate mesQueVem = hoje().plusMonths(1).withDayOfMonth(5);
        criar("Salario", "1000.00", "RECEITA", mesQueVem);

        String resposta = mockMvc.perform(get("/api/previsao").param("cenario", "PROVAVEL")
                        .header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.meses[0].saldoProjetado", is(1000.00)))
                .andExpect(jsonPath("$.meses[1].saldoInicial", is(1000.00)))
                .andReturn().getResponse().getContentAsString();

        objectMapper.readTree(resposta);
    }

    @Test
    @DisplayName("a previsao nao enxerga lancamento de outro usuario")
    void deveIsolarPorUsuario() throws Exception {
        criar("Salario", "5000.00", "RECEITA", hoje().plusMonths(1).withDayOfMonth(5));

        String outro = mockMvc.perform(post("/api/auth/registrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Bruno\",\"email\":\"bruno@clareza.dev\","
                                + "\"senha\":\"senha-secreta\"}"))
                .andReturn().getResponse().getContentAsString();
        String tokenDoBruno = objectMapper.readTree(outro).get("token").asText();

        mockMvc.perform(get("/api/previsao").header("Authorization", "Bearer " + tokenDoBruno))
                .andExpect(jsonPath("$.meses[0].totalReceitasPrevistas", is(0.0)))
                .andExpect(jsonPath("$.meses[0].saldoProjetado", is(0.0)));
    }

    @Test
    @DisplayName("as rotas exigem autenticacao")
    void deveExigirAutenticacao() throws Exception {
        mockMvc.perform(get("/api/previsao")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/preferencia-cenario")).andExpect(status().isUnauthorized());
    }

    private void criar(String descricao, String valor, String tipo, LocalDate data) throws Exception {
        String corpo = String.format(
                "{\"contaId\":%d,\"categoriaId\":%d,\"descricao\":\"%s\",\"valor\":%s,"
                        + "\"tipo\":\"%s\",\"dataPrevista\":\"%s\"}",
                contaId, categoriaId, descricao, valor, tipo, data);
        mockMvc.perform(post("/api/transacoes")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo))
                .andExpect(status().isCreated());
    }
}
