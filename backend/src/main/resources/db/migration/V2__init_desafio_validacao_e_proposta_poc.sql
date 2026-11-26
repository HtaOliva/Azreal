-- ============================================================================
-- Azreal (POC-Match) — Sprint 2 Relational Schema Migration (Flyway V2)
-- Requisitos: RF10, RF11, RF12, RF13, RF14 (Desafios e Propostas de POC)
-- ============================================================================

CREATE TABLE IF NOT EXISTS tb_desafio_validacao (
    id UUID PRIMARY KEY,
    ambiente_validacao_id UUID NOT NULL REFERENCES tb_ambiente_validacao(id) ON DELETE CASCADE,
    titulo VARCHAR(180) NOT NULL,
    descricao_problema TEXT NOT NULL,
    publico_alvo VARCHAR(255),
    tamanho_amostra INTEGER,
    modalidade VARCHAR(30),
    duracao_maxima INTEGER,
    status VARCHAR(30) NOT NULL DEFAULT 'ABERTO',
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    data_criacao TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_desafio_ambiente_id ON tb_desafio_validacao(ambiente_validacao_id);
CREATE INDEX IF NOT EXISTS idx_desafio_status_ativo ON tb_desafio_validacao(ativo, status);

CREATE TABLE IF NOT EXISTS tb_proposta_poc (
    id UUID PRIMARY KEY,
    startup_id UUID NOT NULL REFERENCES tb_startup(id) ON DELETE CASCADE,
    desafio_validacao_id UUID NOT NULL REFERENCES tb_desafio_validacao(id) ON DELETE CASCADE,
    escopo_teste TEXT NOT NULL,
    duracao_sugerida_dias INTEGER NOT NULL,
    responsabilidades_startup TEXT,
    responsabilidades_ambiente TEXT,
    metrica_validacao TEXT NOT NULL,
    aceite_previo_termo_sigilo BOOLEAN NOT NULL DEFAULT FALSE,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDENTE',
    data_criacao TIMESTAMP NOT NULL,
    prazo_resposta_ambiente TIMESTAMP,
    motivo_recusa TEXT
);

CREATE INDEX IF NOT EXISTS idx_proposta_startup_id ON tb_proposta_poc(startup_id);
CREATE INDEX IF NOT EXISTS idx_proposta_desafio_id ON tb_proposta_poc(desafio_validacao_id);
CREATE INDEX IF NOT EXISTS idx_proposta_status ON tb_proposta_poc(status);
