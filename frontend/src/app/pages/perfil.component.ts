import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { SidebarLayoutComponent } from '../components/sidebar-layout.component';
import {
  Estagio,
  ModalidadeTeste,
  PorteOrganizacao
} from '../models/azreal.model';
import { AmbienteValidacaoService } from '../services/ambiente-validacao.service';
import { AuthService } from '../services/auth.service';
import { StartupService } from '../services/startup.service';
import { ToastService } from '../services/toast.service';

@Component({
  selector: 'app-perfil',
  standalone: true,
  imports: [SidebarLayoutComponent, FormsModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './perfil.component.html'
})
export class PerfilComponent implements OnInit {
  private readonly authService = inject(AuthService);
  private readonly startupService = inject(StartupService);
  private readonly ambienteService = inject(AmbienteValidacaoService);
  private readonly toastService = inject(ToastService);

  protected readonly perfil = this.authService.perfil;
  protected readonly displayName = this.authService.displayName;
  protected readonly loading = signal<boolean>(false);
  protected readonly saving = signal<boolean>(false);
  protected readonly entityId = signal<string | null>(this.authService.entityId());

  // Startup fields
  protected readonly startupNome = signal<string>('');
  protected readonly startupAreaAtuacao = signal<string>('');
  protected readonly startupEstagio = signal<Estagio>('MVP_FUNCIONAL');
  protected readonly startupDescricao = signal<string>('');
  protected readonly startupOQueValidar = signal<string>('');
  protected readonly startupModalidades = signal<ModalidadeTeste[]>(['REMOTO']);
  protected readonly startupUf = signal<string>('');
  protected readonly startupMunicipio = signal<string>('');
  protected readonly startupLocalidadeSede = signal<string>('');
  protected readonly startupLocalidadeAtuacao = signal<string>('');
  protected readonly startupIntegrantesText = signal<string>('');
  protected readonly startupLigadaIncubadora = signal<boolean>(false);
  protected readonly startupIncubadoraNome = signal<string>('');

  // Ambiente de Validação fields
  protected readonly ambienteRazaoSocial = signal<string>('');
  protected readonly ambienteNomeFantasia = signal<string>('');
  protected readonly ambienteSetor = signal<string>('');
  protected readonly ambientePorte = signal<PorteOrganizacao>('MEDIO');
  protected readonly ambienteUf = signal<string>('');
  protected readonly ambienteMunicipio = signal<string>('');
  protected readonly ambienteEmailContato = signal<string>('');
  protected readonly ambienteTelefone = signal<string>('');
  protected readonly ambienteLinkedin = signal<string>('');

  ngOnInit(): void {
    this.carregarPerfil();
  }

  carregarPerfil(): void {
    this.loading.set(true);
    if (this.perfil() === 'AMBIENTE_VALIDACAO') {
      const id = this.authService.entityId();
      const obs$ = id ? this.ambienteService.buscar(id) : this.ambienteService.meuAmbiente();
      obs$.subscribe({
        next: (amb) => {
          this.entityId.set(amb.id);
          this.ambienteRazaoSocial.set(amb.razaoSocial || '');
          this.ambienteNomeFantasia.set(amb.nomeFantasia || '');
          this.ambienteSetor.set(amb.setor || '');
          this.ambientePorte.set(amb.porte || 'MEDIO');
          this.ambienteUf.set(amb.ufIbge || '');
          this.ambienteMunicipio.set(amb.municipioIbge || '');
          this.ambienteEmailContato.set(amb.emailContato || '');
          this.ambienteTelefone.set(amb.telefoneContato || '');
          this.ambienteLinkedin.set(amb.linkedinUrl || '');
          this.authService.setEntityContext(amb.id, amb.nomeFantasia || amb.razaoSocial);
          this.loading.set(false);
        },
        error: () => {
          this.loading.set(false);
          this.toastService.show('Não foi possível carregar o perfil do ambiente.');
        }
      });
      return;
    }

    const id = this.authService.entityId();
    if (id) {
      this.startupService.buscar(id).subscribe({
        next: (s) => this.preencherStartup(s),
        error: () => {
          this.loading.set(false);
          this.toastService.show('Não foi possível carregar o perfil da startup.');
        }
      });
      return;
    }

    const email = this.authService.currentUser()?.email || this.authService.currentUser()?.username;
    this.startupService.listar().subscribe({
      next: (lista) => {
        const encontrada = lista.find(
          (s) => email && s.email?.toLowerCase() === email.toLowerCase()
        );
        if (encontrada) {
          this.preencherStartup(encontrada);
        } else {
          this.loading.set(false);
        }
      },
      error: () => {
        this.loading.set(false);
      }
    });
  }

  private preencherStartup(s: {
    id: string;
    nome: string;
    areaAtuacao?: string;
    estagioAtual?: Estagio;
    descricao?: string;
    oQuePrecisaValidar?: string;
    modalidadesSuportadas?: ModalidadeTeste[];
    ufIbge?: string;
    municipioIbge?: string;
    localidadeSede?: string;
    localidadeAtuacao?: string;
    integrantes?: string[];
    ligadaIncubadora?: boolean;
    incubadoraLigada?: string;
  }): void {
    this.entityId.set(s.id);
    this.startupNome.set(s.nome || '');
    this.startupAreaAtuacao.set(s.areaAtuacao || '');
    this.startupEstagio.set(s.estagioAtual || 'MVP_FUNCIONAL');
    this.startupDescricao.set(s.descricao || '');
    this.startupOQueValidar.set(s.oQuePrecisaValidar || '');
    this.startupModalidades.set(
      s.modalidadesSuportadas && s.modalidadesSuportadas.length > 0
        ? s.modalidadesSuportadas
        : ['REMOTO']
    );
    this.startupUf.set(s.ufIbge || '');
    this.startupMunicipio.set(s.municipioIbge || '');
    this.startupLocalidadeSede.set(s.localidadeSede || '');
    this.startupLocalidadeAtuacao.set(s.localidadeAtuacao || '');
    this.startupIntegrantesText.set((s.integrantes || []).join(', '));
    this.startupLigadaIncubadora.set(Boolean(s.ligadaIncubadora));
    this.startupIncubadoraNome.set(s.incubadoraLigada || '');
    this.authService.setEntityContext(s.id, s.nome);
    this.loading.set(false);
  }

  toggleModalidade(modalidade: ModalidadeTeste): void {
    this.startupModalidades.update((list) =>
      list.includes(modalidade) ? list.filter((m) => m !== modalidade) : [...list, modalidade]
    );
  }

  salvarStartup(): void {
    const id = this.entityId();
    if (!id) {
      this.toastService.show('Startup não identificada na sessão.');
      return;
    }

    const integrantes = this.startupIntegrantesText()
      .split(',')
      .map((item) => item.trim())
      .filter((item) => item.length > 0);

    this.saving.set(true);
    this.startupService
      .atualizar(id, {
        nome: this.startupNome().trim() || undefined,
        areaAtuacao: this.startupAreaAtuacao().trim() || undefined,
        estagioAtual: this.startupEstagio(),
        descricao: this.startupDescricao().trim() || undefined,
        oQuePrecisaValidar: this.startupOQueValidar().trim() || undefined,
        modalidadesSuportadas: this.startupModalidades(),
        ufIbge: this.startupUf().trim() || undefined,
        municipioIbge: this.startupMunicipio().trim() || undefined,
        localidadeSede: this.startupLocalidadeSede().trim() || undefined,
        localidadeAtuacao: this.startupLocalidadeAtuacao().trim() || undefined,
        integrantes,
        ligadaIncubadora: this.startupLigadaIncubadora(),
        incubadoraLigada: this.startupLigadaIncubadora()
          ? this.startupIncubadoraNome().trim() || undefined
          : ''
      })
      .subscribe({
        next: (updated) => {
          this.authService.setEntityContext(updated.id, updated.nome);
          this.saving.set(false);
          this.toastService.show('Perfil da startup atualizado com sucesso!');
        },
        error: (err: Error) => {
          this.saving.set(false);
          this.toastService.show(err.message || 'Não foi possível atualizar o perfil.');
        }
      });
  }

  salvarAmbiente(): void {
    const id = this.entityId();
    if (!id) {
      this.toastService.show('Ambiente de validação não identificado na sessão.');
      return;
    }

    this.saving.set(true);
    this.ambienteService
      .atualizar(id, {
        razaoSocial: this.ambienteRazaoSocial().trim() || undefined,
        nomeFantasia: this.ambienteNomeFantasia().trim() || undefined,
        setor: this.ambienteSetor().trim() || undefined,
        porte: this.ambientePorte(),
        ufIbge: this.ambienteUf().trim() || undefined,
        municipioIbge: this.ambienteMunicipio().trim() || undefined,
        emailContato: this.ambienteEmailContato().trim() || undefined,
        telefoneContato: this.ambienteTelefone().trim() || undefined,
        linkedinUrl: this.ambienteLinkedin().trim() || undefined
      })
      .subscribe({
        next: (updated) => {
          this.authService.setEntityContext(
            updated.id,
            updated.nomeFantasia || updated.razaoSocial
          );
          this.saving.set(false);
          this.toastService.show('Perfil do ambiente atualizado com sucesso!');
        },
        error: (err: Error) => {
          this.saving.set(false);
          this.toastService.show(err.message || 'Não foi possível atualizar o perfil.');
        }
      });
  }
}
