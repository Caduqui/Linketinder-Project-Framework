# Revisão: recrutador

Spec: `docs/specs/recrutador/spec.md` · Plano: `plano.md` · Base: `main` (`15e7d1a`) · Branch revisada: `task-rewrite` · Commit revisado: `64d1ee3`
Data: 2026-10-14 · Revisor: agente de IA (Claude Code), o mesmo tipo de agente que escreveu o código da feature

**Por que este registro existe.** A tarefa 15 do plano ("Regressão e documentação") estava marcada como concluída, mas o `AGENTS.md` havia recebido apenas um parágrafo de cinco linhas, o `README.md` ficou se contradizendo sobre o `DELETE`, o `CHANGELOG.md` não tinha a entrada da tarefa e este arquivo não existia. A revisão foi pedida pelo autor como uma conferência entre a spec, o plano e o código; os apontamentos foram registrados antes de serem corrigidos, e as correções entram no mesmo commit deste registro.

## 1. Resumo

Parecer: **aprovado com pendências**. Nenhum apontamento é bloqueante e nenhum viola o Princípio do Anonimato. Doze apontamentos foram encontrados: oito corrigidos aqui e quatro postergados por decisão do autor (são melhorias de qualidade, sem efeito em comportamento).

A revisão teve duas passadas na mesma data. A primeira leu o código e corrigiu os apontamentos. A segunda **rodou** o código contra PostgreSQL 18, Tomcat 9.0.122 e o Vite (seção 7): 94 asserções de execução, nenhuma falha, nenhum defeito novo. Com isso `CA-17` a `CA-19` passaram de pendentes a verificados, `CA-11` a `CA-16` deixaram de depender só de leitura, e o `CA-20` ficou parcial — falta apenas a passada visual no navegador, que a sessão não tinha como fazer.

| Severidade | Encontrados | Corrigidos | Postergados pelo autor | Não procede |
| --- | --- | --- | --- | --- |
| Bloqueante | 0 | 0 | 0 | 0 |
| Importante | 4 | 4 | 0 | 0 |
| Menor | 8 | 4 | 4 | 0 |

## 2. O que foi revisado

- **Arquivos:** `git diff --stat main...HEAD` mostra 77 arquivos, 4524 linhas a mais e 111 a menos, em 14 commits `feat(recrutador)` mais dois de documentação.
- **Lido por inteiro:** `model/Recrutador`, `model/AreaDeRecrutamento`, `model/Match`, `service/ValidadorDeRecrutador`, `service/RecrutadorService`, `service/CurtidaService`, `dao/RecrutadorDAO`, `dao/MatchDAO`, `api/RecrutadorServlet`, `api/Json`, `api/ApiServlet`, `api/LoginServlet`, `api/CurtidaServlet`, `api/Dependencias`, `controller/RecrutadorController`, `view/RecrutadorView`, o bloco do recrutador em `database/Linketinder.sql`, e no frontend `ts/navegacao.ts`, `ts/sessao.ts`, `ts/api/http.ts`, `ts/telas/telaRecrutadores.ts`, `ts/telas/telaMinhasVagas.ts`, `ts/telas/telaCandidatosDaVaga.ts` e as páginas tocadas pela feature.
- **Lido em parte:** `Main.groovy`, `MenuController`, `CurtidaController`, `CurtidaDAO`, `CandidatoDAO`, `VagaDAO`, `EmpresaService` — só os trechos do recrutador. Das specs Spock foram lidos os nomes de todos os testes e os trechos citados neste registro, não cada arquivo inteiro.
- **Verificações rodadas** (2026-10-14):
  - `./gradlew test --rerun-tasks` na raiz, antes das correções — **193 testes, nenhuma falha, nenhum erro**;
  - `./gradlew test` na raiz, depois das correções — **193 testes, nenhuma falha, nenhum erro**;
  - `npx tsc --noEmit` em `frontend/`, antes e depois das correções — sem erro;
  - a suíte do microsserviço não foi rodada: a feature não toca `microservico-cadastro/`, o que o diff confirma.
- **Buscas:** referências a `recrutador.html` no frontend (uma, ver AP-02); uso de `descreverParaEmpresa()` (só em teste, ver AP-04); `innerHTML` nas telas novas (nenhum); senha, CPF e data de nascimento nas saídas do recrutador (nenhum); e-mails fora de `example.com` na carga do recrutador (nenhum).
- **Conferência em execução** (segunda passada, mesma data): o código foi **rodado** contra PostgreSQL 18, Tomcat 9.0.122 e o Vite. O resultado está na seção 7, e é o que tirou `CA-11` a `CA-20` do veredito "por leitura".
- **Não verificado nem na segunda passada:**
  - a **renderização** das telas e o clique nos botões: a sessão não tinha ferramenta de navegador, e `WebFetch` não acessa localhost. O `CA-20` fica parcial;
  - o CLI rodando dentro do IntelliJ, como o `AGENTS.md` descreve: foi exercitado por `stdin` com o classpath do WAR, o que cobre a lógica mas não a ergonomia do terminal;
  - o microsserviço, que a feature não toca.

