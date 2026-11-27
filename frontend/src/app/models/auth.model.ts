export type PerfilAcesso = 'STARTUP' | 'AMBIENTE_VALIDACAO' | 'ADMIN' | 'NONE';

export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  expiresIn: number;
  roles: string[];
}

export interface MeResponse {
  id: string;
  username: string;
  email: string;
  roles: string[];
  perfil: PerfilAcesso;
}

export interface AceiteTermoRequest {
  versaoTermo: string;
  ip?: string;
  consentimentoIaAnonimizado?: boolean;
}

export interface AceiteTermoResponse {
  id: string;
  versaoTermo: string;
  dataHora: string;
  consentimentoIaAnonimizado: boolean;
}
