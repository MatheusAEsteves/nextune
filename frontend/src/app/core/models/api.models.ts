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

export interface Artista {
  id: string;
  usuarioId: string;
  nomeArtistico: string;
  instagramUrl: string | null;
  spotifyUrl: string | null;
  youtubeUrl: string | null;
}

export interface UsuarioPublico {
  id: string;
  nome: string;
  fotoPerfil: string | null;
  bio: string | null;
  dataCriacao: string;
}

export interface Autor {
  id: string;
  nome: string;
  fotoPerfil: string | null;
}

export interface Comentario {
  id: string;
  postId: string;
  autor: Autor;
  conteudo: string;
  dataComentario: string;
}

export type DenunciaStatus = 'PENDENTE' | 'ANALISADA' | 'RESOLVIDA';

export interface Denuncia {
  id: string;
  usuarioId: string;
  postId: string | null;
  artistaId: string | null;
  motivo: string;
  descricao: string | null;
  status: DenunciaStatus;
  dataDenuncia: string;
}

export interface DenunciaRequest {
  postId?: string;
  artistaId?: string;
  motivo: string;
  descricao?: string;
}

/** Atualização parcial do perfil: ausente = mantém, "" = apaga (bio, fotoPerfil). */
export interface PerfilUpdate {
  nome?: string;
  bio?: string;
  fotoPerfil?: string;
}

export interface ArtistaWrite {
  nomeArtistico: string;
  instagramUrl: string | null;
  spotifyUrl: string | null;
  youtubeUrl: string | null;
}

export interface PostUpdate {
  titulo?: string;
  descricao?: string;
  linkMusica?: string;
  generoIds?: number[];
}
