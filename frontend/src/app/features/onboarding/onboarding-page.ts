import { ChangeDetectionStrategy, Component, OnInit, computed, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { Genero } from '../../core/models/api.models';
import { ApiService } from '../../core/services/api.service';
import { mensagemDeErro } from '../../core/util/erros';

@Component({
  selector: 'app-onboarding-page',
  templateUrl: './onboarding-page.html',
  styleUrl: './onboarding-page.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class OnboardingPage implements OnInit {
  private readonly api = inject(ApiService);
  private readonly router = inject(Router);

  protected readonly minimo = 3;
  protected readonly maximo = 20; // limite do backend (DefinirPreferenciasRequest)

  protected readonly generos = signal<Genero[]>([]);
  protected readonly escolhidos = signal<number[]>([]);
  protected readonly carregando = signal(true);
  protected readonly enviando = signal(false);
  protected readonly erro = signal<string | null>(null);
  protected readonly podeContinuar = computed(() => this.escolhidos().length >= this.minimo);

  async ngOnInit(): Promise<void> {
    try {
      this.generos.set(await firstValueFrom(this.api.generos()));
    } catch (e) {
      this.erro.set(mensagemDeErro(e).geral);
    } finally {
      this.carregando.set(false);
    }
  }

  protected alternar(id: number): void {
    this.escolhidos.update((lista) => {
      if (lista.includes(id)) return lista.filter((x) => x !== id);
      return lista.length < this.maximo ? [...lista, id] : lista;
    });
  }

  protected async continuar(): Promise<void> {
    if (!this.podeContinuar() || this.enviando()) return;
    this.enviando.set(true);
    this.erro.set(null);
    try {
      await firstValueFrom(this.api.definirPreferencias(this.escolhidos()));
      await this.router.navigateByUrl('/');
    } catch (e) {
      this.erro.set(mensagemDeErro(e).geral);
    } finally {
      this.enviando.set(false);
    }
  }
}
