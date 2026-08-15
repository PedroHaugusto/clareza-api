CREATE TABLE transacao_recorrente (
    id            BIGSERIAL      NOT NULL,
    usuario_id    BIGINT         NOT NULL,
    conta_id      BIGINT         NOT NULL,
    categoria_id  BIGINT         NOT NULL,
    descricao     VARCHAR(150)   NOT NULL,
    valor         NUMERIC(15, 2) NOT NULL,
    tipo          VARCHAR(10)    NOT NULL,
    periodicidade VARCHAR(10)    NOT NULL,
    -- Mensal e anual usam dia_do_mes; semanal usa dia_da_semana. Nunca os dois.
    dia_do_mes    INTEGER,
    dia_da_semana INTEGER,
    data_inicio   DATE           NOT NULL,
    data_fim      DATE,
    ativa         BOOLEAN        NOT NULL DEFAULT TRUE,
    CONSTRAINT pk_transacao_recorrente PRIMARY KEY (id),
    CONSTRAINT fk_recorrente_usuario FOREIGN KEY (usuario_id) REFERENCES usuario (id) ON DELETE CASCADE,
    CONSTRAINT fk_recorrente_conta FOREIGN KEY (conta_id) REFERENCES conta (id) ON DELETE RESTRICT,
    CONSTRAINT fk_recorrente_categoria FOREIGN KEY (categoria_id) REFERENCES categoria (id) ON DELETE RESTRICT,
    CONSTRAINT ck_recorrente_valor_positivo CHECK (valor > 0),
    CONSTRAINT ck_recorrente_tipo CHECK (tipo IN ('RECEITA', 'DESPESA')),
    CONSTRAINT ck_recorrente_periodicidade CHECK (periodicidade IN ('SEMANAL', 'MENSAL', 'ANUAL')),
    CONSTRAINT ck_recorrente_data_fim CHECK (data_fim IS NULL OR data_fim >= data_inicio),
    CONSTRAINT ck_recorrente_dia CHECK (
        (periodicidade = 'SEMANAL'
            AND dia_da_semana BETWEEN 1 AND 7 AND dia_do_mes IS NULL)
        OR (periodicidade IN ('MENSAL', 'ANUAL')
            AND dia_do_mes BETWEEN 1 AND 31 AND dia_da_semana IS NULL)
    )
);

CREATE INDEX ix_recorrente_usuario ON transacao_recorrente (usuario_id);

-- A coluna ja existia desde a V4, sem destino. Agora a tabela existe e a FK pode ser criada.
-- SET NULL: apagar a regra nao deve apagar o historico de ocorrencias ja confirmadas.
ALTER TABLE transacao
    ADD CONSTRAINT fk_transacao_recorrente
    FOREIGN KEY (transacao_recorrente_id) REFERENCES transacao_recorrente (id) ON DELETE SET NULL;

CREATE INDEX ix_transacao_recorrente ON transacao (transacao_recorrente_id);
