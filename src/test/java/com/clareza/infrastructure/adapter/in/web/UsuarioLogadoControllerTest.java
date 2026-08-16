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

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@Transactional
class UsuarioLogadoControllerTest extends TesteDeIntegracao {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("devolve os dados de quem esta com o token, sem expor a senha")
    void deveDevolverOUsuarioDoToken() throws Exception {
        String token = registrar("ana@clareza.dev");

        mockMvc.perform(get("/api/auth/eu").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome", is("Ana")))
                .andExpect(jsonPath("$.email", is("ana@clareza.dev")))
                .andExpect(jsonPath("$.possuiSenha", is(true)))
                .andExpect(jsonPath("$.vinculadoAoGoogle", is(false)))
                .andExpect(jsonPath("$.senhaHash").doesNotExist())
                .andExpect(jsonPath("$.googleId").doesNotExist());
    }

    @Test
    @DisplayName("cada token devolve o seu proprio usuario")
    void deveDevolverOUsuarioCorretoParaCadaToken() throws Exception {
        String tokenDaAna = registrar("ana@clareza.dev");
        String tokenDoBruno = registrar("bruno@clareza.dev");

        mockMvc.perform(get("/api/auth/eu").header("Authorization", "Bearer " + tokenDaAna))
                .andExpect(jsonPath("$.email", is("ana@clareza.dev")));

        mockMvc.perform(get("/api/auth/eu").header("Authorization", "Bearer " + tokenDoBruno))
                .andExpect(jsonPath("$.email", is("bruno@clareza.dev")));
    }

    @Test
    @DisplayName("a rota exige token, diferente das demais sob /api/auth")
    void deveExigirAutenticacao() throws Exception {
        mockMvc.perform(get("/api/auth/eu"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/auth/eu").header("Authorization", "Bearer token-invalido"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("as rotas de entrada seguem publicas")
    void asRotasDeEntradaDevemSeguirPublicas() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"ninguem@clareza.dev\",\"senha\":\"errada\"}"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/auth/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idToken\":\"qualquer\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("a documentacao da api fica acessivel sem token")
    void aDocumentacaoDeveSerPublica() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title", is("Clareza API")))
                .andExpect(jsonPath("$.paths['/api/auth/eu']").exists())
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.scheme", is("bearer")));
    }

    private String registrar(String email) throws Exception {
        String nome = email.startsWith("ana") ? "Ana" : "Bruno";
        String corpo = String.format(
                "{\"nome\":\"%s\",\"email\":\"%s\",\"senha\":\"senha-secreta\"}", nome, email);
        String resposta = mockMvc.perform(post("/api/auth/registrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(resposta).get("token").asText();
    }
}
