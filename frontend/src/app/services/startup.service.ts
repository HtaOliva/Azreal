import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { StartupRequest, StartupResponse, StartupUpdateRequest } from '../models/azreal.model';

@Injectable({ providedIn: 'root' })
export class StartupService {
  private readonly http = inject(HttpClient);

  listar(): Observable<StartupResponse[]> {
    return this.http.get<StartupResponse[]>('/startups');
  }

  buscar(id: string): Observable<StartupResponse> {
    return this.http.get<StartupResponse>(`/startups/${id}`);
  }

  criar(payload: StartupRequest): Observable<StartupResponse> {
    return this.http.post<StartupResponse>('/startups', payload);
  }

  atualizar(id: string, payload: StartupUpdateRequest): Observable<StartupResponse> {
    return this.http.put<StartupResponse>(`/startups/${id}`, payload);
  }
}
