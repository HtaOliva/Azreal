import '@angular/compiler';
import { describe, it, expect } from 'vitest';
import { Injector, createEnvironmentInjector, runInInjectionContext } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of, throwError } from 'rxjs';
import { AppComponent } from './app.component';
import { BENEFITS, EXAMPLES, STEPS } from './landing.content';

function createComponent(): AppComponent {
  const injector = createEnvironmentInjector([], Injector.NULL);
  return runInInjectionContext(injector, () => new AppComponent());
}

function createComponentWithHttp(get: (url: string) => Observable<unknown>): AppComponent {
  const injector = createEnvironmentInjector([{ provide: HttpClient, useValue: { get } }], Injector.NULL);
  return runInInjectionContext(injector, () => new AppComponent());
}

describe('AppComponent', () => {
  it('should instantiate component and define default values', () => {
    const comp = createComponent();
    expect(comp).toBeDefined();
    expect(comp.title()).toBe('Azreal');
    expect(comp.healthStatus()).toBeNull();
    expect(comp.isOnline()).toBe(false);
  });

  it('should initialize and execute health check and greeting fetch in standalone mode', () => {
    const comp = createComponent();
    comp.ngOnInit();
    expect(comp.helloMessage()).toContain('Olá, Mundo!');
    expect(comp.healthStatus()?.status).toContain('UP');
  });

  it('should use the backend responses when the API is reachable', () => {
    const calls: string[] = [];
    const comp = createComponentWithHttp((url) => {
      calls.push(url);
      return url.endsWith('/health')
        ? of({ status: 'UP', application: 'api', environment: 'prod', timestamp: 'now', uptimeMillis: 42 })
        : of({ message: 'Olá do backend', application: 'api', environment: 'prod', timestamp: 'now' });
    });

    comp.ngOnInit();

    expect(calls).toEqual(['/api/v1/health', '/api/v1/hello']);
    expect(comp.healthStatus()?.environment).toBe('prod');
    expect(comp.isOnline()).toBe(true);
    expect(comp.helloMessage()).toBe('Olá do backend');
  });

  it('should fall back to demo mode when the API is unreachable', () => {
    const comp = createComponentWithHttp(() => throwError(() => new Error('offline')));

    comp.ngOnInit();

    expect(comp.healthStatus()?.status).toContain('OFFLINE');
    expect(comp.healthStatus()?.environment).toBe('mock');
    expect(comp.isOnline()).toBe(false);
    expect(comp.helloMessage()).toContain('Modo Desconectado');
  });

  it('should only report online when the backend status is exactly UP', () => {
    const comp = createComponent();
    const base = {
      application: 'fullstack-api',
      environment: 'local',
      timestamp: new Date().toISOString(),
      uptimeMillis: 10
    };

    comp.healthStatus.set({ ...base, status: 'UP' });
    expect(comp.isOnline()).toBe(true);

    comp.healthStatus.set({ ...base, status: 'OFFLINE (Local Dev Mode)' });
    expect(comp.isOnline()).toBe(false);
  });

  it('should start on the startup audience and switch benefits when the audience changes', () => {
    const comp = createComponent();
    expect(comp.audience()).toBe('startup');
    expect(comp.benefits()).toEqual(BENEFITS.startup);

    comp.selectAudience('investidor');
    expect(comp.audience()).toBe('investidor');
    expect(comp.benefits()).toEqual(BENEFITS.investidor);

    comp.selectAudience('startup');
    expect(comp.benefits()).toEqual(BENEFITS.startup);
  });

  it('should expose the landing page content', () => {
    const comp = createComponent();
    expect(comp.steps).toBe(STEPS);
    expect(comp.examples).toBe(EXAMPLES);
    expect(STEPS).toHaveLength(4);
    expect(BENEFITS.startup.length).toBeGreaterThan(0);
    expect(BENEFITS.investidor.length).toBeGreaterThan(0);
    expect(EXAMPLES.length).toBeGreaterThan(0);
  });
});
