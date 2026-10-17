# AGENTS.md — Linketinder

Guia para agentes de IA que trabalham neste repositório. Leia inteiro antes de alterar código.

## Regra nº 1: na dúvida, pergunte

Sempre que houver dúvida, ambiguidade ou uma decisão que não esteja escrita aqui (regra de negócio, mudança de schema, nova dependência, contrato da API, o que fazer com uma pendência listada abaixo), **pare e pergunte ao autor antes de decidir sozinho**. Explique o que encontrou e quais são as opções. Não "resolva" silenciosamente uma inconsistência entre este documento e o código.

## Status atual do repositório (2026-10-17)

O backend foi reescrito com o **Micronaut Framework** na feature `backend-micronaut` (spec, plano e revisão em `docs/specs/backend-micronaut/`). Ele é formado por **quatro microsserviços** independentes em `microsservicos/`, como na figura do desafio (`assets/img.png`). O CLI, a API de servlets no Tomcat e o antigo `microservico-cadastro/` foram removidos, junto com o build Gradle da raiz e a pasta `lib/`.

O frontend continua em TypeScript + Vite, sem framework; da feature `backend-micronaut` em diante, só a camada `ts/api/` mudou, para chamar cada microsserviço pela própria URL.

As features anteriores (microsserviço de cadastro e recrutador) têm spec, plano e revisão em `docs/specs/microservico/` e `docs/specs/recrutador/`, que ficam como histórico: os caminhos e as classes citados nelas não existem mais. As regras de negócio delas continuam valendo e foram portadas para os microsserviços.

Qualquer mudança nova deve ser refletida aqui, na spec da feature (quando houver) e no `CHANGELOG.md` antes de fechar a tarefa.

## Visão geral

