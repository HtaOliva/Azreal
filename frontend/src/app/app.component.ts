import { Component, OnInit, signal, inject, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HealthService } from './services/health.service';
import { HelloService } from './services/hello.service';
import { UsuarioService } from './services/usuario.service';
import { HealthStatus } from './models/health-status.model';
import { HelloWorldResponse } from './models/hello-world.model';
import { Usuario } from './models/usuario.model';

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
  private readonly usuarioService = inject(UsuarioService, { optional: true });

  protected readonly title = signal('Fullstack Starter (Spring Boot 3 + Angular)');
  protected readonly healthStatus = signal<HealthStatus | null>(null);
  protected readonly helloMessage = signal<string>('');
  protected readonly loading = signal<boolean>(false);
  protected readonly error = signal<string | null>(null);

  protected readonly usuarios = signal<Usuario[]>([]);
  protected readonly nome = signal<string>('');
  protected readonly email = signal<string>('');
  protected readonly senha = signal<string>('');
  protected readonly saving = signal<boolean>(false);

  ngOnInit(): void {
    this.loadHealth();
    this.loadHello();
    this.loadUsuarios();
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

  private loadUsuarios(): void {
    if (!this.usuarioService) {
      return;
    }
    this.usuarioService.listar().subscribe({
      next: (data: Usuario[]) => {
        this.usuarios.set(data);
      },
      error: () => {
        this.error.set('Nao foi possivel carregar os usuarios.');
      }
    });
  }

  salvarUsuario(): void {
    if (!this.usuarioService) {
      return;
    }
    this.saving.set(true);
    this.usuarioService.criar({
      nome: this.nome(),
      email: this.email(),
      senha: this.senha()
    }).subscribe({
      next: (criado: Usuario) => {
        this.usuarios.update((lista) => [...lista, criado]);
        this.nome.set('');
        this.email.set('');
        this.senha.set('');
        this.saving.set(false);
      },
      error: () => {
        this.error.set('Nao foi possivel salvar o usuario.');
        this.saving.set(false);
      }
    });
  }
}
