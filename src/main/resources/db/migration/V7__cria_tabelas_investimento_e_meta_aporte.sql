-- Dado declarativo: o usuario informa valor e rentabilidade, sem cotacao de mercado.
CREATE TABLE investimento (
    id                      BIGSERIAL      NOT NULL,
    usuario_id              BIGINT         NOT NULL,
    nome                    VARCHAR(100)   NOT NULL,
    tipo                    VARCHAR(20)    NOT NULL,
    valor_investido         NUMERIC(15, 2) NOT NULL,
    -- Percentual ao ano. Aceita negativo: aplicacao no prejuizo tambem e informacao.
    rentabilidade_informada NUMERIC(7, 2)  NOT NULL DEFAULT 0,
    CONSTRAINT pk_investimento PRIMARY KEY (id),
    CONSTRAINT fk_investimento_usuario FOREIGN KEY (usuario_id) REFERENCES usuario (id) ON DELETE CASCADE,
    CONSTRAINT ck_investimento_valor CHECK (valor_investido > 0),
    CONSTRAINT ck_investimento_tipo CHECK (tipo IN ('RENDA_FIXA', 'ACOES', 'FIIS', 'CRIPTO', 'TESOURO')),
    CONSTRAINT ck_investimento_rentabilidade CHECK (rentabilidade_informada BETWEEN -1000 AND 1000)
);

CREATE INDEX ix_investimento_usuario ON investimento (usuario_id);

CREATE TABLE meta_aporte_mensal (
    id         BIGSERIAL      NOT NULL,
    -- Um unico registro ativo por usuario, conforme a secao 4.
    usuario_id BIGINT         NOT NULL,
    valor      NUMERIC(15, 2) NOT NULL,
    CONSTRAINT pk_meta_aporte PRIMARY KEY (id),
    CONSTRAINT fk_meta_aporte_usuario FOREIGN KEY (usuario_id) REFERENCES usuario (id) ON DELETE CASCADE,
    CONSTRAINT uk_meta_aporte_usuario UNIQUE (usuario_id),
    CONSTRAINT ck_meta_aporte_valor CHECK (valor > 0)
);
