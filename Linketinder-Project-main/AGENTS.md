# AGENTS.md — Linketinder

Guia para agentes de IA que trabalham neste repositório. Leia inteiro antes de alterar código.

## Regra nº 1: na dúvida, pergunte

Sempre que houver dúvida, ambiguidade ou uma decisão que não esteja escrita aqui (regra de negócio, mudança de schema, nova dependência, contrato da API, o que fazer com uma pendência listada abaixo), **pare e pergunte ao autor antes de decidir sozinho**. Explique o que encontrou e quais são as opções. Não "resolva" silenciosamente uma inconsistência entre este documento e o código.

## Status atual do repositório (2026-10-14)

O microsserviço de cadastro está implementado e validado em `microservico-cadastro/` e continua sendo a referência de arquitetura do cadastro no repositório.

Depois dele, três features entraram **sem spec**: o login, as rotas de competências e de curtidas e o `CorsFilter` na API REST; a troca do `localStorage` do frontend pelo consumo da API REST; e a fila de cards com curtir/passar junto com a área da empresa. Este documento já descreve o estado posterior às três, e o `CHANGELOG.md` as registra. O frontend deixou de ser uma parte isolada: hoje ele é o cliente da API REST.

Depois delas veio o **recrutador**, com spec, plano e revisão em `docs/specs/recrutador/`: um terceiro tipo de usuário, colaborador de uma empresa, que só atua nas vagas atribuídas a ele e nunca recebe dados pessoais restritos de candidatos. A empresa continua sendo a responsável por publicar vagas e por atribuir ou remover vagas dos recrutadores. A feature está implementada nas quatro partes — banco, CLI, API REST e frontend — e coberta por specs Spock; as conferências manuais com PostgreSQL, Tomcat e navegador (`CA-17` a `CA-20` da spec) seguem pendentes com o autor.

Qualquer mudança nova deve ser refletida aqui, na spec da feature (quando houver) e no `CHANGELOG.md` antes de fechar a tarefa.

## Visão geral

