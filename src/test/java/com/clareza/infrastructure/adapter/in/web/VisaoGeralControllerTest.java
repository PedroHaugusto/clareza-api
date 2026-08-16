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
class VisaoGeralControllerTest extends TesteDeIntegracao {

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
    @DisplayName("visao geral separa realizado de previsto e projeta os proximos meses")
    void deveMontarAVisaoGeral() throws Exception {
        LocalDate hoje = hoje();
        LocalDate inicioDoMes = hoje.withDayOfMonth(1);

        criar("Salario", "5000.00", "RECEITA", inicioDoMes, inicioDoMes);
        criar("Aluguel", "1200.00", "DESPESA", inicioDoMes, inicioDoMes);
        criar("Mercado", "89.90", "DESPESA", hoje.withDayOfMonth(hoje.lengthOfMonth()), null);
        criar("Salario futuro", "5000.00", "RECEITA", hoje.plusMonths(1).withDayOfMonth(5), null);

        mockMvc.perform(get("/api/visao-geral").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.saldoRealizado", is(3800.00)))
                .andExpect(jsonPath("$.saldoDisponivel", is(3710.10)))
                .andExpect(jsonPath("$.mesAtual.receitasRealizadas", is(5000.00)))
                .andExpect(jsonPath("$.mesAtual.despesasRealizadas", is(1200.00)))
                .andExpect(jsonPath("$.mesAtual.despesasPrevistas", is(89.90)))
                .andExpect(jsonPath("$.mesAtual.saldoDoMes", is(3710.10)))
                .andExpect(jsonPath("$.proximosMeses", hasSize(3)))
                .andExpect(jsonPath("$.proximosMeses[0].receitasPrevistas", is(5000.00)));
    }

    @Test
    @DisplayName("o lancamento do mes que vem nao entra no saldo disponivel")
    void oMesSeguinteNaoDeveEntrarNoSaldo() throws Exception {
        LocalDate hoje = hoje();
        criar("Salario", "1000.00", "RECEITA", hoje.withDayOfMonth(1), hoje.withDayOfMonth(1));
        criar("Compra futura", "900.00", "DESPESA", hoje.plusMonths(1).withDayOfMonth(10), null);

        mockMvc.perform(get("/api/visao-geral").header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.saldoDisponivel", is(1000.00)))
                .andExpect(jsonPath("$.proximosMeses[0].despesasPrevistas", is(900.00)));
    }

    @Test
    @DisplayName("saldo-disponivel devolve os dois saldos")
    void deveDevolverOSaldoNoEndpointProprio() throws Exception {
        LocalDate hoje = hoje();
        criar("Salario", "2000.00", "RECEITA", hoje.withDayOfMonth(1), hoje.withDayOfMonth(1));
        criar("Conta a pagar", "500.00", "DESPESA", hoje.withDayOfMonth(hoje.lengthOfMonth()), null);

        mockMvc.perform(get("/api/saldo-disponivel").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.saldoDisponivel", is(1500.00)))
                .andExpect(jsonPath("$.saldoRealizado", is(2000.00)));
    }

    @Test
    @DisplayName("usuario sem lancamento recebe zeros")
    void deveDevolverZeradoParaUsuarioNovo() throws Exception {
        mockMvc.perform(get("/api/visao-geral").header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.saldoDisponivel", is(0)))
                .andExpect(jsonPath("$.mesAtual.totalReceitas", is(0)))
                .andExpect(jsonPath("$.proximosMeses", hasSize(3)));
    }

    @Test
    @DisplayName("a visao geral nao enxerga lancamento de outro usuario")
    void deveIsolarPorUsuario() throws Exception {
        LocalDate hoje = hoje();
        criar("Salario", "5000.00", "RECEITA", hoje.withDayOfMonth(1), hoje.withDayOfMonth(1));

        String outro = mockMvc.perform(post("/api/auth/registrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Bruno\",\"email\":\"bruno@clareza.dev\","
                                + "\"senha\":\"senha-secreta\"}"))
                .andReturn().getResponse().getContentAsString();
        String tokenDoBruno = objectMapper.readTree(outro).get("token").asText();

        mockMvc.perform(get("/api/visao-geral").header("Authorization", "Bearer " + tokenDoBruno))
                .andExpect(jsonPath("$.saldoDisponivel", is(0)));
    }

    @Test
    @DisplayName("as duas rotas exigem autenticacao")
    void deveExigirAutenticacao() throws Exception {
        mockMvc.perform(get("/api/visao-geral")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/saldo-disponivel")).andExpect(status().isUnauthorized());
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
