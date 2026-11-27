import { ChangeDetectionStrategy, Component, input, model } from '@angular/core';
import { Genero } from '../../core/models/api.models';

/** Escolha de gêneros em chips, com limite máximo. Uso: <app-seletor-generos [generos]="lista" [(selecionados)]="ids" /> */
@Component({
  selector: 'app-seletor-generos',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="chips" role="group" aria-label="Gêneros">
      @for (g of generos(); track g.id) {
        <button type="button" class="chip" [class.ativo]="selecionados().includes(g.id)"
                [attr.aria-pressed]="selecionados().includes(g.id)" (click)="alternar(g.id)">
          {{ g.nome }}
        </button>
      }
    </div>
  `,
})
export class SeletorGeneros {
  readonly generos = input.required<Genero[]>();
  readonly maximo = input(5);
  readonly selecionados = model<number[]>([]);

  protected alternar(id: number): void {
    const lista = this.selecionados();
    if (lista.includes(id)) this.selecionados.set(lista.filter((x) => x !== id));
    else if (lista.length < this.maximo()) this.selecionados.set([...lista, id]);
  }
}
