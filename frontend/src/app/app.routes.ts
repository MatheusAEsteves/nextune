import { Routes } from '@angular/router';
import { authGuard, preferenciasGuard, visitanteGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  {
    path: 'login',
    canActivate: [visitanteGuard],
    loadComponent: () => import('./features/auth/auth-page').then((m) => m.AuthPage),
  },
  {
    path: 'onboarding',
    canActivate: [authGuard],
    loadComponent: () => import('./features/onboarding/onboarding-page').then((m) => m.OnboardingPage),
  },
  {
    path: '',
    canActivate: [authGuard],
    loadComponent: () => import('./features/shell/shell').then((m) => m.Shell),
    children: [
      {
        path: '',
        pathMatch: 'full',
        canActivate: [preferenciasGuard],
        loadComponent: () => import('./features/feed/feed-page').then((m) => m.FeedPage),
      },
    ],
  },
  { path: '**', redirectTo: '' },
];
