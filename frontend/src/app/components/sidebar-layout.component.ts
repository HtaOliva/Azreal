import { ChangeDetectionStrategy, Component, computed, inject } from '@angular/core';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../services/auth.service';

@Component({
  selector: 'app-sidebar-layout',
  standalone: true,
  imports: [RouterLink, RouterLinkActive],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="app-shell">
      <aside class="sidebar">
        <div>
          <div class="sidebar-brand">
            <img src="assets/img/logo-azreal.svg" alt="Azreal" width="36" height="36" />
            <div class="brand-text">
              <span class="brand-title">AZREAL</span>
              <span class="brand-sub">POC-Match</span>
            </div>
          </div>

          <nav class="sidebar-nav" aria-label="Menu Principal">
            <a [routerLink]="homeRoute()" routerLinkActive="active" class="nav-item">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true">
                <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z" />
                <polyline points="9 22 9 12 15 12 15 22" />
              </svg>
              <span>INÍCIO</span>
            </a>
            <a routerLink="/pocs" routerLinkActive="active" class="nav-item">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true">
                <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14" />
                <polyline points="22 4 12 14.01 9 11.01" />
              </svg>
              <span>POCs</span>
            </a>
            <a routerLink="/ambiente/dashboard" routerLinkActive="active" class="nav-item">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true">
                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" />
                <circle cx="12" cy="7" r="4" />
              </svg>
              <span>AMBIENTE</span>
            </a>
          </nav>
        </div>

        <div class="sidebar-footer">
          <div class="user-profile-mini">
            <div class="user-meta">
              <div class="user-name">{{ authService.displayName() }}</div>
              <div class="user-role">{{ roleLabel() }}</div>
            </div>
          </div>
          <button type="button" class="btn-logout" (click)="logout()">
            <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true">
              <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4" />
              <polyline points="16 17 21 12 16 7" />
              <line x1="21" y1="12" x2="9" y2="12" />
            </svg>
            <span>Sair</span>
          </button>
        </div>
      </aside>

      <main class="main-content">
        <ng-content />
      </main>
    </div>
  `
})
export class SidebarLayoutComponent {
  protected readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  protected readonly homeRoute = computed(() =>
    this.authService.perfil() === 'AMBIENTE_VALIDACAO'
      ? '/ambiente/dashboard'
      : '/startup/dashboard'
  );

  protected readonly roleLabel = computed(() => {
    const perfil = this.authService.perfil();
    if (perfil === 'AMBIENTE_VALIDACAO') {
      return 'Ambiente de Validação';
    }
    if (perfil === 'ADMIN') {
      return 'Administrador';
    }
    return 'Startup';
  });

  logout(): void {
    this.authService.clearToken();
    void this.router.navigate(['/login']);
  }
}
