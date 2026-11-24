import { ChangeDetectionStrategy, Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { catchError, forkJoin, of } from 'rxjs';
import { SidebarLayoutComponent } from '../components/sidebar-layout.component';
import { DesafioValidacaoResponse, PropostaPocResponse } from '../models/azreal.model';
import { AuthService } from '../services/auth.service';
import { DesafioService } from '../services/desafio.service';
import { PropostaPocService } from '../services/proposta-poc.service';
import { ToastService } from '../services/toast.service';

@Component({
  selector: 'app-proposta-poc',
  standalone: true,
  imports: [SidebarLayoutComponent, FormsModule, RouterLink],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './proposta-poc.component.html'
})
export class PropostaPocComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly authService = inject(AuthService);
  private readonly desafioService = inject(DesafioService);
  private readonly propostaService = inject(PropostaPocService);
  private readonly toastService = inject(ToastService);

  protected readonly maxSlots = 3;
  protected readonly slotIndices = [0, 1, 2];
  protected readonly displayName = this.authService.displayName;

  protected readonly desafioId = signal<string>('');
  protected readonly desafio = signal<DesafioValidacaoResponse | null>(null);
  protected readonly propostas = signal<PropostaPocResponse[]>([]);
  protected readonly modalOpen = signal<boolean>(true);
  protected readonly submitting = signal<boolean>(false);

  protected readonly escopoTeste = signal<string>('');
  protected readonly responsabilidadesStartup = signal<string>('');
  protected readonly responsabilidadesAmbiente = signal<string>('');
  protected readonly metricaValidacao = signal<string>('');
  protected readonly duracaoOption = signal<'15' | '30' | 'outro'>('30');
  protected readonly duracaoCustom = signal<number>(20);
  protected readonly aceitePrevioTermoSigilo = signal<boolean>(true);

  protected readonly usedSlots = computed(
    () =>
      this.propostas().filter(
        (p) => p.status === 'PENDENTE' || p.status === 'EM_TESTE'
      ).length
  );

  protected readonly availableSlots = computed(() =>
    Math.max(0, this.maxSlots - this.usedSlots())
  );

  protected readonly duracaoSugeridaDias = computed(() => {
    const opt = this.duracaoOption();
    if (opt === '15') {
      return 15;
    }
    if (opt === '30') {
      return 30;
    }
    return Math.max(5, Math.min(30, Number(this.duracaoCustom()) || 15));
  });

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('desafioId') ?? '';
    this.desafioId.set(id);
    this.carregarContexto(id);
  }

  carregarContexto(desafioId: string): void {
    const startupId = this.authService.entityId() ?? undefined;
    forkJoin({
      desafio: desafioId
        ? this.desafioService.buscar(desafioId).pipe(catchError(() => of(null)))
        : of(null),
      minhas: this.propostaService.listarMinhas(startupId).pipe(catchError(() => of([])))
    }).subscribe(({ desafio, minhas }) => {
      this.desafio.set(desafio);
      this.propostas.set(minhas);
    });
  }

  preencherExemplo(): void {
    this.escopoTeste.set(
      'Implantação do widget inteligente de recuperação de carrinho com gatilhos comportamentais em tempo real no checkout web.'
    );
    this.responsabilidadesStartup.set(
      'SDK/API configurado e dashboard de acompanhamento em tempo real.'
    );
    this.responsabilidadesAmbiente.set(
      'Acesso ao ambiente de homologação e amostra de 500 sessões de checkout.'
    );
    this.metricaValidacao.set(
      'Aumentar a taxa de conversão de carrinhos abandonados em pelo menos 5% ao fim de 30 dias.'
    );
    this.toastService.show('Exemplo realista preenchido nos campos da proposta.');
  }

  enviarProposta(): void {
    if (!this.aceitePrevioTermoSigilo()) {
      this.toastService.show('Marque o aceite prévio do Termo de Piloto e Cláusula de Sigilo.');
      return;
    }

    const startupId = this.authService.entityId();
    const desafioValidacaoId = this.desafioId();
    if (!startupId || !desafioValidacaoId) {
      this.toastService.show('Identificação da startup ou desafio ausente.');
      return;
    }

    if (!this.escopoTeste().trim() || !this.metricaValidacao().trim()) {
      this.toastService.show('Preencha o escopo do teste e a métrica de validação.');
      return;
    }

    this.submitting.set(true);
    this.propostaService
      .criar({
        startupId,
        desafioValidacaoId,
        escopoTeste: this.escopoTeste().trim(),
        duracaoSugeridaDias: this.duracaoSugeridaDias(),
        responsabilidadesStartup: this.responsabilidadesStartup().trim() || undefined,
        responsabilidadesAmbiente: this.responsabilidadesAmbiente().trim() || undefined,
        metricaValidacao: this.metricaValidacao().trim(),
        aceitePrevioTermoSigilo: true
      })
      .subscribe({
        next: (created) => {
          this.propostas.update((list) => [created, ...list]);
          this.submitting.set(false);
          this.modalOpen.set(false);
          this.toastService.show(
            'Proposta de POC enviada! O Ambiente de Validação tem até 5 dias úteis para responder.'
          );
          void this.router.navigate(['/pocs']);
        },
        error: (err: Error) => {
          this.submitting.set(false);
          this.toastService.show(err.message || 'Não foi possível enviar a proposta de POC.');
        }
      });
  }
}
