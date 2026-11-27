import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { RegistroOficialPocResponse } from '../models/azreal.model';

@Injectable({ providedIn: 'root' })
export class RegistroPocService {
  private readonly http = inject(HttpClient);

  listarPorStartup(startupId: string): Observable<RegistroOficialPocResponse[]> {
    return this.http.get<RegistroOficialPocResponse[]>(`/startups/${startupId}/registros-poc`);
  }

  buscar(id: string): Observable<RegistroOficialPocResponse> {
    return this.http.get<RegistroOficialPocResponse>(`/registros-poc/${id}`);
  }

  verificarPorCodigo(codigoVerificador: string): Observable<RegistroOficialPocResponse> {
    return this.http.get<RegistroOficialPocResponse>(
      `/registros-poc/verificar/${codigoVerificador}`
    );
  }
}
