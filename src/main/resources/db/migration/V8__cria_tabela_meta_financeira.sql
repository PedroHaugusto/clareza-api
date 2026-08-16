CREATE TABLE meta_financeira (
    id             BIGSERIAL      NOT NULL,
    usuario_id     BIGINT         NOT NULL,
    nome           VARCHAR(100)   NOT NULL,
    -- percentual_concluido e valor_restante nao existem aqui: sao derivados na leitura.
    valor_atual    NUMERIC(15, 2) NOT NULL DEFAULT 0,
    valor_objetivo NUMERIC(15, 2) NOT NULL,
    prazo          DATE,
    descricao      VARCHAR(255),
    CONSTRAINT pk_meta_financeira PRIMARY KEY (id),
    CONSTRAINT fk_meta_financeira_usuario FOREIGN KEY (usuario_id) REFERENCES usuario (id) ON DELETE CASCADE,
    CONSTRAINT ck_meta_financeira_objetivo CHECK (valor_objetivo > 0),
    -- Pode passar do objetivo: guardar mais que o planejado nao e erro.
    CONSTRAINT ck_meta_financeira_atual CHECK (valor_atual >= 0)
);

CREATE INDEX ix_meta_financeira_usuario ON meta_financeira (usuario_id);
