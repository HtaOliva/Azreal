import { Component, OnInit, signal, computed, inject, ChangeDetectionStrategy } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { catchError } from 'rxjs/operators';
import { of } from 'rxjs';
import { Audience, BENEFITS, EXAMPLES, STEPS } from './landing.content';

export interface HealthStatus {
  status: string;
  application: string;
  environment: string;
  timestamp: string;
  uptimeMillis: number;
}

export interface HelloWorldResponse {
  message: string;
  application: string;
  environment: string;
  timestamp: string;
}

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit {
  private http = inject(HttpClient, { optional: true });

  title = signal('Azreal');
  healthStatus = signal<HealthStatus | null>(null);
  helloMessage = signal<string>('Carregando saudação do backend...');

  readonly steps = STEPS;
  readonly examples = EXAMPLES;

  audience = signal<Audience>('startup');
  benefits = computed(() => BENEFITS[this.audience()]);
  isOnline = computed(() => this.healthStatus()?.status === 'UP');

  ngOnInit(): void {
    this.checkBackendHealth();
    this.fetchHelloMessage();
  }

  selectAudience(audience: Audience): void {
    this.audience.set(audience);
  }

  checkBackendHealth(): void {
    if (!this.http) {
      this.healthStatus.set({
        status: 'UP (Standalone Mode)',
        application: 'fullstack-starter-api',
        environment: 'local',
        timestamp: new Date().toISOString(),
        uptimeMillis: 1000
      });
      return;
    }

    this.http.get<HealthStatus>('/api/v1/health').pipe(
      catchError(() => of({
        status: 'OFFLINE (Local Dev Mode)',
        application: 'fullstack-api',
        environment: 'mock',
        timestamp: new Date().toISOString(),
        uptimeMillis: 0
      }))
    ).subscribe((data) => {
      this.healthStatus.set(data);
    });
  }

  fetchHelloMessage(): void {
    if (!this.http) {
      this.helloMessage.set('Olá, Mundo! Frontend Angular & Backend Spring Boot 3 prontos para uso.');
      return;
    }

    this.http.get<HelloWorldResponse>('/api/v1/hello').pipe(
      catchError(() => of({
        message: 'Olá, Desenvolvedor Senac! (Modo Desconectado)',
        application: 'fullstack-api',
        environment: 'local',
        timestamp: new Date().toISOString()
      }))
    ).subscribe((data) => {
      this.helloMessage.set(data.message);
    });
  }
}
