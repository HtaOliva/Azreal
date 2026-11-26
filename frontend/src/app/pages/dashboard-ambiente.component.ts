import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { catchError, forkJoin, of } from 'rxjs';
import { SidebarLayoutComponent } from '../components/sidebar-layout.component';
import {
  DesafioValidacaoResponse,
  ModalidadeTeste,
  PropostaPocResponse
} from '../models/azreal.model';
import { AuthService } from '../services/auth.service';
import { DesafioService } from '../services/desafio.service';
import { PropostaPocService } from '../services/proposta-poc.service';
import { ToastService } from '../services/toast.service';

@Component({
  selector: 'app-dashboard-ambiente',
  standalone: true,
  imports: [SidebarLayoutComponent, FormsModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './dashboard-ambiente.component.html'
})
export class DashboardAmbienteComponent implements OnInit {
  private readonly authService = inject(AuthService);
  private readonly desafioService = inject(DesafioService);
  private readonly propostaService = inject(PropostaPocService);
  private readonly toastService = inject(ToastService);

  protected readonly displayName = this.authService.displayName;
  protected readonly desafios = signal<DesafioValidacaoResponse[]>([]);
  protected readonly fila = signal<PropostaPocResponse[]>([]);
  protected readonly loading = signal<boolean>(false);
  protected readonly submitting = signal<boolean>(false);

  protected readonly activeModal = signal<'novoDesafio' | 'aceitar' | 'recusar' | null>(null);
  protected readonly selectedProposta = signal<PropostaPocResponse | null>(null);

  // Novo desafio signals
  protected readonly novoTitulo = signal<string>('');
  protected readonly novaDescricao = signal<string>('');
  protected readonly novoPublicoAlvo = signal<string>('');
  protected readonly novaModalidade = signal<ModalidadeTeste>('REMOTO');
  protected readonly novaDuracao = signal<number>(30);

  // Recusa signals
  protected readonly motivoSelecionado = signal<string>(
    'Fora de prioridade operacional no trimestre'
  );
  protected readonly motivoObservacao = signal<string>('');

  ngOnInit(): void {
    this.carregarDados();
  }

  carregarDados(): void {
    this.loading.set(true);
    const ambienteId = this.authService.entityId() ?? undefined;

    const desafios$ = ambienteId
      ? this.desafioService.listarPorAmbiente(ambienteId)
      : this.desafioService.listar();

    forkJoin({
      desafios: desafios$.pipe(catchError(() => of([] as DesafioValidacaoResponse[]))),
      fila: this.propostaService.listarFila(ambienteId).pipe(
        catchError(() => of([] as PropostaPocResponse[]))
      )
    }).subscribe({
      next: ({ desafios, fila }) => {
        this.desafios.set(desafios);
        this.fila.set(fila);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
      }
    });
  }

  openNovoDesafio(): void {
    this.activeModal.set('novoDesafio');
  }

  openAceitarModal(proposta: PropostaPocResponse): void {
    this.selectedProposta.set(proposta);
    this.activeModal.set('aceitar');
  }

  openRecusarModal(proposta: PropostaPocResponse): void {
    this.selectedProposta.set(proposta);
    this.activeModal.set('recusar');
  }

  closeModal(): void {
    this.activeModal.set(null);
    this.selectedProposta.set(null);
  }

  publicarDesafio(): void {
    const titulo = this.novoTitulo().trim();
    const descricaoProblema = this.novaDescricao().trim();
    const ambienteValidacaoId =
      this.authService.entityId() || this.desafios()[0]?.ambienteValidacaoId;

    if (!titulo || !descricaoProblema) {
      this.toastService.show('Informe o título e a descrição do problema operacional.');
      return;
    }

    if (!ambienteValidacaoId) {
      this.toastService.show('Ambiente de validação não identificado na sessão.');
      return;
    }

    this.submitting.set(true);
    this.desafioService
      .criar({
        ambienteValidacaoId,
        titulo,
        descricaoProblema,
        publicoAlvo: this.novoPublicoAlvo().trim() || undefined,
        modalidade: this.novaModalidade(),
        duracaoMaxima: Number(this.novaDuracao()) || 30
      })
      .subscribe({
        next: (created) => {
          this.desafios.update((list) => [created, ...list]);
          this.novoTitulo.set('');
          this.novaDescricao.set('');
          this.novoPublicoAlvo.set('');
          this.submitting.set(false);
          this.closeModal();
          this.toastService.show(
            'Novo Desafio de Validação publicado de forma anônima (RF05/RF06).'
          );
        },
        error: (err: Error) => {
          this.submitting.set(false);
          this.toastService.show(err.message || 'Não foi possível publicar o desafio.');
        }
      });
  }

  confirmarAceite(): void {
    const proposta = this.selectedProposta();
    if (!proposta) {
      return;
    }

    this.submitting.set(true);
    this.propostaService.aceitar(proposta.id).subscribe({
      next: () => {
        this.fila.update((list) =>
          list.map((p) => (p.id === proposta.id ? { ...p, status: 'EM_TESTE' } : p))
        );
        this.submitting.set(false);
        this.closeModal();
        this.toastService.show('Termo de Piloto gerado! Identidade revelada e contatos liberados.');
      },
      error: (err: Error) => {
        this.submitting.set(false);
        this.toastService.show(err.message || 'Não foi possível aceitar a proposta.');
      }
    });
  }

  confirmarRecusa(): void {
    const proposta = this.selectedProposta();
    if (!proposta) {
      return;
    }

    const obs = this.motivoObservacao().trim();
    const motivoRecusa = obs
      ? `${this.motivoSelecionado()} — ${obs}`
      : this.motivoSelecionado();

    this.submitting.set(true);
    this.propostaService.recusar(proposta.id, { motivoRecusa }).subscribe({
      next: (updated) => {
        this.fila.update((list) =>
          list.map((p) => (p.id === proposta.id ? updated : p))
        );
        this.motivoObservacao.set('');
        this.submitting.set(false);
        this.closeModal();
        this.toastService.show('Proposta recusada com motivo. Vaga liberada automaticamente.');
      },
      error: (err: Error) => {
        this.submitting.set(false);
        this.toastService.show(err.message || 'Não foi possível recusar a proposta.');
      }
    });
  }

  desafioTituloPorId(desafioId: string): string {
    return this.desafios().find((d) => d.id === desafioId)?.titulo || desafioId;
  }
}
