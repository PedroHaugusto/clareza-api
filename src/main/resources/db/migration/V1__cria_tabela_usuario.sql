CREATE TABLE usuario (
    id         BIGSERIAL    NOT NULL,
    nome       VARCHAR(120) NOT NULL,
    email      VARCHAR(180) NOT NULL,
    -- Nulo quando a pessoa so entra pelo Google; 60 caracteres e o tamanho fixo do hash BCrypt.
    senha_hash VARCHAR(60),
    -- Nulo quando a pessoa so entra por senha.
    google_id  VARCHAR(255),
    CONSTRAINT pk_usuario PRIMARY KEY (id),
    CONSTRAINT uk_usuario_email UNIQUE (email),
    CONSTRAINT uk_usuario_google_id UNIQUE (google_id),
    -- Uma conta sem nenhuma das duas credenciais nunca conseguiria autenticar.
    CONSTRAINT ck_usuario_credencial CHECK (senha_hash IS NOT NULL OR google_id IS NOT NULL)
);