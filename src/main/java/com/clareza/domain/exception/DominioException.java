package com.clareza.domain.exception;

public abstract class DominioException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    protected DominioException(String mensagem) {
        super(mensagem);
    }
}
