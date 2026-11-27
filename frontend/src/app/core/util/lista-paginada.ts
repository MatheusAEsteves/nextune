import { signal } from '@angular/core';
import { Observable, firstValueFrom } from 'rxjs';
import { Pagina } from '../models/api.models';
import { mensagemDeErro } from './erros';

/** Controla uma lista paginada da API (botão "Carregar mais"): itens, carregando, erro e se há mais páginas. */
export class ListaPaginada<T> {
  readonly itens = signal<T[]>([]);
  readonly carregando = signal(false);
  readonly erro = signal<string | null>(null);
  readonly iniciada = signal(false); // já terminou ao menos um carregamento
  readonly temMais = signal(true);

  private readonly buscar: (pagina: number) => Observable<Pagina<T>>;
  private proxima = 0;
  private geracao = 0; // descarta respostas de buscas antigas quando a lista é recarregada

  constructor(buscar: (pagina: number) => Observable<Pagina<T>>) {
    this.buscar = buscar;
  }

  async carregarMais(): Promise<void> {
    if (this.carregando() || !this.temMais()) return;
    const geracao = this.geracao;
    this.carregando.set(true);
    this.erro.set(null);
    try {
      const resposta = await firstValueFrom(this.buscar(this.proxima));
      if (geracao !== this.geracao) return;
      this.itens.update((l) => [...l, ...resposta.conteudo]);
      this.proxima = resposta.pagina + 1;
      this.temMais.set(this.proxima < resposta.totalPaginas);
    } catch (e) {
      if (geracao === this.geracao) this.erro.set(mensagemDeErro(e).geral);
    } finally {
      if (geracao === this.geracao) {
        this.carregando.set(false);
        this.iniciada.set(true);
      }
    }
  }

  async recarregar(): Promise<void> {
    this.geracao++;
    this.itens.set([]);
    this.proxima = 0;
    this.temMais.set(true);
    this.carregando.set(false);
    this.iniciada.set(false);
    await this.carregarMais();
  }
}
