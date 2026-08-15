package com.clareza.domain.exception;

public class CredenciaisInvalidasException extends DominioException {

    private static final long serialVersionUID = 1L;

    public CredenciaisInvalidasException() {
        super("E-mail ou senha invalidos");
    }
}