## 3. Aderência à spec: `CA-01` a `CA-20`

Os caminhos são relativos a `src/main/groovy/org/example/` e a `src/test/groovy/org/example/`.

| Critério | Onde está implementado | Onde está verificado | Veredito |
| --- | --- | --- | --- |
| CA-01 Cadastro válido | `service/RecrutadorService.groovy:30`; `api/RecrutadorServlet.groovy:100`; `api/Json.groovy:89` | `service/RecrutadorServiceSpec` "cadastra recrutador válido"; `api/RecrutadorServletSpec` "POST cadastra recrutador e não expõe senha CPF nem data de nascimento"; `api/JsonSpec` "deRecrutador devolve somente os campos permitidos" | atende |
| CA-02 Campo obrigatório ausente | `service/ValidadorDeRecrutador.groovy:18-121`; `api/Json.groovy:140` | `service/ValidadorDeRecrutadorSpec` "campo #campo é obrigatório"; `api/RecrutadorServletSpec` "POST responde 400 para dados inválidos e não cadastra" | atende |
| CA-03 Formatos inválidos | `service/ValidadorDeRecrutador.groovy:56-121` | `service/ValidadorDeRecrutadorSpec`: e-mail sem formato, CPF de 11 dígitos, data inexistente, data futura, data fora do ISO, senha de 5 e de 51 caracteres | atende |
| CA-04 Área fora da lista | `service/ValidadorDeRecrutador.groovy:32`; `model/AreaDeRecrutamento.groovy:22` | `service/ValidadorDeRecrutadorSpec` "area precisa estar na lista fixa"; `model/RecrutadorSpec` "enum de áreas expõe todas as opções válidas" | atende |
| CA-05 Cadastro repetido | `service/RecrutadorService.groovy:120` | `service/RecrutadorServiceSpec` "recusa cadastro com e-mail ou CPF repetido" e "recusa cadastro com CPF repetido"; `api/RecrutadorServletSpec` "POST responde 400 para e-mail ou CPF repetido com mensagem genérica" | atende. Na API é `400`, como a RN-05 decidiu |
| CA-06 Login do recrutador | `api/LoginServlet.groovy:58`; `service/RecrutadorService.groovy:36` | `api/LoginServletSpec` "login de recrutador retorna apenas tipo id nome e idEmpresa" e "login de recrutador com credenciais erradas responde 401"; `service/RecrutadorServiceSpec` "autentica recrutador com e-mail e senha corretos" | atende |
| CA-07 Atribuição de vaga | `service/RecrutadorService.groovy:57`; `api/RecrutadorServlet.groovy:133` | `service/RecrutadorServiceSpec` "atribui vaga da própria empresa" e "não grava atribuição repetida"; `api/RecrutadorServletSpec` "POST atribui vaga e responde com a lista atualizada" | atende |
| CA-08 Atribuição fora da empresa | `service/RecrutadorService.groovy:68` | `service/RecrutadorServiceSpec` "recusa vaga de outra empresa"; `api/RecrutadorServletSpec` "POST atribuição responde 400 quando a vaga pertence a outra empresa" | atende |
| CA-09 Escopo de atuação | `service/RecrutadorService.groovy:85`; `dao/RecrutadorDAO.groovy:163` | `service/RecrutadorServiceSpec` "lista vagas atribuídas ao recrutador"; `api/RecrutadorServletSpec` "GET vagas retorna somente vagas atribuídas ao recrutador"; `controller/RecrutadorControllerSpec` "lista vagas atribuídas para seleção sem consultar outras vagas" | atende |
| CA-10 Curtida em vaga não atribuída | `service/CurtidaService.groovy:36`; `service/RecrutadorService.groovy:89` | `service/CurtidaServiceSpec` "não registra curtida quando o recrutador não tem a vaga atribuída"; `service/RecrutadorServiceSpec` "escopo recusa vaga não atribuída"; `api/CurtidaServletSpec` "recrutador não curte vaga não atribuída" | atende |
| CA-11 Match pela curtida do recrutador | `dao/MatchDAO.groovy:33`; `api/CurtidaServlet.groovy:46` | `api/CurtidaServletSpec` "recrutador curte candidato e resposta contém somente resultado do match"; `controller/CurtidaControllerSpec` "recrutador curte candidato anônimo em vaga atribuída e verifica match"; **e em execução** (seção 7, cenário CA-11) | **verificado.** O match é criado e `id_recrutador` recebe o recrutador que curtiu |
| CA-12 Só o primeiro recrutador fecha | `dao/MatchDAO.groovy:79-82` (`ON CONFLICT (id_candidato, id_empresa, id_vaga) DO NOTHING`) | `service/CurtidaServiceSpec` "recrutadorCurtirCandidato informa quando a curtida é repetida"; `controller/CurtidaControllerSpec` "curtida repetida do recrutador não verifica match"; **e em execução** (seção 7) | **verificado.** A curtida do segundo recrutador é gravada, o `executeUpdate` devolve 0 e o match segue com o primeiro |
| CA-13 Candidato curte depois de dois recrutadores | `dao/MatchDAO.groovy:40-52`, subconsulta com `ORDER BY crc.id LIMIT 1` | **em execução** (seção 7, com o segundo recrutador curtindo primeiro) | **verificado.** O match fica com quem curtiu primeiro, e não com o menor `id` de recrutador — o teste foi montado para distinguir os dois casos |
| CA-14 Matches em vagas diferentes da mesma empresa | `dao/MatchDAO.groovy:33`, com o trio no `ON CONFLICT` | **em execução** (seção 7) | **verificado.** Dois matches do mesmo candidato na mesma empresa, um por vaga, com recrutadores diferentes |
| CA-15 Desatribuição e exclusão | `dao/RecrutadorDAO.groovy:62,136`; `service/EmpresaService.groovy:46` | `service/RecrutadorServiceSpec` "remove vaga atribuída ao recrutador"; `service/EmpresaServiceSpec` (duas specs); `controller/RecrutadorControllerSpec` "remove vaga previamente atribuída ao recrutador"; **e em execução** (seções 7 e 7.3) | **verificado.** Curtidas e vínculos do recrutador somem, as dos outros ficam, os matches permanecem com `id_recrutador` nulo, e a empresa com recrutadores é recusada com a mensagem própria |
| CA-16 Anonimato preservado | `model/Recrutador.groovy:31,40`; `api/Json.groovy:89`; `view/RecrutadorView.groovy:36`; `ts/telas/telaCandidatosDaVaga.ts:55` | `model/RecrutadorSpec`, duas specs de descrição; `api/JsonSpec` "deRecrutador devolve somente os campos permitidos"; `view/RecrutadorViewSpec` "lista recrutadores com a empresa e sem senha CPF ou data de nascimento"; `controller/CurtidaControllerSpec` "curtida do recrutador exibe somente a representação anônima do candidato"; `model/MatchSpec` "descrição de match não revela o recrutador"; **e em execução** (seções 7.2 e 7.3) | **verificado.** Nenhuma resposta da API e nenhuma saída do CLI trouxe senha, CPF ou data de nascimento; a fila do CLI usa a representação anônima |
| CA-17 Banco (manual) | `database/Linketinder.sql:397-473` | **em execução**, seção 7.1 | **verificado** em PostgreSQL 18 descartável: 77 comandos aplicados, `UNIQUE`, FK composta e `id_recrutador` nulo conferidos |
| CA-18 API no Tomcat (manual) | `api/` | **em execução**, seção 7.2 | **verificado** no Tomcat 9.0.122 com o WAR e `curl`: 56 asserções, incluindo `405` com `Allow` por servlet e CORS com `DELETE` |
| CA-19 CLI (manual) | `controller/RecrutadorController.groovy`; `view/RecrutadorView.groovy`; `controller/MenuController.groovy:112` | `controller/MenuControllerSpec`, duas specs, cobre a navegação do menu; **em execução**, seção 7.3 | **verificado** contra o PostgreSQL real: CRUD, atribuição, curtida com match e os guards de exclusão |
| CA-20 Site (manual) | `frontend/` | `npx tsc --noEmit` passa; **em execução parcial**, seção 7.4 | **parcial.** O Vite serve as páginas com os perfis certos e a sequência de API das telas funciona com o `Origin` do Vite. Renderização e clique continuam pendentes: a sessão não tinha ferramenta de navegador |

