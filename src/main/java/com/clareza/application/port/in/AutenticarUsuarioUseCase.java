package com.clareza.application.port.in;

public interface AutenticarUsuarioUseCase {

    UsuarioAutenticado autenticar(ComandoDeLogin comando);
}