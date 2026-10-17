# Revisão: backend em microsserviços Micronaut

Spec: `docs/specs/backend-micronaut/spec.md` · Base: `3877a73` (branch `feature/backend-micronaut`) · Código revisado: árvore de trabalho ao fim da tarefa 10, ainda sem commit · PR: sem PR
Data: 2026-10-16 · Revisor: o agente que escreveu o código (revisão do próprio código, feita a partir do diff e da spec)

## 1. Resumo

Parecer: aprovado. Nenhum apontamento bloqueante; os importantes foram corrigidos. O AP-05 foi mantido por decisão do autor, e a conferência no navegador foi feita por ele (2026-10-17).

| Severidade | Encontrados | Corrigidos | Aguardando decisão | Não procede |
| --- | --- | --- | --- | --- |
| Bloqueante | 0 | 0 | 0 | 0 |
| Importante | 3 | 3 | 0 | 0 |
| Menor | 2 | 1 | 1 | 0 |

## 2. O que foi revisado

- Arquivos: `microsservicos/` inteiro (quatro builds novos), `frontend/ts/api/http.ts`, `frontend/ts/api/api.ts`, `frontend/.gitignore`, `CHANGELOG.md`, `docs/specs/backend-micronaut/`. Nenhum arquivo do backend antigo (`src/`, `microservico-cadastro/`, `lib/`) foi alterado até aqui.
- Verificações rodadas:
  - `./gradlew clean test` em cada MS: `ms-usuarios` 175, `ms-competencias` 68, `ms-vagas` 42, `ms-curtidas` 49, ou seja, **334 testes, 0 falhas**.
  - `npx tsc --noEmit` em `frontend/`: sem erros.
  - Conferência manual com o PostgreSQL local e `curl` em cada tarefa (registrada no plano).
  - Integração com os quatro MS e o Vite no ar, simulando o navegador com `Origin` e preflight.
  - `MICRONAUT_SERVER_PORT=8094` subiu uma segunda instância na porta indicada.
  - Buscas por `println`, chamadas de log, `senha` nos mapas de resposta, domínios reais nos testes e SQL montado com valor.
  - Script que confere os dígitos verificadores de todo CPF e CNPJ nos testes.
- Não verificado: as telas no navegador (CA-11 a CA-13, parte visual), que dependem do autor; a remoção do backend antigo e a documentação (tarefas 11 e 12, ainda não feitas).

## 3. Aderência à spec

| Item | Onde está implementado | Onde está verificado | Veredito |
| --- | --- | --- | --- |
| RN-01 | `microsservicos/ms-*` com build, wrapper e `.gitignore` próprios; nenhum `import` entre MS | leitura (`grep org.linketinder.` por MS) | atende |
| RN-02 | `application.yml` de cada MS (8081–8084) | manual: as quatro portas e `MICRONAUT_SERVER_PORT=8094` | atende |
| RN-03 | `banco/ConexaoBanco` sobre `DataSource`; SQL com `RETURNING` e `ON CONFLICT` | manual com o PostgreSQL | atende |
| RN-04 | `ms-curtidas/.../ParticipanteDAO`, `ms-usuarios/.../VagaDAO`, `ms-vagas/.../EmpresaDAO` | leitura: nenhum `HttpClient` em `src/main` | atende |
| RN-05 | `application.yml` + `VerificadorDeAmbiente` | `VerificadorDeAmbienteSpec` e `run` manual sem a senha | atende |
| RN-06 | pacotes `http`, `service`, `repositorio`, `dao`, `banco` | leitura | atende |
| RN-07 | controllers dos quatro MS | specs de rota + `curl` comparando com o contrato do README | atende |
| RN-08 | `ProcessadorDeErros`, tratadores, `CorpoJson` | `ErrosDoServidorSpec` (×4) | atende |
| RN-09 | `TradutorDeErroDeBanco` nos DAOs de candidato e empresa; `RecrutadorService` | specs + `curl` (`409` e `400`) | atende |
| RN-10 | `micronaut.server.cors` em cada `application.yml` | `CorsSpec` (×4) + `curl` | **atende em parte** (AP-01, AP-02) |
| RN-11 | `RegistroDeRequisicoes`, `RegistroDoServidorEmLog`, `TratadorDeFalhaDeBanco` | `RegistroDeRequisicoesSpec` + `grep` nos logs das conferências (0 linhas com dado pessoal) | atende |
| RN-12 a RN-16 | `ms-usuarios` | specs (175 testes) + `curl` | atende |
| RN-17 a RN-19 | `ms-competencias` | specs (68) + `curl` | atende |
| RN-20, RN-21 | `ms-vagas` | specs (42) + `curl` | atende (AP-05 é sugestão fora da spec) |
| RN-22, RN-23 | `ms-curtidas` | specs (49) + `curl` com match e `id_recrutador` | atende |
| RN-24 | `frontend/ts/api/http.ts`, `api.ts` | `tsc` + integração por `curl` | atende; navegador pendente |
| RN-25 | — | — | tarefa 11, não feita |
| RN-26 | specs com `@MockBean` e `datasources.default.enabled: false` | suítes rodam sem banco | **atende em parte** (AP-03) |
| CA-01, CA-02 | — | manual | atende |
| CA-03 a CA-10 | — | specs + manual | atende |
| CA-11, CA-12 | — | integração por `curl`; navegador pendente | não verificável por completo |
| CA-13 | — | specs (sem `senha`, sem CPF/data do recrutador) + `grep` nos logs; telas pendentes | atende em parte (telas com o autor) |
| CA-14 | — | — | tarefa 11, não feita |
| CA-15 | — | 334 testes, `tsc` limpo | atende |

