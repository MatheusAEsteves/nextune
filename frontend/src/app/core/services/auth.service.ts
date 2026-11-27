import { HttpClient, HttpContext } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { environment } from '../../../environments/environment';
import { IGNORAR_401 } from '../interceptors/http-tokens';
import { Usuario } from '../models/api.models';

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
  private readonly router = inject(Router);

  private readonly credencial = signal<string | null>(null);
  readonly usuario = signal<Usuario | null>(null);
  readonly autenticado = computed(() => this.credencial() !== null && this.usuario() !== null);

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

  sair(): void {
    this.credencial.set(null);
    this.usuario.set(null);
    void this.router.navigateByUrl('/login');
  }
}
