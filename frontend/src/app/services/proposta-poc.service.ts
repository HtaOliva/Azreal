import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { PropostaPocResponse } from '../models/azreal.model';

@Injectable({ providedIn: 'root' })
export class PropostaPocService {
  private readonly http = inject(HttpClient);

  listarMinhas(startupId?: string): Observable<PropostaPocResponse[]> {
    let params = new HttpParams();
    if (startupId) {
      params = params.set('startupId', startupId);
    }
    return this.http.get<PropostaPocResponse[]>('/propostas-poc/minhas', { params });
  }
}
