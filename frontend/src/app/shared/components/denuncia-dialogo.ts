import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, inject, input, output, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { firstValueFrom } from 'rxjs';
import { ApiService } from '../../core/services/api.service';
import { mensagemDeErro } from '../../core/util/erros';

/** Janela para denunciar uma música (postId) ou um artista (artistaId). Esc ou clique fora fecham. */
@Component({
  selector: 'app-denuncia-dialogo',
  imports: [FormsModule],
  templateUrl: './denuncia-dialogo.html',
  styleUrl: './denuncia-dialogo.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
  host: { '(document:keydown.escape)': 'fechar.emit()' },
})
export class DenunciaDialogo {
  private readonly api = inject(ApiService);

  readonly postId = input<string | undefined>(undefined);
  readonly artistaId = input<string | undefined>(undefined);
  readonly fechar = output<void>();

  protected readonly motivos = [
    'Conteúdo ofensivo',
    'Spam ou propaganda',
    'Violação de direitos autorais',
    'Informação falsa',
    'Outro',
  ];
  protected readonly motivo = signal(this.motivos[0]);
  protected readonly outro = signal('');
  protected readonly descricao = signal('');
  protected readonly enviando = signal(false);
  protected readonly enviada = signal(false);
  protected readonly erro = signal<string | null>(null);

  protected async enviar(): Promise<void> {
    const motivo = (this.motivo() === 'Outro' ? this.outro() : this.motivo()).trim();
    if (!motivo) {
      this.erro.set('Descreva o motivo da denúncia.');
      return;
    }
    this.enviando.set(true);
    this.erro.set(null);
    try {
      await firstValueFrom(
        this.api.denunciar({
          postId: this.postId(),
          artistaId: this.artistaId(),
          motivo: motivo.slice(0, 100),
          descricao: this.descricao().trim() || undefined,
        }),
      );
      this.enviada.set(true);
    } catch (e) {
      this.erro.set(
        e instanceof HttpErrorResponse && e.status === 409
          ? 'Você já fez essa denúncia e ela ainda está em análise.'
          : mensagemDeErro(e).geral,
      );
    } finally {
      this.enviando.set(false);
    }
  }
}
