import '@angular/compiler';
import { describe, it, expect, beforeEach, vi } from 'vitest';
import { Injector, createEnvironmentInjector, runInInjectionContext } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { of } from 'rxjs';
import { AmbienteValidacaoService } from '../services/ambiente-validacao.service';
import { AuthService } from '../services/auth.service';
import { StartupService } from '../services/startup.service';
import { ToastService } from '../services/toast.service';
import { PerfilComponent } from './perfil.component';

describe('PerfilComponent', () => {
  beforeEach(() => {
    localStorage.clear();
  });

  it('should load and update Startup profile', () => {
    const auth = new AuthService();
    auth.setCurrentUser({
      id: 'u-1',
      username: 'contato@techpulse.com.br',
      email: 'contato@techpulse.com.br',
      roles: ['ROLE_STARTUP'],
      perfil: 'STARTUP'
    });
    auth.setEntityContext('s-1', 'TechPulse');
    const toast = new ToastService();

    const putSpy = vi.fn((_url: string, body: Record<string, unknown>) =>
      of({
        id: 's-1',
        nome: body['nome'],
        email: 'contato@techpulse.com.br',
        descricao: body['descricao'],
        modalidadesSuportadas: body['modalidadesSuportadas']
      })
    );

    const fakeHttp = {
      get: vi.fn(() =>
        of({
          id: 's-1',
          nome: 'TechPulse',
          email: 'contato@techpulse.com.br',
          descricao: 'Descrição inicial',
          areaAtuacao: 'Varejo',
          estagioAtual: 'MVP_FUNCIONAL',
          modalidadesSuportadas: ['REMOTO'],
          integrantes: ['Ana', 'João'],
          ligadaIncubadora: true,
          incubadoraLigada: 'Porto Digital'
        })
      ),
      put: putSpy
    } as unknown as HttpClient;

    const injector = createEnvironmentInjector(
      [
        { provide: HttpClient, useValue: fakeHttp },
        { provide: AuthService, useValue: auth },
        { provide: ToastService, useValue: toast },
        StartupService,
        AmbienteValidacaoService
      ],
      Injector.NULL
    );

    const comp = runInInjectionContext(injector, () => new PerfilComponent());
    comp.ngOnInit();

    expect(comp['startupNome']()).toBe('TechPulse');
    expect(comp['startupIntegrantesText']()).toBe('Ana, João');

    comp['startupDescricao'].set('Nova descrição completa');
    comp.toggleModalidade('HIBRIDO');
    comp.salvarStartup();

    expect(putSpy).toHaveBeenCalledWith(
      '/startups/s-1',
      expect.objectContaining({
        descricao: 'Nova descrição completa',
        modalidadesSuportadas: ['REMOTO', 'HIBRIDO'],
        integrantes: ['Ana', 'João']
      })
    );
    expect(toast.toasts().some((t) => t.message.includes('atualizado com sucesso'))).toBe(true);
  });

  it('should load and update Ambiente de Validação profile', () => {
    const auth = new AuthService();
    auth.setCurrentUser({
      id: 'u-2',
      username: 'inovacao@novaera.com.br',
      email: 'inovacao@novaera.com.br',
      roles: ['ROLE_AMBIENTE_VALIDACAO'],
      perfil: 'AMBIENTE_VALIDACAO'
    });
    auth.setEntityContext('amb-1', 'Nova Era');
    const toast = new ToastService();

    const putSpy = vi.fn((_url: string, body: Record<string, unknown>) =>
      of({
        id: 'amb-1',
        razaoSocial: body['razaoSocial'],
        nomeFantasia: body['nomeFantasia'],
        setor: body['setor'],
        porte: body['porte'],
        ufIbge: body['ufIbge'],
        municipioIbge: body['municipioIbge'],
        emailContato: body['emailContato'],
        ativo: true
      })
    );

    const fakeHttp = {
      get: vi.fn(() =>
        of({
          id: 'amb-1',
          razaoSocial: 'Supermercados Nova Era Ltda',
          nomeFantasia: 'Nova Era',
          setor: 'Varejo',
          porte: 'MEDIO',
          ufIbge: 'PE',
          municipioIbge: 'Recife',
          emailContato: 'inovacao@novaera.com.br',
          ativo: true
        })
      ),
      put: putSpy
    } as unknown as HttpClient;

    const injector = createEnvironmentInjector(
      [
        { provide: HttpClient, useValue: fakeHttp },
        { provide: AuthService, useValue: auth },
        { provide: ToastService, useValue: toast },
        StartupService,
        AmbienteValidacaoService
      ],
      Injector.NULL
    );

    const comp = runInInjectionContext(injector, () => new PerfilComponent());
    comp.ngOnInit();

    expect(comp['ambienteNomeFantasia']()).toBe('Nova Era');
    comp['ambienteTelefone'].set('(81) 99999-0000');
    comp['ambienteLinkedin'].set('https://linkedin.com/company/novaera');
    comp.salvarAmbiente();

    expect(putSpy).toHaveBeenCalledWith(
      '/ambientes-validacao/amb-1',
      expect.objectContaining({
        telefoneContato: '(81) 99999-0000',
        linkedinUrl: 'https://linkedin.com/company/novaera'
      })
    );
  });
});
