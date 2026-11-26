// URL base da API (Spring Boot). Arquivos enviados (áudio, capa, foto) vêm como "/uploads/...":
// monte a URL completa com `${environment.apiUrl}${caminho}`.
export const environment = {
  apiUrl: 'http://localhost:8080'
};
