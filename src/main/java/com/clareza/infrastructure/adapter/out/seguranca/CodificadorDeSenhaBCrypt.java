package com.clareza.infrastructure.adapter.out.seguranca;

import com.clareza.application.port.out.CodificadorDeSenhaPort;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CodificadorDeSenhaBCrypt implements CodificadorDeSenhaPort {

    private final PasswordEncoder passwordEncoder;

    @Override
    public String codificar(String senhaEmTextoPuro) {
        return passwordEncoder.encode(senhaEmTextoPuro);
    }

    @Override
    public boolean confere(String senhaEmTextoPuro, String senhaHash) {
        if (senhaEmTextoPuro == null || senhaHash == null) {
            return false;
        }
        return passwordEncoder.matches(senhaEmTextoPuro, senhaHash);
    }
}
