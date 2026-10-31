-- ============================================================================
-- Azreal (POC-Match) — Sprint 1 Relational Schema Migration (Flyway V1)
-- Requisitos: RF01, RF02, RF03, RF04, RF07, RF08, RF09, RNF04, RNF05
-- ============================================================================

CREATE TABLE IF NOT EXISTS tb_usuario (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(120),
    email VARCHAR(180) UNIQUE,
    senha_hash VARCHAR(255),
    ativo BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS tb_aceite_termo (
    id UUID PRIMARY KEY,
    usuario_id UUID,
    versao_termo VARCHAR(30) NOT NULL,
    consentimento_ia_anonimizado BOOLEAN NOT NULL DEFAULT TRUE,
    data_hora TIMESTAMP NOT NULL,
    ip VARCHAR(45)
);

CREATE TABLE IF NOT EXISTS tb_tema (
    id UUID PRIMARY KEY,
    nome VARCHAR(120) NOT NULL UNIQUE,
    tipo VARCHAR(20) NOT NULL DEFAULT 'TEMA',
    descricao TEXT,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    data_cadastro TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS tb_incubadora (
    id UUID PRIMARY KEY,
    nome VARCHAR(180) NOT NULL,
    foco VARCHAR(255),
    criterios_entrada TEXT,
    localidade VARCHAR(120),
    modalidade VARCHAR(60),
    descricao TEXT,
    link VARCHAR(500),
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    data_cadastro TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS tb_incubadora_abrangencia (
    incubadora_id UUID NOT NULL REFERENCES tb_incubadora(id) ON DELETE CASCADE,
    abrangencia VARCHAR(120)
);

CREATE TABLE IF NOT EXISTS tb_regra_plataforma (
    id UUID PRIMARY KEY,
    versao VARCHAR(30) NOT NULL DEFAULT '1.0',
    titulo VARCHAR(180) NOT NULL DEFAULT 'Termo de Piloto e Cláusula de Sigilo',
    texto TEXT NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    data_atualizacao TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS tb_startup (
    id UUID PRIMARY KEY,
    nome VARCHAR(120) NOT NULL,
    email VARCHAR(180) NOT NULL UNIQUE,
    descricao TEXT,
    area_atuacao VARCHAR(120),
    data_fundacao DATE,
    estagio_atual VARCHAR(30),
    localidade_sede VARCHAR(120),
    localidade_atuacao VARCHAR(120),
    uf_ibge VARCHAR(10),
    municipio_ibge VARCHAR(120),
    o_que_precisa_validar TEXT,
    ligada_incubadora BOOLEAN NOT NULL DEFAULT FALSE,
    incubadora_ligada VARCHAR(180),
    usuario_id UUID,
    data_cadastro TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS tb_startup_integrante (
    startup_id UUID NOT NULL REFERENCES tb_startup(id) ON DELETE CASCADE,
    integrante VARCHAR(120)
);

CREATE TABLE IF NOT EXISTS tb_startup_tema (
    startup_id UUID NOT NULL REFERENCES tb_startup(id) ON DELETE CASCADE,
    tema_id UUID
);

CREATE TABLE IF NOT EXISTS tb_startup_modalidade (
    startup_id UUID NOT NULL REFERENCES tb_startup(id) ON DELETE CASCADE,
    modalidade VARCHAR(30)
);

CREATE TABLE IF NOT EXISTS tb_ambiente_validacao (
    id UUID PRIMARY KEY,
    razao_social VARCHAR(180) NOT NULL,
    nome_fantasia VARCHAR(180) NOT NULL,
    setor VARCHAR(120) NOT NULL,
    porte VARCHAR(40) NOT NULL,
    uf_ibge VARCHAR(10) NOT NULL,
    municipio_ibge VARCHAR(120) NOT NULL,
    email_contato VARCHAR(180) NOT NULL UNIQUE,
    telefone_contato VARCHAR(40),
    linkedin_url VARCHAR(255),
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    usuario_id UUID,
    data_cadastro TIMESTAMP NOT NULL
);
