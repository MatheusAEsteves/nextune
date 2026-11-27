import { HttpClient, HttpContext } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, firstValueFrom, map, of } from 'rxjs';
import { environment } from '../../../environments/environment';
import { IGNORAR_401 } from '../interceptors/http-tokens';
import { Artista, Usuario } from '../models/api.models';
import { ApiService } from './api.service';

/** Codifica "email:senha" em Base64 aceitando acentos (btoa puro só aceita Latin-1). */
function codificarBasic(email: string, senha: string): string {
  const bytes = new TextEncoder().encode(`${email}:${senha}`);
  let binario = '';
  bytes.forEach((b) => (binario += String.fromCharCode(b)));
  return btoa(binario);
}

/**
 * Sessão HTTP Basic. A API não tem endpoint de login: "entrar" = chamar GET /usuarios/me com as credenciais.
 * As credenciais ficam só em memória (como recomenda o API.md), então recarregar a página (F5) desloga.
 */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly api = inject(ApiService);
  private readonly router = inject(Router);

  private readonly credencial = signal<string | null>(null);
  readonly usuario = signal<Usuario | null>(null);
  readonly autenticado = computed(() => this.credencial() !== null && this.usuario() !== null);

  /** Perfil de artista do usuário: undefined = ainda carregando, null = não é artista. */
  readonly artista = signal<Artista | null | undefined>(undefined);

  /**
   * A API não devolve a role no perfil. Descobrimos se é admin tentando abrir uma rota exclusiva de admin
   * (200 = admin, 403 = não). O backend continua sendo quem de fato protege essas rotas.
   */
  readonly admin = signal(false);

  get authorization(): string | null {
    const c = this.credencial();
    return c ? `Basic ${c}` : null;
  }

  async entrar(email: string, senha: string): Promise<Usuario> {
    const credencial = codificarBasic(email, senha);
    const usuario = await firstValueFrom(
      this.http.get<Usuario>(`${environment.apiUrl}/usuarios/me`, {
        headers: { Authorization: `Basic ${credencial}` },
        context: new HttpContext().set(IGNORAR_401, true),
      }),
    );
    this.credencial.set(credencial);
    this.usuario.set(usuario);
    await this.carregarPerfis();
    return usuario;
  }

  async cadastrar(nome: string, email: string, senha: string): Promise<Usuario> {
    await firstValueFrom(
      this.http.post<Usuario>(
        `${environment.apiUrl}/usuarios`,
        { nome, email, senha },
        { context: new HttpContext().set(IGNORAR_401, true) },
      ),
    );
    return this.entrar(email, senha);
  }

  /** Descobre se o usuário é artista (GET /artistas/me: 404 = não) e se é admin. */
  async carregarPerfis(): Promise<void> {
    const [artista, admin] = await Promise.all([
      firstValueFrom(this.api.meuArtista().pipe(catchError(() => of(null)))),
      firstValueFrom(
        this.api.denuncias(null, 0).pipe(
          map(() => true),
          catchError(() => of(false)),
        ),
      ),
    ]);
    this.artista.set(artista);
    this.admin.set(admin);
  }

  sair(): void {
    this.credencial.set(null);
    this.usuario.set(null);
    this.artista.set(undefined);
    this.admin.set(false);
    void this.router.navigateByUrl('/login');
  }
}
