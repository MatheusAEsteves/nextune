import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AuthService } from '../services/auth.service';
import { IGNORAR_401 } from './http-tokens';

/**
 * Para toda chamada à nossa API: manda o X-Requested-With (evita o popup nativo de login do navegador
 * em um 401) e o Authorization Basic da sessão. Em 401 fora do login, encerra a sessão.
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  if (!req.url.startsWith(environment.apiUrl)) {
    return next(req);
  }

  const auth = inject(AuthService);
  const headers: Record<string, string> = { 'X-Requested-With': 'XMLHttpRequest' };
  if (!req.headers.has('Authorization') && auth.authorization) {
    headers['Authorization'] = auth.authorization;
  }

  return next(req.clone({ setHeaders: headers })).pipe(
    catchError((erro: unknown) => {
      if (erro instanceof HttpErrorResponse && erro.status === 401 && !req.context.get(IGNORAR_401)) {
        auth.sair();
      }
      return throwError(() => erro);
    }),
  );
};
