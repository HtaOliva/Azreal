import '@angular/compiler';
import { describe, it, expect, vi } from 'vitest';
import { Injector, createEnvironmentInjector, runInInjectionContext } from '@angular/core';
import { HttpRequest, HttpResponse, HttpErrorResponse } from '@angular/common/http';
import { Router } from '@angular/router';
import { of, throwError, firstValueFrom } from 'rxjs';
import { AuthService } from '../services/auth.service';
import { authInterceptor } from './auth.interceptor';
import { errorInterceptor } from './error.interceptor';

describe('HTTP Interceptors', () => {
  describe('authInterceptor', () => {
    it('should not add Authorization header when token is absent', async () => {
      const authService = { getToken: () => null } as unknown as AuthService;
      const injector = createEnvironmentInjector(
        [{ provide: AuthService, useValue: authService }],
        Injector.NULL
      );

      const req = new HttpRequest('GET', '/api/test');
      let forwardedReq: HttpRequest<unknown> | undefined;

      await firstValueFrom(
        runInInjectionContext(injector, () =>
          authInterceptor(req, (nextReq) => {
            forwardedReq = nextReq;
            return of(new HttpResponse({ status: 200 }));
          })
        )
      );

      expect(forwardedReq?.headers.has('Authorization')).toBe(false);
    });

    it('should add Bearer Authorization header when token is present', async () => {
      const authService = { getToken: () => 'my-jwt-token' } as unknown as AuthService;
      const injector = createEnvironmentInjector(
        [{ provide: AuthService, useValue: authService }],
        Injector.NULL
      );

      const req = new HttpRequest('GET', '/api/test');
      let forwardedReq: HttpRequest<unknown> | undefined;

      await firstValueFrom(
        runInInjectionContext(injector, () =>
          authInterceptor(req, (nextReq) => {
            forwardedReq = nextReq;
            return of(new HttpResponse({ status: 200 }));
          })
        )
      );

      expect(forwardedReq?.headers.get('Authorization')).toBe('Bearer my-jwt-token');
    });
  });

  describe('errorInterceptor', () => {
    it('should clear token and navigate to /login on 401 error with backend message', async () => {
      const clearTokenSpy = vi.fn();
      const navigateSpy = vi.fn().mockResolvedValue(true);

      const injector = createEnvironmentInjector(
        [
          { provide: AuthService, useValue: { clearToken: clearTokenSpy } },
          { provide: Router, useValue: { navigate: navigateSpy } }
        ],
        Injector.NULL
      );

      const req = new HttpRequest('GET', '/api/protected');
      const httpError = new HttpErrorResponse({
        status: 401,
        error: { message: 'Token expirado' }
      });

      await expect(
        firstValueFrom(
          runInInjectionContext(injector, () =>
            errorInterceptor(req, () => throwError(() => httpError))
          )
        )
      ).rejects.toThrow('Token expirado');

      expect(clearTokenSpy).toHaveBeenCalledTimes(1);
      expect(navigateSpy).toHaveBeenCalledWith(['/login']);
    });

    it('should navigate to /acesso-negado on 403 error using error.message', async () => {
      const clearTokenSpy = vi.fn();
      const navigateSpy = vi.fn().mockResolvedValue(true);

      const injector = createEnvironmentInjector(
        [
          { provide: AuthService, useValue: { clearToken: clearTokenSpy } },
          { provide: Router, useValue: { navigate: navigateSpy } }
        ],
        Injector.NULL
      );

      const req = new HttpRequest('GET', '/api/admin');
      const httpError = new HttpErrorResponse({
        status: 403,
        statusText: 'Forbidden'
      });

      await expect(
        firstValueFrom(
          runInInjectionContext(injector, () =>
            errorInterceptor(req, () => throwError(() => httpError))
          )
        )
      ).rejects.toThrow('Http failure response');

      expect(clearTokenSpy).not.toHaveBeenCalled();
      expect(navigateSpy).toHaveBeenCalledWith(['/acesso-negado']);
    });

    it('should use default fallback message when error has no message', async () => {
      const clearTokenSpy = vi.fn();
      const navigateSpy = vi.fn().mockResolvedValue(true);

      const injector = createEnvironmentInjector(
        [
          { provide: AuthService, useValue: { clearToken: clearTokenSpy } },
          { provide: Router, useValue: { navigate: navigateSpy } }
        ],
        Injector.NULL
      );

      const req = new HttpRequest('GET', '/api/other');
      const customError = {
        status: 500,
        error: {},
        message: ''
      } as unknown as HttpErrorResponse;

      await expect(
        firstValueFrom(
          runInInjectionContext(injector, () =>
            errorInterceptor(req, () => throwError(() => customError))
          )
        )
      ).rejects.toThrow('Erro inesperado ao comunicar com o servidor.');

      expect(clearTokenSpy).not.toHaveBeenCalled();
      expect(navigateSpy).not.toHaveBeenCalled();
    });
  });
});
