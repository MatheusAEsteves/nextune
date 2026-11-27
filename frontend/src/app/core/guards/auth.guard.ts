import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { ApiService } from '../services/api.service';
import { AuthService } from '../services/auth.service';

/** Só deixa passar quem está logado. */
export const authGuard: CanActivateFn = () => {
  return inject(AuthService).autenticado() ? true : inject(Router).createUrlTree(['/login']);
};

/** A tela de login só serve para quem ainda não entrou. */
export const visitanteGuard: CanActivateFn = () => {
  return inject(AuthService).autenticado() ? inject(Router).createUrlTree(['/']) : true;
};

/** Sem gêneros escolhidos (lista vazia), manda para o onboarding antes do feed. */
export const preferenciasGuard: CanActivateFn = async () => {
  const router = inject(Router);
  try {
    const preferencias = await firstValueFrom(inject(ApiService).minhasPreferencias());
    return preferencias.length > 0 ? true : router.createUrlTree(['/onboarding']);
  } catch {
    return router.createUrlTree(['/login']);
  }
};
