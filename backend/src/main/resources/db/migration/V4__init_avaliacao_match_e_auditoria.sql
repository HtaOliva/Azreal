-- ============================================================================
-- Azreal (POC-Match) — Sprint 4 Relational Schema Migration (Flyway V4)
-- Requisitos: RF21, RNF08 (Avaliação de Matchmaking e Trilha de Auditoria)
-- ============================================================================

CREATE TABLE IF NOT EXISTS tb_avaliacao_match (
    id UUID PRIMARY KEY,
    proposta_poc_id UUID NOT NULL REFERENCES tb_proposta_poc(id) ON DELETE CASCADE,
    termo_piloto_id UUID REFERENCES tb_termo_piloto(id) ON DELETE SET NULL,
    tipo_avaliador VARCHAR(30) NOT NULL,
    usuario_responsavel VARCHAR(180) NOT NULL,
    encaixe_fez_sentido BOOLEAN NOT NULL,
    nota_qualidade INTEGER NOT NULL,
    comentario TEXT,
    origem_match VARCHAR(20) NOT NULL DEFAULT 'IA',
    data_criacao TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_avaliacao_proposta_id ON tb_avaliacao_match(proposta_poc_id);
CREATE INDEX IF NOT EXISTS idx_avaliacao_termo_id ON tb_avaliacao_match(termo_piloto_id);

CREATE TABLE IF NOT EXISTS tb_registro_auditoria (
    id UUID PRIMARY KEY,
    proposta_poc_id UUID NOT NULL,
    termo_piloto_id UUID,
    status_anterior VARCHAR(30),
    status_novo VARCHAR(30),
    usuario_responsavel VARCHAR(180) NOT NULL,
    acao VARCHAR(60) NOT NULL,
    detalhes TEXT,
    data_hora TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_auditoria_proposta_id ON tb_registro_auditoria(proposta_poc_id);
CREATE INDEX IF NOT EXISTS idx_auditoria_termo_id ON tb_registro_auditoria(termo_piloto_id);
CREATE INDEX IF NOT EXISTS idx_auditoria_data_hora ON tb_registro_auditoria(data_hora);