## 4. Princípio do Anonimato

Todas as saídas da seção 4.2 da spec, uma a uma.

| Saída da feature | Quem vê | Campos que saem | Conforme? | Como foi verificado |
| --- | --- | --- | --- | --- |
| `POST /recrutadores` (resposta) | a empresa que cadastrou | `id`, `nome`, `area`, `descricao`, `email`, `idEmpresa` | sim | `api/Json.groovy:89` tem só esses seis campos; `api/JsonSpec` "deRecrutador devolve somente os campos permitidos"; `api/RecrutadorServletSpec` "POST cadastra recrutador e não expõe senha CPF nem data de nascimento" |
| `GET /recrutadores?idEmpresa={id}` | a empresa | os mesmos seis campos | sim | mesma função `deRecrutador`; `dao/RecrutadorDAO.groovy:102` nem seleciona `cpf`, `data_nascimento` e `senha` do banco |
| `GET /recrutadores/{id}/vagas` | a empresa e o próprio recrutador | o que `Json.deVaga` já devolvia | sim | `api/RecrutadorServlet.groovy:42`; nenhum campo novo |
| `POST /login` com `tipo: "recrutador"` | o próprio recrutador | `tipo`, `id`, `nome`, `idEmpresa` | sim | `api/LoginServlet.groovy:60`; `api/LoginServletSpec` "login de recrutador retorna apenas tipo id nome e idEmpresa" |
| `POST /curtidas` com `tipo: "recrutador"` | o recrutador que curtiu | só `match` | sim | `api/CurtidaServlet.groovy:47`; `api/CurtidaServletSpec` "recrutador curte candidato e resposta contém somente resultado do match" |
| `DELETE /recrutadores/{id}` e `DELETE .../vagas/{idVaga}` | a empresa | sem corpo | sim | `api/ApiServlet.groovy:93` só muda o status; `api/RecrutadorServletSpec`, as duas specs de `DELETE` |
| Fila de candidatos do recrutador (frontend) | o recrutador | `Candidato {id}`, formação, descrição, competências, afinidade | sim | `ts/telas/telaCandidatosDaVaga.ts:55-63` é o mesmo card da empresa, sem ramo por perfil |
| Lista de vagas do recrutador (frontend) | o recrutador | nome, cidade/estado e competências | sim | `ts/telas/telaMinhasVagas.ts:37`, o `criarItemDeVaga` compartilhado |
| Tela "Recrutadores" (frontend) | a empresa | nome, área, descrição, e-mail, vagas atribuídas e contagem por área | sim | `ts/telas/telaRecrutadores.ts:146-150`; o tipo `Recrutador` de `modelos.ts:36` não tem `cpf` nem `dataNascimento`, então não há o que renderizar por descuido |
| Listagem de recrutadores no CLI | quem opera o terminal | `id`, nome, área, descrição, e-mail e a empresa | sim, **depois do AP-04** | `view/RecrutadorView.groovy:38` passou a usar `descreverParaEmpresa()`; `view/RecrutadorViewSpec` "lista recrutadores com a empresa e sem senha CPF ou data de nascimento" |
| "Recrutador curtir um candidato" no CLI | quem opera o terminal | candidato pela representação anônima; vagas atribuídas | sim | `controller/CurtidaControllerSpec` "curtida do recrutador exibe somente a representação anônima do candidato" |
| Mensagens de erro de validação | quem enviou o cadastro | nome do campo e o motivo | sim | `service/ValidadorDeRecrutador.groovy` não interpola o valor em nenhuma mensagem; `service/ValidadorDeRecrutadorSpec`, todos os casos |
| Log da API | o operador do servidor | só a falha de banco | sim | `api/ApiServlet.groovy:43`, inalterado pela feature |

