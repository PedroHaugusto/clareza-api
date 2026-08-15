package com.clareza.infrastructure.adapter.in.web;

import com.clareza.domain.exception.RecursoNaoEncontradoException;
import com.clareza.domain.exception.RegraDeNegocioException;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Valid;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
import java.util.Set;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Exercita o advice contra um controller de apoio, sem subir contexto Spring nem banco: o que
 * esta sob teste e o mapeamento excecao -> resposta HTTP.
 */
class TratadorDeErrosTest {

    private MockMvc mockMvc;

    @BeforeEach
    void configurar() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ControladorDeApoio())
                .setControllerAdvice(new TratadorDeErros())
                .build();
    }

    @Test
    @DisplayName("corpo invalido vira 400 listando cada campo recusado")
    void deveRetornar400ComOsCamposInvalidos_quandoOCorpoFalhaNaValidacao() throws Exception {
        mockMvc.perform(post("/teste/corpo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"\",\"valor\":-5}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.mensagem", is("Falha de validacao")))
                .andExpect(jsonPath("$.path", is("/teste/corpo")))
                .andExpect(jsonPath("$.timestamp", notNullValue()))
                .andExpect(jsonPath("$.campos", hasSize(2)));
    }

    @Test
    @DisplayName("sem erro de validacao a resposta nao traz o atributo campos")
    void naoDeveIncluirCampos_quandoOErroNaoEDeValidacao() throws Exception {
        mockMvc.perform(get("/teste/nao-encontrado"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.campos").doesNotExist());
    }

    @Test
    @DisplayName("recurso inexistente vira 404 preservando a mensagem do dominio")
    void deveRetornar404_quandoORecursoNaoExiste() throws Exception {
        mockMvc.perform(get("/teste/nao-encontrado"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.mensagem", is("Categoria de id 7 nao encontrado")));
    }

    @Test
    @DisplayName("violacao de regra de negocio vira 422")
    void deveRetornar422_quandoUmaRegraDeNegocioEViolada() throws Exception {
        mockMvc.perform(get("/teste/regra"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status", is(422)))
                .andExpect(jsonPath("$.mensagem", is("Categoria padrao do sistema nao pode ser excluida")));
    }

    @Test
    @DisplayName("JSON malformado vira 400 sem vazar detalhe interno do parser")
    void deveRetornar400SemDetalheInterno_quandoOJsonEMalformado() throws Exception {
        mockMvc.perform(post("/teste/corpo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem", is("Corpo da requisicao ilegivel ou mal formatado")));
    }

    @Test
    @DisplayName("parametro com tipo incompativel vira 400 nomeando o parametro")
    void deveRetornar400NomeandoOParametro_quandoOTipoEIncompativel() throws Exception {
        mockMvc.perform(get("/teste/numero").param("valor", "janeiro"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem", containsString("valor")));
    }

    @Test
    @DisplayName("constraint violada reporta so o ultimo no do caminho, sem o prefixo interno")
    void deveEncurtarOCaminhoDoCampo_quandoUmaConstraintEViolada() throws Exception {
        // A violacao nasce como "interno.nome"; o cliente recebe apenas "nome".
        mockMvc.perform(get("/teste/constraint"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos", hasSize(1)))
                .andExpect(jsonPath("$.campos[0].campo", is("nome")))
                .andExpect(jsonPath("$.campos[0].mensagem", is("nao pode ser vazio")));
    }

    @Test
    @DisplayName("metodo HTTP errado vira 405")
    void deveRetornar405_quandoOMetodoNaoESuportado() throws Exception {
        mockMvc.perform(post("/teste/numero"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.status", is(405)));
    }

    @Test
    @DisplayName("falha inesperada vira 500 generico, sem expor a mensagem original")
    void deveRetornar500Generico_quandoAFalhaEInesperada() throws Exception {
        mockMvc.perform(get("/teste/quebrado"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status", is(500)))
                .andExpect(jsonPath("$.mensagem", is("Erro interno inesperado")));
    }

    @RestController
    @RequestMapping("/teste")
    static class ControladorDeApoio {

        @PostMapping("/corpo")
        public void receberCorpo(@Valid @RequestBody CorpoDeTeste corpo) {
            // Sem efeito: o que interessa e a validacao disparar antes de chegar aqui.
        }

        @GetMapping("/numero")
        public int receberNumero(@RequestParam int valor) {
            return valor;
        }

        @GetMapping("/constraint")
        public void violarConstraint() {
            // A validacao por metodo depende de proxy AOP, que o standalone nao monta. Validar um
            // objeto aninhado produz violacoes reais com caminho composto ("interno.nome"), que e
            // justamente o que o advice precisa encurtar antes de devolver ao cliente.
            Validator validador = Validation.buildDefaultValidatorFactory().getValidator();
            Set<ConstraintViolation<CorpoAninhado>> violacoes = validador.validate(new CorpoAninhado());
            throw new ConstraintViolationException(violacoes);
        }

        @GetMapping("/nao-encontrado")
        public void naoEncontrado() {
            throw new RecursoNaoEncontradoException("Categoria", 7L);
        }

        @GetMapping("/regra")
        public void regraViolada() {
            throw new RegraDeNegocioException("Categoria padrao do sistema nao pode ser excluida");
        }

        @GetMapping("/quebrado")
        public void quebrado() {
            throw new IllegalStateException("detalhe interno que nao pode vazar");
        }
    }

    @Getter
    @Setter
    static class CorpoAninhado {

        @Valid
        private CorpoDeTeste interno = new CorpoDeTeste();
    }

    @Getter
    @Setter
    static class CorpoDeTeste {

        @NotBlank(message = "nao pode ser vazio")
        private String nome;

        @Positive(message = "deve ser positivo")
        private Integer valor;
    }
}