Linketinder é um projeto de LinkedIn com Tinder em que **candidatos curtem vagas**, **empresas curtem candidatos** e, quando a curtida é recíproca, acontece um **match**. Até o match, as partes não sabem quem está do outro lado (ver [Princípio do Anonimato](#princípio-do-anonimato-restrição-obrigatória)).

O repositório tem duas partes: o backend, em quatro microsserviços (MS), e o frontend.

| Parte | O que é | Porta |
|---|---|---|
| MS Usuários (`microsservicos/ms-usuarios`) | Candidatos, empresas, recrutadores (com a atribuição de vagas) e login | `8081` |
| MS Competências (`microsservicos/ms-competencias`) | Catálogo de competências e competências do candidato | `8082` |
| MS Vagas (`microsservicos/ms-vagas`) | CRUD de vagas, com as competências de cada vaga | `8083` |
| MS Curtidas (`microsservicos/ms-curtidas`) | Curtidas nos três sentidos e criação do match | `8084` |
| Frontend (`frontend/`) | Páginas estáticas em TypeScript que chamam os MS com `fetch` | `5173` (Vite) |

- Cada MS é uma aplicação à parte: build Gradle, wrapper, `.gitignore` e código próprios (pacote `org.linketinder.<ms>`), aberta em uma janela própria do IntelliJ. **Nenhum MS importa código de outro**: o que é comum (formato de erro, CORS, log, conexão) é repetido em cada um.
- Os quatro gravam no **mesmo PostgreSQL** e **nenhum chama outro por HTTP**. Quando um MS precisa de dado de outro agregado (o MS Curtidas saber de quem é a vaga, o MS Vagas saber se a empresa existe), ele consulta direto no banco.
- O frontend é o único cliente. Ele não guarda dados próprios: o `sessionStorage` guarda apenas quem está logado (`tipo`, `id`, `nome` e, para o recrutador, `idEmpresa`).
- Curtida e match existem só no MS Curtidas e no banco. Não há rota nem tela que liste matches.

## Stack

- **Backend:** Micronaut Platform `5.2.2` (plugin Gradle `io.micronaut.application` `5.0.2`, servidor Netty), Groovy `5.0.8` sobre JDK 25, Gradle `9.6` (wrapper em cada MS).
- **JVM do Gradle:** o plugin do Micronaut 5 exige que o **próprio Gradle** rode no JDK 25. Cada MS tem `gradle/gradle-daemon-jvm.properties` com `toolchainVersion=25`, então pelo terminal o Gradle acha o JDK 25 instalado sozinho. No IntelliJ, a *Gradle JVM* de cada janela precisa apontar para um JDK 25.
- **Dependências de cada MS:** `micronaut-serde-jackson` (JSON), `micronaut-jdbc-hikari` (`DataSource`), `org.postgresql:postgresql:42.7.7`, `groovy`, `logback-classic`, `snakeyaml`; em teste, `micronaut-http-client` e `micronaut-test-spock` (pelo `testRuntime 'spock2'`).
- **Banco:** PostgreSQL via JDBC puro sobre o `DataSource` injetado, sem ORM, sem Micronaut Data e sem ferramenta de migração.
- **Testes:** Spock 2.4 (`groovy-5.0`) com `@MicronautTest`, em JUnit Platform.
- **Frontend:** TypeScript ~6.0 + Vite 8, HTML/CSS sem framework, `fetch` para falar com os MS. Chart.js 4.5 está no `package.json`, mas o gráfico usa a cópia carregada por CDN em `paginas/minhasVagas.html` (`declare const Chart` em `graficoCompetencias.ts`).

## Estrutura

```
database/Linketinder.sql                  schema + carga inicial + consultas de exemplo
assets/der.png, assets/img.png            diagrama entidade-relacionamento e figura do desafio
microsservicos/
  ms-usuarios/ (8081)  ms-competencias/ (8082)  ms-vagas/ (8083)  ms-curtidas/ (8084)
    cada um com:
      build.gradle, settings.gradle, gradle.properties (micronautVersion), gradlew*, .gitignore
      gradle/wrapper/*, gradle/gradle-daemon-jvm.properties
      src/main/groovy/org/linketinder/<ms>/
        Aplicacao            ponto de entrada; confere a senha do banco antes de subir
        VerificadorDeAmbiente, ConfiguracaoInvalidaException
        http/        controllers, Json (conversão), CorpoJson, ProcessadorDeErros, Tratador*,
                     RespostaDeErro, RegistroDeRequisicoes (filtro de log), RegistroDoServidor
                     (interface) + RegistroDoServidorEmLog, CabecalhoVaryOrigin
        service/     regras, validadores e exceções de regra
        repositorio/ interfaces de persistência (e Transacao, onde há transação)
        dao/         implementações JDBC
        banco/       ConexaoBanco, TransacaoJdbc e TradutorDeErroDeBanco (onde são usados)
        model/
      src/main/resources/application.yml, logback.xml
      src/test/groovy/org/linketinder/<ms>/   specs Spock; DadosFicticios concentra os dados de teste
      src/test/resources/application-test.yml datasource desligado e porta aleatória
docs/specs/<feature>/                     spec.md, plano.md e revisao.md de cada feature
skills/                                   skills do fluxo: feature-com-spec e revisao-com-spec
CHANGELOG.md                              mudanças por tarefa, no formato Keep a Changelog
PROMPTS.md                                prompts usados para conduzir o agente em cada feature
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
    api/             http.ts (fetch, URL base de cada MS e ErroApi) e api.ts (uma função por endpoint)
    telas/           telaAcesso, telaCandidato, telaCompetencia, telaMinhasVagas,
                     telaCandidatosDaVaga, telaVaga, telaRecrutadores
```

Fluxo das camadas em cada MS: `http` (controller) → `service` → `repositorio` (interface) ← `dao` → `banco`.

## Domínio

| Entidade | Tabela | Campos principais | Relacionamentos |
|---|---|---|---|
| **Candidato** | `candidatos` | nome, sobrenome, data de nascimento, e-mail (único), CPF (único), país, CEP, descrição, senha, formação | N:N com Competência (`candidato_competencia`) |
| **Empresa** | `empresas` | nome, e-mail (único), CNPJ (único), país, CEP, descrição, senha | 1:N com Vaga |
| **Vaga** | `vagas` | nome, descrição, estado (UF, 2 letras), cidade, `id_empresa` | pertence a uma Empresa; N:N com Competência (`vaga_competencia`) |
| **Competência** | `competencias` | nome (único) | ligada a candidatos e vagas |
| **Recrutador** | `recrutadores` | nome, área (lista fixa), descrição, e-mail (único), CPF (único), data de nascimento, senha, `id_empresa` | pertence a uma Empresa (1:N); N:N com Vaga (`recrutador_vaga`) |
| **Curtida** | `curtida_candidato_vaga`, `curtida_empresa_candidato`, `curtida_recrutador_candidato` | ver abaixo | modelo `Curtida` só no MS Curtidas, que representa o pedido recebido |
| **Match** | `matches` | `id_candidato`, `id_empresa`, `id_vaga` (trio único) e `id_recrutador` (anulável) | liga um Candidato a uma Vaga e à Empresa dona dela; `id_recrutador` aponta para o recrutador que fechou o match, ou é nulo quando foi a empresa |

Detalhes que costumam confundir:

- Empresa e Vaga são entidades diferentes: quem publica é a empresa, quem é curtida pelo candidato é a **vaga**.
- Cada MS tem os próprios modelos, só com o que usa. O `Candidato` existe no MS Usuários e no MS Competências; a `Vaga`, no MS Vagas e no MS Usuários (para as vagas atribuídas ao recrutador). Não há modelo compartilhado.
- Em `Candidato` e `Vaga` as competências são `List<String>` com os **nomes**. A resolução para `competencias` ignora maiúsculas: `java` reaproveita a `Java` já cadastrada (a mais antiga, se houver duas), e a que não existe é criada.
- No frontend o tipo `Vaga` recebe `empresa: {id, nome}`, como o MS Vagas devolve, e só o cadastro (`NovaVaga`) envia `idEmpresa`.

## Regras de negócio centrais

### Contrato HTTP comum aos quatro MS

- Corpo de requisição e de resposta em JSON UTF-8. Todo erro tem o formato `{"erro": "<mensagem>"}`, em português.
- Corpo vazio, JSON inválido ou que não é objeto → `400`; rota desconhecida → `404`; método não aceito em rota conhecida → `405` com `Allow`; falha de banco → `500` com `Erro ao acessar o banco de dados`, sem detalhe técnico.
- Exceções de regra viram status pelos `Tratador*` de `http/`: `RegraDeNegocioException` → `400`, `RecursoNaoEncontradoException` → `404`, `CadastroRepetidoException` → `409`, `SQLException` → `500` e, no MS Usuários, `CredenciaisInvalidasException` → `401`. O `ProcessadorDeErros` substitui o processador de erros do Micronaut para os erros do próprio framework saírem no mesmo formato.
- **CORS:** cada MS libera **uma** origem, `http://localhost:5173` por padrão ou o valor de `LINKETINDER_FRONTEND_ORIGEM`. O preflight `OPTIONS` é respondido; os métodos anunciados são os que o MS usa (o MS Curtidas anuncia só `POST`); toda resposta leva `Vary: Origin`. Requisição com `Origin` de outra origem recebe `403`. Chamada por `curl` sem `Origin` não passa por CORS.
- **Log:** só método, modelo da rota (ou `(rota desconhecida)`) e status. Nunca corpo, parâmetro, senha, e-mail, CPF, CNPJ, data de nascimento nem o caminho digitado.

### Cadastro e atualização de candidato e empresa (MS Usuários)

- **Validação:** campos obrigatórios, tamanhos das colunas, senha de 6 a 50 caracteres usada exatamente como enviada, data `AAAA-MM-DD` existente e não futura, formato de e-mail, CPF (11 dígitos), CNPJ (14) e CEP (8), só os dígitos ou na pontuação padrão, sem conferir dígito verificador (`ValidadorDeCadastro`). Os espaços das pontas saem de todos os campos, menos da senha. O `400` cita o campo e **nunca repete o valor**.
- **Cadastro repetido** (e-mail ou CPF de candidato, e-mail ou CNPJ de empresa) responde `409` com a mensagem genérica `Já existe um cadastro com esses dados`, sem dizer qual campo coincidiu. O banco garante a unicidade e o `TradutorDeErroDeBanco` converte o SQLState `23505`.
- **Atualização (`PUT`):** recebe os mesmos campos e regras do cadastro, inclusive a senha, e substitui os dados. A unicidade ignora o próprio registro. O `PUT /candidatos/{id}` não mexe nas competências.
- **Exclusão:** a do candidato apaga antes as competências dele, as curtidas nos três sentidos e os matches, numa transação; a da empresa é recusada com `400` se ela tiver vagas ou recrutadores.
- O cadastro do candidato tem **dois passos**: `POST /candidatos` no MS Usuários grava os dados pessoais; depois `POST /competencias` no MS Competências grava as competências. Quem leva ao segundo passo é o site, que vai do cadastro direto para a tela de competências. Candidato sem competência continua no banco.
- Depois do cadastro, o frontend já abre a sessão com os dados devolvidos e segue para o próximo passo: candidato vai para as competências, empresa vai para o cadastro de vaga.

### Competências (MS Competências)

- `GET /competencias` lista o catálogo (`id` e `nome`), usado na tela em que o candidato marca as dele.
- `POST /competencias` recebe `idCandidato` e `competencias` e **soma** as novas às que o candidato já tem: nomes sem espaços das pontas, vazios descartados, repetidos (ignorando caixa) contam uma vez, nome com mais de 50 caracteres é `400`, lista vazia depois da limpeza é `400`, candidato inexistente é `404`. A gravação é tudo ou nada. Sucesso é `201` com o cadastro do candidato, sem a senha (pendência conhecida).
- **Catálogo:** `POST /competencias/catalogo` cadastra uma competência avulsa (`{nome}`, até 50 caracteres) e responde `201` com `Location: /competencias/{id}`; `PUT /competencias/{id}` renomeia; nome já existente, ignorando caixa, é `409` com `Essa competência já está cadastrada`. `DELETE /competencias/{id}` é recusado com `400` se a competência estiver ligada a candidato ou vaga. Não existe `GET /competencias/{id}`.
- O frontend ainda troca o nome digitado pelo já cadastrado (`usarNomeJaCadastrado`), mas o MS já reaproveita em qualquer caixa.

### Vagas (MS Vagas)

- Obrigatórios: `nome`, `estado`, `cidade` e uma empresa existente (empresa inexistente é `400`). Vaga não tem as regras mais rígidas do cadastro: valor maior que a coluna (por exemplo, `estado` com mais de 2 letras) chega ao banco e responde `500`, como na API antiga — decisão do autor (revisão `AP-05`).
- A resposta traz `empresa: {id, nome}` (pendência conhecida). `GET /vagas?idEmpresa={id}` filtra pelas vagas de uma empresa.
- O `PUT` substitui os dados e as competências da vaga pelas enviadas e **não muda a empresa dona** (o `idEmpresa` enviado é ignorado).
- A exclusão apaga antes as competências, as curtidas nos três sentidos, os matches e os vínculos `recrutador_vaga` da vaga, numa transação.

### Curtida (MS Curtidas)

- **Candidato → Vaga:** grava `(id_candidato, id_vaga)` em `curtida_candidato_vaga`.
- **Empresa → Candidato, para uma vaga específica dela:** grava `(id_empresa, id_candidato, id_vaga)` em `curtida_empresa_candidato`. O `CurtidaService` compara o `idEmpresa` enviado com a empresa dona da vaga (diferente → `400`), e o banco garante com a FK composta `(id_empresa, id_vaga) → vagas(id_empresa, id)`.
- **Recrutador → Candidato, para uma vaga atribuída a ele:** grava `(id_recrutador, id_candidato, id_vaga)` em `curtida_recrutador_candidato` e vale como interesse **da empresa naquela vaga**. Vaga de outra empresa ou não atribuída a ele é `400`; o banco garante com a FK composta `(id_recrutador, id_vaga) → recrutador_vaga(id_recrutador, id_vaga)`.
- `POST /curtidas` atende os três sentidos conforme o campo `tipo` (`candidato`, `empresa` ou `recrutador`) e responde `201` com `{"match": true|false}`. Candidato, vaga ou recrutador inexistente é `404`; `tipo` fora dos três valores é `400`; `GET /curtidas` é `405` com `Allow: POST`.
- Curtidas são únicas: o `INSERT ... ON CONFLICT DO NOTHING` não insere a repetida, e a resposta é `201` com `match: false`, sem avisar que já havia sido curtido. Não existe "descurtir".

### Match

- O match acontece quando, **para a mesma vaga**, o candidato curtiu a vaga **e** o outro lado curtiu esse candidato — a empresa dona da vaga **ou** um recrutador com aquela vaga atribuída. A ordem das curtidas não importa.
- Após cada curtida, o `CurtidaService` chama `MatchRepositorio.criarMatchSePossivel`: um `INSERT ... SELECT` em `matches` que só insere se as duas curtidas existirem e a vaga for da empresa. Devolve `true` apenas quando o match foi criado naquele momento.
- `matches.id_recrutador` guarda quem fechou a reciprocidade: o recrutador da curtida de recrutador **mais antiga** daquele candidato naquela vaga, ou nulo quando só a empresa curtiu. Como o `UNIQUE (id_candidato, id_empresa, id_vaga)` não mudou, só o primeiro fecha.
- O match é do trio candidato–empresa–vaga: o mesmo candidato pode ter vários matches com a mesma empresa, um por vaga.
- Quem acabou de curtir fica sabendo do match na hora (`match: true`), e o frontend mostra isso acima da fila de cards. Não há rota nem tela que liste os matches existentes.

### Recrutador (MS Usuários)

- O recrutador é colaborador de **uma** empresa (`id_empresa` obrigatório), tem login próprio e não é candidato nem empresa. Quem cadastra, lista, atualiza e exclui é só a empresa: não há autocadastro, e o recrutador não cria, não edita e não exclui vagas.
- A **área** vem da lista fixa do enum `model/AreaDeRecrutamento` (`TI`, `RH`, `Marketing`, `Segurança da Informação`, `Vendas`, `Financeiro`, `Outros`). Ela é informativa e serve para a empresa ver a contagem de recrutadores por área, calculada no frontend a partir de `GET /recrutadores?idEmpresa={id}`.
- A validação fica em `service/ValidadorDeRecrutador`: obrigatórios, tamanhos das colunas, e-mail com formato, CPF de 11 dígitos (só dígitos ou `000.000.000-00`, sem conferir dígito verificador), data `AAAA-MM-DD` existente e não futura, senha de 6 a 50 usada exatamente como enviada. A mensagem cita o campo e **nunca repete o valor**.
- **E-mail e CPF são únicos só dentro de `recrutadores`** — podem existir em `candidatos` ou `empresas`, porque o login recebe o `tipo`. Repetido é `400` com a mensagem genérica `Já existe um recrutador com esses dados` (não `409`, para manter o contrato antigo).
- **Atualização:** `PUT /recrutadores/{id}` substitui os dados, inclusive a senha, e não muda a empresa. Não há tela no site para isso.
- **Escopo de atuação:** a empresa atribui explicitamente as vagas de cada recrutador em `recrutador_vaga`, e ele só vê e só curte nessas. Atribuir vaga de outra empresa é `400`; atribuição repetida não é erro e não grava nada. Desatribuir apaga as curtidas daquele recrutador **naquela vaga** e zera o `id_recrutador` dos matches daquele vínculo; os matches continuam valendo, porque são da empresa.
- **Exclusão:** apaga os vínculos e as curtidas dele e zera o `id_recrutador` dos matches que ele fechou.
- **Rotas:** `POST /recrutadores`, `GET /recrutadores?idEmpresa={id}`, `PUT`/`DELETE /recrutadores/{id}`, `GET`/`POST /recrutadores/{id}/vagas` e `DELETE /recrutadores/{id}/vagas/{idVaga}`. Os `DELETE` respondem `204` sem corpo.
- Senha, CPF e data de nascimento do recrutador **nunca saem do banco**: as consultas de leitura nem leem essas colunas — ver [Princípio do Anonimato](#princípio-do-anonimato-restrição-obrigatória).

### Login e sessão (MS Usuários e frontend)

- `POST /login` recebe `tipo` (`candidato`, `empresa` ou `recrutador`), `email` e `senha`. O `LoginService` busca pelo e-mail (`buscarParaLogin`, a única consulta que lê a coluna `senha`) e compara a senha **exatamente como enviada**, em texto puro.
- Credenciais corretas respondem `200` com `{tipo, id, nome}` — e, para o recrutador, também `idEmpresa`. Nada mais. E-mail ou senha errados respondem `401` com a mensagem genérica `E-mail ou senha inválidos`; `tipo` fora dos três valores é `400`.
- **Não há sessão no servidor:** nem token, nem cookie. Os MS não sabem quem está chamando e nenhuma rota exige login. Quem está logado é só o que o frontend guarda no `sessionStorage` (`sessao.ts`) e reenvia como `idCandidato`/`idEmpresa`/`idRecrutador`; qualquer cliente pode enviar outro `id`.
- No frontend, cada página declara no `<body>` quem pode vê-la (`data-perfil-exigido`, que aceita uma lista como `"empresa,recrutador"`, ou `data-somente-deslogado`) e `navegacao.ts` redireciona quem não se encaixa. É conveniência de navegação, não segurança.

### Outras regras

- Índice de afinidade (só no frontend, `calcularAfinidade` em `ts/afinidade.ts`): competências em comum ÷ total de competências da vaga, em porcentagem arredondada, ignorando maiúsculas/minúsculas e espaços das pontas; vaga sem competências dá 0%.
- As filas de cards vêm em ordem decrescente de afinidade, com empate desfeito por sorteio (`ordenarPorAfinidade` embaralha antes de ordenar). Na fila de candidatos de uma vaga, quem tem 0% de afinidade não aparece; na fila de vagas do candidato, todas as vagas aparecem.
- Com um MS fora do ar, só as telas que dependem dele mostram "Não foi possível conectar ao servidor".

## Princípio do Anonimato (restrição obrigatória)

**Esta regra vale para qualquer feature, em qualquer camada (microsserviços, frontend, banco, logs, testes, mensagens de erro). Nenhuma mudança pode expor dados fora do que está na tabela abaixo.**

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
| **Recrutador → outros recrutadores** | Nada: nenhuma rota ou tela do recrutador lista colegas | Nada |

Regras derivadas:

1. Antes do match são proibidos para a empresa: nome, sobrenome, e-mail, CPF, data de nascimento, país e CEP do candidato. Para o candidato: nome, e-mail, CNPJ, país, CEP e descrição da empresa. Para o recrutador valem os mesmos campos proibidos à empresa.
2. A revelação pós-match vale apenas entre as duas partes daquele match, não para terceiros.
3. **A senha nunca é exibida nem devolvida**, em nenhum momento, por nenhuma camada (nem em JSON, nem em log, nem em mensagem de erro). Ela só é lida do banco no login. **CPF e data de nascimento do recrutador também nunca saem do banco**: não aparecem em `Json.deRecrutador`, em tela, em log nem em mensagem de erro.
4. Curtir não revela nada: uma parte não fica sabendo quem a curtiu até existir o match. A resposta de `POST /curtidas` traz só `match`.
5. Ao exibir candidatos para uma empresa ou para um recrutador, a tela mostra só `Candidato {id}`, formação, descrição e competências. Ao exibir vagas para um candidato não inclua a empresa. Ao exibir recrutadores para a empresa, nunca inclua CPF, data de nascimento ou senha.
6. Se uma feature parecer exigir a exposição de um dado fora da tabela (ou se não estiver claro de qual lado é a tela), **pergunte antes de implementar**.
7. O recrutador é interno à empresa: nenhuma saída, em nenhum momento, revela ao candidato que recrutadores existem ou qual deles o analisou. Curtida de recrutador vale como interesse da empresa, e é a empresa que aparece no match.

### Pendências conhecidas (código ainda fora da regra)

Replicadas da API antiga na feature `backend-micronaut`, por decisão do autor, para preservar o contrato. Não amplie nenhuma delas, não as use como exemplo a seguir e pergunte antes de alterá-las:

- **Leituras abertas:** `GET /vagas` (MS Vagas) devolve `empresa: {id, nome}`; `GET /candidatos`, `GET /candidatos/{id}`, `GET /empresas` e `GET /empresas/{id}` (MS Usuários) devolvem o cadastro completo (sem a senha) para qualquer cliente, sem autenticação. O frontend depende dessas rotas: as telas da empresa usam `GET /candidatos` e a do candidato usa `GET /vagas`.
- **`POST /competencias`** (MS Competências): responde com o perfil completo do candidato (nome, sobrenome, CPF, e-mail, data de nascimento, país e CEP, sem a senha) e aceita qualquer `idCandidato`, sem autenticação. Quem souber um `id` acrescenta competências e recebe o cadastro de volta. O frontend ignora essa resposta.
- **Frontend recebe mais do que mostra:** as telas da empresa trazem o cadastro completo de todos os candidatos e só renderizam `Candidato {id}`, formação, descrição e competências; a tela do candidato traz `empresa: {id, nome}` em cada vaga e não exibe a empresa. O dado proibido chega ao navegador e aparece no DevTools.
- **Nenhuma rota tem autenticação:** existe `POST /login`, mas nenhuma outra rota confere quem chama. Qualquer cliente pode chamar `GET /candidatos`, `POST /curtidas` ou `POST /competencias` com o `id` que quiser.
- **`PUT` e `DELETE` sem autenticação:** os `PUT` e `DELETE` de candidato, empresa, vaga, competência e recrutador também ficam abertos — ampliação autorizada pelo autor na spec `backend-micronaut`. Quem souber um `id` altera ou exclui o registro. Não expõem dado novo (as respostas são as mesmas dos `GET`), mas o risco de integridade aumentou.
- **Rotas de recrutador sem autenticação:** aceitam qualquer `idEmpresa` e `idRecrutador`. Quem souber o `id` de uma empresa cadastra, altera e remove recrutadores dela e lê nome, área, descrição e e-mail deles. A spec do recrutador (seção 4.3) autorizou e deixou a autenticação para feature própria.
- **Senha em texto puro:** candidato, empresa e recrutador gravam a senha sem hash na coluna `senha`, e o `POST /login` compara em texto puro. Ela nunca é devolvida nem registrada em log, mas está legível no banco e trafega assim no corpo da requisição (HTTP, sem TLS, em desenvolvimento). Corrigir exige alterar as colunas, os cadastros e o login.

Resolvidas pela remoção do backend antigo: o CLI imprimindo o nome da empresa na lista de vagas antes do match, o `Match.descrever()` incompleto e o `microservico-cadastro` sem login.

## Banco de dados

- **Scripts:** um único arquivo, `database/Linketinder.sql`, na ordem: criação das tabelas base → carga inicial (competências, candidatos, empresas, vagas, vínculos) → consultas de exemplo → tabelas de curtida e match → constraint empresa/vaga → curtidas e matches de exemplo → bloco do recrutador. O DER está em `assets/der.png`.
- **Conexão:** cada MS lê `LINKETINDER_BANCO_URL` (padrão `jdbc:postgresql://localhost:5432/linketinder`), `LINKETINDER_BANCO_USUARIO` (padrão `postgres`) e `LINKETINDER_BANCO_SENHA` (**sem padrão**, obrigatória) no `application.yml`. `Aplicacao.main` confere a senha antes de subir e, sem ela, encerra com uma mensagem que cita só o nome da variável.
- **Como aplicar:** o script não cria o banco e não é idempotente (sem `DROP`/`IF NOT EXISTS`), então rode uma única vez em um banco vazio:

  ```bash
  createdb -U postgres linketinder
  psql -U postgres -d linketinder -f database/Linketinder.sql
  ```

  Sem `psql` no PATH, abra o arquivo no pgAdmin ou no console SQL do IntelliJ e execute.
- **Problema conhecido no script:** o último `SELECT` antes do bloco `-- curtidas` está sem `;`. Executado de uma vez, o `CREATE TABLE "curtida_candidato_vaga"` falha junto com ele e tudo que depende dessa tabela também. Até ser corrigido, execute o bloco de curtidas/matches separadamente. Pergunte antes de corrigir o arquivo.
- **SQL específico do PostgreSQL:** os DAOs usam `RETURNING` e `ON CONFLICT`. Só o PostgreSQL é suportado.
- **Mudanças de schema:** não há migrações. Qualquer alteração precisa ser refletida em `database/Linketinder.sql`, nos DAOs de todos os MS que usam a tabela e no DER — **pergunte antes de alterar o schema**.

## Como executar

Cada MS roda a partir da própria pasta; no Windows use `gradlew.bat` (ou `./gradlew` no Git Bash).

- **Microsserviços:** exigem o PostgreSQL no ar com o script aplicado e a variável `LINKETINDER_BANCO_SENHA`. Abra cada pasta `microsservicos/ms-*` em uma janela própria do IntelliJ, com a *Gradle JVM* no JDK 25, e rode a classe `Aplicacao` (ou a task `run`) com a variável em *Environment variables*. Pelo terminal:

  ```bash
  cd microsservicos/ms-usuarios
  LINKETINDER_BANCO_SENHA=<sua-senha> ./gradlew run
  ```

  ```powershell
  $env:LINKETINDER_BANCO_SENHA = "<sua-senha>"; .\gradlew.bat run
  ```

  Portas padrão: MS Usuários `8081`, MS Competências `8082`, MS Vagas `8083`, MS Curtidas `8084`; troque com `MICRONAUT_SERVER_PORT`. A origem liberada no CORS vem de `LINKETINDER_FRONTEND_ORIGEM` (padrão `http://localhost:5173`). Rotas e exemplos estão no `README.md`.
- **Frontend:** exige os MS no ar; sem um deles, as telas que dependem dele mostram "Não foi possível conectar ao servidor". As URLs base têm padrão nas portas `8081` a `8084` e podem ser trocadas por `VITE_URL_USUARIOS`, `VITE_URL_COMPETENCIAS`, `VITE_URL_VAGAS` e `VITE_URL_CURTIDAS`, em um `frontend/.env` que **não** vai para o repositório.

  ```bash
  cd frontend
  npm install
  npm run dev        # servidor Vite em http://localhost:5173
  npx tsc --noEmit   # checagem de tipos
  ```

  O fluxo começa em `index.html` (escolher candidato, empresa ou recrutador) e a sessão fica no `sessionStorage`: fechar a aba desloga.

## Como testar

Cada MS tem a própria suíte, rodada na pasta dele. Mudou algo em um MS, rode a suíte dele; mudou algo repetido nos quatro (formato de erro, CORS, log), rode as quatro.

```bash
cd microsservicos/ms-usuarios
./gradlew test                                                                      # suíte do MS
./gradlew test --tests "org.linketinder.usuarios.service.CandidatoServiceSpec"      # uma spec
```

- Os testes **não precisam de banco**. Os services são testados com `Mock` das interfaces de `repositorio`. As rotas sobem o servidor Micronaut em porta aleatória (`@MicronautTest`), com os DAOs trocados por `@MockBean` e o datasource desligado em `src/test/resources/application-test.yml`.
- **Mocks só de interfaces:** o byte-buddy usado hoje não gera classes para o JDK 25, então os testes simulam só interfaces. Por isso `RegistroDoServidor` é interface, com a implementação `RegistroDoServidorEmLog`.
- Os dados de teste vêm de `DadosFicticios` de cada MS: e-mails em `example.com` e documentos com dígito verificador inválido.
- Os DAOs e a transação dependem de conferência manual contra o PostgreSQL real (`curl` e consulta ao banco).
- O frontend não tem testes unitários e não precisa ter (dispensado pelo desafio); valide com `npx tsc --noEmit` e manualmente no navegador.
- Toda mudança de comportamento no backend deve vir com spec nova ou ajustada, e a suíte deve passar antes de considerar a tarefa concluída.

## Convenções

- **Idioma:** tudo em português — classes, métodos, variáveis, mensagens e nomes de teste (`def "curtida repetida não é erro e não fecha match novo"()`).
- **Clean Code** é requisito do projeto: nomes autoexplicativos, funções pequenas e com uma responsabilidade, DRY dentro de cada MS, comentário só quando estritamente necessário, tratamento de erros explícito.
- **Camadas em cada MS:**
  - `http`: controllers recebem e devolvem JSON, sem regra de negócio nem SQL; a conversão fica em `Json` (`deX` escreve — e nunca inclui `senha`) e a leitura do corpo em `CorpoJson`;
  - `service`: concentra as regras e lança `RegraDeNegocioException`, `RecursoNaoEncontradoException` ou `CadastroRepetidoException`;
  - `repositorio`: interfaces; `dao`: só persiste e implementa uma interface de `repositorio`;
  - `banco`: `ConexaoBanco` abre conexões pelo `DataSource` injetado; `TransacaoJdbc` faz a transação por thread.
- **Micronaut:** beans com `@Singleton`, dependências por construtor apontando para interfaces; controllers com `@ExecuteOn(TaskExecutors.BLOCKING)`, porque o JDBC bloqueia e não pode rodar no event loop do Netty. Exceção nova de regra ganha um `Tratador*` em `http/`.
- **Microsserviços independentes:** nenhum MS importa código de outro nem chama outro por HTTP. O que é comum é repetido em cada um; ao mudar algo repetido, mude nos quatro.
- **Leituras sem dado restrito:** consultas de listagem e de busca por id não leem a coluna `senha` (nem CPF e data de nascimento do recrutador). Só `buscarParaLogin` lê a senha.
- **Log:** só o `RegistroDoServidorEmLog` escreve no log das requisições; o `Aplicacao` só escreve a mensagem de configuração inválida antes de subir.
- **SQL:** sempre por `conexaoBanco.executar(sql) { PreparedStatement statement -> ... }` com parâmetros `?`; nunca concatene valor de usuário na query.
- **Estilo Groovy:** tipos explícitos em vez de `def`; sem modificador de visibilidade em classes e métodos públicos; dependências como `private final`; constantes `static final` em `MAIUSCULAS`; indentação de 4 espaços; cabeçalho `@author Guilherme Lima Conte` nas classes.
- **Frontend:** uma tela por módulo em `ts/telas/`; o acesso aos MS fica em `ts/api/` (`http.ts` tem a URL base de cada MS, monta a requisição e lança `ErroApi`; `api.ts` tem uma função por endpoint, usando o cliente do MS daquela rota) e nenhum outro módulo chama `fetch`; tipos em `modelos.ts`, DOM e mensagens em `dom.ts`, sessão em `sessao.ts`, caminhos entre páginas em `navegacao.ts`. Funções exportadas com nomes em português. `main.ts` é o único ponto de entrada e só liga o que existe na página aberta.
- **Nada de `innerHTML`:** texto vindo do usuário ou do banco entra no DOM por `textContent` ou por elementos criados com `createElement`.

## Restrições

- Respeite o [Princípio do Anonimato](#princípio-do-anonimato-restrição-obrigatória) em toda feature.
- Nunca exponha senha em saída, JSON ou log.
- Não altere o schema, o contrato da API nem as dependências dos `build.gradle` dos MS ou do `package.json` sem perguntar.
- Não crie comunicação HTTP entre MS, código compartilhado entre eles, API gateway nem Docker sem pedido explícito.
- Não quebre a separação de camadas (ex.: SQL fora de `dao`, regra de negócio no controller, `fetch` fora de `ts/api/`).
- **Use dados fictícios.** Em scripts de carga e testes, nunca use CPFs, e-mails ou nomes reais. O mesmo vale para exemplos na documentação e para cadastros feitos em conferência manual, que devem ser apagados ao final.
- **Nunca faça commit de credenciais.** Senhas de banco, chaves de API e arquivos `.env` ficam fora do repositório: leia de variável de ambiente e não escreva o valor em código, teste, script ou documentação. Os `.gitignore` da raiz do projeto, do `frontend/` e de cada MS ignoram `.env`.
- Não faça commit nem push por conta própria.
- Encontrou inconsistência, pendência ou algo não coberto aqui? Volte à [Regra nº 1](#regra-nº-1-na-dúvida-pergunte).

### Pendências conhecidas (dados)

- A carga inicial (`database/Linketinder.sql`) usa e-mails em domínio real (`@gmail.com`), que podem pertencer a alguém. Não amplie, não use como modelo e pergunte antes de corrigir.

Resolvidas pela remoção do backend antigo: a senha de banco fixa em `ProvedorPostgreSQL`/`ProvedorMySQL` (os MS leem de variável de ambiente) e os e-mails `@gmail.com` das specs de `src/test`. O `.gitignore` da raiz do projeto e o do `frontend/` passaram a ignorar `.env`.
