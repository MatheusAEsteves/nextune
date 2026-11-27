import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { environment } from '../../../environments/environment';
import { Genero, Interacao, InteracaoRequest, Post, Preferencia, Seguidores } from '../models/api.models';

/** Chamadas à API do Nextune. Autenticação e headers ficam por conta do authInterceptor. */
@Injectable({ providedIn: 'root' })
export class ApiService {
  private readonly http = inject(HttpClient);
  private readonly base = environment.apiUrl;

  /** Arquivos vêm como caminho (/uploads/...); a URL completa é apiUrl + caminho. */
  midiaUrl(caminho: string | null | undefined): string | null {
    if (!caminho) return null;
    return /^https?:\/\//.test(caminho) ? caminho : `${this.base}${caminho}`;
  }

  generos() {
    return this.http.get<Genero[]>(`${this.base}/generos`);
  }

  minhasPreferencias() {
    return this.http.get<Preferencia[]>(`${this.base}/preferencias/me`);
  }

  definirPreferencias(generoIds: number[]) {
    return this.http.put<unknown>(`${this.base}/preferencias/me`, { generoIds });
  }

  feed(limite: number, excluir: string[]) {
    let params = new HttpParams().set('limite', limite);
    if (excluir.length > 0) params = params.set('excluir', excluir.join(','));
    return this.http.get<Post[]>(`${this.base}/feed`, { params });
  }

  interagir(req: InteracaoRequest) {
    return this.http.post<Interacao>(`${this.base}/interacoes`, req);
  }

  seguidores(artistaId: string) {
    return this.http.get<Seguidores>(`${this.base}/artistas/${artistaId}/seguidores`);
  }

  seguir(artistaId: string) {
    return this.http.put<void>(`${this.base}/artistas/${artistaId}/seguir`, null);
  }

  deixarDeSeguir(artistaId: string) {
    return this.http.delete<void>(`${this.base}/artistas/${artistaId}/seguir`);
  }
}
