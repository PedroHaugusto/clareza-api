CREATE TABLE transacao (
    id                      BIGSERIAL      NOT NULL,
    usuario_id              BIGINT         NOT NULL,
    conta_id                BIGINT         NOT NULL,
    categoria_id            BIGINT         NOT NULL,
    descricao               VARCHAR(150)   NOT NULL,
    -- Sempre positivo: o sinal do lancamento vem do tipo.
    valor                   NUMERIC(15, 2) NOT NULL,
    tipo                    VARCHAR(10)    NOT NULL,
    data_prevista           DATE           NOT NULL,
    data_efetivacao         DATE,
    status                  VARCHAR(15)    NOT NULL,
    -- As quatro colunas abaixo so ganham uso no Bloco 6 (recorrencia e parcelamento).
    -- A FK de transacao_recorrente_id entra junto com a tabela, no mesmo bloco.
    transacao_recorrente_id BIGINT,
    grupo_parcelamento_id   UUID,
    numero_parcela          INTEGER,
    total_parcelas          INTEGER,
    CONSTRAINT pk_transacao PRIMARY KEY (id),
    CONSTRAINT fk_transacao_usuario FOREIGN KEY (usuario_id) REFERENCES usuario (id) ON DELETE CASCADE,
    -- RESTRICT de proposito: apagar conta ou categoria em uso deve falhar, nao levar o historico junto.
    CONSTRAINT fk_transacao_conta FOREIGN KEY (conta_id) REFERENCES conta (id) ON DELETE RESTRICT,
    CONSTRAINT fk_transacao_categoria FOREIGN KEY (categoria_id) REFERENCES categoria (id) ON DELETE RESTRICT,
    CONSTRAINT ck_transacao_valor_positivo CHECK (valor > 0),
    CONSTRAINT ck_transacao_tipo CHECK (tipo IN ('RECEITA', 'DESPESA')),
    -- ATRASADA nao entra: e derivado da data prevista no momento da leitura.
    CONSTRAINT ck_transacao_status CHECK (status IN ('PREVISTA', 'CONFIRMADA')),
    CONSTRAINT ck_transacao_efetivacao CHECK (
        (status = 'CONFIRMADA' AND data_efetivacao IS NOT NULL)
        OR (status = 'PREVISTA' AND data_efetivacao IS NULL)
    ),
    CONSTRAINT ck_transacao_parcelamento CHECK (
        (grupo_parcelamento_id IS NULL AND numero_parcela IS NULL AND total_parcelas IS NULL)
        OR (grupo_parcelamento_id IS NOT NULL AND numero_parcela IS NOT NULL AND total_parcelas IS NOT NULL)
    )
);

-- A consulta base da tela e sempre "as transacoes deste usuario em uma janela de datas".
CREATE INDEX ix_transacao_usuario_data ON transacao (usuario_id, data_prevista);
CREATE INDEX ix_transacao_conta ON transacao (conta_id);
CREATE INDEX ix_transacao_categoria ON transacao (categoria_id);
