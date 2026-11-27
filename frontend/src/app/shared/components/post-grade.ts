import { ChangeDetectionStrategy, Component, inject, input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Post } from '../../core/models/api.models';
import { ApiService } from '../../core/services/api.service';
import { Icone } from './icone';

/** Grade de capas; cada item leva para a página da música (/post/:id). */
@Component({
  selector: 'app-post-grade',
  imports: [RouterLink, Icone],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="grade">
      @for (p of posts(); track p.id) {
        <a class="item" [routerLink]="['/post', p.id]">
          <div class="capa">
            <img [src]="api.midiaUrl(p.capa)" [alt]="'Capa de ' + p.titulo" loading="lazy" />
            <span class="curtidas"><app-icone nome="coracao" [tamanho]="14" [preenchido]="true" /> {{ p.totalCurtidas }}</span>
          </div>
          <strong class="titulo">{{ p.titulo }}</strong>
          @if (mostrarArtista()) { <span class="artista">{{ p.artista.nomeArtistico }}</span> }
        </a>
      }
    </div>
  `,
  styles: `
    .grade { display: grid; grid-template-columns: repeat(auto-fill, minmax(170px, 1fr)); gap: 1.25rem; }
    .item { display: grid; gap: .3rem; text-decoration: none; color: inherit; align-content: start; }
    .capa { position: relative; aspect-ratio: 1; border-radius: 14px; overflow: hidden; background: var(--surface-2); }
    .capa img { width: 100%; height: 100%; object-fit: cover; transition: transform .25s; }
    .item:hover .capa img { transform: scale(1.04); }
    .curtidas {
      position: absolute; left: .5rem; bottom: .5rem; display: inline-flex; align-items: center; gap: .3rem;
      background: rgba(0, 0, 0, .6); border-radius: 999px; padding: .15rem .55rem; font-size: .75rem; font-weight: 600;
    }
    .titulo { font-size: .95rem; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
    .artista { font-size: .8rem; color: var(--muted); }
  `,
})
export class PostGrade {
  protected readonly api = inject(ApiService);
  readonly posts = input.required<Post[]>();
  readonly mostrarArtista = input(true);
}
