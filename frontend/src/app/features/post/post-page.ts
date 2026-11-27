import { ChangeDetectionStrategy, Component, computed, effect, inject, input, signal, untracked } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { InteracaoRequest, Post } from '../../core/models/api.models';
import { ApiService } from '../../core/services/api.service';
import { AuthService } from '../../core/services/auth.service';
import { mensagemDeErro } from '../../core/util/erros';
import { ComentariosPainel } from '../../shared/components/comentarios-painel';
import { DenunciaDialogo } from '../../shared/components/denuncia-dialogo';
import { Icone } from '../../shared/components/icone';

/** Página de uma música: player, ações, comentários e (para o dono ou admin) editar e excluir. */
@Component({
  selector: 'app-post-page',
  imports: [RouterLink, Icone, ComentariosPainel, DenunciaDialogo],
  templateUrl: './post-page.html',
  styleUrl: './post-page.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PostPage {
  protected readonly api = inject(ApiService);
  protected readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  readonly id = input.required<string>(); // vem da rota /post/:id

  protected readonly post = signal<Post | null>(null);
  protected readonly curtido = signal(false);
  protected readonly salvo = signal(false);
  protected readonly curtidoNaCarga = signal(false);
  protected readonly erro = signal<string | null>(null);
  protected readonly denunciando = signal(false);

  protected readonly ehDono = computed(() => {
    const p = this.post();
    return !!p && p.artista.id === this.auth.artista()?.id;
  });
  protected readonly podeExcluir = computed(() => this.ehDono() || this.auth.admin());
  protected readonly totalCurtidas = computed(() => {
    const p = this.post();
    if (!p) return 0;
    return p.totalCurtidas + (this.curtido() ? 1 : 0) - (this.curtidoNaCarga() ? 1 : 0);
  });

  constructor() {
    effect(() => {
      const id = this.id();
      untracked(() => void this.carregar(id));
    });
  }

  private async carregar(id: string): Promise<void> {
    this.post.set(null);
    this.erro.set(null);
    try {
      const post = await firstValueFrom(this.api.post(id));
      // Um POST /interacoes só com o postId devolve o estado atual (curtido/salvo) e marca a música como vista.
      const estado = await firstValueFrom(this.api.interagir({ postId: id })).catch(() => null);
      this.curtidoNaCarga.set(estado?.curtido ?? false);
      this.curtido.set(estado?.curtido ?? false);
      this.salvo.set(estado?.salvo ?? false);
      this.post.set(post);
    } catch (e) {
      this.erro.set(mensagemDeErro(e).geral);
    }
  }

  protected alternarCurtida(): void {
    this.alternar('curtido');
  }

  protected alternarSalvo(): void {
    this.alternar('salvo');
  }

  /** Atualiza a tela na hora e desfaz se a API recusar. */
  private alternar(campo: 'curtido' | 'salvo'): void {
    const p = this.post();
    if (!p) return;
    const estado = campo === 'curtido' ? this.curtido : this.salvo;
    const novo = !estado();
    estado.set(novo);
    const req: InteracaoRequest = campo === 'curtido' ? { postId: p.id, curtido: novo } : { postId: p.id, salvo: novo };
    this.api.interagir(req).subscribe({ error: () => estado.set(!novo) });
  }

  protected async excluir(): Promise<void> {
    const p = this.post();
    if (!p || !confirm('Excluir esta música? Essa ação não pode ser desfeita.')) return;
    try {
      await firstValueFrom(this.api.removerPost(p.id));
      await this.router.navigateByUrl(this.ehDono() ? '/estudio' : '/');
    } catch (e) {
      this.erro.set(mensagemDeErro(e).geral);
    }
  }
}
