import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';

@Component({
  selector: 'app-slots-banner',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="slots-banner">
      <span style="font-size: 13px; color: var(--color-text-muted);">Vagas de Propostas em Aberto:</span>
      <strong style="color: var(--color-secondary);">
        [ {{ used() }} / {{ max() }} ] em uso (Você pode enviar mais {{ available() }})
      </strong>
      <div class="slot-pills" aria-hidden="true">
        @for (idx of slotIndices(); track idx) {
          <span class="slot-pill" [class.used]="idx < used()"></span>
        }
      </div>
    </div>
  `
})
export class SlotsBannerComponent {
  readonly used = input<number>(0);
  readonly max = input<number>(3);

  protected readonly slotIndices = computed(() =>
    Array.from({ length: Math.max(0, this.max()) }, (_, i) => i)
  );

  protected readonly available = computed(() =>
    Math.max(0, this.max() - this.used())
  );
}
