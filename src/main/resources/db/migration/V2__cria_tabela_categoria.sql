CREATE TABLE categoria (
    id         BIGSERIAL   NOT NULL,
    -- Nulo identifica a categoria padrao do sistema, visivel para todos e nao excluivel.
    usuario_id BIGINT,
    nome       VARCHAR(60) NOT NULL,
    tipo       VARCHAR(20) NOT NULL,
    cor_hex    VARCHAR(7)  NOT NULL,
    CONSTRAINT pk_categoria PRIMARY KEY (id),
    CONSTRAINT fk_categoria_usuario FOREIGN KEY (usuario_id) REFERENCES usuario (id) ON DELETE CASCADE,
    CONSTRAINT ck_categoria_tipo CHECK (tipo IN ('RECEITA', 'DESPESA', 'AMBOS'))
);

-- Indices parciais em vez de UNIQUE (usuario_id, nome): no Postgres cada NULL e distinto no
-- indice, entao um unique comum deixaria cadastrar varias categorias globais com o mesmo nome.
CREATE UNIQUE INDEX uk_categoria_global_nome
    ON categoria (nome) WHERE usuario_id IS NULL;

CREATE UNIQUE INDEX uk_categoria_usuario_nome
    ON categoria (usuario_id, nome) WHERE usuario_id IS NOT NULL;

CREATE INDEX ix_categoria_usuario ON categoria (usuario_id);

INSERT INTO categoria (usuario_id, nome, tipo, cor_hex) VALUES
    (NULL, 'Salário',       'RECEITA', '#2E7D32'),
    (NULL, 'Moradia',       'DESPESA', '#6D4C41'),
    (NULL, 'Alimentação',   'DESPESA', '#EF6C00'),
    (NULL, 'Transporte',    'DESPESA', '#1565C0'),
    (NULL, 'Lazer',         'DESPESA', '#AD1457'),
    (NULL, 'Investimentos', 'AMBOS',   '#00838F'),
    (NULL, 'Outros',        'AMBOS',   '#546E7A');
