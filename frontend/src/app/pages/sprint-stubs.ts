import { ChangeDetectionStrategy, Component } from '@angular/core';
import { SidebarLayoutComponent } from '../components/sidebar-layout.component';

@Component({
  selector: 'app-login-page',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="auth-wrapper">
      <h1 class="auth-hero-title">AZREAL</h1>
      <p class="auth-hero-sub">Matchmaking estruturado entre Startups early stage e Ambientes de Validação</p>
      <div class="card auth-card">
        <p class="page-subtitle">Tela de Login e Cadastro (Sprint 2).</p>
      </div>
    </div>
  `
})
export class LoginPageComponent {}

@Component({
  selector: 'app-dashboard-startup-page',
  standalone: true,
  imports: [SidebarLayoutComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <app-sidebar-layout>
      <h1 class="page-title">Dashboard da Startup</h1>
      <p class="page-subtitle">Matchmaking de desafios (Sprint 2).</p>
    </app-sidebar-layout>
  `
})
export class DashboardStartupPageComponent {}

@Component({
  selector: 'app-proposta-poc-page',
  standalone: true,
  imports: [SidebarLayoutComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <app-sidebar-layout>
      <h1 class="page-title">Nova Proposta de POC</h1>
      <p class="page-subtitle">Formulário enxuto de POC (Sprint 3).</p>
    </app-sidebar-layout>
  `
})
export class PropostaPocPageComponent {}

@Component({
  selector: 'app-pocs-trajetoria-page',
  standalone: true,
  imports: [SidebarLayoutComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <app-sidebar-layout>
      <h1 class="page-title">Minhas POCs e Trajetória</h1>
      <p class="page-subtitle">Pilotos ativos e registros oficiais (Sprint 4).</p>
    </app-sidebar-layout>
  `
})
export class PocsTrajetoriaPageComponent {}

@Component({
  selector: 'app-dashboard-ambiente-page',
  standalone: true,
  imports: [SidebarLayoutComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <app-sidebar-layout>
      <h1 class="page-title">Dashboard do Ambiente de Validação</h1>
      <p class="page-subtitle">Desafios e fila de propostas (Sprint 3).</p>
    </app-sidebar-layout>
  `
})
export class DashboardAmbientePageComponent {}
