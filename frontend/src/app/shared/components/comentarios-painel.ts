import { ChangeDetectionStrategy, Component, computed, effect, inject, input, output, signal, untracked } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { firstValueFrom } from 'rxjs';
import { Comentario } from '../../core/models/api.models';
import { ApiService } from '../../core/services/api.service';
import { AuthService } from '../../core/services/auth.service';
import { mensagemDeErro } from '../../core/util/erros';
import { ListaPaginada } from '../../core/util/lista-paginada';
import { tempoRelativo } from '../../core/util/validacoes';
import { Avatar } from './avatar';
import { Icone } from './icone';

/** Lista e envio de comentários de um post. Serve como gaveta (feed) ou embutido numa página. */
@Component({
  selector: 'app-comentarios-painel',
  imports: [FormsModule, Icone, Avatar],
  templateUrl: './comentarios-painel.html',
  styleUrl: './comentarios-painel.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
  host: { '[class.embutido]': 'embutido()' },
})
export class ComentariosPainel {
  private readonly api = inject(ApiService);
  private readonly auth = inject(AuthService);

  readonly postId = input.required<string>();
  readonly embutido = input(false);
  readonly fechar = output<void>();

  protected readonly texto = signal('');
  protected readonly enviando = signal(false);
  protected readonly erro = signal<string | null>(null);
  protected readonly lista = new ListaPaginada<Comentario>((p) => this.api.comentarios(this.postId(), p));
  protected readonly tempo = tempoRelativo;
  protected readonly meuId = computed(() => this.auth.usuario()?.id);

  constructor() {
    effect(() => {
      this.postId(); // recarrega quando o post muda (ex.: usuário rola para outra música com a gaveta aberta)
      untracked(() => {
        this.erro.set(null);
        this.texto.set('');
        void this.lista.recarregar();
      });
    });
  }

  protected podeRemover(c: Comentario): boolean {
    return c.autor.id === this.meuId() || this.auth.admin();
  }

  protected async enviar(): Promise<void> {
    const conteudo = this.texto().trim();
    if (!conteudo || this.enviando()) return;
    this.enviando.set(true);
    this.erro.set(null);
    try {
      const novo = await firstValueFrom(this.api.comentar(this.postId(), conteudo));
      this.lista.itens.update((l) => [novo, ...l]);
      this.texto.set('');
    } catch (e) {
      this.erro.set(mensagemDeErro(e).geral);
    } finally {
      this.enviando.set(false);
    }
  }

  protected async remover(c: Comentario): Promise<void> {
    if (!confirm('Excluir este comentário?')) return;
    try {
      await firstValueFrom(this.api.removerComentario(c.id));
      this.lista.itens.update((l) => l.filter((x) => x.id !== c.id));
    } catch (e) {
      this.erro.set(mensagemDeErro(e).geral);
    }
  }
}
