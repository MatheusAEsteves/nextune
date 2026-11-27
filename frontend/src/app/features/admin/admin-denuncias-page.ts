import { DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, effect, inject, signal, untracked } from '@angular/core';
import { RouterLink } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { Denuncia, DenunciaStatus } from '../../core/models/api.models';
import { ApiService } from '../../core/services/api.service';
import { mensagemDeErro } from '../../core/util/erros';
import { ListaPaginada } from '../../core/util/lista-paginada';

interface FiltroStatus {
  valor: DenunciaStatus | null;
  rotulo: string;
}

/** Moderação: lista as denúncias e permite mudar o status. Exige role ADMIN (o backend confere). */
@Component({
  selector: 'app-admin-denuncias-page',
  imports: [DatePipe, RouterLink],
  templateUrl: './admin-denuncias-page.html',
  styleUrl: './admin-denuncias-page.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AdminDenunciasPage {
  private readonly api = inject(ApiService);

  protected readonly filtros: FiltroStatus[] = [
    { valor: 'PENDENTE', rotulo: 'Pendentes' },
    { valor: 'ANALISADA', rotulo: 'Analisadas' },
    { valor: 'RESOLVIDA', rotulo: 'Resolvidas' },
    { valor: null, rotulo: 'Todas' },
  ];
  protected readonly rotulos: Record<DenunciaStatus, string> = {
    PENDENTE: 'Pendente',
    ANALISADA: 'Analisada',
    RESOLVIDA: 'Resolvida',
  };
  protected readonly todosStatus: DenunciaStatus[] = ['PENDENTE', 'ANALISADA', 'RESOLVIDA'];

  protected readonly status = signal<DenunciaStatus | null>('PENDENTE');
  protected readonly erro = signal<string | null>(null);
  protected readonly lista = new ListaPaginada<Denuncia>((p) => this.api.denuncias(this.status(), p));

  constructor() {
    effect(() => {
      this.status();
      untracked(() => void this.lista.recarregar());
    });
  }

  protected async alterar(denuncia: Denuncia, novo: DenunciaStatus): Promise<void> {
    this.erro.set(null);
    try {
      const atualizada = await firstValueFrom(this.api.atualizarStatusDenuncia(denuncia.id, novo));
      const filtro = this.status();
      this.lista.itens.update((l) =>
        filtro && atualizada.status !== filtro
          ? l.filter((x) => x.id !== denuncia.id) // saiu do filtro atual
          : l.map((x) => (x.id === denuncia.id ? atualizada : x)),
      );
    } catch (e) {
      this.erro.set(mensagemDeErro(e).geral);
    }
  }
}
