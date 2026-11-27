import { ChangeDetectionStrategy, Component, computed, effect, inject, input, untracked } from '@angular/core';
import { Post } from '../../core/models/api.models';
import { ApiService } from '../../core/services/api.service';
import { ListaPaginada } from '../../core/util/lista-paginada';
import { PostGrade } from '../../shared/components/post-grade';

/** Músicas curtidas ou salvas. O tipo vem do route data (ver app.routes.ts). */
@Component({
  selector: 'app-biblioteca-page',
  imports: [PostGrade],
  templateUrl: './biblioteca-page.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class BibliotecaPage {
  private readonly api = inject(ApiService);

  readonly tipo = input<'curtidas' | 'salvos'>('curtidas');

  protected readonly lista = new ListaPaginada<Post>((p) =>
    this.tipo() === 'salvos' ? this.api.salvos(p) : this.api.curtidas(p),
  );
  protected readonly titulo = computed(() => (this.tipo() === 'salvos' ? 'Músicas salvas' : 'Músicas curtidas'));
  protected readonly vazio = computed(() =>
    this.tipo() === 'salvos'
      ? 'Você ainda não salvou nenhuma música. Use o botão Salvar no feed.'
      : 'Você ainda não curtiu nenhuma música. Use o coração no feed.',
  );

  constructor() {
    effect(() => {
      this.tipo();
      untracked(() => void this.lista.recarregar());
    });
  }
}
