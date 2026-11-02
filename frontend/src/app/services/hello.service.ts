import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { HelloWorldResponse } from '../models/hello-world.model';

@Injectable({ providedIn: 'root' })
export class HelloService {
  private readonly http = inject(HttpClient);

  getGreeting(name?: string): Observable<HelloWorldResponse> {
    const options = name ? { params: { name } } : undefined;
    return this.http.get<HelloWorldResponse>('/api/v1/hello', options);
  }
}
