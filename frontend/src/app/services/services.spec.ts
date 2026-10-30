import '@angular/compiler';
import { describe, it, expect, beforeEach, vi } from 'vitest';
import { Injector, createEnvironmentInjector, runInInjectionContext } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { of, firstValueFrom } from 'rxjs';
import { AuthService } from './auth.service';
import { HealthService } from './health.service';
import { HelloService } from './hello.service';
import { UsuarioService } from './usuario.service';

describe('Frontend Services', () => {
  beforeEach(() => {
    localStorage.clear();
    vi.restoreAllMocks();
  });

  describe('AuthService', () => {
    it('should manage token in memory and localStorage', () => {
      const service = new AuthService();
      expect(service.getToken()).toBeNull();
      expect(service.token()).toBeNull();
      expect(service.isAuthenticated()).toBe(false);

      service.setToken('jwt-token-123');
      expect(service.getToken()).toBe('jwt-token-123');
      expect(service.token()).toBe('jwt-token-123');
      expect(service.isAuthenticated()).toBe(true);
      expect(localStorage.getItem('auth_token')).toBe('jwt-token-123');

      const reloadedService = new AuthService();
      expect(reloadedService.getToken()).toBe('jwt-token-123');

      service.clearToken();
      expect(service.getToken()).toBeNull();
      expect(service.isAuthenticated()).toBe(false);
      expect(localStorage.getItem('auth_token')).toBeNull();
    });

    it('should return null when localStorage throws an error', () => {
      vi.spyOn(Storage.prototype, 'getItem').mockImplementation(() => {
        throw new Error('Storage disabled');
      });

      const service = new AuthService();
      expect(service.getToken()).toBeNull();
      expect(service.isAuthenticated()).toBe(false);
    });
  });

  describe('HelloService', () => {
    it('should fetch greeting with and without name parameter', async () => {
      const getSpy = vi.fn().mockReturnValue(
        of({
          message: 'Hello!',
          application: 'api',
          environment: 'test',
          timestamp: '2026-01-01T00:00:00Z'
        })
      );
      const injector = createEnvironmentInjector(
        [{ provide: HttpClient, useValue: { get: getSpy } }, HelloService],
        Injector.NULL
      );
      const service = runInInjectionContext(injector, () => injector.get(HelloService));

      const resDefault = await firstValueFrom(service.getGreeting());
      expect(resDefault.message).toBe('Hello!');
      expect(getSpy).toHaveBeenCalledWith('/api/v1/hello', undefined);

      const resNamed = await firstValueFrom(service.getGreeting('Azreal'));
      expect(resNamed.message).toBe('Hello!');
      expect(getSpy).toHaveBeenCalledWith('/api/v1/hello', { params: { name: 'Azreal' } });
    });
  });

  describe('HealthService & UsuarioService', () => {
    it('should call health and usuario endpoints', async () => {
      const getSpy = vi.fn().mockImplementation((url: string) => {
        if (url === '/api/v1/health') {
          return of({
            status: 'UP',
            application: 'api',
            environment: 'test',
            timestamp: '2026-01-01T00:00:00Z',
            uptimeMillis: 100
          });
        }
        return of([{ id: 1, nome: 'User', email: 'user@test.com' }]);
      });
      const postSpy = vi.fn().mockReturnValue(of({ id: 2, nome: 'New', email: 'new@test.com' }));

      const injector = createEnvironmentInjector(
        [
          { provide: HttpClient, useValue: { get: getSpy, post: postSpy } },
          HealthService,
          UsuarioService
        ],
        Injector.NULL
      );

      const healthService = injector.get(HealthService);
      const usuarioService = injector.get(UsuarioService);

      const health = await firstValueFrom(healthService.getHealth());
      expect(health.status).toBe('UP');

      const lista = await firstValueFrom(usuarioService.listar());
      expect(lista.length).toBe(1);

      const criado = await firstValueFrom(
        usuarioService.criar({ nome: 'New', email: 'new@test.com', senha: '123' })
      );
      expect(criado.id).toBe(2);
    });
  });
});
