import { Injectable, signal } from '@angular/core';

const TOKEN_KEY = 'auth_token';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly _token = signal<string | null>(this.readToken());

  readonly token = this._token.asReadonly();

  getToken(): string | null {
    return this._token();
  }

  setToken(token: string): void {
    localStorage.setItem(TOKEN_KEY, token);
    this._token.set(token);
  }

  clearToken(): void {
    localStorage.removeItem(TOKEN_KEY);
    this._token.set(null);
  }

  isAuthenticated(): boolean {
    return !!this._token();
  }

  private readToken(): string | null {
    try {
      return localStorage.getItem(TOKEN_KEY);
    } catch {
      return null;
    }
  }
}
