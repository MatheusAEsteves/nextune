# Nextune - contrato da API (para o front)

Base: `http://localhost:8080`. JSON em UTF-8. Ids são UUID (gêneros usam número). Datas em ISO-8601.
**Todas as rotas exigem login**, exceto `POST /usuarios`, `GET /generos` e `GET /uploads/**`. As marcadas com 👑 exigem ADMIN.

## Convenções

- **Erros** (qualquer status >= 400) vêm sempre assim; `campos` só tem conteúdo em erros de validação (400):
  ```json
  { "timestamp": "...", "status": 400, "erro": "Bad Request", "mensagem": "Dados inválidos",
    "campos": { "titulo": "Campo obrigatório" } }
  ```
  Mostre `campos[nomeDoCampo]` ao lado do input e `mensagem` em um aviso geral.
- **Atualizações (PUT) são parciais:** campo ausente/`null` = mantém; texto vazio `""` = apaga (bio, fotoPerfil, links, descrição).
- **Arquivos** (áudio, capa, foto) vêm como caminho, ex. `/uploads/audio/abc.mp3`. URL completa: `apiUrl + caminho`.
  O `<audio>` aceita avançar/voltar (HTTP Range).
- **Paginação:** `?pagina=0&tamanho=20` (máx. 50). Resposta:
  `{ conteudo: [...], pagina, tamanho, totalElementos, totalPaginas }`.
- **Login (HTTP Basic):** não existe endpoint de login. Em toda requisição envie o header
  `Authorization: Basic base64(email:senha)` e também `X-Requested-With: XMLHttpRequest`
  (sem ele o navegador abre a janelinha nativa de login quando vem um 401).
  Para **validar o login**, chame `GET /usuarios/me`: 200 = credenciais corretas, 401 = e-mail ou senha errados.
  Guarde as credenciais só em memória (não em localStorage) e crie um interceptor do Angular que coloque o header.
  `<audio>` e `<img>` não enviam header: por isso `/uploads/**` é público.

## Regras de validação (espelhe no formulário)

| Campo | Regra |
|---|---|
| senha | 8 a 72 caracteres |
| URLs (links de artista, `linkMusica`, `fotoPerfil`) | começar com `http://` ou `https://` (ex.: `instagram.com/x` é recusado) |
| titulo do post | obrigatório, até 150 |
| descricao do post | até 5000 |
| generoIds do post | de 1 a 5 gêneros |
| comentário | 1 a 1000 caracteres |
| áudio | mp3, wav, ogg, m4a, aac, flac - até 20 MB |
| capa / foto | jpg, jpeg, png, webp - até 5 MB |

## Endpoints

### Usuário
| Método e rota | Descrição |
|---|---|
| `POST /usuarios` | Cadastro (público). Body: `{nome, email, senha}` |
| `GET /usuarios/me` | Perfil completo (com e-mail) |
| `PUT /usuarios/me` | Body: `{nome?, bio?, fotoPerfil?}` (`fotoPerfil` só URL externa) |
| `POST /usuarios/me/foto` | `multipart/form-data`, campo `foto` |
| `GET /usuarios/{id}` | Perfil público (sem e-mail) |
| `GET /usuarios/me/seguindo` | Artistas que sigo (paginado) |

### Artista
| Método e rota | Descrição |
|---|---|
| `POST /artistas/ativar` | Vira artista. Body: `{nomeArtistico, instagramUrl?, spotifyUrl?, youtubeUrl?}` |
| `GET /artistas/me` | Meu perfil de artista (404 se ainda não é artista) |
| `PUT /artistas/me` | Mesmos campos, todos opcionais |
| `GET /artistas/{id}` | Perfil do artista |
| `GET /artistas/{id}/posts` | Músicas do artista (paginado) |
| `GET /artistas/me/posts` | Minhas músicas (paginado) |
| `PUT /artistas/{id}/seguir` | Seguir (204, idempotente) |
| `DELETE /artistas/{id}/seguir` | Deixar de seguir (204) |
| `GET /artistas/{id}/seguidores` | `{ total, seguindo }` |

