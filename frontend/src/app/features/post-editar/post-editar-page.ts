import { ChangeDetectionStrategy, Component, OnDestroy, computed, effect, inject, input, signal, untracked } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { Genero, Post } from '../../core/models/api.models';
import { ApiService } from '../../core/services/api.service';
import { AuthService } from '../../core/services/auth.service';
import { mensagemDeErro } from '../../core/util/erros';
import { IMAGEM_EXTENSOES, IMAGEM_MAX_BYTES, urlValida, validarArquivo } from '../../core/util/validacoes';
import { SeletorGeneros } from '../../shared/components/seletor-generos';

/** Edição de uma música publicada (só o dono). O arquivo de áudio não muda: para isso, publique outra música. */
@Component({
  selector: 'app-post-editar-page',
  imports: [FormsModule, RouterLink, SeletorGeneros],
  templateUrl: './post-editar-page.html',
  styleUrl: './post-editar-page.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PostEditarPage implements OnDestroy {
  protected readonly api = inject(ApiService);
  protected readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  readonly id = input.required<string>(); // vem da rota /post/:id/editar

  protected readonly post = signal<Post | null>(null);
  protected readonly generos = signal<Genero[]>([]);
  protected readonly titulo = signal('');
  protected readonly descricao = signal('');
  protected readonly linkMusica = signal('');
  protected readonly generoIds = signal<number[]>([]);
  protected readonly novaCapa = signal<File | null>(null);
  protected readonly previaCapa = signal<string | null>(null);
  protected readonly salvando = signal(false);
  protected readonly erroGeral = signal<string | null>(null);
  protected readonly erroCampos = signal<Record<string, string>>({});

  protected readonly permitido = computed(() => {
    const p = this.post();
    return !!p && p.artista.id === this.auth.artista()?.id;
  });

  constructor() {
    effect(() => {
      const id = this.id();
      untracked(() => void this.carregar(id));
    });
  }

  ngOnDestroy(): void {
    this.liberarPrevia();
  }

  private async carregar(id: string): Promise<void> {
    this.post.set(null);
    this.erroGeral.set(null);
    try {
      const [post, generos] = await Promise.all([
        firstValueFrom(this.api.post(id)),
        firstValueFrom(this.api.generos()),
      ]);
      this.generos.set(generos);
      this.titulo.set(post.titulo);
      this.descricao.set(post.descricao ?? '');
      this.linkMusica.set(post.linkMusica);
      this.generoIds.set(post.generos.map((g) => g.id));
      this.post.set(post);
    } catch (e) {
      this.erroGeral.set(mensagemDeErro(e).geral);
    }
  }

  protected escolherCapa(evento: Event): void {
    const arquivo = (evento.target as HTMLInputElement).files?.[0];
    if (!arquivo) return;
    const problema = validarArquivo(arquivo, IMAGEM_EXTENSOES, IMAGEM_MAX_BYTES, 'Capa');
    this.liberarPrevia();
    if (problema) {
      this.novaCapa.set(null);
      this.erroCampos.update((m) => ({ ...m, capa: problema }));
      return;
    }
    this.erroCampos.update((m) => {
      const copia = { ...m };
      delete copia['capa'];
      return copia;
    });
    this.novaCapa.set(arquivo);
    this.previaCapa.set(URL.createObjectURL(arquivo));
  }

  protected async salvar(): Promise<void> {
    const post = this.post();
    if (!post) return;
    this.erroGeral.set(null);

    const invalidos: Record<string, string> = {};
    if (!this.titulo().trim()) invalidos['titulo'] = 'Informe o título';
    if (!urlValida(this.linkMusica())) invalidos['linkMusica'] = 'Use um link que comece com http:// ou https://';
    if (this.generoIds().length === 0) invalidos['generoIds'] = 'Escolha de 1 a 5 gêneros';
    if (this.erroCampos()['capa']) invalidos['capa'] = this.erroCampos()['capa'];
    this.erroCampos.set(invalidos);
    if (Object.keys(invalidos).length > 0) return;

    this.salvando.set(true);
    try {
      await firstValueFrom(
        this.api.atualizarPost(post.id, {
          titulo: this.titulo().trim(),
          descricao: this.descricao(), // "" apaga a descrição
          linkMusica: this.linkMusica().trim(),
          generoIds: this.generoIds(),
        }),
      );
      const capa = this.novaCapa();
      if (capa) await firstValueFrom(this.api.atualizarCapa(post.id, capa));
      await this.router.navigate(['/post', post.id]);
    } catch (e) {
      const erro = mensagemDeErro(e);
      this.erroGeral.set(erro.geral);
      this.erroCampos.set(erro.campos);
    } finally {
      this.salvando.set(false);
    }
  }

  private liberarPrevia(): void {
    const url = this.previaCapa();
    if (url) URL.revokeObjectURL(url);
    this.previaCapa.set(null);
  }
}