Linhas da tabela 4.1 conferidas:

- **Candidato → recrutador: nada.** Não existe rota, tela ou menu que mostre recrutador a candidato. `Match.descrever()` (`model/Match.groovy:25`) não cita o recrutador, mesmo com `idRecrutador` preenchido — `model/MatchSpec` prova isso.
- **Empresa → seus recrutadores.** `listarPorEmpresa` devolve o resumo sem CPF, data de nascimento e senha; a tela mostra exatamente nome, área, descrição, e-mail e vagas.
- **Empresa → recrutadores de outra empresa: nada.** `GET /recrutadores` exige `idEmpresa` e filtra por ele no SQL (`dao/RecrutadorDAO.groovy:104`). Que qualquer cliente possa passar outro `idEmpresa` é a pendência de integridade registrada no `AGENTS.md`, não uma exposição nova.
- **Recrutador → candidato: o mesmo que a empresa.** A fila usa `GET /candidatos`, a mesma rota da tela da empresa, e renderiza o mesmo card. A pendência conhecida — a rota devolve o cadastro completo ao navegador — passa a servir um perfil a mais, com os mesmos campos, como a seção 4.3 da spec previu.
- **Recrutador → vaga atribuída: tudo da vaga.** `Json.deVaga` inclui `empresa: {id, nome}`, o que é permitido: a vaga é da empresa dele.
- **Recrutador → outros recrutadores: nada.** `telaRecrutadores` exige `obterUsuarioDoTipo("empresa")` (`ts/telas/telaRecrutadores.ts:234`) e a página declara `data-perfil-exigido="empresa"`.
- **Senha:** não aparece em nenhuma saída, log ou mensagem. Continua em texto puro na coluna, que é a pendência conhecida — agora redigida no `AGENTS.md` citando os quatro cadastros, como a RN-08 pede.
- **Pendências conhecidas ampliadas:** só as duas que a spec autorizou, senha em texto puro e integridade das rotas novas, ambas registradas no `AGENTS.md` nesta revisão.

## 5. Apontamentos

### AP-01 — `matches.id_recrutador` sem migração, impedindo o `CA-17` como está escrito

**Severidade:** importante · **Situação:** corrigido

A coluna foi acrescentada dentro do `CREATE TABLE "matches"` (`database/Linketinder.sql:347`), no meio do arquivo, e não no bloco do fim. O plano (Decisões de implementação → Banco, e tarefa 1) promete "tudo acrescentado ao fim de `database/Linketinder.sql`, em bloco próprio, porque o arquivo não é idempotente", e a validação do `CA-17` manda "aplicar o bloco em um banco com o script antigo já carregado". Nesse banco a coluna não existe, então o bloco do recrutador falharia em `ALTER TABLE "matches" ADD CONSTRAINT "fk_matches_recrutador"` e nos dois `UPDATE matches` do fim.