Mudanças do diff sem regra ou tarefa correspondente: nenhuma. O `package-lock.json` alterado pelo `npm install` foi revertido na tarefa 10.

## 4. Princípio do Anonimato

| Saída | Quem vê | Momento | Campos que saem | Conforme? | Como foi verificado |
| --- | --- | --- | --- | --- | --- |
| `GET /candidatos`, `GET /candidatos/{id}`, `POST`/`PUT /candidatos` | qualquer cliente | qualquer | cadastro sem `senha` (pendência replicada) | sim, conforme decisão do autor | `CandidatoControllerSpec` (conjunto exato de chaves) |
| `GET`/`POST`/`PUT /empresas` | qualquer cliente | qualquer | cadastro sem `senha` (pendência replicada) | sim, conforme decisão | `EmpresaControllerSpec` |
| `POST /competencias` | quem chamou | qualquer | cadastro do candidato sem `senha` (pendência replicada) | sim, conforme decisão | `CompetenciasDoCandidatoControllerSpec`; o `CandidatoDAO` do MS nem lê a coluna `senha` |
| Rotas de recrutador | a empresa | qualquer | `id`, `nome`, `area`, `descricao`, `email`, `idEmpresa` | sim | `RecrutadorControllerSpec` (chaves exatas, sem CPF nem data); o DAO não lê essas colunas |
| `GET /vagas`, rotas de vagas do recrutador | qualquer cliente | qualquer | vaga com `empresa: {id, nome}` (pendência replicada) | sim, conforme decisão | `VagaControllerSpec` |
| `POST /login` | quem entrou | — | `tipo`, `id`, `nome` (+ `idEmpresa`) | sim | `LoginControllerSpec` (igualdade exata do corpo) |
| `POST /curtidas` | quem curtiu | antes do match | só `match` | sim | `CurtidaControllerSpec` (igualdade exata) |
| Erros `400`/`401`/`409` | quem chamou | — | campo e motivo; nunca o valor | sim | specs com `!mensagem.contains(valor)` |
| Logs | quem opera | — | método, modelo da rota, status, SQLState | sim | `RegistroDeRequisicoesSpec` + `grep` nos logs das conferências |

- Senha em saída, JSON, log ou mensagem: não encontrada.
- Curtida revelada antes do match: não. A resposta é só `{"match": ...}` e não diz se o outro lado foi a empresa ou um recrutador.
- Pendências conhecidas ampliadas ou copiadas: as replicadas por decisão do autor e os `PUT`/`DELETE` sem autenticação (ampliação autorizada). Nenhuma outra.

## 5. Apontamentos

### AP-01 — `Vary: Origin` só aparece quando a requisição traz `Origin`

- **Categoria:** Spec
- **Severidade:** Importante
- **Local:** `microsservicos/ms-*/src/main/resources/application.yml` (configuração de CORS do Micronaut)
- **Regra:** RN-10 ("toda resposta leva `Vary: Origin`")
- **Evidência:** `curl http://localhost:8082/competencias` sem `Origin` responde `200` sem `Vary`; com `Origin` responde com `Vary: Origin`. A API antiga mandava `Vary: Origin` em toda resposta (`CorsFilter`).
- **Correção sugerida:** filtro de resposta em cada MS que acrescenta `Vary: Origin` a toda resposta, com teste.
- **Situação:** corrigido
- **O que foi corrigido:**
  - novo `http/CabecalhoVaryOrigin` nos quatro MS, que acrescenta `Vary: Origin` quando ele ainda não está na resposta;
  - teste "toda resposta leva Vary: Origin, uma vez só" na `CorsSpec` de cada MS, que falhou antes do filtro e passou depois;
  - conferido no servidor real: um `POST` sem `Origin` respondeu com `Vary: Origin`.

### AP-02 — MS Curtidas anuncia métodos que não usa

