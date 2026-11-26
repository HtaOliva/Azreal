import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { DesafioRecomendado } from '../models/azreal.model';

@Injectable({ providedIn: 'root' })
export class MatchmakingService {
  private readonly http = inject(HttpClient);

  obterDesafiosCompativeis(startupId?: string): Observable<DesafioRecomendado[]> {
    let params = new HttpParams();
    if (startupId) {
      params = params.set('startupId', startupId);
    }
    return this.http.get<DesafioRecomendado[]>('/matchmaking/desafios-compativeis', { params });
  }
}