**Correção** (autorizada pelo autor, por ser mudança de schema): `ALTER TABLE "matches" ADD COLUMN IF NOT EXISTS "id_recrutador" int NULL;` no início do bloco `-- Recrutadores`, com um comentário explicando por que ele existe. A coluna continua no `CREATE TABLE` para bancos criados do zero, e o `IF NOT EXISTS` deixa o bloco aplicável nos dois casos.

**Confirmado em execução** (seção 7.1). Num banco criado com o script **anterior** à feature, aplicar só o bloco do recrutador **sem** o `ALTER` falha no comando 11 com `SQLState 42703` — `coluna "id_recrutador" referenciada na restrição de chave estrangeira não existe` —, exatamente onde o apontamento previa. Com o `ALTER`, os 17 comandos do bloco passam e o `CA-17` fecha nesse banco migrado. O defeito era real e a correção resolve.

### AP-02 — O recrutador continuava entrando na página provisória da tarefa 12

**Severidade:** importante · **Situação:** corrigido

`PAGINA_INICIAL_POR_TIPO.recrutador` apontava para `paginas/recrutador.html` (`frontend/ts/navegacao.ts:17`), a página que a tarefa 12 criou como "destino provisório até a tela de vagas atribuídas da tarefa 14". A tarefa 14 fez a tela compartilhada e não retirou a provisória: as duas tinham `lista-minhas-vagas`, logo as duas renderizavam a mesma lista por `carregarMinhasVagas`, e o botão do cabeçalho levava de uma para a outra.

**Correção:** `recrutador: "minhasVagas.html"` e remoção de `frontend/paginas/recrutador.html`. Uma busca confirmou que `navegacao.ts` era a única referência ao arquivo.

### AP-03 — O gráfico de competências aparecia na tela do recrutador

**Severidade:** menor · **Situação:** corrigido

`paginas/minhasVagas.html` passou a aceitar `data-perfil-exigido="empresa,recrutador"`, mas a `section.coluna-grafico`, com o título "Competências dos Candidatos" e o `canvas`, não tinha `data-visivel-para`. O recrutador via o título e um quadro vazio, contra a RN-28 e a seção 6 da spec, que dizem que o gráfico continua só na tela da empresa.

**Sem vazamento de dado:** `ts/telas/telaMinhasVagas.ts:15-26` só chama `listarCandidatos()` e `desenharGraficoDeCompetencias` no ramo da empresa, então nenhum dado de candidato era buscado para o recrutador. O problema era de tela.

**Correção:** `data-visivel-para="empresa" hidden` na seção, que é o mecanismo que `configurarCabecalho` já usa para os botões.

### AP-04 — A listagem de recrutadores no CLI não mostrava a empresa

**Severidade:** menor · **Situação:** corrigido

A RN-27 e a seção 4.2 da spec pedem "`id`, nome, área, descrição, e-mail e a empresa". `RecrutadorView.exibir` usava `descrever()`, que não traz a empresa, e `Recrutador.descreverParaEmpresa()` — criado para isso na seção 2.4 da spec — só era chamado por um teste, ou seja, era código morto em produção.

**Correção:** `exibir` passou a usar `descreverParaEmpresa()`, e `view/RecrutadorViewSpec` ganhou as asserções da empresa e da descrição, mantendo as que provam a ausência de senha, CPF e data de nascimento.

### AP-05 — `AGENTS.md` não foi atualizado pela tarefa 15

**Severidade:** importante · **Situação:** corrigido

O commit `64d1ee3` acrescentou cinco linhas ao `AGENTS.md`. Faltavam: `/recrutadores` e o fim da frase "só `GET` e `POST`" na visão geral; o Recrutador, a `recrutador_vaga`, a `curtida_recrutador_candidato` e o `id_recrutador` na tabela de domínio; as classes e os arquivos novos na estrutura; o terceiro `tipo` no login; as rotas e o `DELETE` em "Como executar"; **as linhas do recrutador na tabela do Princípio do Anonimato**, que a seção 8.2 da spec trata como decisão central; a pendência de senha em texto puro citando quatro cadastros (RN-08); e a pendência nova de integridade das rotas de recrutador.

**Correção:** todos os pontos aplicados, mais uma seção "### Recrutador" em "Regras de negócio centrais". O texto da tabela do anonimato foi submetido ao autor e aprovado antes da edição, como a spec exige.

### AP-06 — `README.md` se contradizia sobre o `DELETE` e o contrato estava incompleto

**Severidade:** importante · **Situação:** corrigido

A seção "Perfil de recrutador" listava `DELETE /recrutadores/{id}`, mas a seção "## Contrato" — a que o `AGENTS.md` aponta como o contrato completo — dizia "Temos 10 endpoints, todos sendo POST e GET" e terminava com "Qualquer outra requisição como DELETE, PUT é respondido com erro 405". O contrato também nunca documentou `/login`, `/competencias` e `/curtidas`, que entraram antes desta feature.

