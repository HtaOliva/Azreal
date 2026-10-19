import { Component, OnInit, signal, inject, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HealthService } from './services/health.service';
import { HelloService } from './services/hello.service';
import { HealthStatus } from './models/health-status.model';
import { HelloWorldResponse } from './models/hello-world.model';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit {
  private readonly healthService = inject(HealthService, { optional: true });
  private readonly helloService = inject(HelloService, { optional: true });

  protected readonly title = signal('Fullstack Starter (Spring Boot 3 + Angular)');
  protected readonly healthStatus = signal<HealthStatus | null>(null);
  protected readonly helloMessage = signal<string>('');
  protected readonly loading = signal<boolean>(false);
  protected readonly error = signal<string | null>(null);

  ngOnInit(): void {
    this.loadHealth();
    this.loadHello();
  }

  private loadHealth(): void {
    if (!this.healthService) {
      return;
    }
    this.loading.set(true);
    this.healthService.getHealth().subscribe({
      next: (data: HealthStatus) => {
        this.healthStatus.set(data);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Nao foi possivel carregar o status do backend.');
        this.loading.set(false);
      }
    });
  }

  private loadHello(): void {
    if (!this.helloService) {
      return;
    }
    this.helloService.getGreeting().subscribe({
      next: (data: HelloWorldResponse) => {
        this.helloMessage.set(data.message);
      },
      error: () => {
        this.error.set('Nao foi possivel carregar a saudacao do backend.');
      }
    });
  }
}
