import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { catchError, of, switchMap } from 'rxjs';
import { Estagio, ModalidadeTeste, PorteOrganizacao } from '../models/azreal.model';
import { AmbienteValidacaoService } from '../services/ambiente-validacao.service';
import { AuthApiService } from '../services/auth-api.service';
import { AuthService } from '../services/auth.service';
import { StartupService } from '../services/startup.service';
import { ToastService } from '../services/toast.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './login.component.html'
})
export class LoginComponent {
  private readonly authApi = inject(AuthApiService);
  private readonly authService = inject(AuthService);
  private readonly startupService = inject(StartupService);
  private readonly ambienteService = inject(AmbienteValidacaoService);
  private readonly toastService = inject(ToastService);
  private readonly router = inject(Router);

  protected readonly email = signal('');
  protected readonly password = signal('');
  protected readonly accessRole = signal<'startup' | 'ambiente'>('startup');
  protected readonly lgpdAccepted = signal(true);
  protected readonly submitting = signal(false);

  protected readonly activeModal = signal<'startup' | 'ambiente' | null>(null);
  protected readonly registering = signal(false);

  // Startup registration signals
  protected readonly startupNome = signal('');
  protected readonly startupEmail = signal('');
  protected readonly startupSenha = signal('');
  protected readonly startupEstagio = signal<Estagio>('MVP_FUNCIONAL');
  protected readonly startupValidacao = signal('');
  protected readonly startupModalidades = signal<ModalidadeTeste[]>(['REMOTO']);

  // Ambiente registration signals
  protected readonly ambienteRazaoSocial = signal('');
  protected readonly ambienteNomeFantasia = signal('');
  protected readonly ambienteSetor = signal('');
  protected readonly ambientePorte = signal<PorteOrganizacao>('MEDIO');
  protected readonly ambienteUf = signal('PE');
  protected readonly ambienteMunicipio = signal('Recife');
  protected readonly ambienteEmail = signal('');
  protected readonly ambienteSenha = signal('');

  openModal(modal: 'startup' | 'ambiente'): void {
    this.activeModal.set(modal);
  }

  toggleStartupModalidade(modalidade: ModalidadeTeste): void {
    this.startupModalidades.update((list) =>
      list.includes(modalidade) ? list.filter((m) => m !== modalidade) : [...list, modalidade]
    );
  }

  closeModal(): void {
    this.activeModal.set(null);
  }

  onLoginSubmit(): void {
    if (!this.lgpdAccepted()) {
      this.toastService.show(
        'É obrigatório aceitar os Termos de Uso e Política de Privacidade (LGPD).'
      );
      return;
    }

    const username = this.email().trim();
    const password = this.password();
    if (!username || !password) {
      this.toastService.show('Informe e-mail e senha para entrar.');
      return;
    }

    this.submitting.set(true);
    this.authApi
      .login({ username, password })
      .pipe(
        switchMap((loginRes) => {
          this.authService.setToken(loginRes.token);
          this.authService.setBasicCredentials(username, password);
          return this.authApi.me().pipe(
            catchError(() =>
              of({
                id: '',
                username,
                email: username,
                roles: loginRes.roles,
                perfil:
                  this.accessRole() === 'ambiente' ? ('AMBIENTE_VALIDACAO' as const) : ('STARTUP' as const)
              })
            )
          );
        }),
        switchMap((me) => {
          this.authService.setCurrentUser(me);
          if (this.authService.perfil() === 'STARTUP') {
            return this.startupService.listar().pipe(
              catchError(() => of([])),
              switchMap((startups) => {
                const matched = startups.find(
                  (s) => s.email?.toLowerCase() === username.toLowerCase()
                );
                this.authService.setEntityContext(matched?.id ?? null, matched?.nome ?? username);
                return of(me);
              })
            );
          }
          if (this.authService.perfil() === 'AMBIENTE_VALIDACAO') {
            return this.ambienteService.meuAmbiente().pipe(
              catchError(() => of(null)),
              switchMap((ambiente) => {
                this.authService.setEntityContext(
                  ambiente?.id ?? null,
                  ambiente?.nomeFantasia ?? ambiente?.razaoSocial ?? username
                );
                return of(me);
              })
            );
          }
          this.authService.setEntityContext(null, me.username);
          return of(me);
        })
      )
      .subscribe({
        next: () => {
          this.submitting.set(false);
          this.toastService.show('Autenticação realizada com sucesso! Redirecionando...');
          const target =
            this.authService.perfil() === 'AMBIENTE_VALIDACAO' ||
            (this.authService.perfil() === 'NONE' && this.accessRole() === 'ambiente')
              ? '/ambiente/dashboard'
              : '/startup/dashboard';
          void this.router.navigate([target]);
        },
        error: () => {
          this.submitting.set(false);
          this.authService.clearToken();
          this.toastService.show('Credenciais inválidas ou servidor indisponível.');
        }
      });
  }

