import { ChangeDetectionStrategy, Component, input } from '@angular/core';

export type NomeIcone =
  | 'coracao' | 'salvar' | 'link' | 'volume' | 'mudo' | 'play'
  | 'cima' | 'baixo' | 'inicio' | 'sair' | 'musica';

/** Ícones SVG em linha (traço, 24x24). Uso: <app-icone nome="coracao" [preenchido]="curtido" /> */
@Component({
  selector: 'app-icone',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <svg viewBox="0 0 24 24" [attr.width]="tamanho()" [attr.height]="tamanho()"
         [attr.fill]="preenchido() ? 'currentColor' : 'none'" stroke="currentColor"
         stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
      @switch (nome()) {
        @case ('coracao') {
          <path d="M19 14c1.49-1.46 3-3.21 3-5.5A5.5 5.5 0 0 0 16.5 3c-1.76 0-3 .5-4.5 2-1.5-1.5-2.74-2-4.5-2A5.5 5.5 0 0 0 2 8.5c0 2.3 1.5 4.05 3 5.5l7 7Z" />
        }
        @case ('salvar') {
          <path d="m19 21-7-4-7 4V5a2 2 0 0 1 2-2h10a2 2 0 0 1 2 2v16z" />
        }
        @case ('link') {
          <path d="M15 3h6v6" /><path d="M10 14 21 3" />
          <path d="M18 13v6a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h6" />
        }
        @case ('volume') {
          <path d="M11 5 6 9H2v6h4l5 4V5z" /><path d="M15.54 8.46a5 5 0 0 1 0 7.07" />
          <path d="M19.07 4.93a10 10 0 0 1 0 14.14" />
        }
        @case ('mudo') {
          <path d="M11 5 6 9H2v6h4l5 4V5z" /><path d="m22 9-6 6" /><path d="m16 9 6 6" />
        }
        @case ('play') {
          <path d="M6 3 20 12 6 21z" fill="currentColor" />
        }
        @case ('cima') { <path d="m18 15-6-6-6 6" /> }
        @case ('baixo') { <path d="m6 9 6 6 6-6" /> }
        @case ('inicio') {
          <path d="m3 9 9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z" /><path d="M9 22V12h6v10" />
        }
        @case ('sair') {
          <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4" /><path d="m16 17 5-5-5-5" /><path d="M21 12H9" />
        }
        @case ('musica') {
          <path d="M9 18V5l12-2v13" /><circle cx="6" cy="18" r="3" /><circle cx="18" cy="16" r="3" />
        }
      }
    </svg>
  `,
  styles: `:host { display: inline-flex; }`,
})
export class Icone {
  readonly nome = input.required<NomeIcone>();
  readonly tamanho = input(24);
  readonly preenchido = input(false);
}
