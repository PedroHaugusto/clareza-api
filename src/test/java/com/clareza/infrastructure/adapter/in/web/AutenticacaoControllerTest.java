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

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@Transactional
class AutenticacaoControllerTest extends TesteDeIntegracao {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("registro devolve 201 com o token da aplicacao e nunca a senha")
    void deveRegistrarEDevolverToken() throws Exception {
        mockMvc.perform(post("/api/auth/registrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoDeRegistro("ana@clareza.dev")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.email", is("ana@clareza.dev")))
                .andExpect(jsonPath("$.tipo", is("Bearer")))
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.expiraEm", notNullValue()))
                .andExpect(jsonPath("$.senha").doesNotExist())
                .andExpect(jsonPath("$.senhaHash").doesNotExist());
    }

    @Test
    @DisplayName("e-mail repetido responde 422, e nao 500 de violacao de constraint")
    void deveRecusarEmailJaCadastrado() throws Exception {
        mockMvc.perform(post("/api/auth/registrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoDeRegistro("ana@clareza.dev")))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/auth/registrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoDeRegistro("ana@clareza.dev")))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status", is(422)));
    }

    @Test
    @DisplayName("senha curta responde 400 apontando o campo")
    void deveRecusarSenhaCurta() throws Exception {
        String corpo = "{\"nome\":\"Ana\",\"email\":\"ana@clareza.dev\",\"senha\":\"1234\"}";

        mockMvc.perform(post("/api/auth/registrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos[0].campo", is("senha")));
    }

    @Test
    @DisplayName("login com a senha correta devolve um token novo")
    void deveAutenticarComSenhaCorreta() throws Exception {
        mockMvc.perform(post("/api/auth/registrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoDeRegistro("ana@clareza.dev")))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"ANA@clareza.dev\",\"senha\":\"senha-secreta\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.email", is("ana@clareza.dev")));
    }

    @Test
    @DisplayName("senha errada responde 401 no formato padrao de erro")
    void deveRecusarSenhaErradaComFormatoPadrao() throws Exception {
        mockMvc.perform(post("/api/auth/registrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoDeRegistro("ana@clareza.dev")))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"ana@clareza.dev\",\"senha\":\"senha-errada\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is(401)))
                .andExpect(jsonPath("$.mensagem", is("E-mail ou senha invalidos")))
                .andExpect(jsonPath("$.path", is("/api/auth/login")))
                .andExpect(jsonPath("$.timestamp", notNullValue()));
    }

    @Test
    @DisplayName("rota protegida sem token responde 401 em json, e nao a pagina padrao do spring")
    void deveResponder401EmJson_quandoNaoHaToken() throws Exception {
        mockMvc.perform(get("/api/transacoes"))
                .andExpect(status().isUnauthorized())
                .andExpect(content -> assertThat(content.getResponse().getContentType())
                        .contains(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.status", is(401)))
                .andExpect(jsonPath("$.mensagem", is("Autenticacao necessaria para acessar este recurso")))
                .andExpect(jsonPath("$.path", is("/api/transacoes")));
    }

    @Test
    @DisplayName("token invalido nao autentica")
    void deveResponder401_quandoOTokenEInvalido() throws Exception {
        mockMvc.perform(get("/api/transacoes").header("Authorization", "Bearer token-falsificado"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("token valido atravessa a camada de seguranca")
    void devePassarPelaSeguranca_quandoOTokenEValido() throws Exception {
        String resposta = mockMvc.perform(post("/api/auth/registrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoDeRegistro("ana@clareza.dev")))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        JsonNode json = objectMapper.readTree(resposta);
        String token = json.get("token").asText();

        mockMvc.perform(get("/api/transacoes").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("health continua publico")
    void deveManterOHealthPublico() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }

    private String corpoDeRegistro(String email) {
        return String.format("{\"nome\":\"Ana\",\"email\":\"%s\",\"senha\":\"senha-secreta\"}", email);
    }
}
