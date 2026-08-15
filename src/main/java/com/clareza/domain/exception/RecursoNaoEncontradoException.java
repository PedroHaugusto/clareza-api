package com.clareza.domain.exception;

public class RecursoNaoEncontradoException extends DominioException {

    private static final long serialVersionUID = 1L;

    public RecursoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }

    public RecursoNaoEncontradoException(String recurso, Object id) {
        super(String.format("%s de id %s nao encontrado", recurso, id));
    }
}
