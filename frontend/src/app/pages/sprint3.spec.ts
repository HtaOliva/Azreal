import '@angular/compiler';
import { describe, it, expect, beforeEach, vi } from 'vitest';
import { Injector, createEnvironmentInjector, runInInjectionContext } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { ActivatedRoute, Router } from '@angular/router';
import { of } from 'rxjs';
import { AuthService } from '../services/auth.service';
import { DesafioService } from '../services/desafio.service';
import { PropostaPocService } from '../services/proposta-poc.service';
import { ToastService } from '../services/toast.service';
import { DashboardAmbienteComponent } from './dashboard-ambiente.component';
import { PropostaPocComponent } from './proposta-poc.component';

describe('Sprint 3 — Proposta POC & Ambiente Dashboard', () => {
  beforeEach(() => {
    localStorage.clear();
  });

  it('should pre-fill example and submit POC proposal in PropostaPocComponent', () => {
    const auth = new AuthService();
    auth.setEntityContext('s-1', 'TechPulse Soluções');
    const toast = new ToastService();
    const navigateSpy = vi.fn().mockResolvedValue(true);

    const postSpy = vi.fn((_url: string, body: Record<string, unknown>) =>
      of({
        id: 'prop-1',
        startupId: body['startupId'],
        desafioValidacaoId: body['desafioValidacaoId'],
        escopoTeste: body['escopoTeste'],
        duracaoSugeridaDias: body['duracaoSugeridaDias'],
        metricaValidacao: body['metricaValidacao'],
        aceitePrevioTermoSigilo: true,
        status: 'PENDENTE'
      })
    );

    const fakeHttp = {
      get: vi.fn((url: string) => {
        if (url === '/desafios/d-1') {
          return of({
            id: 'd-1',
            titulo: 'Alta taxa de abandono de carrinho',
            descricaoProblema: 'Checkout web com 70% de abandono',
            setor: 'Varejo',
            modalidade: 'REMOTO',
            duracaoMaxima: 30,
            status: 'ABERTO',
            ativo: true
          });
        }
        return of([]);
      }),
      post: postSpy
    } as unknown as HttpClient;

    const injector = createEnvironmentInjector(
      [
        { provide: HttpClient, useValue: fakeHttp },
        { provide: AuthService, useValue: auth },
        { provide: ToastService, useValue: toast },
        { provide: Router, useValue: { navigate: navigateSpy } },
        {
          provide: ActivatedRoute,
          useValue: { snapshot: { paramMap: { get: () => 'd-1' } } }
        },
        DesafioService,
        PropostaPocService
      ],
      Injector.NULL
    );

    const comp = runInInjectionContext(injector, () => new PropostaPocComponent());
    comp.ngOnInit();

    expect(comp['desafio']()?.titulo).toBe('Alta taxa de abandono de carrinho');
    comp.preencherExemplo();
    expect(comp['escopoTeste']()).toContain('widget inteligente');

    comp.enviarProposta();
    expect(postSpy).toHaveBeenCalledWith(
      '/propostas-poc',
      expect.objectContaining({
        startupId: 's-1',
        desafioValidacaoId: 'd-1',
        duracaoSugeridaDias: 30,
        aceitePrevioTermoSigilo: true
      })
    );
    expect(navigateSpy).toHaveBeenCalledWith(['/pocs']);
  });

  it('should publish challenge, accept proposal, and refuse proposal in DashboardAmbienteComponent', () => {
    const auth = new AuthService();
    auth.setEntityContext('amb-1', 'Supermercados Nova Era');
    const toast = new ToastService();

    const fakeHttp = {
      get: vi.fn((url: string) => {
        if (url === '/desafios/ambiente/amb-1') {
          return of([
            {
              id: 'd-1',
              ambienteValidacaoId: 'amb-1',
              titulo: 'Abandono de carrinho',
              descricaoProblema: 'Checkout',
              modalidade: 'REMOTO',
              duracaoMaxima: 30,
              status: 'ABERTO',
              ativo: true
            }
          ]);
        }
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
              status: 'PENDENTE'
            }
          ]);
        }
        return of([]);
      }),
      post: vi.fn((url: string, body: Record<string, unknown>) => {
        if (url === '/desafios') {
          return of({
            id: 'd-2',
            ambienteValidacaoId: 'amb-1',
            titulo: body['titulo'],
            descricaoProblema: body['descricaoProblema'],
            modalidade: body['modalidade'],
            duracaoMaxima: body['duracaoMaxima'],
            status: 'ABERTO',
            ativo: true
          });
        }
        if (url === '/propostas-poc/p-1/aceitar') {
          return of({ id: 'termo-1', propostaPocId: 'p-1', statusProposta: 'EM_TESTE' });
        }
        if (url === '/propostas-poc/p-1/recusar') {
          return of({
            id: 'p-1',
            startupId: 's-1',
            desafioValidacaoId: 'd-1',
            escopoTeste: 'Widget checkout',
            duracaoSugeridaDias: 30,
            metricaValidacao: '+5% conversão',
            aceitePrevioTermoSigilo: true,
            status: 'RECUSADA',
            motivoRecusa: body['motivoRecusa']
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
        DesafioService,
        PropostaPocService
      ],
      Injector.NULL
    );

    const comp = runInInjectionContext(injector, () => new DashboardAmbienteComponent());
    comp.ngOnInit();

    expect(comp['desafios']()).toHaveLength(1);
    expect(comp['fila']()).toHaveLength(1);

    comp.openNovoDesafio();
    comp['novoTitulo'].set('Ruptura de gôndola');
    comp['novaDescricao'].set('2 lojas piloto');
    comp.publicarDesafio();
    expect(comp['desafios']()).toHaveLength(2);

    comp.openAceitarModal(comp['fila']()[0]);
    comp.confirmarAceite();
    expect(comp['fila']()[0].status).toBe('EM_TESTE');

    comp.openRecusarModal(comp['fila']()[0]);
    comp.confirmarRecusa();
    expect(comp['fila']()[0].status).toBe('RECUSADA');
  });
});