Linketinder é um projeto de LinkedIn com Tinder em que **candidatos curtem vagas**, **empresas curtem candidatos** e, quando a curtida é recíproca, acontece um **match**. Até o match, as partes não sabem quem está do outro lado (ver [Princípio do Anonimato](#princípio-do-anonimato-restrição-obrigatória)).

O repositório tem quatro partes. O CLI, a API REST e o microsserviço gravam no mesmo banco e não se chamam entre si; o frontend consome a API REST pelo navegador e não fala com o CLI nem com o microsserviço:

| Parte | O que é | Persistência |
|---|---|---|
| Backend CLI (`src/main/groovy`, `Main.groovy`) | Menus no terminal com CRUD, curtidas e matches | PostgreSQL |
| API REST (`org.example.api`) | Servlets para candidatos, empresas, vagas, competências, recrutadores, login e curtidas (`GET` e `POST`, mais `DELETE` nas rotas de recrutador) | PostgreSQL |
| Microsserviço de cadastro (`microservico-cadastro/`) | Aplicação independente e sem framework, que só cadastra: candidato, empresa e competências do candidato | PostgreSQL |
| Frontend (`frontend/`) | Páginas estáticas em TypeScript que consomem a API REST com `fetch` | Nenhuma própria: lê e grava pela API. O `sessionStorage` guarda apenas quem está logado (`tipo`, `id`, `nome` e, para o recrutador, `idEmpresa`) |

Curtida e match existem no CLI, na API REST e no banco; o microsserviço não tem esses fluxos.

O microsserviço é um programa à parte: tem build Gradle e código próprios (pacote `org.linketinder.cadastro`) e **não importa nada de `org.example`**. Código não é compartilhado entre ele e o projeto principal.

## Stack

- **Backend:** Groovy 5.0.8 sobre JDK 25, Gradle 9.6 (wrapper), plugins `groovy`, `application`, `war`.
- **Banco:** PostgreSQL via JDBC puro (`org.postgresql:postgresql:42.7.7`), sem ORM e sem ferramenta de migração.
- **API:** `javax.servlet-api` 4.0.1 (`compileOnly`), implantada como WAR no **Tomcat 9** (não usar `jakarta.*` / Tomcat 10+); JSON com `groovy-json`; CORS por `CorsFilter` (`@WebFilter`), para o navegador poder chamar de outra porta.
- **Microsserviço de cadastro:** mesmas versões de Groovy, driver e Spock, em build Gradle separado; HTTP pelo `com.sun.net.httpserver.HttpServer` do JDK, sem framework, Tomcat ou servlet.
- **Testes:** Spock 2.4 (`groovy-5.0`) em JUnit Platform.
- **Frontend:** TypeScript ~6.0 + Vite 8, HTML/CSS sem framework, `fetch` para falar com a API. Chart.js 4.5 está no `package.json`, mas o gráfico usa a cópia carregada por CDN em `paginas/minhasVagas.html` (`declare const Chart` em `graficoCompetencias.ts`).

## Estrutura

```
build.gradle, settings.gradle, gradlew*   build do backend
database/Linketinder.sql                  schema + carga inicial + consultas de exemplo
assets/der.png                            diagrama entidade-relacionamento
lib/                                      jars do Groovy 6.0.0-beta (NÃO usados pelo Gradle; não mexer sem perguntar)
src/main/groovy/org/example/
  Main.groovy        monta as dependências do CLI (único lugar que instancia DAOs)
  Banco/             ConexaoBanco (Singleton), FactoryConexao, ProvedorConexao + PostgreSQL/MySQL, TipoBanco
  model/             Candidato, Empresa (ambos TipoPessoa), Recrutador, AreaDeRecrutamento, Vaga, Competencia, Match
  repositorio/       interfaces de persistência (uma por agregado)
  dao/               implementações JDBC das interfaces de repositorio
  service/           regras de negócio, ValidadorDeRecrutador + RegraDeNegocioException
  controller/        orquestram view <-> service; MenuController navega os menus
  view/              única camada que lê teclado e imprime (EntradaDados, *View)
  api/               ApiServlet (base), *Servlet (candidato, empresa, vaga, competencia, login,
                     curtida, recrutador), CorsFilter, Json (conversão), Dependencias, EncerradorDeConexao
src/test/groovy/org/example/              specs Spock espelhando os pacotes de main
microservico-cadastro/                    aplicação independente (build, wrapper e .gitignore próprios)
  src/main/groovy/org/linketinder/cadastro/
    Main.groovy      monta as dependências e sobe o servidor
    Configuracao     porta e banco por variável de ambiente
    http/            ServidorCadastro, Roteador, Rota, handlers, JSON, RegistroDoServidor (único que escreve no console)
    model/           Candidato, Empresa, Competencia
    service/         ValidadorDeCadastro, serviços de cadastro e de competências, exceções de regra
    repositorio/     interfaces de persistência e Transacao
    dao/, banco/     implementações JDBC, conexão por requisição e transação
  src/test/groovy/org/linketinder/cadastro/   specs Spock; DadosFicticios concentra os dados de teste
docs/specs/<feature>/                     spec.md, plano.md e revisao.md de cada feature
skills/                                   skills do fluxo: feature-com-spec e revisao-com-spec
CHANGELOG.md                              mudanças por tarefa, no formato Keep a Changelog
frontend/
  index.html         escolha do perfil: entrar como candidato, como empresa ou como recrutador
  style.css, assets/
  paginas/           login, cadastro, competencias, candidato, vaga, recrutadores (tela da empresa),
                     minhasVagas e vagaCandidatos (atendem empresa e recrutador)
  ts/
    main.ts          único ponto de entrada das páginas; liga as telas presentes no HTML
    modelos.ts       tipos do domínio e dos corpos da API
    dom.ts           leitura de campos, mensagens, cards e envio de formulário
    sessao.ts        quem está logado, no sessionStorage
    navegacao.ts     caminhos entre páginas, cabeçalho e proteção por perfil
    formularios.ts   cadastro de candidato, empresa e vaga + login
    filaDeCards.ts   fila de um card por vez, com curtir e passar
    afinidade.ts     índice de afinidade e ordenação por afinidade
    graficoCompetencias.ts  gráfico de competências dos candidatos (Chart.js)
    api/             http.ts (fetch, URL base e ErroApi) e api.ts (uma função por endpoint)
    telas/           telaAcesso, telaCandidato, telaCompetencia, telaMinhasVagas,
                     telaCandidatosDaVaga, telaVaga, telaRecrutadores
```

Fluxo das camadas: `view` ⇄ `controller` → `service` → `repositorio` (interface) ← `dao` → `Banco`.

## Domínio

| Entidade | Tabela | Campos principais | Relacionamentos |
|---|---|---|---|
| **Candidato** | `candidatos` | nome, sobrenome, data de nascimento, e-mail (único), CPF (único), país, CEP, descrição, senha, formação | N:N com Competência (`candidato_competencia`) |
| **Empresa** | `empresas` | nome, e-mail (único), CNPJ (único), país, CEP, descrição, senha | 1:N com Vaga |
| **Vaga** | `vagas` | nome, descrição, estado (UF, 2 letras), cidade, `id_empresa` | pertence a uma Empresa; N:N com Competência (`vaga_competencia`) |
| **Competência** | `competencias` | nome (único) | ligada a candidatos e vagas |
| **Recrutador** | `recrutadores` | nome, área (lista fixa), descrição, e-mail (único), CPF (único), data de nascimento, senha, `id_empresa` | pertence a uma Empresa (1:N); N:N com Vaga (`recrutador_vaga`) |
| **Curtida** | `curtida_candidato_vaga`, `curtida_empresa_candidato`, `curtida_recrutador_candidato` | ver abaixo | não tem classe de modelo; existe só nas tabelas e em `CurtidaRepositorio` |
| **Match** | `matches` | `id_candidato`, `id_empresa`, `id_vaga` (trio único) e `id_recrutador` (anulável) | liga um Candidato a uma Vaga e à Empresa dona dela; `id_recrutador` aponta para o recrutador que fechou o match, ou é nulo quando foi a empresa |

Detalhes que costumam confundir:

- Empresa e Vaga são entidades diferentes: quem publica é a empresa, quem é curtida pelo candidato é a **vaga**.
- Em `Candidato` e `Vaga` as competências são `List<String>` com os **nomes**; os DAOs resolvem para `competencias` com `buscarOuInserir`, criando a competência se ela ainda não existir.
- No backend `Vaga` guarda o objeto `Empresa`; no frontend o tipo `Vaga` recebe `empresa: {id, nome}`, como a API devolve, e só o cadastro (`NovaVaga`) envia `idEmpresa`.

## Regras de negócio centrais

### Curtida

- **Candidato → Vaga:** grava `(id_candidato, id_vaga)` em `curtida_candidato_vaga`.
- **Empresa → Candidato, para uma vaga específica dela:** grava `(id_empresa, id_candidato, id_vaga)` em `curtida_empresa_candidato`. A empresa só pode curtir usando uma vaga própria: o CLI valida com `VagaService.buscarVagaDaEmpresa`, o `CurtidaServlet` compara o `idEmpresa` enviado com `vaga.empresa.id` (diferente → `400`), e o banco garante com a FK composta `(id_empresa, id_vaga) → vagas(id_empresa, id)`.
- **Recrutador → Candidato, para uma vaga atribuída a ele:** grava `(id_recrutador, id_candidato, id_vaga)` em `curtida_recrutador_candidato` e vale como interesse **da empresa naquela vaga**. O escopo é validado em `RecrutadorService.validarEscopo`, chamado por `CurtidaService.recrutadorCurtirCandidato`, e garantido no banco pela FK composta `(id_recrutador, id_vaga) → recrutador_vaga(id_recrutador, id_vaga)`.
- Curtidas são únicas. Repetir uma curtida não gera erro: o `INSERT ... ON CONFLICT DO NOTHING` não insere nada, o repositório devolve `false` e a view avisa que já foi curtido.
- Não existe "descurtir".
- **Pela API:** `POST /curtidas` atende os três sentidos conforme o campo `tipo` (`candidato`, `empresa` ou `recrutador`) e responde `201` com `{"match": true|false}`. Candidato, vaga ou recrutador inexistente é `404`; `tipo` fora dos três valores é `400`. Curtida repetida não é erro: a resposta é `201` com `match: false`, sem avisar que já havia sido curtido — a API, ao contrário da view, não distingue os dois casos.

### Match

- O match acontece quando, **para a mesma vaga**, o candidato curtiu a vaga **e** o outro lado curtiu esse candidato — a empresa dona da vaga **ou** um recrutador com aquela vaga atribuída. A ordem das curtidas não importa.
- `matches.id_recrutador` guarda quem fechou a reciprocidade: o recrutador da curtida de recrutador **mais antiga** daquele candidato naquela vaga, ou nulo quando só a empresa curtiu. Como o `UNIQUE (id_candidato, id_empresa, id_vaga)` não mudou, só o primeiro fecha — curtidas posteriores de outros recrutadores na mesma vaga são gravadas e não criam match novo.
- Após cada curtida nova, `CurtidaController.informarMatch` (CLI) e o `CurtidaServlet` (API) chamam `CurtidaService.houveMatch`, que delega a `MatchRepositorio.criarMatchSePossivel`: um `INSERT ... SELECT` em `matches` que só insere se as duas curtidas existirem e a vaga for da empresa. Devolve `true` apenas quando o match foi criado naquele momento.
- O match é do trio candidato–empresa–vaga: o mesmo candidato pode ter vários matches com a mesma empresa, um por vaga.
- Quem acabou de curtir fica sabendo do match na hora: o CLI imprime a mensagem e a API devolve `match: true`, que o frontend mostra acima da fila de cards. O CLI também lista os matches já existentes (`CurtidaController.listarMatches`); a API e o frontend não têm rota nem tela para isso.

### Recrutador

- O recrutador é colaborador de **uma** empresa (`id_empresa` obrigatório), tem login próprio e não é candidato nem empresa. Quem cadastra, lista, atualiza e exclui é só a empresa: não há autocadastro, e o recrutador não cria, não edita e não exclui vagas, nem as atribuídas a ele.
- **Atualizar recrutador é só pelo CLI** (opção "Atualizar" do menu "Recrutadores"). A API e o site não editam recrutador.
- A **área** vem da lista fixa do enum `model/AreaDeRecrutamento` (`TI`, `RH`, `Marketing`, `Segurança da Informação`, `Vendas`, `Financeiro`, `Outros`). Ela é informativa — não restringe atribuição — e existe para a empresa ver a contagem de recrutadores por área, calculada no frontend a partir de `GET /recrutadores?idEmpresa={id}`.
- A validação do cadastro fica em `service/ValidadorDeRecrutador`, primeira validação de formato no `service` do projeto principal, para CLI e API compartilharem as regras: obrigatórios, tamanhos das colunas, e-mail com formato, CPF de 11 dígitos (só dígitos ou `000.000.000-00`, sem conferir dígito verificador), data `AAAA-MM-DD` existente e não futura, senha de 6 a 50 usada exatamente como enviada. Os espaços das pontas saem de todos os campos, menos da senha. A mensagem cita o campo e **nunca repete o valor**.
- **E-mail e CPF são únicos só dentro de `recrutadores`** — podem existir em `candidatos` ou `empresas`, porque o login recebe o `tipo` e sabe onde procurar. Repetido é erro com a mensagem genérica `Já existe um recrutador com esses dados`, que não diz qual campo coincidiu, e `400` na API (não `409`: o `ApiServlet` mapeia regra de negócio para `400`).
- **Escopo de atuação:** a empresa atribui explicitamente as vagas de cada recrutador em `recrutador_vaga`, e ele só vê e só curte nessas. Atribuir vaga de outra empresa, ou a recrutador de outra empresa, é erro; atribuição repetida não é erro e não grava nada. Desatribuir apaga as curtidas daquele recrutador **naquela vaga** e zera o `id_recrutador` dos matches daquele vínculo; os matches continuam valendo, porque são da empresa.
- **Rotas:** `POST /recrutadores`, `GET /recrutadores?idEmpresa={id}`, `DELETE /recrutadores/{id}`, `GET`/`POST /recrutadores/{id}/vagas` e `DELETE /recrutadores/{id}/vagas/{idVaga}`. Os `DELETE` respondem `204` sem corpo e são a primeira exceção à convenção "só `GET` e `POST`": `RecrutadorServlet` sobrepõe `metodosAceitos()` com `"GET, POST, DELETE"` e o `CorsFilter` anuncia `DELETE`.
- Senha, CPF e data de nascimento do recrutador **nunca saem do banco** — ver [Princípio do Anonimato](#princípio-do-anonimato-restrição-obrigatória).

### Login e sessão (API REST e frontend)

- `POST /login` recebe `tipo` (`candidato`, `empresa` ou `recrutador`), `email` e `senha`. `CandidatoService.autenticar`, `EmpresaService.autenticar` e `RecrutadorService.autenticar` buscam pelo e-mail (`buscarPorEmail`, busca exata) e comparam a senha em texto puro.
- Credenciais corretas respondem `200` com `{tipo, id, nome}` — e, para o recrutador, também `idEmpresa`, que o site precisa para as telas dele. Nada mais, nunca a senha, nem o CPF ou a data de nascimento do recrutador. E-mail ou senha errados respondem `401` com a mensagem genérica `E-mail ou senha inválidos`, que não diz qual dos dois falhou; `tipo` fora dos três valores é `400`. `GET /login` é `405` com `Allow: POST`.
- **Não há sessão no servidor:** nem token, nem cookie. A API não sabe quem está chamando e nenhuma outra rota exige login. Quem está logado é só o `{tipo, id, nome}` (mais o `idEmpresa`, no caso do recrutador) que o frontend guarda no `sessionStorage` (`sessao.ts`) e reenvia como `idCandidato`/`idEmpresa`/`idRecrutador`; qualquer cliente pode enviar outro `id`.
- No frontend, cada página declara no `<body>` quem pode vê-la (`data-perfil-exigido`, que aceita uma lista como `"empresa,recrutador"`, ou `data-somente-deslogado`) e `navegacao.ts` redireciona quem não se encaixa. É conveniência de navegação, não segurança.
- Depois do cadastro o frontend já abre a sessão com os dados devolvidos pela API e segue para o próximo passo: candidato vai para as competências, empresa vai para o cadastro de vaga.

### Competências pela API REST

- `GET /competencias` lista as competências já cadastradas (`id` e `nome`), usada na tela em que o candidato marca as dele.
- `POST /competencias` recebe `idCandidato` e `competencias` e **soma** as novas às que o candidato já tem, descartando repetidas sem diferenciar maiúsculas (`CandidatoService.adicionarCompetencias`). Lista vazia (ou só com nomes em branco) é `400`; candidato inexistente é `404`; sucesso é `201`.
- A resolução no banco continua exata (`CompetenciaDAO.buscarOuInserir`), então por esta rota `java` e `Java` criam duas linhas em `competencias` — diferente do microsserviço, que reaproveita em qualquer caixa. O frontend contorna isso trocando o nome digitado pelo já cadastrado (`usarNomeJaCadastrado`), mas a API aceita qualquer caixa.

### Outras regras

- Empresa com vagas cadastradas não pode ser excluída, e o mesmo vale para empresa com recrutadores (`EmpresaService.excluir`).
- Competência vinculada a candidato ou vaga não pode ser excluída; nome de competência não pode ser vazio nem duplicado (`CompetenciaService`).
- Excluir um candidato ou uma vaga apaga antes suas competências, curtidas (inclusive as de recrutador) e matches; excluir uma vaga apaga também os vínculos `recrutador_vaga` dela, e excluir um recrutador apaga os vínculos e as curtidas dele e zera o `id_recrutador` dos matches que ele fechou (tudo feito no DAO).
- Senha tem no mínimo 6 caracteres; data de nascimento no formato `AAAA-MM-DD`; estado é a sigla com 2 letras.
- Índice de afinidade (só no frontend, `calcularAfinidade` em `ts/afinidade.ts`): competências em comum ÷ total de competências da vaga, em porcentagem arredondada, ignorando maiúsculas/minúsculas e espaços das pontas; vaga sem competências dá 0%.
- As filas de cards vêm em ordem decrescente de afinidade, com empate desfeito por sorteio (`ordenarPorAfinidade` embaralha antes de ordenar). Na fila de candidatos de uma vaga, quem tem 0% de afinidade não aparece; na fila de vagas do candidato, todas as vagas aparecem.
- Regra violada → o service lança `RegraDeNegocioException`; o `MenuController` exibe a mensagem no CLI e o `ApiServlet` responde `400`.

### Cadastro pelo microsserviço

O microsserviço tem regras próprias, mais rígidas que as do CLI e da API REST. Os três caminhos gravam nas mesmas tabelas, então o comportamento depende de por onde o cadastro entra. **O site não usa o microsserviço:** ele cadastra pela API REST, e o microsserviço segue sendo chamado só por `curl`.

- **Dois passos para o candidato:** `POST /candidatos` grava os dados pessoais; depois `POST /candidatos/{id}/competencias` grava as competências. Empresa (`POST /empresas`) não tem competências. O site repete esse desenho de dois passos, mas com as rotas da API REST (`POST /candidatos` e `POST /competencias`).
- **Ao menos uma competência:** a rota de competências recusa lista vazia, aqui e na API REST. Nenhum dos serviços consegue obrigar a segunda chamada; quem encaminha o candidato para ela é o site, que vai do cadastro direto para a tela de competências. O CLI continua aceitando candidato sem competência.
- **Competências somadas e sem diferenciar maiúsculas:** a rota soma as competências novas às que o candidato já tem e reaproveita a que já existe no banco em qualquer caixa (`java` usa `Java`). No CLI e na API REST a busca é exata, e `java` criaria outra competência.
- **Validação:** campos obrigatórios, tamanhos das colunas, senha de 6 a 50 caracteres, data existente e não futura, formato de e-mail, CPF (11 dígitos), CNPJ (14) e CEP (8), só os dígitos ou na pontuação padrão, sem conferir dígito verificador. Os valores são gravados como enviados.
- **Cadastro repetido:** responde `409` com mensagem genérica, sem dizer qual campo coincidiu. Na API REST o mesmo caso vira `500`.
- **Erros:** `400` cita o campo e nunca repete o valor; `404` para rota ou candidato inexistente; `405` com `Allow`; `500` genérico para falha de banco.

## Princípio do Anonimato (restrição obrigatória)

**Esta regra vale para qualquer feature, em qualquer camada (CLI, API, frontend, banco, logs, testes, mensagens de erro). Nenhuma mudança pode expor dados fora do que está na tabela abaixo.**

| Quem vê | Antes do match | Depois do match |
|---|---|---|
| **Empresa → candidato** | Somente ID, formação, descrição e competências | Perfil completo do candidato, **exceto a senha** |
| **Candidato → vaga/empresa** | Somente os dados da vaga: ID, nome da vaga, descrição, cidade/estado, competências e índice de afinidade. **Nada da empresa, nem o nome** | Perfil completo da empresa dona da vaga, **exceto a senha** |
| **Candidato → recrutador** | Nada. O candidato não sabe que recrutadores existem, quantos são, nem qual deles o analisou ou o curtiu | Nada. A revelação pós-match é da empresa, não da pessoa que operou a conta |
| **Empresa → seus recrutadores** | A qualquer momento: `id`, nome, área, descrição, e-mail e as vagas atribuídas. **Nunca** CPF, data de nascimento ou senha | Igual: o match não muda o que a empresa vê dos próprios recrutadores |
| **Empresa → recrutadores de outra empresa** | Nada, em nenhum momento | Nada |
| **Recrutador → candidato** | O mesmo que a empresa: ID, formação, descrição, competências e o índice de afinidade com a vaga | O mesmo que a empresa. Não existe tela nem rota pós-match, então na prática o recrutador não vê perfil revelado em lugar nenhum |
| **Recrutador → própria empresa** | Nome da empresa dele (própria parte se identificando, como o `nome` no login) | Igual |
| **Recrutador → vaga atribuída** | Todos os dados da vaga, inclusive a empresa: a vaga é da empresa dele | Igual |
| **Recrutador → outros recrutadores** | Nada: nenhuma rota, tela ou menu do recrutador lista colegas | Nada |

Regras derivadas:

1. Antes do match são proibidos para a empresa: nome, sobrenome, e-mail, CPF, data de nascimento, país e CEP do candidato. Para o candidato: nome, e-mail, CNPJ, país, CEP e descrição da empresa. Para o recrutador valem os mesmos campos proibidos à empresa.
2. A revelação pós-match vale apenas entre as duas partes daquele match, não para terceiros.
3. **A senha nunca é exibida nem devolvida**, em nenhum momento, por nenhuma camada (nem em JSON, nem em `descrever()`, nem em log). **CPF e data de nascimento do recrutador também nunca saem do banco**: não aparecem em `Recrutador.descrever*()`, em `Json.deRecrutador`, em tela, em log nem em mensagem de erro.
4. Curtir não revela nada: uma parte não fica sabendo quem a curtiu até existir o match.
5. Ao exibir candidatos para uma empresa ou para um recrutador use a representação anônima (`Candidato.descreverAnonimo()`); nunca reutilize `descrever()` nesse contexto. Ao exibir vagas para um candidato não inclua a empresa. Ao exibir recrutadores para a empresa, nunca inclua CPF, data de nascimento ou senha.
6. Se uma feature parecer exigir a exposição de um dado fora da tabela (ou se não estiver claro de qual lado é a tela), **pergunte antes de implementar**.
7. O recrutador é interno à empresa: nenhuma saída, em nenhum momento, revela ao candidato que recrutadores existem ou qual deles o analisou. Curtida de recrutador vale como interesse da empresa, e é a empresa que aparece no match.

### Pendências conhecidas (código ainda fora da regra)

Não amplie nenhuma delas, não as use como exemplo a seguir e pergunte antes de alterá-las:

- **CLI:** em `CurtidaController.candidatoCurtirVaga` as vagas são listadas com `Vaga.descrever()`, que imprime o nome da empresa antes do match.
- **API:** `GET /vagas` devolve `empresa: {id, nome}`; `GET /candidatos`, `GET /candidatos/{id}`, `GET /empresas` e `GET /empresas/{id}` devolvem o cadastro completo (sem a senha) para qualquer cliente, sem autenticação. O frontend depende dessas rotas: as telas da empresa usam `GET /candidatos` e a do candidato usa `GET /vagas`.
- **`POST /competencias`:** responde com o perfil completo do candidato (`Json.deCandidato`: nome, sobrenome, CPF, e-mail, data de nascimento, país e CEP, sem a senha) e aceita qualquer `idCandidato`, sem autenticação. Quem souber um `id` acrescenta competências e recebe o cadastro de volta. É mais grave que a pendência equivalente do microsserviço, cuja rota devolve só `id` e `competencias`; o frontend ignora essa resposta.
- **Pós-match:** `Match.descrever()` mostra só nome e e-mail das partes; a regra permite o perfil completo sem a senha.
- **Frontend:** não guarda mais cadastro nem senha no navegador (o `dados.ts` e o `localStorage` saíram), mas recebe da API mais do que mostra: as telas da empresa trazem o cadastro completo de todos os candidatos e só renderizam `Candidato {id}`, formação, descrição e competências; a tela do candidato traz `empresa: {id, nome}` em cada vaga e não exibe a empresa. O dado proibido chega ao navegador e aparece no DevTools.
- **Login sem autenticação das demais rotas:** existe `POST /login`, mas nenhuma outra rota confere quem chama. Quem está vendo é decidido pelo `sessionStorage` do navegador, então qualquer cliente pode chamar `GET /candidatos`, `POST /curtidas` ou `POST /competencias` com o `id` que quiser. No CLI a parte continua sendo escolhida numa lista de nomes (`listarNomes`), que mostra nomes por ser a própria parte se identificando, não a parte contrária.
- **Microsserviço sem login:** `POST /candidatos/{id}/competencias` aceita qualquer `id`. Quem souber o `id` de um candidato pode acrescentar competências a ele e ver a lista dele na resposta. A lista é um dado permitido antes do match, então o risco é de integridade, não de exposição.
- **Senha em texto puro:** os cadastros de candidato, empresa e recrutador gravam a senha sem hash na coluna `senha` — pelo CLI e pela API REST, e, para candidato e empresa, também pelo microsserviço —, e o `POST /login` compara a senha recebida com a do banco em texto puro nos três tipos de usuário. Ela nunca é devolvida nem registrada em log, mas está legível no banco e trafega assim no corpo da requisição (HTTP, sem TLS, em desenvolvimento). Corrigir exige alterar as colunas, os quatro cadastros e o login.
- **Rotas de recrutador sem autenticação:** `POST /recrutadores`, `GET /recrutadores?idEmpresa={id}`, `DELETE /recrutadores/{id}`, `GET`/`POST /recrutadores/{id}/vagas` e `DELETE /recrutadores/{id}/vagas/{idVaga}` aceitam qualquer `idEmpresa` e `idRecrutador`, como o resto da API. Quem souber o `id` de uma empresa cadastra e remove recrutadores dela e lê nome, área, descrição e e-mail dos recrutadores dela. Nenhum dado de candidato ou de empresa a mais é exposto: o risco é de integridade, não de exposição. A spec do recrutador (seção 4.3) autorizou a ampliação e deixou a autenticação para feature própria.

O microsserviço **não tem rota de leitura** e deve continuar assim: as respostas de cadastro devolvem a quem se cadastrou os próprios dados, sem a senha, e a rota de competências devolve só `id` e `competencias`. O log dele registra apenas método, modelo da rota e status; nunca corpo, e-mail, CPF, CNPJ, senha ou o caminho digitado em rota desconhecida.

## Banco de dados

- **Scripts:** um único arquivo, `database/Linketinder.sql`, na ordem: criação das tabelas base → carga inicial (competências, candidatos, empresas, vagas, vínculos) → consultas de exemplo → tabelas de curtida e match → constraint empresa/vaga → curtidas e matches de exemplo. O DER está em `assets/der.png`.
- **Conexão esperada pelo código** (`ProvedorPostgreSQL`): `jdbc:postgresql://localhost:5432/linketinder`, com usuário e senha fixos no código-fonte (ver as pendências de dados e credenciais, em [Restrições](#restrições)).
- **Como aplicar:** o script não cria o banco e não é idempotente (sem `DROP`/`IF NOT EXISTS`), então rode uma única vez em um banco vazio:

  ```bash
  createdb -U postgres linketinder
  psql -U postgres -d linketinder -f database/Linketinder.sql
  ```

  Sem `psql` no PATH, abra o arquivo no pgAdmin ou no console SQL do IntelliJ (data source em `.idea/dataSources.xml`) e execute.
- **Problema conhecido no script:** o último `SELECT` antes do bloco `-- curtidas` (linhas 309–315) está sem `;`. Executado de uma vez, o `CREATE TABLE "curtida_candidato_vaga"` falha junto com ele e tudo que depende dessa tabela também. Até ser corrigido, execute o bloco de curtidas/matches separadamente. Pergunte antes de corrigir o arquivo.
- **Escolha do banco:** a variável de ambiente `LINKETINDER_BANCO` (`POSTGRESQL` padrão, ou `MYSQL`) define o provedor via `FactoryConexao`. Na prática só o PostgreSQL funciona: não há driver MySQL no `build.gradle` e os DAOs usam SQL específico do PostgreSQL (`RETURNING`, `ON CONFLICT`).
- **Mudanças de schema:** não há migrações. Qualquer alteração precisa ser refletida em `database/Linketinder.sql`, nos DAOs e no DER — **pergunte antes de alterar o schema**.

## Como executar

Todos os comandos do backend partem da raiz do repositório; no Windows use `gradlew.bat` (ou `./gradlew` no Git Bash).

- **CLI:** exige o PostgreSQL no ar com o script aplicado. Rode `org.example.Main` pelo IntelliJ. `./gradlew run` **não funciona hoje** (falta `application.mainClass` no `build.gradle`); o comando `groovy -cp ...` do README depende de Groovy instalado e do driver JDBC no classpath.
- **API REST:**
  1. `./gradlew war` gera `build/libs/linketinder.war`;
  2. copie para `webapps/` de um Tomcat 9 e suba o servidor (`bin/startup.sh` ou `bin\startup.bat`);
  3. base: `http://localhost:8080/linketinder` — `GET`/`POST` em `/candidatos`, `/empresas`, `/vagas`, `/competencias` e `/recrutadores`, `POST` em `/login` e `/curtidas`, `GET` em `/{recurso}/{id}`, `GET /vagas?idEmpresa={id}`, `GET /recrutadores?idEmpresa={id}`, `GET`/`POST` em `/recrutadores/{id}/vagas` e `DELETE` em `/recrutadores/{id}` e `/recrutadores/{id}/vagas/{idVaga}`. O `DELETE` das rotas de recrutador é a única exceção à convenção de só `GET` e `POST`; os demais métodos respondem `405` com `Allow`. O contrato completo está no `README.md`;
  4. o `CorsFilter` libera o navegador para **uma** origem: `http://localhost:5173` (Vite) por padrão, ou o valor de `LINKETINDER_FRONTEND_ORIGEM`. Requisição `OPTIONS` responde `204` e toda resposta leva `Vary: Origin`. Chamada por `curl` não passa por CORS.
- **Microsserviço de cadastro:** exige o PostgreSQL no ar e a variável de ambiente `LINKETINDER_CADASTRO_BANCO_SENHA` com a senha do banco (sem ela o serviço não sobe e diz qual variável falta). Abra a pasta `microservico-cadastro/` em outra janela do IntelliJ e rode `org.linketinder.cadastro.Main`, ou, pelo terminal, a partir dessa pasta:

  ```bash
  LINKETINDER_CADASTRO_BANCO_SENHA=<sua-senha> ./gradlew run
  ```

  Sobe em `http://localhost:8081`, com `POST /candidatos`, `POST /empresas` e `POST /candidatos/{id}/competencias`. Porta, URL e usuário do banco têm padrão e podem ser trocados por `LINKETINDER_CADASTRO_PORTA`, `LINKETINDER_CADASTRO_BANCO_URL` e `LINKETINDER_CADASTRO_BANCO_USUARIO`. Rotas, exemplos e a exploração da integração com o frontend estão no `README.md`.
- **Frontend:** exige a API REST no ar; sem ela as telas mostram "Não foi possível conectar ao servidor". A URL base é `http://localhost:8080/linketinder`, trocável pela variável `VITE_API_URL` (em um `.env` que **não** vai para o repositório).

  ```bash
  cd frontend
  npm install
  npm run dev        # servidor Vite em http://localhost:5173
  npx tsc --noEmit   # checagem de tipos
  ```

  O fluxo começa em `index.html` (escolher candidato, empresa ou recrutador) e a sessão fica no `sessionStorage`: fechar a aba desloga.

## Como testar

```bash
./gradlew test                                                   # suíte inteira
./gradlew test --tests "org.example.service.CurtidaServiceSpec"  # uma spec
```

O microsserviço tem a própria suíte, que a raiz não executa. Mudou algo nele, rode as duas:

```bash
cd microservico-cadastro
./gradlew test                                                                            # suíte do microsserviço
./gradlew test --tests "org.linketinder.cadastro.service.ValidadorDeCadastroSpec"        # uma spec
```

- No microsserviço, regras e serviços usam `Mock` dos repositórios; as rotas são testadas subindo o servidor de verdade em uma porta livre e chamando com o `HttpClient` do JDK. Os dados de teste vêm de `DadosFicticios`. Os DAOs e a transação contra o PostgreSQL real dependem de conferência manual (`curl` e consulta ao banco).
- Os testes **não precisam de banco**: services e controllers recebem `Mock` das interfaces de `repositorio`; servlets recebem `Dependencias` montadas com mocks. As rotas novas têm spec em `src/test/groovy/org/example/api` (`LoginServletSpec`, `CompetenciaServletSpec`, `CurtidaServletSpec`, `RecrutadorServletSpec`, `CorsFilterSpec`).
- Utilitários de teste: `EntradaFalsa.comRespostas([...])` simula o teclado e `SaidaConsole.capturar { ... }` captura o que a view imprimiu.
- Os DAOs e `ConexaoBanco` não têm testes automatizados (dependem de banco real); mudanças neles precisam de verificação manual com o PostgreSQL.
- O frontend não tem testes unitários e não precisa ter (dispensado pelo desafio); valide com `npx tsc --noEmit` e manualmente no navegador.
- Toda mudança de comportamento no backend deve vir com spec nova ou ajustada, e a suíte deve passar antes de considerar a tarefa concluída.

## Convenções

- **Idioma:** tudo em português — classes, métodos, variáveis, mensagens e nomes de teste (`def "candidatoCurtirVaga informa quando a curtida é repetida"()`).
- **Clean Code** é requisito do projeto: nomes autoexplicativos, funções pequenas e com uma responsabilidade, DRY, comentário só quando estritamente necessário, tratamento de erros explícito.
- **Camadas (MVC):**
  - `model` não imprime: expõe `descrever()` (e variantes) que **retornam** texto;
  - `view` é a única camada com `println` e leitura de entrada;
  - `controller` não tem regra de negócio nem SQL;
  - `service` concentra as regras e lança `RegraDeNegocioException`;
  - `dao` só persiste e implementa uma interface de `repositorio`.
- **Dependências por construtor**, apontando para interfaces. Apenas `Main` (CLI) e `api/Dependencias` (API) conhecem classes concretas e chamam `ConexaoBanco.obterInstancia()`.
- **SQL:** sempre por `conexaoBanco.executar(sql) { PreparedStatement statement -> ... }` com parâmetros `?`; nunca concatene valor de usuário na query.
- **Estilo Groovy:** tipos explícitos em vez de `def`; sem modificador de visibilidade em classes e métodos públicos; dependências como `private final`; constantes `static final` em `MAIUSCULAS`; indentação de 4 espaços; cabeçalho `@author Guilherme Lima Conte` nas classes.
- **API:** servlets estendem `ApiServlet` e usam `responder*`/`lerCorpo`; a conversão de e para JSON fica em `Json` (`paraX` lê, `deX` escreve — e nunca inclui `senha`).
- **Microsserviço:** as mesmas camadas, sem `view`: `http` → `service` → `repositorio` ← `dao` → `banco`. Só o `Main` conhece classes concretas; só `RegistroDoServidor` escreve no console; exceções de regra viram código de status no `Roteador`; rota nova é registrada em `RotasDeCadastro`.
- **Frontend:** uma tela por módulo em `ts/telas/`; o acesso à API fica em `ts/api/` (`http.ts` monta a requisição e lança `ErroApi`, `api.ts` tem uma função por endpoint) e nenhum outro módulo chama `fetch`; tipos em `modelos.ts`, DOM e mensagens em `dom.ts`, sessão em `sessao.ts`, caminhos entre páginas em `navegacao.ts`. Funções exportadas com nomes em português. `main.ts` é o único ponto de entrada e só liga o que existe na página aberta.
- **Nada de `innerHTML`:** texto vindo do usuário ou do banco entra no DOM por `textContent` ou por elementos criados com `createElement`.

## Restrições

- Respeite o [Princípio do Anonimato](#princípio-do-anonimato-restrição-obrigatória) em toda feature.
- Nunca exponha senha em saída, JSON ou log.
- Não altere o schema, o contrato da API, as dependências do `build.gradle`/`package.json` nem a pasta `lib/` sem perguntar.
- Mantenha `javax.servlet` + Tomcat 9; não migre para `jakarta` sem pedido explícito.
- Não quebre a separação de camadas (ex.: `println` fora de `view`, SQL fora de `dao`, `new XxxDAO` fora de `Main`/`Dependencias`).
- **Use dados fictícios.** Em scripts de carga e testes, nunca use CPFs, e-mails ou nomes reais. O mesmo vale para exemplos na documentação e para cadastros feitos em conferência manual.
- **Nunca faça commit de credenciais.** Senhas de banco, chaves de API e arquivos `.env` devem ficar fora do repositório: leia de variável de ambiente e não escreva o valor em código, teste, script ou documentação.
- Não faça commit nem push por conta própria.
- Encontrou inconsistência, pendência ou algo não coberto aqui? Volte à [Regra nº 1](#regra-nº-1-na-dúvida-pergunte).

### Pendências conhecidas (dados e credenciais)

Código já commitado que ainda está fora das duas regras acima. Não amplie, não use como modelo e pergunte antes de corrigir:

- `ProvedorPostgreSQL` e `ProvedorMySQL` trazem usuário e senha de banco fixos no código-fonte.
- A carga inicial (`database/Linketinder.sql`) e várias specs em `src/test/groovy` usam e-mails em domínio real (`@gmail.com`), que podem pertencer a alguém. O `frontend/ts/dados.ts`, que tinha o mesmo problema, deixou de existir.
- Nem o `.gitignore` da raiz nem o do `frontend/` ignoram arquivos `.env` — e o frontend agora pode ter um, para a `VITE_API_URL`.

O microsserviço já segue as duas regras e serve de referência: a senha do banco vem só de variável de ambiente, o `.gitignore` dele ignora `.env`, e os testes usam e-mails em `example.com` e documentos com dígitos verificadores inválidos.
