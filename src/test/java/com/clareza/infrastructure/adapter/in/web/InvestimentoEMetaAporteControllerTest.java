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

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@Transactional
class InvestimentoEMetaAporteControllerTest extends TesteDeIntegracao {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String token;

    @BeforeEach
    void prepararCenario() throws Exception {
        token = registrar("ana@clareza.dev");
    }

    @Test
    @DisplayName("carteira nova vem zerada, sem divisao por zero")
    void carteiraNovaDeveVirZerada() throws Exception {
        mockMvc.perform(get("/api/investimentos").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalInvestido", is(0)))
                .andExpect(jsonPath("$.rentabilidadeMediaPonderada", is(0.00)))
                .andExpect(jsonPath("$.quantidade", is(0)))
                .andExpect(jsonPath("$.investimentos", hasSize(0)));
    }

    @Test
    @DisplayName("a carteira consolida total e rentabilidade ponderada")
    void deveConsolidarACarteira() throws Exception {
        criar("CDB Banco X", "RENDA_FIXA", "50000.00", "11.00");
        criar("Bitcoin", "CRIPTO", "1000.00", "25.00");

        mockMvc.perform(get("/api/investimentos").header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.totalInvestido", is(51000.00)))
                .andExpect(jsonPath("$.rentabilidadeMediaPonderada", is(11.27)))
                .andExpect(jsonPath("$.quantidade", is(2)))
                .andExpect(jsonPath("$.investimentos", hasSize(2)));
    }

    @Test
    @DisplayName("rentabilidade omitida vira zero, e nao nulo")
    void rentabilidadeOmitidaDeveVirarZero() throws Exception {
        mockMvc.perform(post("/api/investimentos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Poupanca\",\"tipo\":\"RENDA_FIXA\",\"valorInvestido\":100.00}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rentabilidadeInformada", is(0.00)));
    }

    @Test
    @DisplayName("investimento e editado e excluido")
    void deveEditarEExcluir() throws Exception {
        Long id = criar("CDB", "RENDA_FIXA", "1000.00", "10.00");

        mockMvc.perform(put("/api/investimentos/" + id)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"CDB reforcado\",\"tipo\":\"RENDA_FIXA\","
                                + "\"valorInvestido\":2000.00,\"rentabilidadeInformada\":12.50}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(id.intValue())))
                .andExpect(jsonPath("$.nome", is("CDB reforcado")))
                .andExpect(jsonPath("$.valorInvestido", is(2000.00)));

        mockMvc.perform(delete("/api/investimentos/" + id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/investimentos").header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.quantidade", is(0)));
    }

    @Test
    @DisplayName("valor zero ou negativo responde 400")
    void deveRecusarValorNaoPositivo() throws Exception {
        mockMvc.perform(post("/api/investimentos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"X\",\"tipo\":\"ACOES\",\"valorInvestido\":0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos[0].campo", is("valorInvestido")));
    }

    @Test
    @DisplayName("tipo inexistente responde 400")
    void deveRecusarTipoInvalido() throws Exception {
        mockMvc.perform(post("/api/investimentos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"X\",\"tipo\":\"NFT\",\"valorInvestido\":100.00}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("investimento de outro usuario e invisivel e inexcluivel")
    void deveIsolarInvestimentosEntreUsuarios() throws Exception {
        Long id = criar("CDB", "RENDA_FIXA", "1000.00", "10.00");
        String tokenDoBruno = registrar("bruno@clareza.dev");

        mockMvc.perform(get("/api/investimentos").header("Authorization", "Bearer " + tokenDoBruno))
                .andExpect(jsonPath("$.quantidade", is(0)));

        mockMvc.perform(delete("/api/investimentos/" + id)
                        .header("Authorization", "Bearer " + tokenDoBruno))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("meta nao definida responde 200 marcando a ausencia")
    void metaNaoDefinidaDeveResponder200() throws Exception {
        mockMvc.perform(get("/api/meta-aporte").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.definida", is(false)))
                .andExpect(jsonPath("$.valor", nullValue()));
    }

    @Test
    @DisplayName("meta definida e relida, e o PUT seguinte substitui em vez de duplicar")
    void deveDefinirEAtualizarAMeta() throws Exception {
        mockMvc.perform(put("/api/meta-aporte")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"valor\":800.00}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valor", is(800.00)))
                .andExpect(jsonPath("$.definida", is(true)));

        mockMvc.perform(put("/api/meta-aporte")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"valor\":1200.00}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/meta-aporte").header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.valor", is(1200.00)));
    }

    @Test
    @DisplayName("meta removida volta a responder como nao definida")
    void deveRemoverAMeta() throws Exception {
        mockMvc.perform(put("/api/meta-aporte")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"valor\":800.00}"))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/meta-aporte").header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/meta-aporte").header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.definida", is(false)));
    }

    @Test
    @DisplayName("meta zero ou negativa responde 400")
    void deveRecusarMetaNaoPositiva() throws Exception {
        mockMvc.perform(put("/api/meta-aporte")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"valor\":0}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("a meta de um usuario nao aparece para outro")
    void deveIsolarAMetaEntreUsuarios() throws Exception {
        mockMvc.perform(put("/api/meta-aporte")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"valor\":800.00}"))
                .andExpect(status().isOk());

        String tokenDoBruno = registrar("bruno@clareza.dev");

        mockMvc.perform(get("/api/meta-aporte").header("Authorization", "Bearer " + tokenDoBruno))
                .andExpect(jsonPath("$.definida", is(false)));
    }

    @Test
    @DisplayName("as rotas exigem autenticacao")
    void deveExigirAutenticacao() throws Exception {
        mockMvc.perform(get("/api/investimentos")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/meta-aporte")).andExpect(status().isUnauthorized());
    }

    private String registrar(String email) throws Exception {
        String corpo = String.format(
                "{\"nome\":\"Teste\",\"email\":\"%s\",\"senha\":\"senha-secreta\"}", email);
        String resposta = mockMvc.perform(post("/api/auth/registrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(resposta).get("token").asText();
    }

    private Long criar(String nome, String tipo, String valor, String rentabilidade) throws Exception {
        String corpo = String.format(
                "{\"nome\":\"%s\",\"tipo\":\"%s\",\"valorInvestido\":%s,\"rentabilidadeInformada\":%s}",
                nome, tipo, valor, rentabilidade);
        String resposta = mockMvc.perform(post("/api/investimentos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(resposta).get("id").asLong();
    }
}
