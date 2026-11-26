import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { DesafioValidacaoRequest, DesafioValidacaoResponse } from '../models/azreal.model';

@Injectable({ providedIn: 'root' })
export class DesafioService {
  private readonly http = inject(HttpClient);

  listar(): Observable<DesafioValidacaoResponse[]> {
    return this.http.get<DesafioValidacaoResponse[]>('/desafios');
  }

  buscar(id: string): Observable<DesafioValidacaoResponse> {
    return this.http.get<DesafioValidacaoResponse>(`/desafios/${id}`);
  }

  listarPorAmbiente(ambienteId: string): Observable<DesafioValidacaoResponse[]> {
    return this.http.get<DesafioValidacaoResponse[]>(`/desafios/ambiente/${ambienteId}`);
  }

  criar(payload: DesafioValidacaoRequest): Observable<DesafioValidacaoResponse> {
    return this.http.post<DesafioValidacaoResponse>('/desafios', payload);
  }

  inativar(id: string): Observable<DesafioValidacaoResponse> {
    return this.http.patch<DesafioValidacaoResponse>(`/desafios/${id}/inativar`, {});
  }
}
