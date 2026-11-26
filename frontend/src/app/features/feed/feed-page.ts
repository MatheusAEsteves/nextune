import {
  ChangeDetectionStrategy, Component, ElementRef, OnDestroy, OnInit,
  effect, inject, signal, viewChild, viewChildren,
} from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { InteracaoRequest, Post, Seguidores } from '../../core/models/api.models';
import { ApiService } from '../../core/services/api.service';
import { mensagemDeErro } from '../../core/util/erros';
import { Icone } from '../../shared/components/icone';

const POR_LOTE = 10;
const CARREGAR_QUANDO_FALTAREM = 3;
const FRACAO_CONSIDERADA_OUVIDA = 0.8; // ouviu 80% e saiu = não conta como "pulou"
const JANELA_EXCLUIR = 100; // ids da fila enviados em ?excluir= (a API aceita até 200)
const FRACAO_VISIVEL = 0.6; // o card vira o "ativo" quando 60% dele aparece

/**
 * Feed vertical estilo TikTok. A rolagem usa scroll-snap (um card por vez); um IntersectionObserver
 * descobre qual card está na tela, toca o áudio dele e registra a interação do card que saiu.
 */
@Component({
  selector: 'app-feed-page',
  imports: [Icone],
  templateUrl: './feed-page.html',
  styleUrl: './feed-page.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
  host: { '(document:keydown)': 'aoTeclar($event)' },
})
export class FeedPage implements OnInit, OnDestroy {
  protected readonly api = inject(ApiService);

  private readonly audioRef = viewChild.required<ElementRef<HTMLAudioElement>>('audio');
  private readonly cards = viewChildren<ElementRef<HTMLElement>>('card');

  protected readonly posts = signal<Post[]>([]);
  protected readonly indiceAtivo = signal(0);
  protected readonly carregando = signal(false);
  protected readonly fim = signal(false);
  protected readonly erro = signal<string | null>(null);
  protected readonly tocando = signal(false);
  protected readonly progresso = signal(0);
  protected readonly mudo = signal(false);
  protected readonly curtidos = signal<Record<string, boolean>>({});
  protected readonly salvos = signal<Record<string, boolean>>({});
  protected readonly seguidores = signal<Record<string, Seguidores>>({});

  private readonly observador: IntersectionObserver;
  private idAtivo: string | null = null;
  private maxOuvido = 0; // maior posição (s) alcançada no card atual
  private proximo: HTMLAudioElement | null = null; // só para pré-carregar a próxima faixa

  constructor() {
    this.observador = new IntersectionObserver(
      (entradas) => {
        for (const e of entradas) {
          if (e.isIntersecting && e.intersectionRatio >= FRACAO_VISIVEL) {
            this.ativar(Number((e.target as HTMLElement).dataset['indice']));
          }
        }
      },
      { threshold: [FRACAO_VISIVEL] },
    );

    // Cards novos (lotes seguintes) passam a ser observados; observar de novo o mesmo elemento é inofensivo.
    effect(() => {
      for (const card of this.cards()) this.observador.observe(card.nativeElement);
    });
  }

  ngOnInit(): void {
    void this.carregarMais();
  }

  ngOnDestroy(): void {
    this.observador.disconnect();
    this.encerrarAtual(); // registra o card que estava na tela ao sair do feed
    this.audioRef().nativeElement.pause();
    this.proximo = null;
  }

  // ---------- carregamento ----------

  protected async carregarMais(): Promise<void> {
    if (this.carregando() || this.fim()) return;
    this.carregando.set(true);
    this.erro.set(null);
    try {
      const excluir = this.posts().slice(-JANELA_EXCLUIR).map((p) => p.id);
      const novos = await firstValueFrom(this.api.feed(POR_LOTE, excluir));
      const existentes = new Set(this.posts().map((p) => p.id));
      const unicos = novos.filter((p) => !existentes.has(p.id));
      if (unicos.length === 0) this.fim.set(true);
      this.posts.update((lista) => [...lista, ...unicos]);
    } catch (e) {
      this.erro.set(mensagemDeErro(e).geral);
    } finally {
      this.carregando.set(false);
    }
  }

  // ---------- card ativo ----------

  private ativar(indice: number): void {
    const post = this.posts()[indice];
    if (!post || post.id === this.idAtivo) return;

    this.encerrarAtual();
    this.idAtivo = post.id;
    this.indiceAtivo.set(indice);
    this.tocar(post);
    this.prepararProximo(indice + 1);
    void this.carregarSeguidores(post);

    if (indice >= this.posts().length - CARREGAR_QUANDO_FALTAREM) void this.carregarMais();
  }

  private tocar(post: Post): void {
    const url = this.api.midiaUrl(post.arquivoAudio);
    if (!url) return;
    const audio = this.audioRef().nativeElement;
    this.maxOuvido = 0;
    this.progresso.set(0);
    audio.src = url;
    audio.muted = this.mudo();
    // Se o navegador bloquear o autoplay, o card mostra o ícone de play e um clique resolve.
    audio.play().catch(() => undefined);
  }

  private prepararProximo(indice: number): void {
    const url = this.api.midiaUrl(this.posts()[indice]?.arquivoAudio);
    this.proximo = null;
    if (!url) return;
    const audio = new Audio();
    audio.preload = 'auto';
    audio.src = url;
    this.proximo = audio;
  }

