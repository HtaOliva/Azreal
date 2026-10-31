import '@angular/compiler';
import { describe, it, expect } from 'vitest';
import { Injector, createEnvironmentInjector, runInInjectionContext } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { of, throwError } from 'rxjs';
import { HealthService } from './services/health.service';
import { HelloService } from './services/hello.service';
import { UsuarioService } from './services/usuario.service';
import { AppComponent } from './app.component';
import { appConfig } from './app.config';
import { STEPS, BENEFITS, EXAMPLES } from './landing.content';

describe('AppComponent', () => {
  it('should instantiate component, define default values, and handle missing optional services', () => {
    const injector = createEnvironmentInjector([], Injector.NULL);
    const comp = runInInjectionContext(injector, () => new AppComponent());
    expect(comp).toBeDefined();
    expect(comp.title()).toContain('Fullstack Starter');
    expect(comp.healthStatus()).toBeNull();
    expect(comp.helloMessage()).toBe('');

    comp.ngOnInit();
    comp.salvarUsuario();

    expect(comp.loading()).toBe(false);
    expect(comp.saving()).toBe(false);
    expect(comp.usuarios()).toEqual([]);
  });

  it('should load health, greeting, and usuarios from the backend and save a new usuario', () => {
    const fakeHttp = {
      get: (url: string) => {
        if (url === '/usuarios') {
          return of([{ id: 1, nome: 'Inicial', email: 'inicial@example.com' }]);
        }
        if (url === '/api/v1/health') {
          return of({
            status: 'UP',
            application: 'fullstack-api',
            environment: 'development',
            timestamp: new Date().toISOString(),
            uptimeMillis: 1000
          });
        }
        if (url === '/api/v1/hello') {
          return of({
            message: 'Ola, Mundo! Frontend Angular & Backend Spring Boot 3 prontos para uso.',
            application: 'fullstack-api',
            environment: 'development',
            timestamp: new Date().toISOString()
          });
        }
        return of({});
      },
      post: (url: string, body: { nome: string; email: string; senha: string }) => {
        if (url === '/usuarios') {
          return of({ id: 2, nome: body.nome, email: body.email });
        }
        return of({});
      }
    } as unknown as HttpClient;

    const injector = createEnvironmentInjector([
      { provide: HttpClient, useValue: fakeHttp },
      HealthService,
      HelloService,
      UsuarioService
    ], Injector.NULL);

    const comp = runInInjectionContext(injector, () => new AppComponent());
    comp.ngOnInit();

    expect(comp.healthStatus()?.status).toContain('UP');
    expect(comp.helloMessage()).toContain('Ola, Mundo!');
    expect(comp.usuarios()).toHaveLength(1);

    comp['nome'].set('Novo Usuario');
    comp['email'].set('novo@example.com');
    comp['senha'].set('123456');
    comp.salvarUsuario();

    expect(comp.usuarios()).toHaveLength(2);
    expect(comp['nome']()).toBe('');
    expect(comp['email']()).toBe('');
    expect(comp['senha']()).toBe('');
    expect(comp['saving']()).toBe(false);
  });

  it('should handle errors when loading health, greeting, usuarios, and saving usuario', () => {
    const errorHttp = {
      get: () => throwError(() => new Error('Network error')),
      post: () => throwError(() => new Error('Save error'))
    } as unknown as HttpClient;

    const injector = createEnvironmentInjector([
      { provide: HttpClient, useValue: errorHttp },
      HealthService,
      HelloService,
      UsuarioService
    ], Injector.NULL);

    const comp = runInInjectionContext(injector, () => new AppComponent());
    comp.ngOnInit();

    expect(comp['loading']()).toBe(false);
    expect(comp['error']()).toBe('Nao foi possivel carregar os usuarios.');

    comp.salvarUsuario();
    expect(comp['saving']()).toBe(false);
    expect(comp['error']()).toBe('Nao foi possivel salvar o usuario.');
  });

  it('should export appConfig and landing content constants', () => {
    expect(appConfig.providers.length).toBeGreaterThan(0);
    expect(STEPS).toHaveLength(4);
    expect(BENEFITS.startup).toHaveLength(3);
    expect(BENEFITS.investidor).toHaveLength(3);
    expect(EXAMPLES).toHaveLength(3);
  });
});