**Correção:** o contrato passou a listar os 20 endpoints, com os códigos de cada um, e a frase final separa `PUT`, que é sempre `405`, do `DELETE`, que é `405` fora das rotas de recrutador. A seção "Perfil de recrutador" deixou de duplicar a lista e aponta para o contrato.

### AP-07 — `CHANGELOG.md` sem a entrada da tarefa 15

**Severidade:** menor · **Situação:** corrigido

Havia 14 entradas em "Adicionado", uma por tarefa da 1 à 14, e nenhuma da tarefa 15, embora o próprio plano exija "uma entrada por tarefa, da 1 à 15".

**Correção:** entradas em "Modificado" (documentação, empresa na listagem do CLI, página inicial do recrutador e gráfico), "Corrigido" (o `ALTER TABLE` do AP-01) e "Removido" (a página provisória do AP-02).

### AP-08 — `plano.md` se contradizia e não havia `revisao.md`

**Severidade:** menor · **Situação:** corrigido

A linha 5 dizia "Nenhuma tarefa de implementação foi iniciada nesta sequência" com as 15 tarefas concluídas; as tarefas 5, 6, 12, 13, 14 e 15 não tinham `[Concluída]` no título, embora o campo "Situação" de cada uma dissesse que estavam; e o checklist de validação da tarefa 15 — `CA-01` a `CA-20` um a um, total de testes, anonimato item a item — não existia em lugar nenhum: `docs/specs/recrutador/` tinha só `spec.md` e `plano.md`, contra o `spec.md`, `plano.md` e `revisao.md` que o `AGENTS.md` descreve e que `microservico/` tem.

**Correção:** linha 5 reescrita, os seis títulos marcados, as tarefas 1 e 12 com o que mudou nesta revisão, a pendência da tabela do anonimato fechada, e este arquivo, que é o checklist preenchido.

### AP-09 — Lista de áreas duplicada em três lugares

**Severidade:** menor · **Situação:** **postergado por decisão do autor**

As sete áreas aparecem em `model/AreaDeRecrutamento.groovy:8-14`, em `AREAS_RECRUTADOR` (`frontend/ts/telas/telaRecrutadores.ts:14-22`) e nos `option` de `frontend/paginas/recrutadores.html:33-39`. O backend é a fonte da verdade, e as duas cópias do frontend podem divergir dele em silêncio: uma área acrescentada no enum não aparece no `select`, e uma área tirada do enum continua ofertada e só falha na validação. O `AGENTS.md` pede DRY como requisito de Clean Code. Sugestão: a tela popula o `select` a partir da constante, e o HTML fica só com a opção inicial.

### AP-10 — `CurtidaService` com dependência opcional e `IllegalStateException`

**Severidade:** menor · **Situação:** **postergado por decisão do autor**

`service/CurtidaService.groovy:18-20` tem um construtor de dois argumentos que deixa `recrutadorService` nulo, e `recrutadorCurtirCandidato` (`:37-39`) lança `IllegalStateException` nesse caso. Nenhum caminho usa esse construtor: `Main.groovy:68`, `api/Dependencias.groovy:61` e `service/CurtidaServiceSpec:17` passam os três argumentos. É um estado inválido representável sem necessidade, contra a convenção de dependências por construtor, e `IllegalStateException` não é o tipo de erro do projeto. Sugestão: remover o construtor de dois argumentos e o `if`.

### AP-11 — `Location` do `POST /recrutadores` aponta para uma rota que não existe

**Severidade:** menor · **Situação:** **postergado por decisão do autor**

`responderCriado` (`api/ApiServlet.groovy:85`) devolve `Location: /recrutadores/{id}`, mas `GET /recrutadores/{id}` cai no `responderNaoEncontrado` do servlet (`api/RecrutadorServlet.groovy:46`), porque a RN-26 não previu rota de item. Um cliente que siga o `Location` recebe `404`. Sugestão: criar a rota ou não enviar o cabeçalho neste caso — é decisão de contrato, então fica com o autor.

### AP-12 — A verificação de escopo carrega todas as vagas do recrutador

**Severidade:** menor · **Situação:** **postergado por decisão do autor**

`RecrutadorService.atribuirVaga` (`:72`) e `validarEscopo` (`:104`) chamam `listarVagasDoRecrutador`, e o DAO (`dao/RecrutadorDAO.groovy:181`) resolve cada id com `vagaRepositorio.buscarPorId`, que monta a `Vaga` inteira com empresa e competências — só para conferir se um id está na lista. Toda curtida de recrutador paga isso. Na tela da empresa há o mesmo padrão em outra camada: `carregarListaDeRecrutadores` (`ts/telas/telaRecrutadores.ts:244-247`) faz um `GET /recrutadores/{id}/vagas` por recrutador. Sugestão: `existeAtribuicao(idRecrutador, idVaga)` no repositório, com spec nova.

