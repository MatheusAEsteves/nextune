// Espelha os DTOs do backend (ver backend/API.md). Datas chegam como string ISO-8601.

export interface Genero {
  id: number;
  nome: string;
}

export interface Usuario {
  id: string;
  nome: string;
  email?: string | null; // só vem no perfil próprio (/usuarios/me)
  fotoPerfil: string | null;
  bio: string | null;
  dataCriacao: string;
}

export interface ArtistaResumo {
  id: string;
  nomeArtistico: string;
}

export interface Post {
  id: string;
  titulo: string;
  descricao: string | null;
  arquivoAudio: string; // caminho: /uploads/audio/x.mp3
  capa: string; // caminho: /uploads/capas/x.jpg
  linkMusica: string;
  dataPublicacao: string;
  artista: ArtistaResumo;
  generos: Genero[];
  totalCurtidas: number;
}

export interface Preferencia {
  generoId: number;
  genero: string;
  pontuacao: number;
}

export interface InteracaoRequest {
  postId: string;
  curtido?: boolean;
  salvo?: boolean;
  pulou?: boolean;
  segundosOuvidos?: number;
}

export interface Interacao {
  postId: string;
  curtido: boolean;
  salvo: boolean;
  pulou: boolean;
  segundosOuvidos: number;
}

export interface Seguidores {
  total: number;
  seguindo: boolean;
}

export interface Pagina<T> {
  conteudo: T[];
  pagina: number;
  tamanho: number;
  totalElementos: number;
  totalPaginas: number;
}

export interface ErroApi {
  timestamp: string;
  status: number;
  erro: string;
  mensagem: string;
  campos?: Record<string, string>;
}
