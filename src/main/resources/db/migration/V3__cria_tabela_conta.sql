-- Contas e cartoes compartilham a mesma tabela, diferenciados apenas pelo tipo.
CREATE TABLE conta (
    id         BIGSERIAL   NOT NULL,
    usuario_id BIGINT      NOT NULL,
    nome       VARCHAR(60) NOT NULL,
    tipo       VARCHAR(20) NOT NULL,
    CONSTRAINT pk_conta PRIMARY KEY (id),
    CONSTRAINT fk_conta_usuario FOREIGN KEY (usuario_id) REFERENCES usuario (id) ON DELETE CASCADE,
    CONSTRAINT uk_conta_usuario_nome UNIQUE (usuario_id, nome),
    CONSTRAINT ck_conta_tipo CHECK (tipo IN ('CONTA_CORRENTE', 'CONTA_POUPANCA', 'CARTAO_CREDITO', 'CARTEIRA'))
);

CREATE INDEX ix_conta_usuario ON conta (usuario_id);
