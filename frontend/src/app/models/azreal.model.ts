export type Estagio = 'IDEACAO' | 'PROTOTIPO_INICIAL' | 'MVP_FUNCIONAL' | 'EM_TESTE';

export type ModalidadeTeste = 'PRESENCIAL' | 'REMOTO' | 'HIBRIDO';

export type PorteOrganizacao =
  | 'PEQUENO'
  | 'MEDIO'
  | 'GRANDE'
  | 'ORGAO_PUBLICO'
  | 'INSTITUICAO_ENSINO_SAUDE'
  | 'COMUNIDADE';

export type StatusDesafio = 'ABERTO' | 'EM_ANDAMENTO' | 'EM_TESTE' | 'CONCLUIDO' | 'CANCELADO';

export type StatusProposta =
  | 'PENDENTE'
  | 'EM_TESTE'
  | 'RECUSADA'
  | 'CANCELADA'
  | 'POC_CONCLUIDA'
  | 'POC_NAO_CONFIRMADA';

export type AvaliacaoMetrica =
  | 'ATENDEU_TOTALMENTE'
  | 'ATENDEU_PARCIALMENTE'
  | 'NAO_ATINGIU_METRICA';

export type ModoPrivacidadePoc = 'EXIBICAO_NOMINAL' | 'ANONIMO_QUALIFICADO';

export interface StartupRequest {
  nome: string;
  email: string;
  senha: string;
  descricao?: string;
  areaAtuacao?: string;
  estagioAtual?: Estagio;
  oQuePrecisaValidar?: string;
  aceiteTermoId: string;
  ufIbge?: string;
  municipioIbge?: string;
  modalidadesSuportadas?: ModalidadeTeste[];
}

export interface StartupResponse {
  id: string;
  nome: string;
  email: string;
  descricao?: string;
  areaAtuacao?: string;
  estagioAtual?: Estagio;
  oQuePrecisaValidar?: string;
  dataCadastro?: string;
  ufIbge?: string;
  municipioIbge?: string;
  modalidadesSuportadas?: ModalidadeTeste[];
}

export interface AmbienteValidacaoRequest {
  razaoSocial: string;
  nomeFantasia: string;
  setor: string;
  porte: PorteOrganizacao;
  ufIbge: string;
  municipioIbge: string;
  emailContato: string;
  telefoneContato?: string;
  linkedinUrl?: string;
  senha: string;
  aceiteTermoId: string;
}

export interface AmbienteValidacaoResponse {
  id: string;
  razaoSocial: string;
  nomeFantasia: string;
  setor: string;
  porte: PorteOrganizacao;
  ufIbge: string;
  municipioIbge: string;
  emailContato: string;
  telefoneContato?: string;
  linkedinUrl?: string;
  ativo: boolean;
  dataCadastro?: string;
}

export interface DesafioRecomendado {
  id: string;
  titulo: string;
  descricaoProblema: string;
  setor: string;
  porte: PorteOrganizacao;
  ufIbge?: string;
  municipioIbge?: string;
  modalidade: ModalidadeTeste;
  publicoAlvo?: string;
  tamanhoAmostra?: number;
  duracaoMaxima?: number;
  status: StatusDesafio;
  dataCriacao?: string;
  motivo?: string;
  origemRecomendacao?: string;
}

export interface PropostaPocResponse {
  id: string;
  startupId: string;
  desafioValidacaoId: string;
  escopoTeste: string;
  duracaoSugeridaDias: number;
  responsabilidadesStartup?: string;
  responsabilidadesAmbiente?: string;
  metricaValidacao: string;
  aceitePrevioTermoSigilo: boolean;
  status: StatusProposta;
  dataCriacao?: string;
  prazoRespostaAmbiente?: string;
  motivoRecusa?: string;
}
