package com.clareza.domain.exception;

public class AutenticacaoGoogleException extends DominioException {

    private static final long serialVersionUID = 1L;

    public AutenticacaoGoogleException(String motivo) {
        super(motivo);
    }
}
