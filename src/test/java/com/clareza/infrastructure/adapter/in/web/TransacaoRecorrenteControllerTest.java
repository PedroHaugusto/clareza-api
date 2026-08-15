package com.clareza.infrastructure.adapter.in.web;

import com.clareza.TesteDeIntegracao;
import com.fasterxml.jackson.databind.JsonNode;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@Transactional
class TransacaoRecorrenteControllerTest extends TesteDeIntegracao {

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
    @DisplayName("recorrencia mensal materializa 12 ocorrencias no horizonte")
    void deveMaterializarAsOcorrenciasMensais() throws Exception {
        mockMvc.perform(post("/api/transacoes-recorrentes")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mensal("Aluguel", "1200.00", 10, LocalDate.now(), null)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.ativa", is(true)))
                .andExpect(jsonPath("$.periodicidade", is("MENSAL")));

        String transacoes = mockMvc.perform(get("/api/transacoes")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode json = objectMapper.readTree(transacoes);
        assertThat(json.size()).isBetween(12, 13);
        for (JsonNode ocorrencia : json) {
            assertThat(ocorrencia.get("descricao").asText()).isEqualTo("Aluguel");
            assertThat(ocorrencia.get("valor").asDouble()).isEqualTo(1200.00);
            assertThat(LocalDate.parse(ocorrencia.get("dataPrevista").asText()))
                    .isAfterOrEqualTo(LocalDate.now());
        }
    }

    @Test
    @DisplayName("data de fim limita quantas ocorrencias sao geradas")
    void deveRespeitarADataDeFim() throws Exception {
        mockMvc.perform(post("/api/transacoes-recorrentes")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mensal("Curso", "300.00", 5, LocalDate.now(),
                                LocalDate.now().plusMonths(3))))
                .andExpect(status().isCreated());

        String transacoes = mockMvc.perform(get("/api/transacoes")
                        .header("Authorization", "Bearer " + token))
                .andReturn().getResponse().getContentAsString();

        assertThat(objectMapper.readTree(transacoes).size()).isBetween(3, 4);
    }

    @Test
    @DisplayName("as ocorrencias sao lancamentos comuns e podem ser confirmadas")
    void asOcorrenciasDevemSerConfirmaveis() throws Exception {
        mockMvc.perform(post("/api/transacoes-recorrentes")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mensal("Aluguel", "1200.00", 10, LocalDate.now(), null)))
                .andExpect(status().isCreated());

        String transacoes = mockMvc.perform(get("/api/transacoes")
                        .header("Authorization", "Bearer " + token))
                .andReturn().getResponse().getContentAsString();
        Long primeira = objectMapper.readTree(transacoes).get(0).get("id").asLong();

        mockMvc.perform(patch("/api/transacoes/" + primeira + "/confirmar")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("CONFIRMADA")));
    }

    @Test
    @DisplayName("desativar apaga as futuras previstas e preserva as confirmadas")
    void deveApagarSomenteAsFuturasNaoConfirmadas() throws Exception {
        String criada = mockMvc.perform(post("/api/transacoes-recorrentes")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mensal("Academia", "150.00", 10, LocalDate.now(), null)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Long recorrenteId = objectMapper.readTree(criada).get("id").asLong();

        String transacoes = mockMvc.perform(get("/api/transacoes")
                        .header("Authorization", "Bearer " + token))
                .andReturn().getResponse().getContentAsString();
        int totalAntes = objectMapper.readTree(transacoes).size();
        Long paraConfirmar = objectMapper.readTree(transacoes).get(totalAntes - 1).get("id").asLong();

        mockMvc.perform(patch("/api/transacoes/" + paraConfirmar + "/confirmar")
                .header("Authorization", "Bearer " + token)).andExpect(status().isOk());

        mockMvc.perform(delete("/api/transacoes-recorrentes/" + recorrenteId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/transacoes").header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].status", is("CONFIRMADA")))
                .andExpect(jsonPath("$[0].id", is(paraConfirmar.intValue())));

        mockMvc.perform(get("/api/transacoes-recorrentes")
                        .header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$[0].ativa", is(false)));
    }

    @Test
    @DisplayName("desativar duas vezes responde 422")
    void deveRecusarDesativacaoRepetida() throws Exception {
        String criada = mockMvc.perform(post("/api/transacoes-recorrentes")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mensal("Academia", "150.00", 10, LocalDate.now(), null)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Long recorrenteId = objectMapper.readTree(criada).get("id").asLong();

        mockMvc.perform(delete("/api/transacoes-recorrentes/" + recorrenteId)
                .header("Authorization", "Bearer " + token)).andExpect(status().isNoContent());

        mockMvc.perform(delete("/api/transacoes-recorrentes/" + recorrenteId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @DisplayName("semanal sem dia da semana responde 422")
    void deveRecusarSemanalSemDiaDaSemana() throws Exception {
        String corpo = String.format(
                "{\"contaId\":%d,\"categoriaId\":%d,\"descricao\":\"Feira\",\"valor\":120.00,"
                        + "\"tipo\":\"DESPESA\",\"periodicidade\":\"SEMANAL\",\"dataInicio\":\"%s\"}",
                contaId, categoriaId, LocalDate.now());

        mockMvc.perform(post("/api/transacoes-recorrentes")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @DisplayName("dia do mes fora da faixa responde 400")
    void deveRecusarDiaDoMesInvalido() throws Exception {
        mockMvc.perform(post("/api/transacoes-recorrentes")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mensal("Aluguel", "1200.00", 45, LocalDate.now(), null)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos[0].campo", is("diaDoMes")));
    }

    @Test
    @DisplayName("recorrencia de outro usuario nao pode ser desativada")
    void deveIsolarRecorrenciasEntreUsuarios() throws Exception {
        String criada = mockMvc.perform(post("/api/transacoes-recorrentes")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mensal("Academia", "150.00", 10, LocalDate.now(), null)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Long recorrenteId = objectMapper.readTree(criada).get("id").asLong();

        String outro = mockMvc.perform(post("/api/auth/registrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Bruno\",\"email\":\"bruno@clareza.dev\","
                                + "\"senha\":\"senha-secreta\"}"))
                .andReturn().getResponse().getContentAsString();
        String tokenDoBruno = objectMapper.readTree(outro).get("token").asText();

        mockMvc.perform(delete("/api/transacoes-recorrentes/" + recorrenteId)
                        .header("Authorization", "Bearer " + tokenDoBruno))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/transacoes-recorrentes")
                        .header("Authorization", "Bearer " + tokenDoBruno))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("sem token nao se cadastra recorrencia")
    void deveExigirAutenticacao() throws Exception {
        mockMvc.perform(get("/api/transacoes-recorrentes")).andExpect(status().isUnauthorized());
    }

    private String mensal(String descricao, String valor, int diaDoMes,
                          LocalDate inicio, LocalDate fim) {
        String dataFim = fim == null ? "" : String.format(",\"dataFim\":\"%s\"", fim);
        return String.format(
                "{\"contaId\":%d,\"categoriaId\":%d,\"descricao\":\"%s\",\"valor\":%s,"
                        + "\"tipo\":\"DESPESA\",\"periodicidade\":\"MENSAL\",\"diaDoMes\":%d,"
                        + "\"dataInicio\":\"%s\"%s}",
                contaId, categoriaId, descricao, valor, diaDoMes, inicio, dataFim);
    }
}
