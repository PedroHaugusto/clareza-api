package com.clareza.application.port.in;

public interface AutenticarComGoogleUseCase {

    UsuarioAutenticado autenticar(ComandoDeLoginComGoogle comando);
}
