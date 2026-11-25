import '@angular/compiler';
import { describe, it, expect, beforeEach, vi } from 'vitest';
import { Injector, createEnvironmentInjector, runInInjectionContext } from '@angular/core';
import { ActivatedRouteSnapshot, Router, RouterStateSnapshot, UrlTree } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { authGuard, roleGuard } from './auth.guard';

describe('Auth & Role Guards', () => {
  const dummyRoute = {} as ActivatedRouteSnapshot;
  const dummyState = {} as RouterStateSnapshot;

  beforeEach(() => {
    localStorage.clear();
  });

  it('authGuard should allow authenticated users and redirect unauthenticated users to /login', () => {
    const auth = new AuthService();
    const loginTree = { path: '/login' } as unknown as UrlTree;
    const createUrlTree = vi.fn().mockReturnValue(loginTree);

    const injector = createEnvironmentInjector(
      [
        { provide: AuthService, useValue: auth },
        { provide: Router, useValue: { createUrlTree } }
      ],
      Injector.NULL
    );

    const denied = runInInjectionContext(injector, () => authGuard(dummyRoute, dummyState));
    expect(denied).toBe(loginTree);
    expect(createUrlTree).toHaveBeenCalledWith(['/login']);

    auth.setToken('tok-1');
    const allowed = runInInjectionContext(injector, () => authGuard(dummyRoute, dummyState));
    expect(allowed).toBe(true);
  });

  it('roleGuard should check authentication, allowed roles, ADMIN override, and /acesso-negado', () => {
    const auth = new AuthService();
    const loginTree = { path: '/login' } as unknown as UrlTree;
    const deniedTree = { path: '/acesso-negado' } as unknown as UrlTree;
    const createUrlTree = vi.fn((commands: string[]) =>
      commands[0] === '/login' ? loginTree : deniedTree
    );

    const injector = createEnvironmentInjector(
      [
        { provide: AuthService, useValue: auth },
        { provide: Router, useValue: { createUrlTree } }
      ],
      Injector.NULL
    );

    const guard = roleGuard(['STARTUP']);

    expect(runInInjectionContext(injector, () => guard(dummyRoute, dummyState))).toBe(loginTree);

    auth.setToken('tok-1');
    auth.setCurrentUser({
      id: 'u-1',
      username: 'amb@test.com',
      email: 'amb@test.com',
      roles: ['ROLE_AMBIENTE_VALIDACAO'],
      perfil: 'AMBIENTE_VALIDACAO'
    });
    expect(runInInjectionContext(injector, () => guard(dummyRoute, dummyState))).toBe(deniedTree);

    auth.setCurrentUser({
      id: 'u-2',
      username: 'startup@test.com',
      email: 'startup@test.com',
      roles: ['ROLE_STARTUP'],
      perfil: 'STARTUP'
    });
    expect(runInInjectionContext(injector, () => guard(dummyRoute, dummyState))).toBe(true);

    auth.setCurrentUser({
      id: 'u-3',
      username: 'admin@test.com',
      email: 'admin@test.com',
      roles: ['ROLE_ADMIN'],
      perfil: 'ADMIN'
    });
    expect(runInInjectionContext(injector, () => guard(dummyRoute, dummyState))).toBe(true);
  });
});
