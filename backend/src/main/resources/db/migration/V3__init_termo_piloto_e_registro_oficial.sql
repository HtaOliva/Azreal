-- ============================================================================
-- Azreal (POC-Match) — Sprint 3 Relational Schema Migration (Flyway V3)
-- Requisitos: RF15, RF18, RF19, RF20 (Termo de Piloto e Registro Oficial de POC)
-- ============================================================================

CREATE TABLE IF NOT EXISTS tb_termo_piloto (
    id UUID PRIMARY KEY,
    proposta_poc_id UUID NOT NULL REFERENCES tb_proposta_poc(id) ON DELETE CASCADE,
    startup_id UUID NOT NULL REFERENCES tb_startup(id),
    desafio_validacao_id UUID NOT NULL REFERENCES tb_desafio_validacao(id),
    ambiente_validacao_id UUID NOT NULL REFERENCES tb_ambiente_validacao(id),
    escopo_teste TEXT NOT NULL,
    duracao_dias INTEGER NOT NULL,
    responsabilidades_startup TEXT,
    responsabilidades_ambiente TEXT,
    metrica_validacao TEXT NOT NULL,
    data_criacao TIMESTAMP NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'EM_TESTE',
    resumo_entrega TEXT,
    data_entrega TIMESTAMP,
    links_evidencias TEXT,
    observacoes_entrega TEXT,
    piloto_executado BOOLEAN,
    data_confirmacao TIMESTAMP,
    avaliacao_metrica VARCHAR(30),
    parecer TEXT,
    modo_privacidade VARCHAR(30),
    direcionamento_pos_poc VARCHAR(120),
    justificativa_nao_execucao TEXT,
    certificado_emitido BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS idx_termo_proposta_id ON tb_termo_piloto(proposta_poc_id);
CREATE INDEX IF NOT EXISTS idx_termo_startup_id ON tb_termo_piloto(startup_id);
CREATE INDEX IF NOT EXISTS idx_termo_ambiente_id ON tb_termo_piloto(ambiente_validacao_id);
CREATE INDEX IF NOT EXISTS idx_termo_status ON tb_termo_piloto(status);

CREATE TABLE IF NOT EXISTS tb_registro_oficial_poc (
    id UUID PRIMARY KEY,
    codigo_verificador VARCHAR(40) NOT NULL UNIQUE,
    termo_piloto_id UUID NOT NULL REFERENCES tb_termo_piloto(id) ON DELETE CASCADE,
    proposta_poc_id UUID NOT NULL REFERENCES tb_proposta_poc(id),
    startup_id UUID NOT NULL REFERENCES tb_startup(id),
    ambiente_validacao_id UUID NOT NULL REFERENCES tb_ambiente_validacao(id),
    desafio_validacao_id UUID REFERENCES tb_desafio_validacao(id),
    escopo_testado TEXT NOT NULL,
    duracao_dias INTEGER NOT NULL,
    avaliacao_metrica VARCHAR(30) NOT NULL,
    parecer TEXT NOT NULL,
    modo_privacidade VARCHAR(30) NOT NULL,
    direcionamento_pos_poc VARCHAR(120),
    metrica_validacao TEXT,
    resumo_entrega TEXT,
    nome_organizacao VARCHAR(180),
    setor_organizacao VARCHAR(120),
    porte_organizacao VARCHAR(40),
    data_emissao TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_registro_codigo_verificador ON tb_registro_oficial_poc(codigo_verificador);
CREATE INDEX IF NOT EXISTS idx_registro_termo_id ON tb_registro_oficial_poc(termo_piloto_id);
CREATE INDEX IF NOT EXISTS idx_registro_startup_id ON tb_registro_oficial_poc(startup_id);
CREATE INDEX IF NOT EXISTS idx_registro_ambiente_id ON tb_registro_oficial_poc(ambiente_validacao_id);
