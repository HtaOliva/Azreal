import '@angular/compiler';
import { describe, it, expect, beforeEach, vi } from 'vitest';
import { Injector, createEnvironmentInjector, runInInjectionContext } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { AmbienteValidacaoService } from '../services/ambiente-validacao.service';
import { AuthApiService } from '../services/auth-api.service';
import { AuthService } from '../services/auth.service';
import { MatchmakingService } from '../services/matchmaking.service';
import { PropostaPocService } from '../services/proposta-poc.service';
import { StartupService } from '../services/startup.service';
import { ToastService } from '../services/toast.service';
import { DashboardStartupComponent } from './dashboard-startup.component';
import { LoginComponent } from './login.component';

describe('Sprint 2 — Login, Registration & Startup Matchmaking', () => {
  beforeEach(() => {
    localStorage.clear();
  });

  it('should block login when LGPD is not accepted and authenticate startup on submit', () => {
    const navigateSpy = vi.fn().mockResolvedValue(true);
    const toast = new ToastService();
    const auth = new AuthService();

    const fakeHttp = {
      post: vi.fn((url: string) => {
        if (url === '/auth/login') {
          return of({ token: 'mock-jwt-token', expiresIn: 7200, roles: ['ROLE_STARTUP'] });
        }
        return of({});
      }),
      get: vi.fn((url: string) => {
        if (url === '/me') {
          return of({
            id: 'u-1',
            username: 'contato@techpulse.com.br',
            email: 'contato@techpulse.com.br',
            roles: ['ROLE_STARTUP'],
            perfil: 'STARTUP'
          });
        }
        if (url === '/startups') {
          return of([{ id: 's-1', nome: 'TechPulse Soluções', email: 'contato@techpulse.com.br' }]);
        }
        return of({});
      })
    } as unknown as HttpClient;

    const injector = createEnvironmentInjector(
      [
        { provide: HttpClient, useValue: fakeHttp },
        { provide: Router, useValue: { navigate: navigateSpy } },
        { provide: AuthService, useValue: auth },
        { provide: ToastService, useValue: toast },
        AuthApiService,
        StartupService,
        AmbienteValidacaoService
      ],
      Injector.NULL
    );

    const comp = runInInjectionContext(injector, () => new LoginComponent());

    comp['lgpdAccepted'].set(false);
    comp.onLoginSubmit();
    expect(toast.toasts().some((t) => t.message.includes('LGPD'))).toBe(true);
    expect(navigateSpy).not.toHaveBeenCalled();

    comp['lgpdAccepted'].set(true);
    comp['email'].set('contato@techpulse.com.br');
    comp['password'].set('123456');
    comp.onLoginSubmit();

    expect(auth.isAuthenticated()).toBe(true);
    expect(auth.entityId()).toBe('s-1');
    expect(auth.displayName()).toBe('TechPulse Soluções');
    expect(navigateSpy).toHaveBeenCalledWith(['/startup/dashboard']);
  });

  it('should register startup via /termos/aceite and /startups', () => {
    const toast = new ToastService();
    const postSpy = vi.fn((url: string) => {
      if (url === '/termos/aceite') {
        return of({ id: 'aceite-1', versaoTermo: 'LGPD-2026.1', dataHora: '', consentimentoIaAnonimizado: true });
      }
      if (url === '/startups') {
        return of({ id: 's-2', nome: 'NovaStart', email: 'nova@start.com' });
      }
      return of({});
    });

    const injector = createEnvironmentInjector(
      [
        { provide: HttpClient, useValue: { post: postSpy } as unknown as HttpClient },
        { provide: Router, useValue: { navigate: vi.fn() } },
        AuthService,
        { provide: ToastService, useValue: toast },
        AuthApiService,
        StartupService,
        AmbienteValidacaoService
      ],
      Injector.NULL
    );

    const comp = runInInjectionContext(injector, () => new LoginComponent());
    comp.openModal('startup');
    comp['startupNome'].set('NovaStart');
    comp['startupEmail'].set('nova@start.com');
    comp['startupSenha'].set('123456');
    comp['startupValidacao'].set('Checkout inteligente');
    comp.onRegisterStartup();

    expect(postSpy).toHaveBeenCalledWith('/termos/aceite', expect.any(Object));
    expect(postSpy).toHaveBeenCalledWith('/startups', expect.objectContaining({ aceiteTermoId: 'aceite-1' }));
    expect(comp['activeModal']()).toBeNull();
    expect(comp['email']()).toBe('nova@start.com');
  });

  it('should load compatible challenges and compute open proposal slots in DashboardStartupComponent', () => {
    const auth = new AuthService();
    auth.setEntityContext('s-1', 'TechPulse Soluções');
    const toast = new ToastService();

    const fakeHttp = {
      get: vi.fn((url: string) => {
        if (url.startsWith('/matchmaking/desafios-compativeis')) {
          return of([
            {
              id: 'd-1',
              titulo: 'Abandono de carrinho',
              descricaoProblema: 'Alta taxa de abandono no checkout',
              setor: 'Varejo',
              porte: 'MEDIO',
              modalidade: 'REMOTO',
              duracaoMaxima: 30,
              status: 'ABERTO',
              motivo: 'Alta sinergia com checkout web',
              origemRecomendacao: 'IA'
            }
          ]);
        }
        if (url.startsWith('/propostas-poc/minhas')) {
          return of([
            {
              id: 'p-1',
              startupId: 's-1',
              desafioValidacaoId: 'd-2',
              escopoTeste: 'Piloto',
              duracaoSugeridaDias: 30,
              metricaValidacao: '5%',
              aceitePrevioTermoSigilo: true,
              status: 'PENDENTE'
            },
            {
              id: 'p-2',
              startupId: 's-1',
              desafioValidacaoId: 'd-3',
              escopoTeste: 'Piloto 2',
              duracaoSugeridaDias: 15,
              metricaValidacao: '10%',
              aceitePrevioTermoSigilo: true,
              status: 'RECUSADA'
            }
          ]);
        }
        return of([]);
      })
    } as unknown as HttpClient;

    const injector = createEnvironmentInjector(
      [
        { provide: HttpClient, useValue: fakeHttp },
        { provide: AuthService, useValue: auth },
        { provide: ToastService, useValue: toast },
        MatchmakingService,
        PropostaPocService
      ],
      Injector.NULL
    );

    const comp = runInInjectionContext(injector, () => new DashboardStartupComponent());
    comp.ngOnInit();

    expect(comp['desafios']()).toHaveLength(1);
    expect(comp['usedSlots']()).toBe(1);
    expect(comp['availableSlots']()).toBe(2);
    expect(comp.formatModalidade('REMOTO')).toBe('Remoto');
    expect(comp.formatPorte('MEDIO')).toBe('Médio Porte');

    comp.atualizarMatchIa();
    expect(toast.toasts().some((t) => t.message.includes('Matchmaking semântico atualizado'))).toBe(true);
  });

  it('should handle matchmaking error gracefully in DashboardStartupComponent', () => {
    const auth = new AuthService();
    const toast = new ToastService();
    const errorHttp = {
      get: vi.fn(() => throwError(() => new Error('Falha de rede')))
    } as unknown as HttpClient;

    const injector = createEnvironmentInjector(
      [
        { provide: HttpClient, useValue: errorHttp },
        { provide: AuthService, useValue: auth },
        { provide: ToastService, useValue: toast },
        MatchmakingService,
        PropostaPocService
      ],
      Injector.NULL
    );

    const comp = runInInjectionContext(injector, () => new DashboardStartupComponent());
    comp.ngOnInit();

    expect(comp['error']()).toBe('Falha de rede');
    expect(comp['loading']()).toBe(false);

    comp.atualizarMatchIa();
    expect(toast.toasts().some((t) => t.message.includes('Falha ao atualizar'))).toBe(true);

    expect(comp.formatModalidade('HIBRIDO')).toBe('Híbrido');
    expect(comp.formatModalidade('PRESENCIAL')).toBe('Presencial');
    expect(comp.formatPorte('PEQUENO')).toBe('Pequeno Porte');
    expect(comp.formatPorte('GRANDE')).toBe('Grande Porte');
    expect(comp.formatPorte('ORGAO_PUBLICO')).toBe('Órgão Público');
    expect(comp.formatPorte('INSTITUICAO_ENSINO_SAUDE')).toBe('Ensino / Saúde');
    expect(comp.formatPorte('COMUNIDADE')).toBe('Comunidade');
  });

  it('should register ambiente, toggle modalidades, and handle login errors in LoginComponent', () => {
    const navigateSpy = vi.fn().mockResolvedValue(true);
    const toast = new ToastService();
    const auth = new AuthService();

    const fakeHttp = {
      post: vi.fn((url: string) => {
        if (url === '/termos/aceite') {
          return of({ id: 'aceite-2', versaoTermo: 'LGPD-2026.1', dataHora: '', consentimentoIaAnonimizado: true });
        }
        if (url === '/ambientes-validacao') {
          return of({
            id: 'amb-1',
            razaoSocial: 'Nova Era Ltda',
            nomeFantasia: 'Nova Era',
            setor: 'Varejo',
            porte: 'MEDIO',
            ufIbge: 'PE',
            municipioIbge: 'Recife',
            emailContato: 'amb@novaera.com',
            ativo: true
          });
        }
        if (url === '/auth/login') {
          return throwError(() => new Error('Unauthorized'));
        }
        return of({});
      }),
      get: vi.fn(() => of({}))
    } as unknown as HttpClient;

    const injector = createEnvironmentInjector(
      [
        { provide: HttpClient, useValue: fakeHttp },
        { provide: Router, useValue: { navigate: navigateSpy } },
        { provide: AuthService, useValue: auth },
        { provide: ToastService, useValue: toast },
        AuthApiService,
        StartupService,
        AmbienteValidacaoService
      ],
      Injector.NULL
    );

    const comp = runInInjectionContext(injector, () => new LoginComponent());

    comp.toggleStartupModalidade('HIBRIDO');
    expect(comp['startupModalidades']()).toEqual(['REMOTO', 'HIBRIDO']);
    comp.toggleStartupModalidade('REMOTO');
    expect(comp['startupModalidades']()).toEqual(['HIBRIDO']);

    comp.openModal('ambiente');
    comp['ambienteRazaoSocial'].set('Nova Era Ltda');
    comp['ambienteSetor'].set('Varejo');
    comp['ambienteEmail'].set('amb@novaera.com');
    comp['ambienteSenha'].set('123456');
    comp.onRegisterAmbiente();

    expect(comp['activeModal']()).toBeNull();
    expect(comp['email']()).toBe('amb@novaera.com');
    expect(comp['accessRole']()).toBe('ambiente');

    comp['password'].set('wrongpass');
    comp.onLoginSubmit();
    expect(auth.isAuthenticated()).toBe(false);
    expect(toast.toasts().some((t) => t.message.includes('Credenciais inválidas'))).toBe(true);
  });
});
