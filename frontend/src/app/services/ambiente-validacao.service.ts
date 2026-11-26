import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import {
  AmbienteValidacaoRequest,
  AmbienteValidacaoResponse
} from '../models/azreal.model';

@Injectable({ providedIn: 'root' })
export class AmbienteValidacaoService {
  private readonly http = inject(HttpClient);

  criar(payload: AmbienteValidacaoRequest): Observable<AmbienteValidacaoResponse> {
    return this.http.post<AmbienteValidacaoResponse>('/ambientes-validacao', payload);
  }

  buscar(id: string): Observable<AmbienteValidacaoResponse> {
    return this.http.get<AmbienteValidacaoResponse>(`/ambientes-validacao/${id}`);
  }
}
