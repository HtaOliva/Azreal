import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Usuario, UsuarioRequest } from '../models/usuario.model';

@Injectable({ providedIn: 'root' })
export class UsuarioService {
  private readonly http = inject(HttpClient);

  listar(): Observable<Usuario[]> {
    return this.http.get<Usuario[]>('/usuarios');
  }

  criar(usuario: UsuarioRequest): Observable<Usuario> {
    return this.http.post<Usuario>('/usuarios', usuario);
  }
}
