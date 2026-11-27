import { ChangeDetectionStrategy, Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-acesso-negado',
  standalone: true,
  imports: [RouterLink],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="auth-wrapper">
      <div class="card auth-card" style="text-align: center;">
        <h1 class="page-title" style="margin-bottom: 12px;">Acesso Negado</h1>
        <p class="page-subtitle" style="margin-bottom: 24px;">
          Seu perfil atual não possui permissão para acessar esta área.
        </p>
        <a routerLink="/login" class="btn btn-primary">Voltar para o Login</a>
      </div>
    </div>
  `
})
export class AcessoNegadoComponent {}
