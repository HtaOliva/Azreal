import '@angular/compiler';
import { describe, it, expect, beforeEach, vi } from 'vitest';
import { Injector, createEnvironmentInjector, runInInjectionContext } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { of } from 'rxjs';
import { AuthService } from '../services/auth.service';
import { PropostaPocService } from '../services/proposta-poc.service';
import { RegistroPocService } from '../services/registro-poc.service';
import { ToastService } from '../services/toast.service';
import { PocsTrajetoriaComponent } from './pocs-trajetoria.component';

describe('Sprint 4 — POCs Trajectory, Pilot Terms, Delivery & Evidence', () => {
  beforeEach(() => {
    localStorage.clear();
  });

  it('should load active POCs, pilot term, official records, and register final delivery', () => {
    const auth = new AuthService();
    auth.setCurrentUser({
      id: 'u-1',
      username: 'contato@techpulse.com.br',
      email: 'contato@techpulse.com.br',
      roles: ['ROLE_STARTUP'],
      perfil: 'STARTUP'
    });
    auth.setEntityContext('s-1', 'TechPulse Soluções');
    const toast = new ToastService();

    const fakeHttp = {
      get: vi.fn((url: string) => {
        if (url.startsWith('/propostas-poc/minhas')) {
          return of([
            {
              id: 'p-1',
              startupId: 's-1',
              desafioValidacaoId: 'd-1',
              escopoTeste: 'Widget checkout',
              duracaoSugeridaDias: 30,
              metricaValidacao: '+5% conversão',
              aceitePrevioTermoSigilo: true,
              status: 'EM_TESTE'
            }
          ]);
        }
        if (url === '/startups/s-1/registros-poc') {
          return of([
            {
              id: 'reg-1',
              codigoVerificador: 'AZR-2026-9941-IMP',
              termoPilotoId: 'termo-0',
              propostaPocId: 'p-0',
              startupId: 's-1',
              ambienteValidacaoId: 'amb-0',
              desafioValidacaoId: 'd-0',
              escopoTestado: 'Alertas preventivos',
              duracaoDias: 30,
              avaliacaoMetrica: 'ATENDEU_TOTALMENTE',
              parecer: 'Atendeu totalmente.',
              modoPrivacidade: 'EXIBICAO_NOMINAL',
              metricaValidacao: 'Redução de 18% na evasão',
              nomeOrganizacao: 'Colégio Impacto',
              setorOrganizacao: 'Educação',
              anonimo: false
            }
          ]);
        }
        if (url === '/propostas-poc/p-1/termo') {
          return of({
            id: 'termo-1',
            propostaPocId: 'p-1',
            startupId: 's-1',
            desafioValidacaoId: 'd-1',
            ambienteValidacaoId: 'amb-1',
            escopoTeste: 'Widget checkout',
            duracaoDias: 30,
            metricaValidacao: '+5% conversão',
            statusProposta: 'EM_TESTE',
            nomeFantasia: 'Supermercados Nova Era',
            emailContato: 'inovacao@novaera.com.br'
          });
        }
        return of([]);
      }),
      post: vi.fn((url: string, body: Record<string, unknown>) => {
        if (url === '/propostas-poc/p-1/entrega') {
          return of({
            id: 'termo-1',
            propostaPocId: 'p-1',
            startupId: 's-1',
            desafioValidacaoId: 'd-1',
            ambienteValidacaoId: 'amb-1',
            escopoTeste: 'Widget checkout',
            duracaoDias: 30,
            metricaValidacao: '+5% conversão',
            statusProposta: 'EM_TESTE',
            resumoEntrega: body['resumoEntrega']
          });
        }
        return of({});
      })
    } as unknown as HttpClient;

    const injector = createEnvironmentInjector(
      [
        { provide: HttpClient, useValue: fakeHttp },
        { provide: AuthService, useValue: auth },
        { provide: ToastService, useValue: toast },
        PropostaPocService,
        RegistroPocService
      ],
      Injector.NULL
    );

    const comp = runInInjectionContext(injector, () => new PocsTrajetoriaComponent());
    comp.ngOnInit();

    expect(comp['ativasEPendentes']()).toHaveLength(1);
    expect(comp['registros']()).toHaveLength(1);
    expect(comp.termoDaProposta('p-1')?.nomeFantasia).toBe('Supermercados Nova Era');
    expect(comp.formatAvaliacao('ATENDEU_TOTALMENTE')).toBe('Atendeu totalmente');

    comp.openEntregaModal(comp['ativasEPendentes']()[0]);
    comp['resumoEntrega'].set('Widget integrado com +7% de conversão.');
    comp.registrarEntrega();
    expect(comp.termoDaProposta('p-1')?.resumoEntrega).toContain('+7%');

    comp.openEvidenciaModal(comp['registros']()[0]);
    comp.baixarComprovante();
    expect(toast.toasts().some((t) => t.message.includes('AZR-2026-9941-IMP'))).toBe(true);
  });

  it('should confirm execution as Ambiente de Validação in PocsTrajetoriaComponent', () => {
    const auth = new AuthService();
    auth.setCurrentUser({
      id: 'u-2',
      username: 'inovacao@novaera.com.br',
      email: 'inovacao@novaera.com.br',
      roles: ['ROLE_AMBIENTE_VALIDACAO'],
      perfil: 'AMBIENTE_VALIDACAO'
    });
    auth.setEntityContext('amb-1', 'Supermercados Nova Era');
    const toast = new ToastService();

    const fakeHttp = {
      get: vi.fn((url: string) => {
        if (url.startsWith('/propostas-poc/fila')) {
          return of([
            {
              id: 'p-1',
              startupId: 's-1',
              desafioValidacaoId: 'd-1',
              escopoTeste: 'Widget checkout',
              duracaoSugeridaDias: 30,
              metricaValidacao: '+5% conversão',
              aceitePrevioTermoSigilo: true,
              status: 'EM_TESTE'
            }
          ]);
        }
        return of({});
      }),
      post: vi.fn(() =>
        of({
          termoPilotoId: 'termo-1',
          propostaPocId: 'p-1',
          startupId: 's-1',
          ambienteValidacaoId: 'amb-1',
          status: 'POC_CONCLUIDA',
          pilotoExecutado: true,
          certificadoEmitido: true
        })
      )
    } as unknown as HttpClient;

    const injector = createEnvironmentInjector(
      [
        { provide: HttpClient, useValue: fakeHttp },
        { provide: AuthService, useValue: auth },
        { provide: ToastService, useValue: toast },
        PropostaPocService,
        RegistroPocService
      ],
      Injector.NULL
    );

    const comp = runInInjectionContext(injector, () => new PocsTrajetoriaComponent());
    comp.ngOnInit();

    comp.openConfirmacaoModal(comp['ativasEPendentes']()[0]);
    comp['parecer'].set('Excelente resultado.');
    comp.confirmarResultado();

    expect(comp['propostas']()[0].status).toBe('POC_CONCLUIDA');
    expect(comp['ativasEPendentes']()).toHaveLength(0);
  });
});
