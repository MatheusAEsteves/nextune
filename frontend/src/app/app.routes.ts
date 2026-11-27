import { Routes } from '@angular/router';
import { adminGuard, authGuard, preferenciasGuard, visitanteGuard } from './core/guards/auth.guard';

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
      {
        path: 'curtidas',
        data: { tipo: 'curtidas' }, // vira o input "tipo" (withComponentInputBinding)
        loadComponent: () => import('./features/biblioteca/biblioteca-page').then((m) => m.BibliotecaPage),
      },
      {
        path: 'salvos',
        data: { tipo: 'salvos' },
        loadComponent: () => import('./features/biblioteca/biblioteca-page').then((m) => m.BibliotecaPage),
      },
      {
        path: 'post/:id',
        loadComponent: () => import('./features/post/post-page').then((m) => m.PostPage),
      },
      {
        path: 'post/:id/editar',
        loadComponent: () => import('./features/post-editar/post-editar-page').then((m) => m.PostEditarPage),
      },
      {
        path: 'artista/:id',
        loadComponent: () => import('./features/artista/artista-page').then((m) => m.ArtistaPage),
      },
      {
        path: 'perfil',
        loadComponent: () => import('./features/perfil/perfil-page').then((m) => m.PerfilPage),
      },
      {
        path: 'estudio',
        loadComponent: () => import('./features/estudio/estudio-page').then((m) => m.EstudioPage),
      },
      {
        path: 'publicar',
        loadComponent: () => import('./features/publicar/publicar-page').then((m) => m.PublicarPage),
      },
      {
        path: 'admin/denuncias',
        canActivate: [adminGuard],
        loadComponent: () => import('./features/admin/admin-denuncias-page').then((m) => m.AdminDenunciasPage),
      },
    ],
  },
  { path: '**', redirectTo: '' },
];