  /**
   * Registra o que aconteceu com o card que está saindo da tela. Rolar para a próxima sem curtir/salvar
   * e sem ter ouvido quase tudo = "pulou" (senão o card voltaria no próximo feed).
   */
  private encerrarAtual(): void {
    const post = this.posts().find((p) => p.id === this.idAtivo);
    if (!post) return;

    const audio = this.audioRef().nativeElement;
    const duracao = Number.isFinite(audio.duration) ? audio.duration : 0;
    const interagiu = !!this.curtidos()[post.id] || !!this.salvos()[post.id];
    const ouviuBastante = duracao > 0 && this.maxOuvido >= duracao * FRACAO_CONSIDERADA_OUVIDA;

    this.api
      .interagir({
        postId: post.id,
        pulou: !interagiu && !ouviuBastante,
        segundosOuvidos: Math.floor(this.maxOuvido),
      })
      .subscribe({ error: () => undefined });
  }

  // ---------- player ----------

  protected aoAtualizarTempo(): void {
    const audio = this.audioRef().nativeElement;
    if (audio.currentTime > this.maxOuvido) this.maxOuvido = audio.currentTime;
    this.progresso.set(audio.duration > 0 ? audio.currentTime / audio.duration : 0);
  }

  protected aoClicarCartao(indice: number): void {
    if (indice === this.indiceAtivo()) this.alternarReproducao();
  }

  protected alternarReproducao(): void {
    const audio = this.audioRef().nativeElement;
    if (audio.paused) audio.play().catch(() => undefined);
    else audio.pause();
  }

  protected alternarMudo(): void {
    this.mudo.update((v) => !v);
    this.audioRef().nativeElement.muted = this.mudo();
  }

  protected buscar(evento: MouseEvent, indice: number): void {
    evento.stopPropagation();
    if (indice !== this.indiceAtivo()) return;
    const audio = this.audioRef().nativeElement;
    if (!(audio.duration > 0)) return;
    const caixa = (evento.currentTarget as HTMLElement).getBoundingClientRect();
    const fracao = Math.min(Math.max((evento.clientX - caixa.left) / caixa.width, 0), 1);
    audio.currentTime = fracao * audio.duration;
  }

  protected irPara(indice: number): void {
    this.cards()[indice]?.nativeElement.scrollIntoView({ behavior: 'smooth', block: 'center' });
  }

  // ---------- curtir, salvar, seguir ----------

  protected totalCurtidas(post: Post): number {
    return post.totalCurtidas + (this.curtidos()[post.id] ? 1 : 0);
  }

  protected alternarCurtida(post: Post): void {
    this.alternar(post, 'curtido');
  }

  protected alternarSalvo(post: Post): void {
    this.alternar(post, 'salvo');
  }

  /** Atualiza a tela na hora e desfaz se a API recusar. */
  private alternar(post: Post, campo: 'curtido' | 'salvo'): void {
    const estado = campo === 'curtido' ? this.curtidos : this.salvos;
    const novo = !estado()[post.id];
    estado.update((m) => ({ ...m, [post.id]: novo }));

    const req: InteracaoRequest =
      campo === 'curtido' ? { postId: post.id, curtido: novo } : { postId: post.id, salvo: novo };
    this.api.interagir(req).subscribe({
      error: () => estado.update((m) => ({ ...m, [post.id]: !novo })),
    });
  }

  protected estaSeguindo(post: Post): boolean {
    return this.seguidores()[post.artista.id]?.seguindo ?? false;
  }

  private async carregarSeguidores(post: Post): Promise<void> {
    const id = post.artista.id;
    if (this.seguidores()[id]) return;
    try {
      const dados = await firstValueFrom(this.api.seguidores(id));
      this.seguidores.update((m) => ({ ...m, [id]: dados }));
    } catch {
      // sem o estado do "seguir" o botão só fica desabilitado; não atrapalha a música
    }
  }

  protected alternarSeguir(evento: Event, post: Post): void {
    evento.stopPropagation();
    const id = post.artista.id;
    const atual = this.seguidores()[id];
    if (!atual) return;

    const seguindo = !atual.seguindo;
    this.seguidores.update((m) => ({ ...m, [id]: { total: atual.total + (seguindo ? 1 : -1), seguindo } }));
    const chamada = seguindo ? this.api.seguir(id) : this.api.deixarDeSeguir(id);
    chamada.subscribe({ error: () => this.seguidores.update((m) => ({ ...m, [id]: atual })) });
  }

  // ---------- teclado ----------

  aoTeclar(e: KeyboardEvent): void {
    const alvo = e.target as HTMLElement | null;
    if (alvo?.closest('input, textarea, select, [contenteditable]')) return;
    if (e.ctrlKey || e.metaKey || e.altKey) return;

    const post = this.posts()[this.indiceAtivo()];
    switch (e.key) {
      case 'ArrowDown':
        e.preventDefault();
        this.irPara(this.indiceAtivo() + 1);
        break;
      case 'ArrowUp':
        e.preventDefault();
        this.irPara(this.indiceAtivo() - 1);
        break;
      case ' ':
        if (alvo?.closest('button, a')) return; // espaço em botão já é um clique
        e.preventDefault();
        this.alternarReproducao();
        break;
      case 'l':
      case 'L':
        if (post) this.alternarCurtida(post);
        break;
      case 's':
      case 'S':
        if (post) this.alternarSalvo(post);
        break;
      case 'm':
      case 'M':
        this.alternarMudo();
        break;
    }
  }
}
