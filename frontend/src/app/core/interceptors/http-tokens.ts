import { HttpContextToken } from '@angular/common/http';

/** Requests marcadas com este token não derrubam a sessão em caso de 401 (ex.: tentativa de login). */
export const IGNORAR_401 = new HttpContextToken<boolean>(() => false);
