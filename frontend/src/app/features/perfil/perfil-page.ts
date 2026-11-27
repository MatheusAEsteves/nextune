import { ChangeDetectionStrategy, Component, effect, inject, signal, untracked } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { Artista } from '../../core/models/api.models';
import { ApiService } from '../../core/services/api.service';
import { AuthService } from '../../core/services/auth.service';
import { mensagemDeErro } from '../../core/util/erros';
import { ListaPaginada } from '../../core/util/lista-paginada';
import { IMAGEM_EXTENSOES, IMAGEM_MAX_BYTES, validarArquivo } from '../../core/util/validacoes';
import { Avatar } from '../../shared/components/avatar';
import { Icone } from '../../shared/components/icone';

@Component({
  selector: 'app-perfil-page',
  imports: [FormsModule, RouterLink, Avatar, Icone],
  templateUrl: './perfil-page.html',
  styleUrl: './perfil-page.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PerfilPage {
  private readonly api = inject(ApiService);
  protected readonly auth = inject(AuthService);

  protected readonly nome = signal('');
  protected readonly bio = signal('');
  protected readonly salvando = signal(false);
  protected readonly enviandoFoto = signal(false);
  protected readonly mensagem = signal<string | null>(null);
  protected readonly erro = signal<string | null>(null);
  protected readonly seguindo = new ListaPaginada<Artista>((p) => this.api.seguindo(p));

  constructor() {
    // Preenche o formulário uma vez com os dados da sessão.
    effect(() => {
      const u = this.auth.usuario();
      untracked(() => {
        if (u && this.nome() === '') {
          this.nome.set(u.nome);
          this.bio.set(u.bio ?? '');
        }
      });
    });
    void this.seguindo.carregarMais();
  }

  protected async salvar(): Promise<void> {
    const nome = this.nome().trim();
    if (!nome) {
      this.erro.set('O nome não pode ficar vazio.');
      return;
    }
    this.salvando.set(true);
    this.limparAvisos();
    try {
      const usuario = await firstValueFrom(this.api.atualizarPerfil({ nome, bio: this.bio() }));
      this.auth.usuario.set(usuario);
      this.mensagem.set('Perfil atualizado.');
    } catch (e) {
      this.erro.set(mensagemDeErro(e).geral);
    } finally {
      this.salvando.set(false);
    }
  }

  protected async escolherFoto(evento: Event): Promise<void> {
    const entrada = evento.target as HTMLInputElement;
    const arquivo = entrada.files?.[0];
    entrada.value = ''; // permite escolher o mesmo arquivo de novo
    if (!arquivo) return;

    this.limparAvisos();
    const problema = validarArquivo(arquivo, IMAGEM_EXTENSOES, IMAGEM_MAX_BYTES, 'Foto');
    if (problema) {
      this.erro.set(problema);
      return;
    }
    this.enviandoFoto.set(true);
    try {
      this.auth.usuario.set(await firstValueFrom(this.api.atualizarFoto(arquivo)));
      this.mensagem.set('Foto atualizada.');
    } catch (e) {
      this.erro.set(mensagemDeErro(e).geral);
    } finally {
      this.enviandoFoto.set(false);
    }
  }

  protected async removerFoto(): Promise<void> {
    this.limparAvisos();
    this.enviandoFoto.set(true);
    try {
      this.auth.usuario.set(await firstValueFrom(this.api.atualizarPerfil({ fotoPerfil: '' })));
      this.mensagem.set('Foto removida.');
    } catch (e) {
      this.erro.set(mensagemDeErro(e).geral);
    } finally {
      this.enviandoFoto.set(false);
    }
  }

  private limparAvisos(): void {
    this.mensagem.set(null);
    this.erro.set(null);
  }
}
