import { ChangeDetectionStrategy, Component, input } from '@angular/core';

export type NomeIcone =
  | 'coracao' | 'salvar' | 'link' | 'volume' | 'mudo' | 'play'
  | 'cima' | 'baixo' | 'inicio' | 'sair' | 'musica'
  | 'comentario' | 'bandeira' | 'fechar' | 'usuario' | 'microfone'
  | 'mais' | 'escudo' | 'editar' | 'lixeira' | 'enviar';

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
        @case ('salvar') { <path d="m19 21-7-4-7 4V5a2 2 0 0 1 2-2h10a2 2 0 0 1 2 2v16z" /> }
        @case ('link') {
          <path d="M15 3h6v6" /><path d="M10 14 21 3" />
          <path d="M18 13v6a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h6" />
        }
        @case ('volume') {
          <path d="M11 5 6 9H2v6h4l5 4V5z" /><path d="M15.54 8.46a5 5 0 0 1 0 7.07" />
          <path d="M19.07 4.93a10 10 0 0 1 0 14.14" />
        }
        @case ('mudo') { <path d="M11 5 6 9H2v6h4l5 4V5z" /><path d="m22 9-6 6" /><path d="m16 9 6 6" /> }
        @case ('play') { <path d="M6 3 20 12 6 21z" fill="currentColor" /> }
        @case ('cima') { <path d="m18 15-6-6-6 6" /> }
        @case ('baixo') { <path d="m6 9 6 6 6-6" /> }
        @case ('inicio') { <path d="m3 9 9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z" /><path d="M9 22V12h6v10" /> }
        @case ('sair') { <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4" /><path d="m16 17 5-5-5-5" /><path d="M21 12H9" /> }
        @case ('musica') { <path d="M9 18V5l12-2v13" /><circle cx="6" cy="18" r="3" /><circle cx="18" cy="16" r="3" /> }
        @case ('comentario') { <path d="M7.9 20A9 9 0 1 0 4 16.1L2 22Z" /> }
        @case ('bandeira') { <path d="M4 15s1-1 4-1 5 2 8 2 4-1 4-1V3s-1 1-4 1-5-2-8-2-4 1-4 1z" /><path d="M4 22v-7" /> }
        @case ('fechar') { <path d="M18 6 6 18" /><path d="m6 6 12 12" /> }
        @case ('usuario') { <path d="M19 21v-2a4 4 0 0 0-4-4H9a4 4 0 0 0-4 4v2" /><circle cx="12" cy="7" r="4" /> }
        @case ('microfone') {
          <path d="M12 2a3 3 0 0 0-3 3v7a3 3 0 0 0 6 0V5a3 3 0 0 0-3-3Z" /><path d="M19 10v2a7 7 0 0 1-14 0v-2" />
          <path d="M12 19v3" />
        }
        @case ('mais') { <path d="M5 12h14" /><path d="M12 5v14" /> }
        @case ('escudo') {
          <path d="M20 13c0 5-3.5 7.5-7.66 8.95a1 1 0 0 1-.67-.01C7.5 20.5 4 18 4 13V6a1 1 0 0 1 1-1c2 0 4.5-1.2 6.24-2.72a1.17 1.17 0 0 1 1.52 0C14.51 3.81 17 5 19 5a1 1 0 0 1 1 1z" />
        }
        @case ('editar') { <path d="M17 3a2.85 2.83 0 1 1 4 4L7.5 20.5 2 22l1.5-5.5Z" /> }
        @case ('lixeira') {
          <path d="M3 6h18" /><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6" />
          <path d="M8 6V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2" />
        }
        @case ('enviar') {
          <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4" /><path d="m17 8-5-5-5 5" /><path d="M12 3v12" />
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
