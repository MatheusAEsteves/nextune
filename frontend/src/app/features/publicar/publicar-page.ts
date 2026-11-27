import { ChangeDetectionStrategy, Component, OnDestroy, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { Genero } from '../../core/models/api.models';
import { ApiService } from '../../core/services/api.service';
import { AuthService } from '../../core/services/auth.service';
import { mensagemDeErro } from '../../core/util/erros';
import {
  AUDIO_EXTENSOES, AUDIO_MAX_BYTES, IMAGEM_EXTENSOES, IMAGEM_MAX_BYTES, urlValida, validarArquivo,
} from '../../core/util/validacoes';
import { Icone } from '../../shared/components/icone';
import { SeletorGeneros } from '../../shared/components/seletor-generos';

@Component({
  selector: 'app-publicar-page',
  imports: [FormsModule, RouterLink, Icone, SeletorGeneros],
  templateUrl: './publicar-page.html',
  styleUrl: './publicar-page.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PublicarPage implements OnDestroy {
  private readonly api = inject(ApiService);
  private readonly router = inject(Router);
  protected readonly auth = inject(AuthService);

  protected readonly generos = signal<Genero[]>([]);
  protected readonly titulo = signal('');
  protected readonly descricao = signal('');
  protected readonly linkMusica = signal('');
  protected readonly generoIds = signal<number[]>([]);
  protected readonly audio = signal<File | null>(null);
  protected readonly capa = signal<File | null>(null);
  protected readonly previaCapa = signal<string | null>(null);
  protected readonly enviando = signal(false);
  protected readonly erroGeral = signal<string | null>(null);
  protected readonly erroCampos = signal<Record<string, string>>({});

  constructor() {
    firstValueFrom(this.api.generos())
      .then((g) => this.generos.set(g))
      .catch((e) => this.erroGeral.set(mensagemDeErro(e).geral));
  }

  ngOnDestroy(): void {
    this.liberarPrevia();
  }

  protected escolherAudio(evento: Event): void {
    const arquivo = this.arquivoDe(evento);
    if (!arquivo) return;
    const problema = validarArquivo(arquivo, AUDIO_EXTENSOES, AUDIO_MAX_BYTES, 'Áudio');
    this.marcarErro('audio', problema);
    this.audio.set(problema ? null : arquivo);
  }

  protected escolherCapa(evento: Event): void {
    const arquivo = this.arquivoDe(evento);
    if (!arquivo) return;
    const problema = validarArquivo(arquivo, IMAGEM_EXTENSOES, IMAGEM_MAX_BYTES, 'Capa');
    this.marcarErro('capa', problema);
    this.liberarPrevia();
    this.capa.set(problema ? null : arquivo);
    if (!problema) this.previaCapa.set(URL.createObjectURL(arquivo));
  }

  protected async publicar(): Promise<void> {
    this.erroGeral.set(null);
    const invalidos: Record<string, string> = {};
    if (!this.titulo().trim()) invalidos['titulo'] = 'Informe o título';
    if (!this.linkMusica().trim()) invalidos['linkMusica'] = 'Informe o link da música completa';
    else if (!urlValida(this.linkMusica())) invalidos['linkMusica'] = 'Use um link que comece com http:// ou https://';
    if (this.generoIds().length === 0) invalidos['generoIds'] = 'Escolha de 1 a 5 gêneros';
    if (!this.audio()) invalidos['audio'] = 'Escolha o arquivo de áudio';
    if (!this.capa()) invalidos['capa'] = 'Escolha a imagem de capa';
    this.erroCampos.set(invalidos);
    if (Object.keys(invalidos).length > 0) return;

    const form = new FormData();
    form.append('titulo', this.titulo().trim());
    if (this.descricao().trim()) form.append('descricao', this.descricao().trim());
    form.append('linkMusica', this.linkMusica().trim());
    this.generoIds().forEach((id) => form.append('generoIds', String(id)));
    form.append('audio', this.audio()!);
    form.append('capa', this.capa()!);

    this.enviando.set(true);
    try {
      const post = await firstValueFrom(this.api.criarPost(form));
      await this.router.navigate(['/post', post.id]);
    } catch (e) {
      const erro = mensagemDeErro(e);
      this.erroGeral.set(erro.geral);
      this.erroCampos.set(erro.campos);
    } finally {
      this.enviando.set(false);
    }
  }

  private arquivoDe(evento: Event): File | null {
    return (evento.target as HTMLInputElement).files?.[0] ?? null;
  }

  private marcarErro(campo: string, mensagem: string | null): void {
    this.erroCampos.update((m) => {
      const copia = { ...m };
      if (mensagem) copia[campo] = mensagem;
      else delete copia[campo];
      return copia;
    });
  }

  private liberarPrevia(): void {
    const url = this.previaCapa();
    if (url) URL.revokeObjectURL(url);
    this.previaCapa.set(null);
  }
}
