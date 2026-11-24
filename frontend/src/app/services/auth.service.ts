import { Injectable, computed, signal } from '@angular/core';
import { MeResponse, PerfilAcesso } from '../models/auth.model';

const TOKEN_KEY = 'auth_token';
const BASIC_KEY = 'auth_basic';
const USER_KEY = 'auth_user';
const ENTITY_ID_KEY = 'auth_entity_id';
const ENTITY_NAME_KEY = 'auth_entity_name';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly _token = signal<string | null>(this.readStorage(TOKEN_KEY));
  private readonly _basicCredentials = signal<string | null>(this.readStorage(BASIC_KEY));
  private readonly _currentUser = signal<MeResponse | null>(this.readUser());
  private readonly _entityId = signal<string | null>(this.readStorage(ENTITY_ID_KEY));
  private readonly _entityName = signal<string | null>(this.readStorage(ENTITY_NAME_KEY));

  readonly token = this._token.asReadonly();
  readonly currentUser = this._currentUser.asReadonly();
  readonly entityId = this._entityId.asReadonly();
  readonly entityName = this._entityName.asReadonly();

  readonly perfil = computed<PerfilAcesso>(() => {
    const user = this._currentUser();
    if (user?.perfil && user.perfil !== 'NONE') {
      return user.perfil;
    }
    const roles = user?.roles ?? [];
    if (roles.includes('ROLE_ADMIN') || roles.includes('ROLE_AZRAEL_ADMIN')) {
      return 'ADMIN';
    }
    if (roles.includes('ROLE_AMBIENTE_VALIDACAO')) {
      return 'AMBIENTE_VALIDACAO';
    }
    if (roles.includes('ROLE_STARTUP')) {
      return 'STARTUP';
    }
    return 'NONE';
  });

  readonly displayName = computed<string>(
    () => this._entityName() || this._currentUser()?.username || 'Usuário Azreal'
  );

  getToken(): string | null {
    return this._token();
  }

  getBasicCredentials(): string | null {
    return this._basicCredentials();
  }

  setToken(token: string): void {
    this.writeStorage(TOKEN_KEY, token);
    this._token.set(token);
  }

  setBasicCredentials(username: string, password: string): void {
    const encoded = btoa(`${username}:${password}`);
    this.writeStorage(BASIC_KEY, encoded);
    this._basicCredentials.set(encoded);
  }

  setCurrentUser(user: MeResponse): void {
    this.writeStorage(USER_KEY, JSON.stringify(user));
    this._currentUser.set(user);
  }

  setEntityContext(entityId: string | null, entityName: string | null): void {
    if (entityId) {
      this.writeStorage(ENTITY_ID_KEY, entityId);
    } else {
      this.removeStorage(ENTITY_ID_KEY);
    }
    if (entityName) {
      this.writeStorage(ENTITY_NAME_KEY, entityName);
    } else {
      this.removeStorage(ENTITY_NAME_KEY);
    }
    this._entityId.set(entityId);
    this._entityName.set(entityName);
  }

  clearToken(): void {
    this.removeStorage(TOKEN_KEY);
    this.removeStorage(BASIC_KEY);
    this.removeStorage(USER_KEY);
    this.removeStorage(ENTITY_ID_KEY);
    this.removeStorage(ENTITY_NAME_KEY);
    this._token.set(null);
    this._basicCredentials.set(null);
    this._currentUser.set(null);
    this._entityId.set(null);
    this._entityName.set(null);
  }

  isAuthenticated(): boolean {
    return !!(this._token() || this._basicCredentials());
  }

  private readStorage(key: string): string | null {
    try {
      return localStorage.getItem(key);
    } catch {
      return null;
    }
  }

  private writeStorage(key: string, value: string): void {
    try {
      localStorage.setItem(key, value);
    } catch {
      // Ignore storage errors in restricted environments
    }
  }

  private removeStorage(key: string): void {
    try {
      localStorage.removeItem(key);
    } catch {
      // Ignore storage errors in restricted environments
    }
  }

  private readUser(): MeResponse | null {
    const raw = this.readStorage(USER_KEY);
    if (!raw) {
      return null;
    }
    try {
      return JSON.parse(raw) as MeResponse;
    } catch {
      return null;
    }
  }
}
