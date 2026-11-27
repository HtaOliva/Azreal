import { Routes } from '@angular/router';
import { authGuard, roleGuard } from './guards/auth.guard';
import { AcessoNegadoComponent } from './pages/acesso-negado.component';
import { DashboardAmbienteComponent } from './pages/dashboard-ambiente.component';
import { DashboardStartupComponent } from './pages/dashboard-startup.component';
import { LoginComponent } from './pages/login.component';
import { PerfilComponent } from './pages/perfil.component';
import { PocsTrajetoriaComponent } from './pages/pocs-trajetoria.component';
import { PropostaPocComponent } from './pages/proposta-poc.component';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'login' },
  { path: 'login', component: LoginComponent },
  { path: 'acesso-negado', component: AcessoNegadoComponent },
  {
    path: 'startup/dashboard',
    component: DashboardStartupComponent,
    canActivate: [roleGuard(['STARTUP'])]
  },
  {
    path: 'startup/proposta/:desafioId',
    component: PropostaPocComponent,
    canActivate: [roleGuard(['STARTUP'])]
  },
  {
    path: 'pocs',
    component: PocsTrajetoriaComponent,
    canActivate: [authGuard]
  },
  {
    path: 'perfil',
    component: PerfilComponent,
    canActivate: [authGuard]
  },
  {
    path: 'ambiente/dashboard',
    component: DashboardAmbienteComponent,
    canActivate: [roleGuard(['AMBIENTE_VALIDACAO'])]
  },
  { path: '**', redirectTo: 'login' }
];
