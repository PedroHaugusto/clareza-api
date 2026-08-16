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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@Transactional
class MetaFinanceiraControllerTest extends TesteDeIntegracao {

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
    @DisplayName("meta criada devolve os derivados calculados no servidor")
    void deveCriarComDerivados() throws Exception {
        mockMvc.perform(post("/api/metas")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Viagem\",\"valorAtual\":2500.00,"
                                + "\"valorObjetivo\":10000.00,\"descricao\":\"Japao\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome", is("Viagem")))
                .andExpect(jsonPath("$.percentualConcluido", is(25.00)))
                .andExpect(jsonPath("$.valorRestante", is(7500.00)))
                .andExpect(jsonPath("$.concluida", is(false)))
                .andExpect(jsonPath("$.descricao", is("Japao")));
    }

    @Test
    @DisplayName("meta superada mostra percentual acima de cem e restante zerado")
    void metaSuperadaDeveTerRestanteZerado() throws Exception {
        mockMvc.perform(post("/api/metas")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Reserva\",\"valorAtual\":12000.00,"
                                + "\"valorObjetivo\":10000.00}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.percentualConcluido", is(120.00)))
                .andExpect(jsonPath("$.valorRestante", is(0.00)))
                .andExpect(jsonPath("$.concluida", is(true)));
    }

    @Test
    @DisplayName("meta sem valor atual comeca zerada")
    void metaSemValorAtualDeveComecarEmZero() throws Exception {
        mockMvc.perform(post("/api/metas")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Carro\",\"valorObjetivo\":50000.00}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.percentualConcluido", is(0.00)))
                .andExpect(jsonPath("$.valorRestante", is(50000.00)));
    }

    @Test
    @DisplayName("prazo futuro devolve os dias restantes e nao marca atraso")
    void deveCalcularOsDiasAtePrazo() throws Exception {
        LocalDate prazo = LocalDate.now().plusDays(30);

        mockMvc.perform(post("/api/metas")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("{\"nome\":\"Viagem\",\"valorObjetivo\":1000.00,"
                                + "\"prazo\":\"%s\"}", prazo)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.diasAtePrazo", is(30)))
                .andExpect(jsonPath("$.prazoVencido", is(false)));
    }

    @Test
    @DisplayName("prazo passado com meta aberta marca atraso com dias negativos")
    void prazoPassadoDeveMarcarAtraso() throws Exception {
        LocalDate prazo = LocalDate.now().minusDays(5);

        mockMvc.perform(post("/api/metas")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("{\"nome\":\"Atrasada\",\"valorObjetivo\":1000.00,"
                                + "\"prazo\":\"%s\"}", prazo)))
                .andExpect(jsonPath("$.diasAtePrazo", is(-5)))
                .andExpect(jsonPath("$.prazoVencido", is(true)));
    }

    @Test
    @DisplayName("meta concluida nao aparece vencida mesmo com prazo passado")
    void metaConcluidaNaoDeveAparecerVencida() throws Exception {
        LocalDate prazo = LocalDate.now().minusDays(5);

        mockMvc.perform(post("/api/metas")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("{\"nome\":\"Concluida\",\"valorAtual\":1000.00,"
                                + "\"valorObjetivo\":1000.00,\"prazo\":\"%s\"}", prazo)))
                .andExpect(jsonPath("$.prazoVencido", is(false)))
                .andExpect(jsonPath("$.concluida", is(true)));
    }

    @Test
    @DisplayName("meta sem prazo nao traz dias nem atraso")
    void metaSemPrazoNaoDeveTrazerDerivadosDeData() throws Exception {
        mockMvc.perform(post("/api/metas")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Sem prazo\",\"valorObjetivo\":1000.00}"))
                .andExpect(jsonPath("$.prazo").doesNotExist())
                .andExpect(jsonPath("$.diasAtePrazo").doesNotExist())
                .andExpect(jsonPath("$.prazoVencido", is(false)));
    }

    @Test
    @DisplayName("guardar mais dinheiro atualiza o progresso pelo PUT")
    void deveAtualizarOProgresso() throws Exception {
        Long id = criar("Viagem", "1000.00", "10000.00");

        mockMvc.perform(put("/api/metas/" + id)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Viagem\",\"valorAtual\":5000.00,"
                                + "\"valorObjetivo\":10000.00}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(id.intValue())))
                .andExpect(jsonPath("$.percentualConcluido", is(50.00)));
    }

    @Test
    @DisplayName("meta excluida some da listagem")
    void deveExcluir() throws Exception {
        Long id = criar("Viagem", "0", "10000.00");

        mockMvc.perform(delete("/api/metas/" + id).header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/metas").header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("objetivo zero responde 400")
    void deveRecusarObjetivoZero() throws Exception {
        mockMvc.perform(post("/api/metas")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"X\",\"valorObjetivo\":0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos[0].campo", is("valorObjetivo")));
    }

    @Test
    @DisplayName("meta de outro usuario e invisivel e inexcluivel")
    void deveIsolarEntreUsuarios() throws Exception {
        Long id = criar("Viagem", "0", "10000.00");
        String tokenDoBruno = registrar("bruno@clareza.dev");

        mockMvc.perform(get("/api/metas").header("Authorization", "Bearer " + tokenDoBruno))
                .andExpect(jsonPath("$", hasSize(0)));

        mockMvc.perform(delete("/api/metas/" + id).header("Authorization", "Bearer " + tokenDoBruno))
                .andExpect(status().isNotFound());

        mockMvc.perform(put("/api/metas/" + id)
                        .header("Authorization", "Bearer " + tokenDoBruno)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Sequestrada\",\"valorObjetivo\":1.00}"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("a rota exige autenticacao")
    void deveExigirAutenticacao() throws Exception {
        mockMvc.perform(get("/api/metas")).andExpect(status().isUnauthorized());
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

    private Long criar(String nome, String atual, String objetivo) throws Exception {
        String corpo = String.format(
                "{\"nome\":\"%s\",\"valorAtual\":%s,\"valorObjetivo\":%s}", nome, atual, objetivo);
        String resposta = mockMvc.perform(post("/api/metas")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(resposta).get("id").asLong();
    }
}
