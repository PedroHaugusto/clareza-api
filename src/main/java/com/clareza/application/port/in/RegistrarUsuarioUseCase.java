package com.clareza.application.port.in;

public interface RegistrarUsuarioUseCase {

    UsuarioAutenticado registrar(ComandoDeRegistro comando);
}