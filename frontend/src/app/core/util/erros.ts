import { HttpErrorResponse } from '@angular/common/http';
import { ErroApi } from '../models/api.models';

export interface ErroTratado {
  geral: string;
  campos: Record<string, string>;
}

/** Converte qualquer erro em mensagem para a tela, no formato { mensagem, campos } da API. */
export function mensagemDeErro(erro: unknown): ErroTratado {
  if (erro instanceof HttpErrorResponse) {
    if (erro.status === 0) {
      return { geral: 'Não foi possível conectar ao servidor. Verifique se a API está no ar.', campos: {} };
    }
    const corpo = erro.error as Partial<ErroApi> | null;
    return {
      geral: corpo?.mensagem ?? 'Algo deu errado. Tente novamente.',
      campos: corpo?.campos ?? {},
    };
  }
  return { geral: 'Algo deu errado. Tente novamente.', campos: {} };
}
