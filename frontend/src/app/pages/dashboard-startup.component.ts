import { ChangeDetectionStrategy, Component, OnInit, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { catchError, forkJoin, of } from 'rxjs';
import { SidebarLayoutComponent } from '../components/sidebar-layout.component';
import {
  DesafioRecomendado,
  ModalidadeTeste,
  PorteOrganizacao,
  PropostaPocResponse
} from '../models/azreal.model';
import { AuthService } from '../services/auth.service';
import { MatchmakingService } from '../services/matchmaking.service';
import { PropostaPocService } from '../services/proposta-poc.service';
import { ToastService } from '../services/toast.service';

@Component({
  selector: 'app-dashboard-startup',
  standalone: true,
  imports: [SidebarLayoutComponent, RouterLink],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './dashboard-startup.component.html'
})
export class DashboardStartupComponent implements OnInit {
  private readonly authService = inject(AuthService);
  private readonly matchmakingService = inject(MatchmakingService);
  private readonly propostaService = inject(PropostaPocService);
  private readonly toastService = inject(ToastService);

  protected readonly maxSlots = 3;
  protected readonly slotIndices = [0, 1, 2];

  protected readonly displayName = this.authService.displayName;
  protected readonly desafios = signal<DesafioRecomendado[]>([]);
  protected readonly propostas = signal<PropostaPocResponse[]>([]);
  protected readonly loading = signal<boolean>(false);
  protected readonly error = signal<string | null>(null);

  protected readonly usedSlots = computed(
    () =>
      this.propostas().filter(
        (p) => p.status === 'PENDENTE' || p.status === 'EM_TESTE'
      ).length
  );

  protected readonly availableSlots = computed(() =>
    Math.max(0, this.maxSlots - this.usedSlots())
  );

  ngOnInit(): void {
    this.carregarDados();
  }

  carregarDados(): void {
    this.loading.set(true);
    this.error.set(null);
    const startupId = this.authService.entityId() ?? undefined;

    forkJoin({
      desafios: this.matchmakingService.obterDesafiosCompativeis(startupId).pipe(
        catchError((err: Error) => {
          this.error.set(err.message || 'Não foi possível carregar os desafios compatíveis.');
          return of([] as DesafioRecomendado[]);
        })
      ),
      minhas: this.propostaService.listarMinhas(startupId).pipe(
        catchError(() => of([] as PropostaPocResponse[]))
      )
    }).subscribe({
      next: ({ desafios, minhas }) => {
        this.desafios.set(desafios);
        this.propostas.set(minhas);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
      }
    });
  }

  atualizarMatchIa(): void {
    this.loading.set(true);
    this.error.set(null);
    const startupId = this.authService.entityId() ?? undefined;

    this.matchmakingService.obterDesafiosCompativeis(startupId).subscribe({
      next: (desafios) => {
        this.desafios.set(desafios);
        this.loading.set(false);
        this.toastService.show('Matchmaking semântico atualizado via IA.');
      },
      error: (err: Error) => {
        this.loading.set(false);
        this.error.set(err.message || 'Não foi possível atualizar o matchmaking.');
        this.toastService.show('Falha ao atualizar o Matchmaking IA.');
      }
    });
  }

  formatModalidade(modalidade: ModalidadeTeste): string {
    switch (modalidade) {
      case 'REMOTO':
        return 'Remoto';
      case 'HIBRIDO':
        return 'Híbrido';
      case 'PRESENCIAL':
        return 'Presencial';
      default:
        return modalidade;
    }
  }

  formatPorte(porte: PorteOrganizacao): string {
    switch (porte) {
      case 'PEQUENO':
        return 'Pequeno Porte';
      case 'MEDIO':
        return 'Médio Porte';
      case 'GRANDE':
        return 'Grande Porte';
      case 'ORGAO_PUBLICO':
        return 'Órgão Público';
      case 'INSTITUICAO_ENSINO_SAUDE':
        return 'Ensino / Saúde';
      case 'COMUNIDADE':
        return 'Comunidade';
      default:
        return porte;
    }
  }
}
