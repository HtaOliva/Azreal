import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import {
  ConfirmacaoExecucaoRequest,
  ConfirmacaoExecucaoResponse,
  EntregaPilotoRequest,
  PropostaPocRequest,
  PropostaPocResponse,
  RecusaPropostaRequest,
  TermoPilotoResponse
} from '../models/azreal.model';

@Injectable({ providedIn: 'root' })
export class PropostaPocService {
  private readonly http = inject(HttpClient);

  criar(payload: PropostaPocRequest): Observable<PropostaPocResponse> {
    return this.http.post<PropostaPocResponse>('/propostas-poc', payload);
  }

  listarMinhas(startupId?: string): Observable<PropostaPocResponse[]> {
    let params = new HttpParams();
    if (startupId) {
      params = params.set('startupId', startupId);
    }
    return this.http.get<PropostaPocResponse[]>('/propostas-poc/minhas', { params });
  }

  listarFila(ambienteValidacaoId?: string): Observable<PropostaPocResponse[]> {
    let params = new HttpParams();
    if (ambienteValidacaoId) {
      params = params.set('ambienteValidacaoId', ambienteValidacaoId);
    }
    return this.http.get<PropostaPocResponse[]>('/propostas-poc/fila', { params });
  }

  aceitar(propostaId: string): Observable<TermoPilotoResponse> {
    return this.http.post<TermoPilotoResponse>(`/propostas-poc/${propostaId}/aceitar`, {});
  }

  recusar(propostaId: string, payload: RecusaPropostaRequest): Observable<PropostaPocResponse> {
    return this.http.post<PropostaPocResponse>(`/propostas-poc/${propostaId}/recusar`, payload);
  }

  obterTermo(propostaId: string): Observable<TermoPilotoResponse> {
    return this.http.get<TermoPilotoResponse>(`/propostas-poc/${propostaId}/termo`);
  }

  registrarEntrega(
    propostaId: string,
    payload: EntregaPilotoRequest
  ): Observable<TermoPilotoResponse> {
    return this.http.post<TermoPilotoResponse>(`/propostas-poc/${propostaId}/entrega`, payload);
  }

  confirmarResultado(
    propostaId: string,
    payload: ConfirmacaoExecucaoRequest
  ): Observable<ConfirmacaoExecucaoResponse> {
    return this.http.post<ConfirmacaoExecucaoResponse>(
      `/propostas-poc/${propostaId}/confirmacao-resultado`,
      payload
    );
  }
}
