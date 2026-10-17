# Changelog

Todas as mudanças relevantes do Linketinder são registradas neste arquivo.

O formato segue o [Keep a Changelog](https://keepachangelog.com/pt-BR/1.1.0/).

## [Não lançado]

### Documentado
- Prompts da feature do recrutador no `PROMPTS.md` (etapas 9 a 15), reconstruídos
  a partir da spec, do plano, do `revisao.md` e dos commits, porque a sessão
  original do agente se perdeu; o arquivo passou a separar as duas features.
- Sincronização do `AGENTS.md` e do contrato da API no `README.md` com o login,
  as rotas de competências e de curtidas, o CORS e o frontend integrado à API
  (revisão de 2026-10-13). As três features abaixo entraram sem spec em
  `docs/specs/`, onde só existe `microservico`.
- Sincronização da documentação do microsserviço com o estado atual do código e do repositório, incluindo `AGENTS.md`, `docs/specs/microservico/spec.md` e `docs/specs/microservico/plano.md` (revisão de 2026-10-13).

### Adicionado
- MS Curtidas em Micronaut (`microsservicos/ms-curtidas`, porta 8084) com
  `POST /curtidas` nos três sentidos e criação do match pelo mesmo `INSERT ...
  SELECT` da API antiga; a resposta traz só `match` e o serviço consulta vagas e
  recrutadores direto no banco, sem chamar outro microsserviço
  (spec `backend-micronaut`, tarefa 9).
- MS Vagas em Micronaut (`microsservicos/ms-vagas`, porta 8083) com CRUD de vagas:
  competências resolvidas sem diferenciar maiúsculas, `PUT` que troca as
  competências sem mudar a empresa dona e `DELETE` que apaga competências,
  curtidas, matches e atribuições da vaga numa transação
  (spec `backend-micronaut`, tarefa 8).
- `POST /login` no MS Usuários para candidato, empresa e recrutador, com `401`
  genérico e resposta restrita a tipo, id, nome (e `idEmpresa` do recrutador);
  a senha é comparada exatamente como enviada (spec `backend-micronaut`, tarefa 7).
- Recrutadores e atribuição de vagas no MS Usuários, com o mesmo contrato da API
  antiga e a nova atualização por `PUT /recrutadores/{id}`, que não troca a
  empresa; as consultas não carregam CPF, data de nascimento nem senha
  (spec `backend-micronaut`, tarefa 6).
- CRUD de empresas no MS Usuários, com a mesma validação e o `409` do cadastro e
  recusa da exclusão de empresa com vagas ou recrutadores
  (spec `backend-micronaut`, tarefa 5).
- MS Usuários em Micronaut (`microsservicos/ms-usuarios`, porta 8081) com CRUD
  de candidatos: validação rígida do cadastro portada do `microservico-cadastro`,
  `409` genérico em cadastro repetido, `PUT` sem mexer nas competências e
  `DELETE` apagando competências, curtidas e matches numa transação
  (spec `backend-micronaut`, tarefa 4).
- `POST /competencias` no MS Competências: soma competências ao candidato numa
  única transação, reaproveitando a já cadastrada em qualquer caixa, e mantém a
  resposta `201` com o cadastro do candidato sem a senha
  (spec `backend-micronaut`, tarefa 3).
- CRUD do catálogo de competências no MS Competências (`GET /competencias`,
  `POST /competencias/catalogo`, `PUT` e `DELETE /competencias/{id}`), com nome
  repetido em qualquer caixa respondendo `409` e recusa de exclusão de
  competência ligada a candidato ou vaga (spec `backend-micronaut`, tarefa 2).
- Esqueleto do MS Competências em Micronaut (`microsservicos/ms-competencias`):
  build próprio, erros no formato `{"erro"}`, CORS, log sem dados pessoais,
  conexão por `DataSource` e senha do banco só por variável de ambiente
  (spec `backend-micronaut`, tarefa 1).
- Estrutura do recrutador no banco: tabelas `recrutadores`,
  `recrutador_vaga` e `curtida_recrutador_candidato`, mais a coluna
  `matches.id_recrutador` e carga fictícia para as atribuições e curtidas do
  novo perfil.
- Modelo do recrutador e enum das áreas de recrutamento, com descrições que
  nunca expõem senha, CPF ou data de nascimento.
- Validação do cadastro de recrutador: campos obrigatórios, área da lista
  fixa, e-mail, CPF, data de nascimento e senha com mensagens que citam o
  campo e não repetem o valor informado.
- Repositório e service do recrutador: cadastro, consulta, autenticação e
  proteção de exclusão de empresa que ainda tem recrutadores ligados a ela.
- Atribuição e desatribuição de vagas aos recrutadores, com validação de
  escopo para impedir atuação fora do conjunto permitido de vagas da empresa.
- Curtida do recrutador em candidatos para vagas atribuídas, preservando o
  fluxo de match e registrando o recrutador que fechou a reciprocidade.
- Persistência JDBC de recrutadores, incluindo cadastro, consultas, atualização,
  exclusão, atribuição de vagas e listagem sem carregar dados pessoais restritos.
- Criação de matches considerando curtidas da empresa ou dos recrutadores, com
  identificação de quem curtiu primeiro, além da limpeza das curtidas e
  atribuições de recrutador ao excluir candidato ou vaga.
- Menu de recrutadores no CLI, com cadastro, atualização, exclusão, atribuição
  e remoção de vagas, listagem das vagas atribuídas e curtida de candidatos.
- Rotas REST de recrutadores para cadastro, listagem, consulta das vagas
  atribuídas, atribuição e remoção de vagas e exclusão; respostas não expõem
  senha, CPF ou data de nascimento, e o CORS anuncia o método `DELETE`.
- Login de recrutador pela API REST com resposta restrita a tipo, id, nome e
  id da empresa, e curtida de candidato pelo recrutador para vaga atribuída.
- Perfil de recrutador no frontend: escolha na entrada, login, sessão tipada com
  `idEmpresa`, tipos e chamadas da API; a checagem TypeScript inclui os fontes
  no diretório `ts/`.
- Tela de recrutadores da empresa com cadastro, contagem por área, atribuição e
  remoção de vagas e exclusão; os dados pessoais restritos não são exibidos.
- Telas do recrutador com listagem das vagas atribuídas e fila de candidatos,
  compartilhando a mesma lógica da área da empresa para curtir e passar sem
  expor dados fora do escopo do perfil.
- Login na API REST: `POST /login` autentica candidato ou empresa e responde
  `tipo`, `id` e `nome`, sem a senha; credenciais erradas respondem `401` com
  mensagem genérica. Os repositórios e DAOs de candidato e empresa ganharam
  `buscarPorEmail` e os services, `autenticar`.
- `CorsFilter` libera o navegador para a origem do frontend
  (`LINKETINDER_FRONTEND_ORIGEM`, padrão `http://localhost:5173`) e responde
  `204` ao `OPTIONS`.
- Rotas de competências na API REST: `GET /competencias` lista as cadastradas e
  `POST /competencias` soma competências a um candidato já cadastrado.
- `POST /curtidas` registra a curtida do candidato na vaga e a da empresa no
  candidato, recusa vaga que não é da empresa e responde se houve match.
- Frontend: acesso à API em `ts/api/http.ts` e `ts/api/api.ts`, sessão no
  `sessionStorage` (`ts/sessao.ts`), proteção das páginas por perfil e cabeçalho
  em `ts/navegacao.ts` e utilitários de DOM em `ts/dom.ts`.
- Telas de escolha de perfil e de login, tela de competências do candidato logo
  após o cadastro, e área da empresa com "Minhas vagas", gráfico de competências
  e os candidatos de cada vaga.
- Fila de cards com os botões curtir e passar (`ts/filaDeCards.ts`), com vagas e
  candidatos em ordem decrescente de afinidade (`ts/afinidade.ts`); candidatos
  com 0% de afinidade ficam fora da fila da vaga.
- Microsserviço de cadastro: aplicação independente em `microservico-cadastro/`,
  com servidor HTTP do JDK na porta 8081
  (spec `microservico`, tarefa 1).
- Validação e regras de cadastro de candidato e empresa no microsserviço
  (spec `microservico`, tarefa 2).
- Rotas `POST /candidatos` e `POST /empresas` do microsserviço, com respostas
  em JSON sem a senha (spec `microservico`, tarefa 3).
- Cadastro de candidatos e empresas gravado no PostgreSQL pelo microsserviço;
  cadastro repetido responde `409` e corpo fora de UTF-8 responde `400`
  (spec `microservico`, tarefa 4).
- Regras e rota `POST /candidatos/{id}/competencias` do microsserviço:
  competências somadas às do candidato, reaproveitando as existentes sem
  diferenciar maiúsculas (spec `microservico`, tarefa 5).
- Competências do candidato gravadas no PostgreSQL pelo microsserviço, em
  transação (spec `microservico`, tarefa 6).
- README descreve o microsserviço de cadastro e a exploração da comunicação
  com o frontend (spec `microservico`, tarefa 7).

### Modificado
- `AGENTS.md` e `README.md` descrevem o backend em quatro microsserviços
  Micronaut: stack, estrutura, como executar e testar, contrato, convenções e
  pendências; a spec `backend-micronaut` fica marcada como implementada
  (spec `backend-micronaut`, tarefa 12).
- O frontend passa a chamar cada microsserviço pela própria URL base
  (`ts/api/http.ts` com um cliente por MS, configurável por `VITE_URL_USUARIOS`,
  `VITE_URL_COMPETENCIAS`, `VITE_URL_VAGAS` e `VITE_URL_CURTIDAS`; a
  `VITE_API_URL` sai), e o `.gitignore` do frontend passa a ignorar `.env`
  (spec `backend-micronaut`, tarefa 10).
- Documentação da feature do recrutador no `AGENTS.md` e no `README.md`: domínio, estrutura,
  regras do recrutador, rotas com `DELETE`, a tabela do Princípio do Anonimato com os três
  perfis e as pendências de senha em texto puro e de integridade das rotas novas
  (spec `recrutador`, tarefa 15).
- Revisão da feature do recrutador registrada em `docs/specs/recrutador/revisao.md`, com o
  checklist de `CA-01` a `CA-20`, os doze apontamentos e a conferência em execução contra
  PostgreSQL, Tomcat e o site (spec `recrutador`, tarefa 15).
- A listagem de recrutadores no CLI passa a mostrar a empresa, como a RN-27 da spec pede
  (spec `recrutador`, tarefa 15).
- O recrutador entra direto na página de vagas atribuídas compartilhada com a empresa, e o
  gráfico de competências volta a ser exclusivo da empresa (spec `recrutador`, tarefa 15).
- Frontend reorganizado: páginas em `frontend/paginas/`, uma tela por módulo em
  `ts/telas/` e `index.html` reduzido à escolha entre candidato e empresa.
- Texto vindo do usuário ou do banco passa a entrar no DOM por `textContent`, e
  não mais por `innerHTML`.
- O gráfico de competências passou para a página "Minhas vagas" da empresa, com
  o Chart.js carregado por CDN em `paginas/minhasVagas.html`.
- `AGENTS.md` passa a descrever o microsserviço de cadastro e suas pendências
  (spec `microservico`, tarefa 8).

### Corrigido
- Os quatro microsserviços passam a mandar `Vary: Origin` em toda resposta,
  e não só quando a requisição traz `Origin` (spec `backend-micronaut`, revisão AP-01).
- O MS Curtidas anuncia só `POST` no CORS, o único método que usa
  (spec `backend-micronaut`, revisão AP-02).
- Os testes do MS Usuários deixam de usar CPF com dígito verificador válido
  (spec `backend-micronaut`, revisão AP-03).
- Sai a infraestrutura copiada e não usada: tradutor de erro de banco no MS Vagas
  e no MS Curtidas, e a transação no MS Curtidas (spec `backend-micronaut`, revisão AP-04).
- Bloco do recrutador em `database/Linketinder.sql` ganha
  `ALTER TABLE matches ADD COLUMN IF NOT EXISTS id_recrutador`, para também poder ser
  aplicado em um banco que já tinha o script antigo carregado (spec `recrutador`, tarefa 15).
- Transação do microsserviço: falha no rollback não esconde mais o erro
  original da gravação (spec `microservico`, revisão AP-01).
- Senha do banco só com espaços é recusada na subida do microsserviço, como a
  senha ausente (spec `microservico`, revisão AP-02).
- DAOs do microsserviço acusam falha de banco quando o `INSERT ... RETURNING`
  não devolve linha (spec `microservico`, revisão AP-03).
- Falha de leitura ou de envio de uma requisição no microsserviço passa a ser
  registrada, só com o tipo da falha (spec `microservico`, revisão AP-04 e
  AP-05).
- Mensagem de e-mail inválido do microsserviço dá o exemplo no domínio
  reservado `example.com` (spec `microservico`, revisão AP-08).

### Removido
- CLI, API de servlets, `microservico-cadastro/`, `lib/` e o build Gradle da
  raiz (`build.gradle`, `settings.gradle`, `gradlew*`, `gradle/`), substituídos
  pelos quatro microsserviços; o `.gitignore` do projeto passa a ignorar `.env`
  (spec `backend-micronaut`, tarefa 11).
- `frontend/paginas/recrutador.html`, página provisória da tarefa 12, substituída pela
  `minhasVagas.html`, que atende empresa e recrutador (spec `recrutador`, tarefa 15).
- `frontend/ts/dados.ts` e a gravação de cadastros e senhas no `localStorage` do
  navegador: o frontend lê e grava pela API REST.
- Páginas `candidato.html`, `empresa.html` e `vaga.html` da raiz do `frontend/` e
  os módulos `ts/telaCandidato.ts`, `ts/telaEmpresa.ts` e `ts/telaVaga.ts`,
  substituídos pelas páginas de `frontend/paginas/` e pelas telas de `ts/telas/`.
- Seleção de "quem está vendo" por `<select>` no frontend, substituída pelo login.
