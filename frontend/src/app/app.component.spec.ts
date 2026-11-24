import '@angular/compiler';
import { describe, it, expect, beforeEach } from 'vitest';
import { Injector, createEnvironmentInjector, runInInjectionContext } from '@angular/core';
import { Router } from '@angular/router';
import { AppComponent } from './app.component';
import { appConfig } from './app.config';
import { routes } from './app.routes';
import { AuthService } from './services/auth.service';
import { ToastService } from './services/toast.service';
import { SidebarLayoutComponent } from './components/sidebar-layout.component';

describe('Azreal App Shell & Core Services', () => {
  beforeEach(() => {
    localStorage.clear();
  });

  it('should instantiate AppComponent and export zoneless appConfig with routes', () => {
    const injector = createEnvironmentInjector([], Injector.NULL);
    const comp = runInInjectionContext(injector, () => new AppComponent());
    expect(comp).toBeDefined();
    expect(appConfig.providers.length).toBeGreaterThan(0);
    expect(routes.some((r) => r.path === 'login')).toBe(true);
    expect(routes.some((r) => r.path === 'startup/dashboard')).toBe(true);
    expect(routes.some((r) => r.path === 'ambiente/dashboard')).toBe(true);
    expect(routes.some((r) => r.path === 'pocs')).toBe(true);
  });

  it('should manage AuthService basic credentials, user profile, and logout', () => {
    const auth = new AuthService();
    expect(auth.isAuthenticated()).toBe(false);
    expect(auth.perfil()).toBe('NONE');

    auth.setBasicCredentials('contato@techpulse.com.br', 'secret');
    auth.setCurrentUser({
      id: 'u-1',
      username: 'contato@techpulse.com.br',
      email: 'contato@techpulse.com.br',
      roles: ['ROLE_STARTUP'],
      perfil: 'STARTUP'
    });
    auth.setEntityContext('s-1', 'TechPulse Soluções');

    expect(auth.isAuthenticated()).toBe(true);
    expect(auth.getBasicCredentials()).toBe(btoa('contato@techpulse.com.br:secret'));
    expect(auth.perfil()).toBe('STARTUP');
    expect(auth.displayName()).toBe('TechPulse Soluções');

    auth.clearToken();
    expect(auth.isAuthenticated()).toBe(false);
    expect(auth.currentUser()).toBeNull();
    expect(auth.entityId()).toBeNull();
  });

  it('should add and dismiss toasts in ToastService', () => {
    const toast = new ToastService();
    toast.show('Teste de notificação', 10000);
    expect(toast.toasts()).toHaveLength(1);
    const id = toast.toasts()[0].id;
    toast.dismiss(id);
    expect(toast.toasts()).toHaveLength(0);
  });

  it('should compute SidebarLayoutComponent routes and logout cleanly', () => {
    const auth = new AuthService();
    auth.setCurrentUser({
      id: 'u-2',
      username: 'ambiente@novaera.com.br',
      email: 'ambiente@novaera.com.br',
      roles: ['ROLE_AMBIENTE_VALIDACAO'],
      perfil: 'AMBIENTE_VALIDACAO'
    });

    const navigateSpy = { navigate: () => Promise.resolve(true) };
    const injector = createEnvironmentInjector(
      [
        { provide: AuthService, useValue: auth },
        { provide: Router, useValue: navigateSpy }
      ],
      Injector.NULL
    );

    const sidebar = runInInjectionContext(injector, () => new SidebarLayoutComponent());
    expect(sidebar['homeRoute']()).toBe('/ambiente/dashboard');
    expect(sidebar['roleLabel']()).toBe('Ambiente de Validação');

    sidebar.logout();
    expect(auth.isAuthenticated()).toBe(false);
  });
});
