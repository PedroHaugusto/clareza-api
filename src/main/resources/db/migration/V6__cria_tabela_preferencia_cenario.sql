CREATE TABLE preferencia_cenario (
    id                         BIGSERIAL     NOT NULL,
    -- Um unico registro por usuario: a preferencia e configuracao, nao historico.
    usuario_id                 BIGINT        NOT NULL,
    percentual_ajuste_receita  NUMERIC(5, 2) NOT NULL DEFAULT 10,
    percentual_ajuste_despesa  NUMERIC(5, 2) NOT NULL DEFAULT 10,
    CONSTRAINT pk_preferencia_cenario PRIMARY KEY (id),
    CONSTRAINT fk_preferencia_usuario FOREIGN KEY (usuario_id) REFERENCES usuario (id) ON DELETE CASCADE,
    CONSTRAINT uk_preferencia_usuario UNIQUE (usuario_id),
    CONSTRAINT ck_preferencia_receita CHECK (percentual_ajuste_receita BETWEEN 0 AND 100),
    CONSTRAINT ck_preferencia_despesa CHECK (percentual_ajuste_despesa BETWEEN 0 AND 100)
);