### Gêneros e preferências
| Método e rota | Descrição |
|---|---|
| `GET /generos` | Lista `[{id, nome}]` em ordem alfabética |
| `GET /preferencias/me` | Gêneros do usuário com pontuação. **Lista vazia = mostrar o onboarding** |
| `PUT /preferencias/me` | Onboarding. Body: `{generoIds: [1,2,3]}` |

### Posts (músicas)
| Método e rota | Descrição |
|---|---|
| `POST /posts` | Só artista. `multipart/form-data`: `titulo`, `descricao?`, `linkMusica`, `generoIds` (repetir o campo), `audio`, `capa` |
| `GET /posts/{id}` | Detalhe |
| `PUT /posts/{id}` | Só dono. Body: `{titulo?, descricao?, linkMusica?, generoIds?}` |
| `PUT /posts/{id}/capa` | Só dono. `multipart/form-data`, campo `capa` |
| `DELETE /posts/{id}` | Dono ou ADMIN (204) |

`PostResponse`: `id, titulo, descricao, arquivoAudio, capa, linkMusica, dataPublicacao, artista{id,nomeArtistico}, generos[{id,nome}], totalCurtidas`.

### Feed e interações (tela de swipe)
| Método e rota | Descrição |
|---|---|
| `GET /feed?limite=10&excluir=id1,id2` | Próximos cards (máx. 20). Não traz posts já vistos nem os próprios. `excluir` = ids que o front já tem na fila |
| `POST /interacoes` | `{postId, curtido?, salvo?, pulou?, segundosOuvidos?}`. Direita: `curtido:true`. Esquerda: `pulou:true`. Desfazer: mandar `false` |
| `GET /interacoes/curtidas` | Músicas curtidas (paginado) |
| `GET /interacoes/salvos` | Músicas salvas (paginado) |

**Fluxo recomendado do swipe**
1. Ao logar: `GET /preferencias/me`; se vier vazio, tela de gêneros -> `PUT /preferencias/me`.
2. `GET /feed?limite=10` e montar a fila de cards.
3. A cada swipe, `POST /interacoes` (**inclusive o "pular"**, senão o card volta no próximo feed).
4. Quando restarem ~3 cards, `GET /feed?limite=10&excluir=<ids que ainda estão na fila>`.
5. Feed vazio = acabaram as músicas novas (mostrar estado vazio).

Um `POST /interacoes` só com `postId` registra o post como "visto".

### Comentários
| Método e rota | Descrição |
|---|---|
| `POST /posts/{postId}/comentarios` | Body: `{conteudo}` |
| `GET /posts/{postId}/comentarios` | Mais recentes primeiro (paginado) |
| `DELETE /comentarios/{id}` | Autor ou ADMIN (204) |

### Denúncias
| Método e rota | Descrição |
|---|---|
| `POST /denuncias` | Body: `{postId` **ou** `artistaId, motivo, descricao?}` (um alvo só). 409 se já houver denúncia pendente sua para o mesmo alvo |
| 👑 `GET /admin/denuncias?status=PENDENTE` | `status`: PENDENTE, ANALISADA, RESOLVIDA (opcional) |
| 👑 `PATCH /admin/denuncias/{id}/status` | Body: `{status}` |

## Segurança (já configurada em `SecurityConfig`)

- HTTP Basic com e-mail e senha (`CustomUserDetailService`), senha com BCrypt, CSRF desligado (API sem sessão).
- CORS liberado para as origens de `nextune.cors.allowed-origins` (padrão `http://localhost:4200`).
- Públicas: `OPTIONS` (preflight), `POST /usuarios`, `GET /generos`, `GET /uploads/**`. `/admin/**` só ADMIN.
