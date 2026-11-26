import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { mensagemDeErro } from '../../core/util/erros';
import { Icone } from '../../shared/components/icone';

type Modo = 'entrar' | 'cadastrar';

@Component({
  selector: 'app-auth-page',
  imports: [FormsModule, Icone],
  templateUrl: './auth-page.html',
  styleUrl: './auth-page.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AuthPage {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  protected readonly modo = signal<Modo>('entrar');
  protected readonly nome = signal('');
  protected readonly email = signal('');
  protected readonly senha = signal('');
  protected readonly enviando = signal(false);
  protected readonly erroGeral = signal<string | null>(null);
  protected readonly erroCampos = signal<Record<string, string>>({});

  protected trocarModo(): void {
    this.modo.update((m) => (m === 'entrar' ? 'cadastrar' : 'entrar'));
    this.erroGeral.set(null);
    this.erroCampos.set({});
  }

  protected async enviar(): Promise<void> {
    this.erroGeral.set(null);
    const invalidos = this.validar();
    this.erroCampos.set(invalidos);
    if (Object.keys(invalidos).length > 0) return;

    this.enviando.set(true);
    try {
      const email = this.email().trim();
      if (this.modo() === 'cadastrar') {
        await this.auth.cadastrar(this.nome().trim(), email, this.senha());
      } else {
        await this.auth.entrar(email, this.senha());
      }
      await this.router.navigateByUrl('/');
    } catch (erro) {
      this.tratarErro(erro);
    } finally {
      this.enviando.set(false);
    }
  }

  /** Mesmas regras do backend, para avisar antes de ir à rede. */
  private validar(): Record<string, string> {
    const erros: Record<string, string> = {};
    if (this.modo() === 'cadastrar' && !this.nome().trim()) erros['nome'] = 'Informe seu nome';
    if (!/^\S+@\S+\.\S+$/.test(this.email().trim())) erros['email'] = 'Informe um e-mail válido';
    if (this.modo() === 'cadastrar') {
      const tamanho = this.senha().length;
      if (tamanho < 8 || tamanho > 72) erros['senha'] = 'A senha deve ter de 8 a 72 caracteres';
    } else if (!this.senha()) {
      erros['senha'] = 'Informe sua senha';
    }
    return erros;
  }

  private tratarErro(erro: unknown): void {
    if (erro instanceof HttpErrorResponse && erro.status === 401) {
      this.erroGeral.set('E-mail ou senha incorretos.');
    } else if (erro instanceof HttpErrorResponse && erro.status === 409) {
      this.erroCampos.set({ email: 'Este e-mail já está cadastrado' });
    } else {
      const tratado = mensagemDeErro(erro);
      this.erroGeral.set(tratado.geral);
      this.erroCampos.set(tratado.campos);
    }
  }
}
