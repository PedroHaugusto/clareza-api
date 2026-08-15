package com.clareza.infrastructure.adapter.in.web;

import com.clareza.domain.exception.AutenticacaoGoogleException;
import com.clareza.domain.exception.CredenciaisInvalidasException;
import com.clareza.domain.exception.RecursoNaoEncontradoException;
import com.clareza.domain.exception.RegraDeNegocioException;
import com.clareza.infrastructure.adapter.in.web.dto.RespostaErro;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestControllerAdvice
public class TratadorDeErros {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<RespostaErro> tratarCorpoInvalido(MethodArgumentNotValidException excecao,
                                                            HttpServletRequest requisicao) {
        List<RespostaErro.CampoInvalido> campos = new ArrayList<>();
        for (FieldError erro : excecao.getBindingResult().getFieldErrors()) {
            campos.add(new RespostaErro.CampoInvalido(erro.getField(), erro.getDefaultMessage()));
        }
        return montar(HttpStatus.BAD_REQUEST, "Falha de validacao", requisicao, campos);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<RespostaErro> tratarParametroInvalido(ConstraintViolationException excecao,
                                                                HttpServletRequest requisicao) {
        List<RespostaErro.CampoInvalido> campos = new ArrayList<>();
        for (ConstraintViolation<?> violacao : excecao.getConstraintViolations()) {
            campos.add(new RespostaErro.CampoInvalido(ultimoNo(violacao), violacao.getMessage()));
        }
        return montar(HttpStatus.BAD_REQUEST, "Falha de validacao", requisicao, campos);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<RespostaErro> tratarCorpoIlegivel(HttpMessageNotReadableException excecao,
                                                            HttpServletRequest requisicao) {
        log.debug("Corpo da requisicao ilegivel em {}", requisicao.getRequestURI(), excecao);
        return montar(HttpStatus.BAD_REQUEST, "Corpo da requisicao ilegivel ou mal formatado",
                requisicao, null);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<RespostaErro> tratarTipoIncompativel(MethodArgumentTypeMismatchException excecao,
                                                               HttpServletRequest requisicao) {
        String mensagem = String.format("O parametro '%s' recebeu um valor invalido: '%s'",
                excecao.getName(), excecao.getValue());
        return montar(HttpStatus.BAD_REQUEST, mensagem, requisicao, null);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<RespostaErro> tratarMetodoNaoSuportado(HttpRequestMethodNotSupportedException excecao,
                                                                  HttpServletRequest requisicao) {
        String mensagem = String.format("O metodo %s nao e suportado neste recurso", excecao.getMethod());
        return montar(HttpStatus.METHOD_NOT_ALLOWED, mensagem, requisicao, null);
    }

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<RespostaErro> tratarRecursoNaoEncontrado(RecursoNaoEncontradoException excecao,
                                                                    HttpServletRequest requisicao) {
        return montar(HttpStatus.NOT_FOUND, excecao.getMessage(), requisicao, null);
    }

    @ExceptionHandler(AutenticacaoGoogleException.class)
    public ResponseEntity<RespostaErro> tratarFalhaNoLoginComGoogle(AutenticacaoGoogleException excecao,
                                                                     HttpServletRequest requisicao) {
        return montar(HttpStatus.UNAUTHORIZED, excecao.getMessage(), requisicao, null);
    }

    @ExceptionHandler(CredenciaisInvalidasException.class)
    public ResponseEntity<RespostaErro> tratarCredenciaisInvalidas(CredenciaisInvalidasException excecao,
                                                                    HttpServletRequest requisicao) {
        return montar(HttpStatus.UNAUTHORIZED, excecao.getMessage(), requisicao, null);
    }

    @ExceptionHandler(RegraDeNegocioException.class)
    public ResponseEntity<RespostaErro> tratarRegraDeNegocio(RegraDeNegocioException excecao,
                                                             HttpServletRequest requisicao) {
        return montar(HttpStatus.UNPROCESSABLE_ENTITY, excecao.getMessage(), requisicao, null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<RespostaErro> tratarErroInesperado(Exception excecao,
                                                             HttpServletRequest requisicao) {
        log.error("Erro nao tratado em {} {}", requisicao.getMethod(), requisicao.getRequestURI(), excecao);
        return montar(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno inesperado", requisicao, null);
    }

    private ResponseEntity<RespostaErro> montar(HttpStatus status,
                                                String mensagem,
                                                HttpServletRequest requisicao,
                                                List<RespostaErro.CampoInvalido> campos) {
        RespostaErro corpo = RespostaErro.builder()
                .timestamp(Instant.now())
                .status(status.value())
                .erro(status.getReasonPhrase())
                .mensagem(mensagem)
                .path(requisicao.getRequestURI())
                .campos(campos == null || campos.isEmpty() ? null : campos)
                .build();
        return ResponseEntity.status(status).body(corpo);
    }

    private String ultimoNo(ConstraintViolation<?> violacao) {
        String caminho = violacao.getPropertyPath().toString();
        int ultimoPonto = caminho.lastIndexOf('.');
        return ultimoPonto < 0 ? caminho : caminho.substring(ultimoPonto + 1);
    }
}