## 6. Depois das correções

- **Verificações rodadas de novo:** `./gradlew test` na raiz — 193 testes, nenhuma falha; `npx tsc --noEmit` em `frontend/` — sem erro. A contagem não mudou porque o AP-04 reforçou as asserções de uma spec existente, sem criar spec nova.
- **Itens repassados:** o `CA-16` e a seção 4 inteira, por causa dos AP-03 e AP-04; a RN-27, que o AP-04 fechou; a RN-28 e a seção 6 da spec, por causa dos AP-02 e AP-03.
- **Fluxo da empresa conferido** (seção 6 da spec, "o comportamento para a empresa deve continuar idêntico"): no diff de `telaMinhasVagas.ts` e `telaCandidatosDaVaga.ts` contra `main`, o ramo da empresa mantém as mesmas chamadas (`listarVagasDaEmpresa`, `listarCandidatos`, `curtirCandidato`), as mesmas mensagens e o gráfico. A única diferença é que em `telaCandidatosDaVaga` a busca dos candidatos deixou de ser paralela à das vagas e passou a acontecer depois de a vaga ser encontrada — muda a ordem das requisições, não o resultado, e evita buscar candidatos quando a vaga é inválida. O fluxo do candidato não foi tocado pela feature.
- **Fica para o autor:**
  - **a passada visual do `CA-20`**: abrir o site no navegador como empresa e como recrutador, clicar em curtir e passar, e conferir o gráfico. É o único item das conferências manuais que continua aberto — os outros foram executados na seção 7;
  - decidir sobre os AP-09 a AP-12;
  - responder se `POST /recrutadores` deve ganhar um `GET /recrutadores/{id}` ou perder o `Location` (AP-11);
  - atualizar o DER (`assets/der.png`), pendência da tarefa 1 registrada na seção 8.2 da spec;
  - preencher a coluna "Discutida com o Tutor?" da seção 8.1 da spec, que segue com "a preencher pelo autor" nas cinco perguntas do desafio.

## 7. Conferência em execução (2026-10-14)

Segunda passada da revisão, pedida pelo autor com a pergunta "o código está funcionando?". Aqui o código foi **rodado**, não lido. Ambiente: PostgreSQL 18 na 5432, Tomcat 9.0.122, JDK 25, Node com Vite. Não havia `psql` no PATH nem data source no IntelliJ, então o SQL foi executado por JDBC, com programas de arquivo único descartáveis; as credenciais foram extraídas de `ProvedorPostgreSQL` em tempo de execução e nunca escritas nem impressas.

**Parecer da conferência: o código funciona.** 94 asserções de execução, nenhuma falha, nenhum defeito novo encontrado. O que já estava registrado como pendência conhecida continua pendente, sem agravamento.

### 7.1 Banco (`CA-17`) e a prova do AP-01

| Verificação | Resultado |
| --- | --- |
| `database/Linketinder.sql` inteiro num banco vazio (`linketinder_ca17`) | **77 comandos, todos passaram.** O `;` que falta nas linhas 307-315 continua exigindo descartar aquele `SELECT`; é a pendência conhecida, fora do escopo |
| Tabelas `recrutadores`, `recrutador_vaga`, `curtida_recrutador_candidato` | criadas |
| `matches.id_recrutador` aceita nulo | sim, e o `INSERT` com nulo é aceito |
| Carga fictícia do bloco | 4 recrutadores, 4 atribuições, 3 curtidas, e os dois `UPDATE matches` pegaram as linhas certas |
| `UNIQUE (id_recrutador, id_candidato, id_vaga)` | recusa curtida duplicada, `SQLState 23505` |
| FK composta `(id_recrutador, id_vaga)` | recusa curtida em vaga **não atribuída**, `SQLState 23503` |
| Bloco do recrutador em banco pré-feature, **sem** o `ALTER` do AP-01 | **falha** no comando 11, `SQLState 42703` |
| Mesmo bloco **com** o `ALTER` | **17 comandos, todos passaram**, e o `CA-17` fecha nesse banco |

### 7.2 Match e exclusões, com o SQL real dos DAOs

O SQL de `MatchDAO.criarMatchSePossivel` foi **extraído do próprio arquivo** em tempo de execução (texto entre as triplas aspas), para garantir que o que rodou é a consulta do projeto, e chamado com a mesma ordem de parâmetros do DAO. Os comandos de `RecrutadorDAO.removerVaga` e `deletar` foram reproduzidos na mesma ordem do código. Dados fictícios isolados, criados só para o teste.

