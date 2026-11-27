import { ChangeDetectionStrategy, Component, computed, effect, inject, input, signal, untracked } from '@angular/core';
import { RouterLink } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { Artista, Post, Seguidores, UsuarioPublico } from '../../core/models/api.models';
import { ApiService } from '../../core/services/api.service';
import { AuthService } from '../../core/services/auth.service';
import { mensagemDeErro } from '../../core/util/erros';
import { ListaPaginada } from '../../core/util/lista-paginada';
import { Avatar } from '../../shared/components/avatar';
import { DenunciaDialogo } from '../../shared/components/denuncia-dialogo';
import { Icone } from '../../shared/components/icone';
import { PostGrade } from '../../shared/components/post-grade';

@Component({
  selector: 'app-artista-page',
  imports: [RouterLink, Avatar, Icone, PostGrade, DenunciaDialogo],
  templateUrl: './artista-page.html',
  styleUrl: './artista-page.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ArtistaPage {
  private readonly api = inject(ApiService);
  private readonly auth = inject(AuthService);

  readonly id = input.required<string>(); // vem da rota /artista/:id

  protected readonly artista = signal<Artista | null>(null);
  protected readonly dono = signal<UsuarioPublico | null>(null);
  protected readonly seguidores = signal<Seguidores | null>(null);
  protected readonly erro = signal<string | null>(null);
  protected readonly denunciando = signal(false);
  protected readonly posts = new ListaPaginada<Post>((p) => this.api.postsDoArtista(this.id(), p));
  protected readonly ehMeu = computed(() => this.auth.artista()?.id === this.id());

  constructor() {
    effect(() => {
      const id = this.id();
      untracked(() => void this.carregar(id));
    });
  }

  private async carregar(id: string): Promise<void> {
    this.artista.set(null);
    this.dono.set(null);
    this.seguidores.set(null);
    this.erro.set(null);
    try {
      const artista = await firstValueFrom(this.api.artista(id));
      this.artista.set(artista);
      void this.posts.recarregar();
      const [dono, seguidores] = await Promise.all([
        firstValueFrom(this.api.usuarioPublico(artista.usuarioId)).catch(() => null),
        firstValueFrom(this.api.seguidores(id)).catch(() => null),
      ]);
      this.dono.set(dono);
      this.seguidores.set(seguidores);
    } catch (e) {
      this.erro.set(mensagemDeErro(e).geral);
    }
  }

  protected alternarSeguir(): void {
    const atual = this.seguidores();
    if (!atual) return;
    const seguindo = !atual.seguindo;
    this.seguidores.set({ total: atual.total + (seguindo ? 1 : -1), seguindo });
    const chamada = seguindo ? this.api.seguir(this.id()) : this.api.deixarDeSeguir(this.id());
    chamada.subscribe({ error: () => this.seguidores.set(atual) });
  }
}
