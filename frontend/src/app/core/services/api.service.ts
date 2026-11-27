import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { environment } from '../../../environments/environment';
import {
  Artista, ArtistaWrite, Comentario, Denuncia, DenunciaRequest, DenunciaStatus, Genero, Interacao,
  InteracaoRequest, Pagina, PerfilUpdate, Post, PostUpdate, Preferencia, Seguidores, Usuario, UsuarioPublico,
} from '../models/api.models';

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

  private pagina(pagina: number, tamanho = 20): HttpParams {
    return new HttpParams().set('pagina', pagina).set('tamanho', tamanho);
  }

  // ----- usuário -----
  atualizarPerfil(corpo: PerfilUpdate) {
    return this.http.put<Usuario>(`${this.base}/usuarios/me`, corpo);
  }

  atualizarFoto(foto: File) {
    const form = new FormData();
    form.append('foto', foto);
    return this.http.post<Usuario>(`${this.base}/usuarios/me/foto`, form);
  }

  usuarioPublico(id: string) {
    return this.http.get<UsuarioPublico>(`${this.base}/usuarios/${id}`);
  }

  seguindo(pagina: number) {
    return this.http.get<Pagina<Artista>>(`${this.base}/usuarios/me/seguindo`, { params: this.pagina(pagina) });
  }

  // ----- artista -----
  meuArtista() {
    return this.http.get<Artista>(`${this.base}/artistas/me`);
  }

  artista(id: string) {
    return this.http.get<Artista>(`${this.base}/artistas/${id}`);
  }

  ativarArtista(corpo: ArtistaWrite) {
    return this.http.post<Artista>(`${this.base}/artistas/ativar`, corpo);
  }

  atualizarArtista(corpo: Partial<ArtistaWrite>) {
    return this.http.put<Artista>(`${this.base}/artistas/me`, corpo);
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

  // ----- gêneros e preferências -----
  generos() {
    return this.http.get<Genero[]>(`${this.base}/generos`);
  }

  minhasPreferencias() {
    return this.http.get<Preferencia[]>(`${this.base}/preferencias/me`);
  }

  definirPreferencias(generoIds: number[]) {
    return this.http.put<unknown>(`${this.base}/preferencias/me`, { generoIds });
  }

  // ----- posts -----
  post(id: string) {
    return this.http.get<Post>(`${this.base}/posts/${id}`);
  }

  criarPost(form: FormData) {
    return this.http.post<Post>(`${this.base}/posts`, form);
  }

  atualizarPost(id: string, corpo: PostUpdate) {
    return this.http.put<Post>(`${this.base}/posts/${id}`, corpo);
  }

  atualizarCapa(id: string, capa: File) {
    const form = new FormData();
    form.append('capa', capa);
    return this.http.put<Post>(`${this.base}/posts/${id}/capa`, form);
  }

  removerPost(id: string) {
    return this.http.delete<void>(`${this.base}/posts/${id}`);
  }

  meusPosts(pagina: number) {
    return this.http.get<Pagina<Post>>(`${this.base}/artistas/me/posts`, { params: this.pagina(pagina) });
  }

  postsDoArtista(artistaId: string, pagina: number) {
    return this.http.get<Pagina<Post>>(`${this.base}/artistas/${artistaId}/posts`, { params: this.pagina(pagina) });
  }

  // ----- feed e interações -----
  feed(limite: number, excluir: string[]) {
    let params = new HttpParams().set('limite', limite);
    if (excluir.length > 0) params = params.set('excluir', excluir.join(','));
    return this.http.get<Post[]>(`${this.base}/feed`, { params });
  }

  interagir(req: InteracaoRequest) {
    return this.http.post<Interacao>(`${this.base}/interacoes`, req);
  }

  curtidas(pagina: number) {
    return this.http.get<Pagina<Post>>(`${this.base}/interacoes/curtidas`, { params: this.pagina(pagina) });
  }

  salvos(pagina: number) {
    return this.http.get<Pagina<Post>>(`${this.base}/interacoes/salvos`, { params: this.pagina(pagina) });
  }

  // ----- comentários -----
  comentarios(postId: string, pagina: number) {
    return this.http.get<Pagina<Comentario>>(`${this.base}/posts/${postId}/comentarios`, {
      params: this.pagina(pagina),
    });
  }

  comentar(postId: string, conteudo: string) {
    return this.http.post<Comentario>(`${this.base}/posts/${postId}/comentarios`, { conteudo });
  }

  removerComentario(id: string) {
    return this.http.delete<void>(`${this.base}/comentarios/${id}`);
  }

  // ----- denúncias -----
  denunciar(corpo: DenunciaRequest) {
    return this.http.post<Denuncia>(`${this.base}/denuncias`, corpo);
  }

  denuncias(status: DenunciaStatus | null, pagina: number) {
    let params = this.pagina(pagina);
    if (status) params = params.set('status', status);
    return this.http.get<Pagina<Denuncia>>(`${this.base}/admin/denuncias`, { params });
  }

  atualizarStatusDenuncia(id: string, status: DenunciaStatus) {
    return this.http.patch<Denuncia>(`${this.base}/admin/denuncias/${id}/status`, { status });
  }
}
