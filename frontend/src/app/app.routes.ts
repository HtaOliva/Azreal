import { Routes } from '@angular/router';
import { authGuard, roleGuard } from './guards/auth.guard';
import { AcessoNegadoComponent } from './pages/acesso-negado.component';
import {
  DashboardAmbientePageComponent,
  DashboardStartupPageComponent,
  LoginPageComponent,
  PocsTrajetoriaPageComponent,
  PropostaPocPageComponent
} from './pages/sprint-stubs';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'login' },
  { path: 'login', component: LoginPageComponent },
  { path: 'acesso-negado', component: AcessoNegadoComponent },
  {
    path: 'startup/dashboard',
    component: DashboardStartupPageComponent,
    canActivate: [roleGuard(['STARTUP'])]
  },
  {
    path: 'startup/proposta/:desafioId',
    component: PropostaPocPageComponent,
    canActivate: [roleGuard(['STARTUP'])]
  },
  {
    path: 'pocs',
    component: PocsTrajetoriaPageComponent,
    canActivate: [authGuard]
  },
  {
    path: 'ambiente/dashboard',
    component: DashboardAmbientePageComponent,
    canActivate: [roleGuard(['AMBIENTE_VALIDACAO'])]
  },
  { path: '**', redirectTo: 'login' }
];
