import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import {
  AceiteTermoRequest,
  AceiteTermoResponse,
  LoginRequest,
  LoginResponse,
  MeResponse
} from '../models/auth.model';

@Injectable({ providedIn: 'root' })
export class AuthApiService {
  private readonly http = inject(HttpClient);

  login(payload: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>('/auth/login', payload);
  }

  me(): Observable<MeResponse> {
    return this.http.get<MeResponse>('/me');
  }

  aceitarTermo(payload: AceiteTermoRequest): Observable<AceiteTermoResponse> {
    return this.http.post<AceiteTermoResponse>('/termos/aceite', payload);
  }
}
