import '@angular/compiler';
import { describe, it, expect } from 'vitest';
import { Injector, createEnvironmentInjector, runInInjectionContext } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { of } from 'rxjs';
import { HealthService } from './services/health.service';
import { HelloService } from './services/hello.service';
import { AppComponent } from './app.component';

describe('AppComponent', () => {
  it('should instantiate component and define default values', () => {
    const injector = createEnvironmentInjector([], Injector.NULL);
    const comp = runInInjectionContext(injector, () => new AppComponent());
    expect(comp).toBeDefined();
    expect(comp.title()).toContain('Fullstack Starter');
    expect(comp.healthStatus()).toBeNull();
    expect(comp.helloMessage()).toBe('');
  });

  it('should load health and greeting from the backend', () => {
    const fakeHttp = {
      get: (url: string) => {
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
      }
    } as unknown as HttpClient;

    const injector = createEnvironmentInjector([
      { provide: HttpClient, useValue: fakeHttp },
      HealthService,
      HelloService
    ], Injector.NULL);

    const comp = runInInjectionContext(injector, () => new AppComponent());
    comp.ngOnInit();

    expect(comp.healthStatus()?.status).toContain('UP');
    expect(comp.helloMessage()).toContain('Ola, Mundo!');
  });
});
