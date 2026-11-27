import { ChangeDetectionStrategy, Component, computed, inject, input } from '@angular/core';
import { ApiService } from '../../core/services/api.service';

/** Foto de perfil redonda; sem foto, mostra a inicial do nome. */
@Component({
  selector: 'app-avatar',
  changeDetection: ChangeDetectionStrategy.OnPush,
  host: {
    '[style.width.px]': 'tamanho()',
    '[style.height.px]': 'tamanho()',
    '[style.font-size.px]': 'tamanho() * 0.42',
  },
  template: `
    @if (url(); as u) {
      <img [src]="u" alt="" />
    } @else {
      <span>{{ inicial() }}</span>
    }
  `,
  styles: `
    :host {
      display: grid; place-items: center; flex: none; overflow: hidden; border-radius: 50%;
      font-weight: 700; color: #fff; background: linear-gradient(135deg, var(--accent), var(--accent-2));
    }
    img { width: 100%; height: 100%; object-fit: cover; }
  `,
})
export class Avatar {
  private readonly api = inject(ApiService);

  readonly nome = input('');
  readonly foto = input<string | null | undefined>(null);
  readonly tamanho = input(36);

  protected readonly url = computed(() => this.api.midiaUrl(this.foto()));
  protected readonly inicial = computed(() => (this.nome() || '?').trim().charAt(0).toUpperCase());
}
