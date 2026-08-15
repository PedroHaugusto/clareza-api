package com.clareza.application.port.out;

public interface CodificadorDeSenhaPort {

    String codificar(String senhaEmTextoPuro);

    boolean confere(String senhaEmTextoPuro, String senhaHash);
}
