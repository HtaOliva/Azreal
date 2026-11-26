import '@angular/compiler';
import { describe, it, expect, beforeEach, vi } from 'vitest';
import { Injector, createEnvironmentInjector, runInInjectionContext } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { of, throwError } from 'rxjs';
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

    comp.openTermoModal(comp['ativasEPendentes']()[0]);
    expect(comp['activeModal']()).toBe('termo');
    comp.closeModal();
    expect(comp['activeModal']()).toBeNull();

    expect(comp.formatAvaliacao('ATENDEU_PARCIALMENTE')).toBe('Atendeu parcialmente');
    expect(comp.formatAvaliacao('NAO_ATINGIU_METRICA')).toBe('Não atingiu métrica');
    expect(comp.formatAvaliacao(undefined)).toBe('-');

    const regSvc = injector.get(RegistroPocService);
    regSvc.buscar('reg-1').subscribe();
    regSvc.verificarPorCodigo('AZR-2026-9941-IMP').subscribe();
    expect(fakeHttp.get).toHaveBeenCalledWith('/registros-poc/reg-1');
    expect(fakeHttp.get).toHaveBeenCalledWith('/registros-poc/verificar/AZR-2026-9941-IMP');
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

  it('should handle date formatting, uncached termo modal, validation, and API errors in PocsTrajetoriaComponent', () => {
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

    let termoCallCount = 0;
    const fakeHttp = {
      get: vi.fn((url: string) => {
        if (url === '/propostas-poc/p-2/termo') {
          termoCallCount += 1;
          if (termoCallCount === 1) {
            return throwError(() => new Error('Not found'));
          }
          return of({
            id: 'termo-2',
            propostaPocId: 'p-2',
            startupId: 's-1',
            desafioValidacaoId: 'd-2',
            ambienteValidacaoId: 'amb-2',
            escopoTeste: 'Escopo 2',
            duracaoDias: 15,
            metricaValidacao: 'Métrica 2',
            statusProposta: 'EM_TESTE'
          });
        }
        return throwError(() => new Error('Network error'));
      }),
      post: vi.fn(() => throwError(() => new Error('Server error')))
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
    expect(comp['propostas']()).toEqual([]);
    expect(comp['registros']()).toEqual([]);

    const proposta = {
      id: 'p-2',
      startupId: 's-1',
      desafioValidacaoId: 'd-2',
      escopoTeste: 'Escopo 2',
      duracaoSugeridaDias: 15,
      metricaValidacao: 'Métrica 2',
      aceitePrevioTermoSigilo: true,
      status: 'EM_TESTE' as const
    };

    // Uncached termo error -> toast
    comp.openTermoModal(proposta);
    expect(toast.toasts().some((t) => t.message.includes('ainda não disponível'))).toBe(true);

    // Uncached termo success -> opens modal
    comp.openTermoModal(proposta);
    expect(comp['activeModal']()).toBe('termo');
    expect(comp['selectedTermo']()?.id).toBe('termo-2');
    comp.closeModal();

    // Validation on empty resumoEntrega
    comp.openEntregaModal(proposta);
    comp.registrarEntrega();
    expect(toast.toasts().some((t) => t.message.includes('resumo da entrega'))).toBe(true);

    // Error on registrarEntrega
    comp['resumoEntrega'].set('Entrega com falha');
    comp.registrarEntrega();
    expect(toast.toasts().some((t) => t.message.includes('Server error'))).toBe(true);

    // Early return on confirmarResultado without selected proposta
    comp.closeModal();
    comp.confirmarResultado();

    // Error on confirmarResultado
    comp.openConfirmacaoModal(proposta);
    comp.confirmarResultado();
    expect(toast.toasts().some((t) => t.message.includes('Server error'))).toBe(true);

    // Date formatting helpers
    expect(comp.formatData(undefined)).toBe('-');
    expect(comp.formatData('invalid-date')).toBe('invalid-date');
    expect(comp.formatData('2026-05-10T12:00:00Z')).toMatch(/\d{2}\/\d{2}\/2026/);

    expect(comp.formatDataHora(undefined)).toBe('-');
    expect(comp.formatDataHora('invalid-date')).toBe('invalid-date');
    expect(comp.formatDataHora('2026-05-10T12:30:00Z')).toContain(' às ');

    const baseTermo = {
      id: 't-1',
      propostaPocId: 'p-1',
      startupId: 's-1',
      desafioValidacaoId: 'd-1',
      ambienteValidacaoId: 'amb-1',
      escopoTeste: 'Teste',
      duracaoDias: 15,
      metricaValidacao: 'Meta',
      statusProposta: 'EM_TESTE' as const
    };
    expect(comp.obterDataFim({ ...baseTermo, dataFim: '2026-06-01T12:00:00Z' })).toMatch(
      /\d{2}\/\d{2}\/2026/
    );
    expect(comp.obterDataFim({ ...baseTermo, dataCriacao: '2026-05-01T12:00:00Z' })).toMatch(
      /\d{2}\/\d{2}\/2026/
    );
    expect(comp.obterDataFim({ ...baseTermo, dataCriacao: 'invalid-date' })).toBe('-');
    expect(comp.obterDataFim(baseTermo)).toBe('-');
  });
});