| Cenário | Resultado |
| --- | --- |
| Só a curtida do candidato | nenhum match criado |
| `CA-11` recrutador com a vaga curte depois | match criado, `id_recrutador` = esse recrutador |
| `CA-12` segundo recrutador curte depois do match | curtida gravada, `executeUpdate` devolve 0, o match segue com o primeiro |
| `CA-13` R2 curte, **depois** R1, e só então o candidato curte | match fica com **R2** — quem curtiu primeiro, apesar de ter o maior `id` de recrutador. O cenário foi montado nessa ordem justamente para distinguir "curtida mais antiga" de "menor id de recrutador" |
| `CA-14` segunda vaga da mesma empresa, outro recrutador | segundo match criado, com o outro recrutador; o candidato passa a ter dois matches na mesma empresa |
| Empresa curte e candidato curte, sem recrutador | match criado com `id_recrutador` nulo |
| Borda da spec: recrutador curtiu antes, empresa curte depois | match fica com o recrutador, não nulo |
| `CA-15` desatribuir a vaga | curtidas daquele recrutador **naquela vaga** apagadas, as dos outros intactas, nenhum match apagado, o match dele com `id_recrutador` nulo e o match em outra vaga sem alteração |
| `CA-15` excluir o recrutador | recrutador, vínculos e curtidas apagados, nenhum match apagado, e nenhum match ainda apontando para ele |

### 7.3 API no Tomcat (`CA-18`) e CLI (`CA-19`)

O WAR foi gerado com `./gradlew war`, implantado no Tomcat 9.0.122 e exercitado com `curl` contra o banco real: **56 asserções, nenhuma falha.** Cobriu as oito rotas da RN-26, os códigos de erro (`400`, `401`, `404`, `405`), o `Allow` por servlet (`GET, POST, DELETE` no recrutador e `GET, POST` nos outros), o preflight `OPTIONS` com `DELETE` em `Access-Control-Allow-Methods` e `Vary: Origin`, o match fechado ponta a ponta pela API, e a regressão de `GET /vagas`, `GET /competencias` e das curtidas de candidato e de empresa. **Nenhum corpo de resposta continha senha, CPF ou data de nascimento.**

O CLI foi rodado com o classpath tirado do WAR, alimentando o menu por `stdin` contra o mesmo banco. Verificado: a listagem de recrutadores sai com a empresa (**a correção do AP-04 confirmada ao vivo**) e sem senha, CPF ou data de nascimento; cadastro; atribuição de vaga; "Listar vagas atribuídas"; e "Recrutador curtir um candidato", que imprimiu `MATCH!! A curtida entre vocês dois foi recíproca!` e gravou o match com o `id_recrutador` certo. A fila de candidatos dessa tela sai na representação anônima — só `ID`, formação, descrição e competências, sem nome nem e-mail. Os dois guards de exclusão de empresa responderam com a mensagem própria de cada caso, inclusive o novo "porque ela possui recrutadores cadastrados".

Observação de ambiente, não defeito: rodado por `stdin` no Git Bash, o texto acentuado do CLI sai com a codificação errada no console. Dentro do IntelliJ, que é o caminho documentado, isso não aparece.

### 7.4 Site (`CA-20`), parcial

O Vite serviu `index.html`, `login.html`, `minhasVagas.html`, `vagaCandidatos.html` e `recrutadores.html`. O HTML entregue confirma as correções desta revisão: a seção do gráfico vem com `data-visivel-para="empresa" hidden` (AP-03), `minhasVagas.html` e `vagaCandidatos.html` declaram `empresa,recrutador`, `recrutadores.html` declara `empresa`, e a entrada tem a terceira opção de perfil. A `paginas/recrutador.html` removida no AP-02 não existe mais no disco — a URL cai no fallback do Vite e abre a tela de entrada, sem link quebrado.

A sequência exata que a tela do recrutador faz foi repetida com o cabeçalho `Origin` do Vite: `POST /login` devolveu `{tipo, id, nome, idEmpresa}` e nada mais, `GET /recrutadores/{id}/vagas` trouxe só a vaga atribuída, `GET /candidatos` respondeu e `POST /curtidas` devolveu só `{"match": false}` — todas com `Access-Control-Allow-Origin` para a origem do Vite.

**O que falta:** renderização e clique. Sem ferramenta de navegador na sessão, isso continua com o autor, e é a única parte do `CA-20` em aberto.

### 7.5 Estado do ambiente depois da conferência

- Tomcat derrubado, Vite encerrado, portas 8080 e 5173 livres. A pasta `webapps/` do Tomcat ficou como estava (o `linketinder.war` e a pasta explodida já existiam antes), agora com o WAR da build atual.
- Os bancos descartáveis `linketinder_ca17` e `linketinder_ap01` foram apagados.
- O banco `linketinder` **já tinha** as três tabelas e a coluna `id_recrutador` antes desta conferência, então nada de schema foi aplicado nele. Os registros fictícios criados durante os testes (duas empresas, duas vagas, três recrutadores, curtidas e matches) foram apagados ao final, e o banco voltou ao conteúdo que tinha antes: empresa `ZG Soluções`, o recrutador `Guilherme` com a vaga `FullStack` atribuída, e os dois matches originais.
- `./gradlew test --rerun-tasks` depois de tudo: **193 testes, nenhuma falha.** `npx tsc --noEmit`: sem erro.
