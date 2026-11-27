import { ChangeDetectionStrategy, Component, computed, inject } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { Icone } from '../../shared/components/icone';

/** Estrutura das telas logadas: barra lateral + conteúdo. */
@Component({
  selector: 'app-shell',
  imports: [RouterOutlet, RouterLink, RouterLinkActive, Icone],
  templateUrl: './shell.html',
  styleUrl: './shell.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Shell {
  protected readonly auth = inject(AuthService);
  protected readonly inicial = computed(() => (this.auth.usuario()?.nome ?? '?').charAt(0).toUpperCase());

  protected sair(): void {
    this.auth.sair();
  }
}
