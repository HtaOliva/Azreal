import { ChangeDetectionStrategy, Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { catchError, forkJoin, of } from 'rxjs';
import { SidebarLayoutComponent } from '../components/sidebar-layout.component';
import {
  AvaliacaoMetrica,
  ModoPrivacidadePoc,
  PropostaPocResponse,
  RegistroOficialPocResponse,
  TermoPilotoResponse
} from '../models/azreal.model';
import { AuthService } from '../services/auth.service';
import { PropostaPocService } from '../services/proposta-poc.service';
import { RegistroPocService } from '../services/registro-poc.service';
import { ToastService } from '../services/toast.service';

@Component({
  selector: 'app-pocs-trajetoria',
  standalone: true,
  imports: [SidebarLayoutComponent, FormsModule, RouterLink],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './pocs-trajetoria.component.html'
})
export class PocsTrajetoriaComponent implements OnInit {
  private readonly authService = inject(AuthService);
  private readonly propostaService = inject(PropostaPocService);
  private readonly registroService = inject(RegistroPocService);
  private readonly toastService = inject(ToastService);

  protected readonly perfil = this.authService.perfil;
  protected readonly displayName = this.authService.displayName;

  protected readonly propostas = signal<PropostaPocResponse[]>([]);
  protected readonly registros = signal<RegistroOficialPocResponse[]>([]);
  protected readonly termosPorProposta = signal<Record<string, TermoPilotoResponse>>({});
  protected readonly loading = signal<boolean>(false);
  protected readonly submitting = signal<boolean>(false);

  protected readonly activeModal = signal<
    'termo' | 'entrega' | 'confirmacao' | 'evidencia' | null
  >(null);
  protected readonly selectedTermo = signal<TermoPilotoResponse | null>(null);
  protected readonly selectedProposta = signal<PropostaPocResponse | null>(null);
  protected readonly selectedRegistro = signal<RegistroOficialPocResponse | null>(null);

  // Entrega Final form signals (RF18)
  protected readonly resumoEntrega = signal<string>('');
  protected readonly linksEvidencias = signal<string>('');

  // Confirmação Execução form signals (RF19)
  protected readonly pilotoExecutado = signal<boolean>(true);
  protected readonly avaliacaoMetrica = signal<AvaliacaoMetrica>('ATENDEU_TOTALMENTE');
  protected readonly parecer = signal<string>('');
  protected readonly modoPrivacidade = signal<ModoPrivacidadePoc>('EXIBICAO_NOMINAL');

  protected readonly ativasEPendentes = computed(() =>
    this.propostas().filter((p) => p.status === 'EM_TESTE' || p.status === 'PENDENTE')
  );

  ngOnInit(): void {
    this.carregarDados();
  }

  carregarDados(): void {
    this.loading.set(true);
    const entityId = this.authService.entityId() ?? undefined;
    const isAmbiente = this.perfil() === 'AMBIENTE_VALIDACAO';

    const propostas$ = isAmbiente
      ? this.propostaService.listarFila(entityId)
      : this.propostaService.listarMinhas(entityId);

    const registros$ =
      !isAmbiente && entityId
        ? this.registroService.listarPorStartup(entityId)
        : of([] as RegistroOficialPocResponse[]);

    forkJoin({
      propostas: propostas$.pipe(catchError(() => of([] as PropostaPocResponse[]))),
      registros: registros$.pipe(catchError(() => of([] as RegistroOficialPocResponse[])))
    }).subscribe({
      next: ({ propostas, registros }) => {
        this.propostas.set(propostas);
        this.registros.set(registros);
        this.loading.set(false);
        this.carregarTermosAtivos(propostas);
      },
      error: () => {
        this.loading.set(false);
      }
    });
  }

  private carregarTermosAtivos(propostas: PropostaPocResponse[]): void {
    const emTeste = propostas.filter((p) => p.status === 'EM_TESTE');
    for (const proposta of emTeste) {
      this.propostaService
        .obterTermo(proposta.id)
        .pipe(catchError(() => of(null)))
        .subscribe((termo) => {
          if (termo) {
            this.termosPorProposta.update((map) => ({
              ...map,
              [proposta.id]: termo
            }));
          }
        });
    }
  }

  termoDaProposta(propostaId: string): TermoPilotoResponse | undefined {
    return this.termosPorProposta()[propostaId];
  }

  openTermoModal(proposta: PropostaPocResponse): void {
    const cached = this.termoDaProposta(proposta.id);
    if (cached) {
      this.selectedTermo.set(cached);
      this.activeModal.set('termo');
      return;
    }

    this.propostaService
      .obterTermo(proposta.id)
      .pipe(catchError(() => of(null)))
      .subscribe((termo) => {
        if (!termo) {
          this.toastService.show('Termo de Piloto ainda não disponível para esta proposta.');
          return;
        }
        this.termosPorProposta.update((map) => ({ ...map, [proposta.id]: termo }));
        this.selectedTermo.set(termo);
        this.activeModal.set('termo');
      });
  }

  openEntregaModal(proposta: PropostaPocResponse): void {
    this.selectedProposta.set(proposta);
    this.resumoEntrega.set('');
    this.linksEvidencias.set('');
    this.activeModal.set('entrega');
  }

  openConfirmacaoModal(proposta: PropostaPocResponse): void {
    this.selectedProposta.set(proposta);
    this.pilotoExecutado.set(true);
    this.avaliacaoMetrica.set('ATENDEU_TOTALMENTE');
    this.parecer.set('');
    this.modoPrivacidade.set('EXIBICAO_NOMINAL');
    this.activeModal.set('confirmacao');
  }

  openEvidenciaModal(registro: RegistroOficialPocResponse): void {
    this.selectedRegistro.set(registro);
    this.activeModal.set('evidencia');
  }

  closeModal(): void {
    this.activeModal.set(null);
    this.selectedTermo.set(null);
    this.selectedProposta.set(null);
    this.selectedRegistro.set(null);
  }

  registrarEntrega(): void {
    const proposta = this.selectedProposta();
    const resumo = this.resumoEntrega().trim();
    if (!proposta || !resumo) {
      this.toastService.show('Descreva o resumo da entrega final.');
      return;
    }

    this.submitting.set(true);
    this.propostaService
      .registrarEntrega(proposta.id, {
        resumoEntrega: resumo,
        linksEvidencias: this.linksEvidencias().trim() || undefined
      })
      .subscribe({
        next: (termo) => {
          this.termosPorProposta.update((map) => ({ ...map, [proposta.id]: termo }));
          this.submitting.set(false);
          this.closeModal();
          this.toastService.show(
            'Entrega final registrada! O Ambiente de Validação foi notificado para avaliação.'
          );
        },
        error: (err: Error) => {
          this.submitting.set(false);
          this.toastService.show(err.message || 'Não foi possível registrar a entrega.');
        }
      });
  }

  confirmarResultado(): void {
    const proposta = this.selectedProposta();
    if (!proposta) {
      return;
    }

    this.submitting.set(true);
    this.propostaService
      .confirmarResultado(proposta.id, {
        pilotoExecutado: this.pilotoExecutado(),
        avaliacaoMetrica: this.avaliacaoMetrica(),
        parecer: this.parecer().trim() || undefined,
        modoPrivacidade: this.modoPrivacidade()
      })
      .subscribe({
        next: (res) => {
          this.propostas.update((list) =>
            list.map((p) => (p.id === proposta.id ? { ...p, status: res.status } : p))
          );
          this.submitting.set(false);
          this.closeModal();
          this.toastService.show('Execução confirmada e Registro Oficial emitido (RF19/RF20).');
        },
        error: (err: Error) => {
          this.submitting.set(false);
          this.toastService.show(err.message || 'Não foi possível confirmar a execução.');
        }
      });
  }

  baixarComprovante(): void {
    const reg = this.selectedRegistro();
    this.toastService.show(
      `Download do Certificado Oficial ${reg?.codigoVerificador ?? ''} iniciado.`
    );
    this.closeModal();
  }

  formatAvaliacao(avaliacao?: AvaliacaoMetrica): string {
    switch (avaliacao) {
      case 'ATENDEU_TOTALMENTE':
        return 'Atendeu totalmente';
      case 'ATENDEU_PARCIALMENTE':
        return 'Atendeu parcialmente';
      case 'NAO_ATINGIU_METRICA':
        return 'Não atingiu métrica';
      default:
        return '-';
    }
  }
}
