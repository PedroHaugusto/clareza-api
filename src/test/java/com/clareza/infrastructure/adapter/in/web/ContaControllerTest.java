package com.clareza.infrastructure.adapter.in.web;

import com.clareza.TesteDeIntegracao;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@Transactional
class ContaControllerTest extends TesteDeIntegracao {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("quem acaba de se registrar ja tem a conta e o cartao principais")
    void deveSemearAsContasPadraoNoRegistro() throws Exception {
        String token = registrarEObterToken("ana@clareza.dev");

        mockMvc.perform(get("/api/contas").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].nome", contains("Cartão principal", "Conta principal")))
                .andExpect(jsonPath("$[0].cartaoDeCredito", is(true)))
                .andExpect(jsonPath("$[1].cartaoDeCredito", is(false)));
    }

    @Test
    @DisplayName("conta criada aparece na listagem")
    void deveCriarEListarConta() throws Exception {
        String token = registrarEObterToken("ana@clareza.dev");

        mockMvc.perform(post("/api/contas")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Nubank\",\"tipo\":\"CARTAO_CREDITO\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome", is("Nubank")))
                .andExpect(jsonPath("$.cartaoDeCredito", is(true)));

        mockMvc.perform(get("/api/contas").header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$", hasSize(3)));
    }

    @Test
    @DisplayName("nome repetido e recusado, mesmo com outra caixa")
    void deveRecusarNomeDuplicado() throws Exception {
        String token = registrarEObterToken("ana@clareza.dev");

        mockMvc.perform(post("/api/contas")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"conta principal\",\"tipo\":\"CONTA_CORRENTE\"}"))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @DisplayName("tipo invalido responde 400 sem estourar erro interno")
    void deveRecusarTipoInvalido() throws Exception {
        String token = registrarEObterToken("ana@clareza.dev");

        mockMvc.perform(post("/api/contas")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Nubank\",\"tipo\":\"POUPANCINHA\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("conta propria e excluida e some da listagem")
    void deveExcluirContaPropria() throws Exception {
        String token = registrarEObterToken("ana@clareza.dev");
        Long id = criarConta(token, "Nubank");

        mockMvc.perform(delete("/api/contas/" + id).header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/contas").header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].nome", not(hasItem("Nubank"))));
    }

    @Test
    @DisplayName("as contas de um usuario sao invisiveis e inexcluiveis para outro")
    void deveIsolarContasEntreUsuarios() throws Exception {
        String tokenDaAna = registrarEObterToken("ana@clareza.dev");
        String tokenDoBruno = registrarEObterToken("bruno@clareza.dev");

        Long idDaAna = criarConta(tokenDaAna, "Nubank");

        mockMvc.perform(get("/api/contas").header("Authorization", "Bearer " + tokenDoBruno))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].nome", not(hasItem("Nubank"))));

        mockMvc.perform(delete("/api/contas/" + idDaAna)
                        .header("Authorization", "Bearer " + tokenDoBruno))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/contas").header("Authorization", "Bearer " + tokenDaAna))
                .andExpect(jsonPath("$[*].nome", hasItem("Nubank")));
    }

    @Test
    @DisplayName("o mesmo nome de conta pode existir em usuarios diferentes")
    void devePermitirONomeRepetidoEntreUsuariosDistintos() throws Exception {
        String tokenDaAna = registrarEObterToken("ana@clareza.dev");
        String tokenDoBruno = registrarEObterToken("bruno@clareza.dev");

        criarConta(tokenDaAna, "Nubank");

        mockMvc.perform(post("/api/contas")
                        .header("Authorization", "Bearer " + tokenDoBruno)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Nubank\",\"tipo\":\"CARTAO_CREDITO\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("sem token nao se lista nem se cria conta")
    void deveExigirAutenticacao() throws Exception {
        mockMvc.perform(get("/api/contas")).andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/contas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Nubank\",\"tipo\":\"CARTAO_CREDITO\"}"))
                .andExpect(status().isUnauthorized());
    }

    private String registrarEObterToken(String email) throws Exception {
        String corpo = String.format(
                "{\"nome\":\"Teste\",\"email\":\"%s\",\"senha\":\"senha-secreta\"}", email);
        String resposta = mockMvc.perform(post("/api/auth/registrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(resposta).get("token").asText();
    }

    private Long criarConta(String token, String nome) throws Exception {
        String resposta = mockMvc.perform(post("/api/contas")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format(
                                "{\"nome\":\"%s\",\"tipo\":\"CARTAO_CREDITO\"}", nome)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(resposta).get("id").asLong();
    }
}
