import { ChangeDetectionStrategy, Component, effect, inject, signal, untracked } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { Post } from '../../core/models/api.models';
import { ApiService } from '../../core/services/api.service';
import { AuthService } from '../../core/services/auth.service';
import { mensagemDeErro } from '../../core/util/erros';
import { ListaPaginada } from '../../core/util/lista-paginada';
import { urlValida } from '../../core/util/validacoes';
import { Icone } from '../../shared/components/icone';
import { PostGrade } from '../../shared/components/post-grade';

/** Quem ainda não é artista vê o formulário de ativação; quem já é edita o perfil e vê as próprias músicas. */
@Component({
  selector: 'app-estudio-page',
  imports: [FormsModule, RouterLink, Icone, PostGrade],
  templateUrl: './estudio-page.html',
  styleUrl: './estudio-page.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class EstudioPage {
  private readonly api = inject(ApiService);
  protected readonly auth = inject(AuthService);

  protected readonly nomeArtistico = signal('');
  protected readonly instagram = signal('');
  protected readonly spotify = signal('');
  protected readonly youtube = signal('');
  protected readonly salvando = signal(false);
  protected readonly mensagem = signal<string | null>(null);
  protected readonly erroGeral = signal<string | null>(null);
  protected readonly erroCampos = signal<Record<string, string>>({});
  protected readonly posts = new ListaPaginada<Post>((p) => this.api.meusPosts(p));

  private postsIniciados = false;

  constructor() {
    effect(() => {
      const artista = this.auth.artista();
      untracked(() => {
        if (!artista) return;
        this.nomeArtistico.set(artista.nomeArtistico);
        this.instagram.set(artista.instagramUrl ?? '');
        this.spotify.set(artista.spotifyUrl ?? '');
        this.youtube.set(artista.youtubeUrl ?? '');
        if (!this.postsIniciados) {
          this.postsIniciados = true;
          void this.posts.carregarMais();
        }
      });
    });
  }

  protected async salvar(): Promise<void> {
    this.mensagem.set(null);
    this.erroGeral.set(null);

    const nome = this.nomeArtistico().trim();
    const links = {
      instagramUrl: this.instagram().trim(),
      spotifyUrl: this.spotify().trim(),
      youtubeUrl: this.youtube().trim(),
    };
    const invalidos: Record<string, string> = {};
    if (!nome) invalidos['nomeArtistico'] = 'Informe o nome artístico';
    for (const [campo, valor] of Object.entries(links)) {
      if (valor && !urlValida(valor)) invalidos[campo] = 'Use um link que comece com http:// ou https://';
    }
    this.erroCampos.set(invalidos);
    if (Object.keys(invalidos).length > 0) return;

    this.salvando.set(true);
    try {
      const jaEhArtista = !!this.auth.artista();
      const artista = jaEhArtista
        ? await firstValueFrom(this.api.atualizarArtista({ nomeArtistico: nome, ...links })) // "" apaga o link
        : await firstValueFrom(
            this.api.ativarArtista({
              nomeArtistico: nome,
              instagramUrl: links.instagramUrl || null,
              spotifyUrl: links.spotifyUrl || null,
              youtubeUrl: links.youtubeUrl || null,
            }),
          );
      this.auth.artista.set(artista);
      this.mensagem.set(
        jaEhArtista ? 'Estúdio atualizado.' : 'Perfil de artista ativado! Agora você já pode publicar músicas.',
      );
    } catch (e) {
      const erro = mensagemDeErro(e);
      this.erroGeral.set(erro.geral);
      this.erroCampos.set(erro.campos);
    } finally {
      this.salvando.set(false);
    }
  }
}