- **Categoria:** Spec
- **Severidade:** Importante
- **Local:** `microsservicos/ms-curtidas/src/main/resources/application.yml` (`allowed-methods`)
- **Regra:** RN-10 ("os métodos anunciados são os que o MS usa")
- **Evidência:** `allowed-methods: [GET, POST, PUT, DELETE]`, mas a única rota é `POST /curtidas`.
- **Correção sugerida:** anunciar só `POST` e ajustar o `CorsSpec` do MS para usar uma rota `POST`.
- **Situação:** corrigido
- **O que foi corrigido:**
  - `allowed-methods: [POST]` no `application.yml` do MS Curtidas;
  - a `CorsSpec` do MS foi reescrita para `POST`, com um teste de preflight de `GET` para `/teste/{id}`, uma rota que aceita `GET`, para que a recusa venha da configuração de CORS e não da falta de rota. O teste falhou antes da correção e passou depois. Um primeiro rascunho, que pedia `GET` em `/teste`, passava mesmo sem a correção, porque ali não há rota `GET`, e foi descartado;
  - conferido no servidor real: preflight de `POST` `200` com `Access-Control-Allow-Methods: POST`, de `GET` `403`.

### AP-03 — CPF com dígito verificador válido nos testes

- **Categoria:** Testes
- **Severidade:** Importante
- **Local:**
  - `ms-usuarios/src/test/groovy/org/linketinder/usuarios/DadosFicticios.groovy` (`recrutador`: `987.654.321-00`), repetido no `RecrutadorControllerSpec`;
  - `ms-usuarios/.../service/ValidadorDeRecrutadorSpec.groovy` (`12345678909`, herdado da spec antiga).
- **Regra:** RN-26 e as Restrições do `AGENTS.md`: documentos de teste com dígitos verificadores inválidos.
- **Evidência:** o script de conferência marcou os dois como válidos.
- **Correção sugerida:** trocar por números com dígito verificador inválido e rodar o script de novo.
- **Situação:** corrigido
- **O que foi corrigido:** `987.654.321-00` virou `987.654.321-11`, e `12345678909` virou `12345678900`. O script, rodado de novo sobre todos os testes dos quatro MS, respondeu "nenhum documento com dígito verificador válido".

### AP-04 — Infraestrutura copiada sem uso

- **Categoria:** Convenções
- **Severidade:** Menor
- **Local:** `TradutorDeErroDeBanco` (e a spec dele) em `ms-vagas` e `ms-curtidas`; `Transacao` e `TransacaoJdbc` (e a spec) em `ms-curtidas`.
- **Regra:** Clean Code do `AGENTS.md` (nada de código morto)
- **Evidência:** nenhuma classe de `src/main` desses MS usa essas classes.
- **Correção sugerida:** remover; mover para uma `ConexaoBancoSpec` o teste de `ConexaoBanco` fora de transação que estava na `TransacaoJdbcSpec` do `ms-curtidas`.
- **Situação:** corrigido
- **O que foi corrigido:**
  - removidos `TradutorDeErroDeBanco` (e a spec) do `ms-vagas` e do `ms-curtidas`, e `Transacao`, `TransacaoJdbc` (e a spec) do `ms-curtidas`;
  - o `ConexaoBanco` do `ms-curtidas` perdeu a parte de transação, que só a `TransacaoJdbc` usava;
  - nova `ConexaoBancoSpec` no `ms-curtidas`.

### AP-05 — Vaga com campo maior que a coluna responde `500`

- **Categoria:** Spec (sugestão)
- **Severidade:** Menor
- **Local:** `ms-vagas/.../service/ValidadorDeVaga.groovy`
- **Regra:** nenhuma violada. A spec decidiu manter a validação atual da vaga, e a API antiga também respondia `500`.
- **Evidência:** `estado` é `varchar(2)`; `"estado": "Mato Grosso do Sul"` chega ao banco e vira `500` genérico.
- **Correção sugerida:** validar tamanho (e sigla de 2 letras) com `400`, como no cadastro de candidato. Isso muda a spec, então a decisão é do autor.
- **Situação:** mantido como está, por decisão do autor (2026-10-17): a vaga segue sem validação de tamanho, como a API antiga. Registrado no `AGENTS.md`

## 6. Depois das correções

- Verificações rodadas de novo:
  - `./gradlew clean test` em cada MS: `ms-usuarios` 176, `ms-competencias` 69, `ms-vagas` 41, `ms-curtidas` 45, ou seja, **331 testes, 0 falhas**. O total caiu em relação aos 334 porque saíram as specs das classes removidas no AP-04 e entraram os testes novos de `Vary` e de CORS;
  - o script de documentos não encontrou nenhum documento válido.
- Itens repassados: RN-10 (`Vary` e métodos anunciados), RN-26 (dados fictícios), RN-11 (o log continua só com método, rota e status), CA-12 (CORS no servidor real).
- Fica para o autor:
  - decidir o AP-05;
  - conferir no navegador os fluxos de candidato, empresa e recrutador (CA-11 a CA-13), com os quatro MS e o Vite no ar;
  - autorizar a remoção do backend antigo (tarefa 11), que o plano condiciona a essa conferência.

Respostas do autor (2026-10-17): AP-05 mantido como está; conferência no navegador feita; tarefa 11 liberada.