  onRegisterStartup(): void {
    const nome = this.startupNome().trim();
    const email = this.startupEmail().trim();
    const senha = this.startupSenha();
    const oQuePrecisaValidar = this.startupValidacao().trim();

    if (!nome || !email || senha.length < 6 || !oQuePrecisaValidar) {
      this.toastService.show('Preencha todos os campos obrigatórios (senha mínima de 6 caracteres).');
      return;
    }

    const modalidades = this.startupModalidades();
    if (modalidades.length === 0) {
      this.toastService.show('Selecione pelo menos uma modalidade suportada.');
      return;
    }

    this.registering.set(true);
    this.authApi
      .aceitarTermo({ versaoTermo: 'LGPD-2026.1', consentimentoIaAnonimizado: true })
      .pipe(
        switchMap((aceite) =>
          this.startupService.criar({
            nome,
            email,
            senha,
            estagioAtual: this.startupEstagio(),
            oQuePrecisaValidar,
            modalidadesSuportadas: modalidades,
            aceiteTermoId: aceite.id
          })
        )
      )
      .subscribe({
        next: (created) => {
          this.registering.set(false);
          this.email.set(created.email);
          this.accessRole.set('startup');
          this.closeModal();
          this.toastService.show(`Perfil de Startup "${created.nome}" criado com sucesso!`);
        },
        error: (err: Error) => {
          this.registering.set(false);
          this.toastService.show(err.message || 'Não foi possível cadastrar a startup.');
        }
      });
  }

  onRegisterAmbiente(): void {
    const razaoSocial = this.ambienteRazaoSocial().trim();
    const nomeFantasia = (this.ambienteNomeFantasia().trim() || razaoSocial);
    const setor = this.ambienteSetor().trim();
    const emailContato = this.ambienteEmail().trim();
    const senha = this.ambienteSenha();

    if (!razaoSocial || !setor || !emailContato || senha.length < 6) {
      this.toastService.show('Preencha todos os campos obrigatórios (senha mínima de 6 caracteres).');
      return;
    }

    this.registering.set(true);
    this.authApi
      .aceitarTermo({ versaoTermo: 'LGPD-2026.1', consentimentoIaAnonimizado: true })
      .pipe(
        switchMap((aceite) =>
          this.ambienteService.criar({
            razaoSocial,
            nomeFantasia,
            setor,
            porte: this.ambientePorte(),
            ufIbge: this.ambienteUf().trim() || 'PE',
            municipioIbge: this.ambienteMunicipio().trim() || 'Recife',
            emailContato,
            senha,
            aceiteTermoId: aceite.id
          })
        )
      )
      .subscribe({
        next: (created) => {
          this.registering.set(false);
          this.email.set(created.emailContato);
          this.accessRole.set('ambiente');
          this.closeModal();
          this.toastService.show(`Ambiente "${created.nomeFantasia}" cadastrado com sucesso!`);
        },
        error: (err: Error) => {
          this.registering.set(false);
          this.toastService.show(err.message || 'Não foi possível cadastrar o ambiente.');
        }
      });
  }
}
