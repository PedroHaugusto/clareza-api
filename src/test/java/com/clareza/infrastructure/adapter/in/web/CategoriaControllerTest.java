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

import static org.hamcrest.Matchers.everyItem;
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
class CategoriaControllerTest extends TesteDeIntegracao {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("usuario novo ja enxerga as sete categorias padrao, com acentuacao intacta")
    void deveListarAsCategoriasPadrao() throws Exception {
        String token = registrarEObterToken("ana@clareza.dev");

        mockMvc.perform(get("/api/categorias").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(7)))
                .andExpect(jsonPath("$[*].nome", hasItem("Alimentação")))
                .andExpect(jsonPath("$[*].nome", hasItem("Salário")))
                .andExpect(jsonPath("$[*].padraoDoSistema", everyItem(is(true))));
    }

    @Test
    @DisplayName("categoria criada aparece na listagem marcada como nao padrao")
    void deveCriarEListarCategoriaPropria() throws Exception {
        String token = registrarEObterToken("ana@clareza.dev");

        mockMvc.perform(post("/api/categorias")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Pets\",\"tipo\":\"DESPESA\",\"corHex\":\"#ad1457\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome", is("Pets")))
                .andExpect(jsonPath("$.corHex", is("#AD1457")))
                .andExpect(jsonPath("$.padraoDoSistema", is(false)));

        mockMvc.perform(get("/api/categorias").header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$", hasSize(8)))
                .andExpect(jsonPath("$[*].nome", hasItem("Pets")));
    }

    @Test
    @DisplayName("nome que ja existe entre as padrao e recusado")
    void deveRecusarNomeQueColideComCategoriaPadrao() throws Exception {
        String token = registrarEObterToken("ana@clareza.dev");

        mockMvc.perform(post("/api/categorias")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"moradia\",\"tipo\":\"DESPESA\",\"corHex\":\"#6D4C41\"}"))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @DisplayName("cor fora do formato responde 400 apontando o campo")
    void deveRecusarCorInvalida() throws Exception {
        String token = registrarEObterToken("ana@clareza.dev");

        mockMvc.perform(post("/api/categorias")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Pets\",\"tipo\":\"DESPESA\",\"corHex\":\"vermelho\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos[0].campo", is("corHex")));
    }

    @Test
    @DisplayName("excluir categoria padrao do sistema responde 422")
    void deveRecusarExclusaoDeCategoriaPadrao() throws Exception {
        String token = registrarEObterToken("ana@clareza.dev");
        Long idDeUmaPadrao = primeiroId(token);

        mockMvc.perform(delete("/api/categorias/" + idDeUmaPadrao)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @DisplayName("categoria propria e excluida e some da listagem")
    void deveExcluirCategoriaPropria() throws Exception {
        String token = registrarEObterToken("ana@clareza.dev");
        Long id = criarCategoria(token, "Pets");

        mockMvc.perform(delete("/api/categorias/" + id).header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/categorias").header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$", hasSize(7)))
                .andExpect(jsonPath("$[*].nome", not(hasItem("Pets"))));
    }

    @Test
    @DisplayName("a categoria de um usuario e invisivel e inexcluivel para outro")
    void deveIsolarCategoriasEntreUsuarios() throws Exception {
        String tokenDaAna = registrarEObterToken("ana@clareza.dev");
        String tokenDoBruno = registrarEObterToken("bruno@clareza.dev");

        Long idDaAna = criarCategoria(tokenDaAna, "Pets");

        mockMvc.perform(get("/api/categorias").header("Authorization", "Bearer " + tokenDoBruno))
                .andExpect(jsonPath("$", hasSize(7)))
                .andExpect(jsonPath("$[*].nome", not(hasItem("Pets"))));

        mockMvc.perform(delete("/api/categorias/" + idDaAna)
                        .header("Authorization", "Bearer " + tokenDoBruno))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/categorias").header("Authorization", "Bearer " + tokenDaAna))
                .andExpect(jsonPath("$[*].nome", hasItem("Pets")));
    }

    @Test
    @DisplayName("sem token nao se lista nem se cria categoria")
    void deveExigirAutenticacao() throws Exception {
        mockMvc.perform(get("/api/categorias")).andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/categorias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Pets\",\"tipo\":\"DESPESA\",\"corHex\":\"#AD1457\"}"))
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

    private Long criarCategoria(String token, String nome) throws Exception {
        String resposta = mockMvc.perform(post("/api/categorias")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format(
                                "{\"nome\":\"%s\",\"tipo\":\"DESPESA\",\"corHex\":\"#AD1457\"}", nome)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(resposta).get("id").asLong();
    }

    private Long primeiroId(String token) throws Exception {
        String resposta = mockMvc.perform(get("/api/categorias")
                        .header("Authorization", "Bearer " + token))
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(resposta).get(0).get("id").asLong();
    }
}
