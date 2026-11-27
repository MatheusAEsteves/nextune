export const AUDIO_EXTENSOES = ['mp3', 'wav', 'ogg', 'm4a', 'aac', 'flac'];
export const AUDIO_MAX_BYTES = 20 * 1024 * 1024;
export const IMAGEM_EXTENSOES = ['jpg', 'jpeg', 'png', 'webp'];
export const IMAGEM_MAX_BYTES = 5 * 1024 * 1024;

/** Mesmas regras do backend (ArmazenamentoService); devolve a mensagem de erro ou null se estiver ok. */
export function validarArquivo(arquivo: File, extensoes: string[], maxBytes: number, rotulo: string): string | null {
  const extensao = arquivo.name.split('.').pop()?.toLowerCase() ?? '';
  if (!extensoes.includes(extensao)) return `${rotulo}: use ${extensoes.join(', ')}.`;
  if (arquivo.size > maxBytes) return `${rotulo}: máximo de ${Math.round(maxBytes / 1048576)} MB.`;
  return null;
}

/** O backend só aceita links que começam com http:// ou https://. */
export function urlValida(valor: string): boolean {
  return /^https?:\/\/\S+$/i.test(valor.trim());
}

export function tempoRelativo(iso: string): string {
  const segundos = Math.max(0, Math.floor((Date.now() - new Date(iso).getTime()) / 1000));
  if (segundos < 60) return 'agora';
  const minutos = Math.floor(segundos / 60);
  if (minutos < 60) return `há ${minutos} min`;
  const horas = Math.floor(minutos / 60);
  if (horas < 24) return `há ${horas} h`;
  const dias = Math.floor(horas / 24);
  if (dias < 7) return `há ${dias} d`;
  return new Date(iso).toLocaleDateString('pt-BR');
}
