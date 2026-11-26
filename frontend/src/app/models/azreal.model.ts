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
  integrantes?: string[];
  localidadeSede?: string;
  localidadeAtuacao?: string;
  ligadaIncubadora?: boolean;
  incubadoraLigada?: string;
  oQuePrecisaValidar?: string;
  dataCadastro?: string;
  ufIbge?: string;
  municipioIbge?: string;
  modalidadesSuportadas?: ModalidadeTeste[];
}

export interface StartupUpdateRequest {
  nome?: string;
  descricao?: string;
  areaAtuacao?: string;
  estagioAtual?: Estagio;
  integrantes?: string[];
  localidadeSede?: string;
  localidadeAtuacao?: string;
  ligadaIncubadora?: boolean;
  incubadoraLigada?: string;
  ufIbge?: string;
  municipioIbge?: string;
  oQuePrecisaValidar?: string;
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

export interface AmbienteValidacaoUpdateRequest {
  razaoSocial?: string;
  nomeFantasia?: string;
  setor?: string;
  porte?: PorteOrganizacao;
  ufIbge?: string;
  municipioIbge?: string;
  emailContato?: string;
  telefoneContato?: string;
  linkedinUrl?: string;
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

export interface DesafioValidacaoRequest {
  ambienteValidacaoId: string;
  titulo: string;
  descricaoProblema: string;
  publicoAlvo?: string;
  tamanhoAmostra?: number;
  modalidade: ModalidadeTeste;
  duracaoMaxima?: number;
}

export interface DesafioValidacaoResponse {
  id: string;
  ambienteValidacaoId?: string;
  ambienteValidacaoNome?: string;
  razaoSocial?: string;
  emailContato?: string;
  telefoneContato?: string;
  setor?: string;
  porte?: PorteOrganizacao;
  ufIbge?: string;
  municipioIbge?: string;
  titulo: string;
  descricaoProblema: string;
  publicoAlvo?: string;
  tamanhoAmostra?: number;
  modalidade: ModalidadeTeste;
  duracaoMaxima?: number;
  status: StatusDesafio;
  ativo: boolean;
  dataCriacao?: string;
}

export interface PropostaPocRequest {
  startupId: string;
  desafioValidacaoId: string;
  escopoTeste: string;
  duracaoSugeridaDias: number;
  responsabilidadesStartup?: string;
  responsabilidadesAmbiente?: string;
  metricaValidacao: string;
  aceitePrevioTermoSigilo: boolean;
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

export interface RecusaPropostaRequest {
  motivoRecusa: string;
}

export interface TermoPilotoResponse {
  id: string;
  propostaPocId: string;
  startupId: string;
  desafioValidacaoId: string;
  ambienteValidacaoId: string;
  escopoTeste: string;
  duracaoDias: number;
  responsabilidadesStartup?: string;
  responsabilidadesAmbiente?: string;
  metricaValidacao: string;
  dataCriacao?: string;
  statusProposta: StatusProposta;
  razaoSocial?: string;
  nomeFantasia?: string;
  emailContato?: string;
  telefoneContato?: string;
  setor?: string;
  porte?: PorteOrganizacao;
  identidadeAmbienteRevelada?: AmbienteValidacaoResponse;
  resumoEntrega?: string;
  dataEntrega?: string;
  pilotoExecutado?: boolean;
  avaliacaoMetrica?: AvaliacaoMetrica;
  parecer?: string;
  modoPrivacidade?: ModoPrivacidadePoc;
  certificadoEmitido?: boolean;
  dataConfirmacao?: string;
  nomeStartup?: string;
  codigoTermo?: string;
  dataInicio?: string;
  dataFim?: string;
}

export interface EntregaPilotoRequest {
  resumoEntrega: string;
  linksEvidencias?: string;
  observacoes?: string;
}

export interface ConfirmacaoExecucaoRequest {
  pilotoExecutado: boolean;
  avaliacaoMetrica?: AvaliacaoMetrica;
  parecer?: string;
  modoPrivacidade?: ModoPrivacidadePoc;
  direcionamentoPosPoc?: string;
  justificativaNaoExecucao?: string;
}

export interface ConfirmacaoExecucaoResponse {
  termoPilotoId: string;
  propostaPocId: string;
  startupId: string;
  ambienteValidacaoId: string;
  status: StatusProposta;
  pilotoExecutado: boolean;
  avaliacaoMetrica?: AvaliacaoMetrica;
  parecer?: string;
  modoPrivacidade?: ModoPrivacidadePoc;
  direcionamentoPosPoc?: string;
  justificativaNaoExecucao?: string;
  certificadoEmitido?: boolean;
  dataConfirmacao?: string;
  nomeStartup?: string;
  codigoTermo?: string;
  dataInicio?: string;
  dataFim?: string;
}

export interface RegistroOficialPocResponse {
  id: string;
  codigoVerificador: string;
  termoPilotoId: string;
  propostaPocId: string;
  startupId: string;
  ambienteValidacaoId: string;
  desafioValidacaoId: string;
  escopoTestado: string;
  duracaoDias: number;
  avaliacaoMetrica: AvaliacaoMetrica;
  parecer?: string;
  modoPrivacidade: ModoPrivacidadePoc;
  direcionamentoPosPoc?: string;
  metricaValidacao: string;
  resumoEntrega?: string;
  nomeOrganizacao: string;
  setorOrganizacao?: string;
  porteOrganizacao?: PorteOrganizacao;
  dataEmissao?: string;
  anonimo: boolean;
}
