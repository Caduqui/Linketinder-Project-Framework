# Plano de implementação: recrutador

Implementa `docs/specs/recrutador/spec.md`, aprovada pelo autor em 2026-10-13. Os códigos `RN-xx` e `CA-xx` são as regras e os critérios de aceite da spec.

Situação do plano: aprovado pelo autor em 2026-10-13 e **implementado por inteiro** — as 15 tarefas estão concluídas. A revisão final está em `revisao.md`, que inclui a conferência em execução de 2026-10-14: `CA-17` a `CA-19` verificados contra PostgreSQL, Tomcat e CLI reais, e `CA-20` verificado menos a passada visual no navegador, que segue com o autor.

## Ponto de partida

- Branch `task-rewrite`, recriada a partir da `main` em `15e7d1a` ("feat: fila de cards, curtidas e área da empresa").
- As atualizações de documentação da revisão de 2026-10-13 e os documentos desta feature são registrados em commits separados antes das tarefas, para manter cada commit de implementação isolado.
- `./gradlew test` passa por inteiro e `npx tsc --noEmit` passa em `frontend/` no estado atual da branch.

## Decisões de implementação

| Tema | Decisão |
| --- | --- |
| Testes | Specs Spock novas em `src/test/groovy/org/example/`, espelhando o pacote da classe: `model/RecrutadorSpec`, `service/ValidadorDeRecrutadorSpec`, `service/RecrutadorServiceSpec`, `service/CurtidaServiceSpec` (ampliada), `controller/RecrutadorControllerSpec`, `controller/CurtidaControllerSpec` (ampliada), `view/RecrutadorViewSpec`, `api/RecrutadorServletSpec`, `api/LoginServletSpec` (ampliada), `api/CurtidaServletSpec` (ampliada), `api/JsonSpec` (ampliada). Nenhuma dependência nova |
| Onde fica a validação | Em `service/ValidadorDeRecrutador`, não na `view`. É a primeira validação de formato no `service` do projeto principal (hoje a senha é validada em `view/EntradaDados` e os campos da API em `api/Json`), e é o que faz CLI e API compartilharem as regras da RN-04, como a spec exige. `RecrutadorView` apenas lê e repassa; as mensagens vêm da `RegraDeNegocioException` |
| Lista de áreas | Enum `model/AreaDeRecrutamento`, com o nome de exibição de cada área (`TI`, `RH`, `Marketing`, `Segurança da Informação`, `Vendas`, `Financeiro`, `Outros`). A coluna `area` guarda o nome; a validação compara com o nome exato e a mensagem de erro lista as opções (RN-03) |
| Organização do código | `model/Recrutador`, `model/AreaDeRecrutamento`, `repositorio/RecrutadorRepositorio`, `dao/RecrutadorDAO`, `service/RecrutadorService`, `service/ValidadorDeRecrutador`, `controller/RecrutadorController`, `view/RecrutadorView`, `api/RecrutadorServlet`. `CurtidaRepositorio`/`CurtidaDAO`, `MatchRepositorio`/`MatchDAO`, `CurtidaService`, `CurtidaController`, `CurtidaView`, `EmpresaService`/`EmpresaDAO`, `CandidatoDAO`, `VagaDAO`, `Json`, `Dependencias`, `Main`, `MenuController`, `MenuView` e `CorsFilter` são alterados |
| Criação do match | Uma única consulta em `MatchDAO.criarMatchSePossivel(idCandidato, idEmpresa, idVaga)` passa a aceitar as duas origens de curtida (empresa ou recrutador daquela empresa com a vaga atribuída) e grava em `id_recrutador` o recrutador da **curtida de recrutador mais antiga** daquele candidato naquela vaga, ou nulo quando só a empresa curtiu. A assinatura não muda, então os três fluxos (candidato, empresa e recrutador) continuam chamando o mesmo método, e RN-20 e RN-21 saem da mesma regra |
| `DELETE` na API | `ApiServlet` ganha `responderSemConteudo` (204) e passa a calcular o `Allow` do `405` por servlet (método sobreponível `metodosAceitos()`, com `"GET, POST"` como padrão); `RecrutadorServlet` devolve `"GET, POST, DELETE"`. Os outros servlets não mudam de comportamento |
| Banco | **Muda** (RN autorizadas na spec): tabelas `recrutadores`, `recrutador_vaga` e `curtida_recrutador_candidato`, coluna `matches.id_recrutador`, FKs (inclusive a composta `(id_recrutador, id_vaga)`) e carga fictícia. Tudo acrescentado ao fim de `database/Linketinder.sql`, em bloco próprio, porque o arquivo não é idempotente e o bloco anterior (curtidas) já é executado à parte. O `;` que falta na linha 309 **não** é corrigido aqui (fora do escopo) |
| DER | `assets/der.png` não é alterado por esta feature: o autor atualiza depois (spec, seção 8.2). A tarefa 1 descreve o que mudou, para ele aplicar no diagrama |
| Ordem | Teste antes do código em toda tarefa que tem teste. Lógica isolada primeiro (model, validação, services), depois persistência, depois CLI, depois API, depois frontend, e por fim regressão e documentação |
| Commits | Um commit semântico em português por tarefa, após validar e revisar o diff. O commit leva código, testes, a situação da tarefa neste plano e a entrada do `CHANGELOG.md` |

## Estrutura ao final

```
database/Linketinder.sql                        (alterado: bloco do recrutador no fim)
src/main/groovy/org/example/
  model/
    AreaDeRecrutamento.groovy                   (novo)
    Recrutador.groovy                           (novo)
    Match.groovy                                (alterado: recrutador que fechou)
  repositorio/
    RecrutadorRepositorio.groovy                (novo)
    CurtidaRepositorio.groovy                   (alterado)
    MatchRepositorio.groovy                     (alterado)
    EmpresaRepositorio.groovy                   (alterado: possuiRecrutadores)
  dao/
    RecrutadorDAO.groovy                        (novo)
    CurtidaDAO.groovy, MatchDAO.groovy          (alterados)
    EmpresaDAO.groovy, CandidatoDAO.groovy, VagaDAO.groovy  (alterados)
  service/
    ValidadorDeRecrutador.groovy                (novo)
    RecrutadorService.groovy                    (novo)
    CurtidaService.groovy, EmpresaService.groovy            (alterados)
  controller/
    RecrutadorController.groovy                 (novo)
    CurtidaController.groovy, MenuController.groovy         (alterados)
  view/
    RecrutadorView.groovy                       (novo)
    CurtidaView.groovy, MenuView.groovy         (alterados)
  api/
    RecrutadorServlet.groovy                    (novo)
    ApiServlet.groovy, Json.groovy, Dependencias.groovy,
    LoginServlet.groovy, CurtidaServlet.groovy, CorsFilter.groovy  (alterados)
  Main.groovy                                   (alterado)
src/test/groovy/org/example/                    (specs novas e ampliadas, espelhando os pacotes)
frontend/
  index.html                                    (alterado: terceiro perfil)
  paginas/
    login.html, minhasVagas.html, vagaCandidatos.html       (alterados: aceitam recrutador)
    recrutadores.html                           (novo: tela da empresa)
  ts/
    modelos.ts, sessao.ts, navegacao.ts, api/http.ts, api/api.ts  (alterados)
    telas/
      telaRecrutadores.ts                       (novo)
      telaMinhasVagas.ts, telaCandidatosDaVaga.ts           (alterados: dois perfis)
AGENTS.md, README.md, CHANGELOG.md              (alterados na tarefa 13)
docs/specs/recrutador/spec.md, plano.md         (situação atualizada)
```

## Como rodar os testes

```bash
./gradlew test                                                        # suíte inteira
./gradlew test --tests "org.example.service.RecrutadorServiceSpec"    # uma spec
```

Os dois comandos foram verificados nesta branch antes de escrever o plano (`--tests "org.example.service.EmpresaServiceSpec"` rodou com `BUILD SUCCESSFUL`). O microsserviço não é tocado por esta feature, então a suíte dele não precisa rodar.

Tocando o frontend:

```bash
cd frontend && npx tsc --noEmit
```

**O que os testes cobrem:** model, validação, services, controllers, views e servlets, sempre com `Mock` das interfaces de `repositorio` e sem banco — é o que verifica `CA-01` a `CA-16`.

**O que depende de conferência manual:** `RecrutadorDAO`, as alterações em `CurtidaDAO`, `MatchDAO`, `CandidatoDAO`, `VagaDAO` e `EmpresaDAO`, e o script do banco (`CA-17`, com `psql`); a API no Tomcat 9 (`CA-18`, com `curl`); o CLI contra o banco (`CA-19`); o site no navegador (`CA-20`). Nenhum DAO tem teste automatizado no projeto, e esta feature não muda isso.

## Tarefas

### 1. Banco: tabelas, coluna e carga fictícia [Concluída]

- **O que:** acrescentar ao fim de `database/Linketinder.sql`, em bloco próprio, as tabelas `recrutadores`, `recrutador_vaga` e `curtida_recrutador_candidato`, a coluna `matches.id_recrutador`, as FKs e os `UNIQUE` da seção 2 da spec (inclusive a FK composta `(id_recrutador, id_vaga) → recrutador_vaga`), mais carga fictícia: alguns recrutadores de empresas da carga existente, atribuições e curtidas (RN-01, RN-09, RN-11, RN-17, RN-18, modelo de dados da spec).
- **Arquivos:** `database/Linketinder.sql`.
- **Testes (escritos antes):** não se aplica — o projeto não tem teste de schema.
- **Validação:** `CA-17`, manual: aplicar o bloco em um banco com o script antigo já carregado e conferir que a curtida duplicada é recusada pelo `UNIQUE`, que a curtida em vaga não atribuída é recusada pela FK composta e que `matches.id_recrutador` aceita nulo. Conferir também que nenhuma tabela existente foi alterada além da coluna em `matches`.
- **Anonimato:** a carga usa **dados fictícios** (nomes inventados, e-mails em `example.com`, CPFs com dígito verificador inválido), como manda o `AGENTS.md`. Nenhuma senha real. O script não exibe nada: é carga.
- **CHANGELOG:** Adicionado — Tabelas `recrutadores`, `recrutador_vaga` e `curtida_recrutador_candidato`, e o recrutador que fechou o match em `matches` (spec `recrutador`, tarefa 1).
- **Situação:** implementação concluída; `CA-17` verificado em execução na revisão de 2026-10-14 (ver `revisao.md`, seção 7.1): o script inteiro aplicou em banco vazio e as asserções de `UNIQUE`, FK composta e coluna anulável passaram. A coluna `matches.id_recrutador` ficou no `CREATE TABLE "matches"`, no meio do arquivo, em vez de só no bloco do fim; a revisão de 2026-10-14 acrescentou `ALTER TABLE matches ADD COLUMN IF NOT EXISTS id_recrutador` no início do bloco do recrutador, para ele também poder ser aplicado em banco já carregado (ver `revisao.md`, AP-01).
- **Para o autor:** o DER (`assets/der.png`) precisa ganhar a entidade Recrutador (1:N com Empresa), a N:N Recrutador–Vaga, a N:N Recrutador–Candidato por vaga e a ligação opcional Match–Recrutador.

### 2. Model: `Recrutador` e `AreaDeRecrutamento` [Concluída]

- **O que:** criar o enum das áreas e a classe de modelo, com `descrever()` e `descreverParaEmpresa()` que **nunca** incluem senha, CPF e data de nascimento (RN-01, RN-02, RN-03, RN-09).
- **Arquivos:** `src/main/groovy/org/example/model/AreaDeRecrutamento.groovy`, `src/main/groovy/org/example/model/Recrutador.groovy`; `src/test/groovy/org/example/model/RecrutadorSpec.groovy`.
- **Testes (escritos antes):**
  - given um recrutador com todos os campos, when `descrever()`, then o texto traz id, nome, área, descrição e e-mail;
  - given o mesmo recrutador, when `descrever()` e `descreverParaEmpresa()`, then o texto **não contém** a senha, o CPF nem a data de nascimento;
  - given cada valor do enum, when é pedido o nome da área, then devolve o nome de exibição esperado.
- **Validação:** `./gradlew test --tests "org.example.model.RecrutadorSpec"` e a suíte inteira; `CA-16` na parte de model.
- **Anonimato:** esta tarefa é exatamente a garantia da RN-09 no model. O teste prova que senha, CPF e data de nascimento não aparecem em nenhuma representação em texto.
- **CHANGELOG:** Adicionado — Modelo `Recrutador` e as áreas de recrutamento, com descrição que nunca mostra senha, CPF nem data de nascimento (spec `recrutador`, tarefa 2).
- **Situação:** concluída.

### 3. Validação do cadastro [Concluída]

- **O que:** criar `ValidadorDeRecrutador` com todas as regras da RN-04 e a área da RN-03: obrigatórios, tamanhos, formato de e-mail, CPF de 11 dígitos (só dígitos ou pontuado), data `AAAA-MM-DD` existente e não futura, senha de 6 a 50, remoção dos espaços das pontas e mensagens que citam o campo e **não** repetem o valor (RN-03, RN-04, RN-09).
- **Arquivos:** `src/main/groovy/org/example/service/ValidadorDeRecrutador.groovy`; `src/test/groovy/org/example/service/ValidadorDeRecrutadorSpec.groovy`.
- **Testes (escritos antes):**
  - given cada campo obrigatório ausente ou em branco, when valida, then lança `RegraDeNegocioException` citando aquele campo;
  - given e-mail sem `@`, sem domínio com ponto, com espaço ou acima de 200 caracteres; CPF com 10 dígitos ou com pontuação fora dos dois formatos aceitos; data `2030-01-01`, `2026-02-30` ou `13/10/2026`; senha com 5 e com 51 caracteres, when valida, then lança a exceção certa em cada caso;
  - given área `Jurídico`, when valida, then a mensagem cita `area` e lista as áreas válidas;
  - given campos válidos com espaços nas pontas, when valida, then remove esses espaços dos campos exceto da senha, que permanece exatamente como enviada; CPF não é reformatado nem a caixa do e-mail é alterada;
  - given qualquer falha, when a mensagem é lida, then ela não contém o valor enviado.
- **Validação:** `./gradlew test --tests "org.example.service.ValidadorDeRecrutadorSpec"` e a suíte inteira; `CA-02`, `CA-03`, `CA-04`.
- **Anonimato:** cobre a parte da RN-04 e da RN-09 que vale para mensagens de erro: nenhum valor enviado (e nenhum CPF ou e-mail) aparece no texto.
- **CHANGELOG:** Adicionado — Validação do cadastro de recrutador, com mensagens que citam o campo e nunca repetem o valor (spec `recrutador`, tarefa 3).
- **Situação:** concluída.

### 4. Repositório e service: cadastro, consulta e exclusão [Concluída]

- **O que:** criar a interface `RecrutadorRepositorio` e o `RecrutadorService` com cadastrar (validando e recusando e-mail ou CPF repetido com a mensagem genérica), listar por empresa, buscar por id, atualizar, excluir e autenticar; e alterar `EmpresaService`/`EmpresaRepositorio` para recusar a exclusão de empresa com recrutadores (RN-01, RN-05, RN-06, RN-07, RN-10, RN-24).
- **Arquivos:** `repositorio/RecrutadorRepositorio.groovy` (novo), `repositorio/EmpresaRepositorio.groovy`, `service/RecrutadorService.groovy` (novo), `service/EmpresaService.groovy`; `src/test/groovy/org/example/service/RecrutadorServiceSpec.groovy` (nova), `service/EmpresaServiceSpec.groovy` (ampliada).
- **Testes (escritos antes):**
  - given dados válidos e e-mail e CPF livres, when cadastra, then o repositório recebe o recrutador com a empresa vinculada;
  - given e-mail já usado (e, em outro caso, CPF já usado), when cadastra, then lança a exceção com a mensagem genérica, que não diz qual campo coincidiu, e o repositório **não** é chamado para inserir;
  - given e-mail e CPF próprios (e, em outro caso, de outro recrutador), when atualiza, then mantém a atualização sem falso conflito e recusa duplicidade real;
  - given e-mail que existe em `candidatos` mas não em `recrutadores`, when cadastra, then o cadastro é aceito (RN-05);
  - given e-mail e senha corretos, when autentica, then devolve o recrutador; com senha errada ou e-mail inexistente, devolve nulo;
  - given uma empresa com recrutadores, when a empresa é excluída, then lança `RegraDeNegocioException` e o repositório não deleta;
  - given uma empresa sem recrutadores e sem vagas, when é excluída, then o repositório deleta.
- **Validação:** `./gradlew test --tests "org.example.service.RecrutadorServiceSpec"`, idem para `EmpresaServiceSpec`, e a suíte inteira; `CA-01`, `CA-05`, `CA-06` (parte do service), `CA-15` (parte da empresa).
- **Anonimato:** nenhuma saída nova; o service devolve objetos de modelo, e quem formata é a `view` ou o `Json`. O teste de autenticação confere que o retorno não é usado para expor a senha.
- **CHANGELOG:** Adicionado — Cadastro, consulta e autenticação de recrutador nas regras de negócio, e empresa com recrutadores não pode mais ser excluída (spec `recrutador`, tarefa 4).
- **Situação:** concluída.

### 5. Service: atribuição de vagas e escopo de atuação [Concluída]

- **O que:** no `RecrutadorService`, atribuir vaga (só vaga da própria empresa e só a recrutador da própria empresa; repetida não é erro e o retorno distingue se foi atribuída agora), desatribuir (apagando as curtidas daquele recrutador naquela vaga), listar as vagas atribuídas e a verificação de escopo usada pela curtida (RN-11, RN-12, RN-13, RN-14, RN-15, RN-16).
- **Arquivos:** `service/RecrutadorService.groovy`, `repositorio/RecrutadorRepositorio.groovy`; `src/test/groovy/org/example/service/RecrutadorServiceSpec.groovy`.
- **Testes (escritos antes):**
  - given recrutador da empresa A e vaga da empresa A, when atribui, then o repositório grava o vínculo;
  - given vaga da empresa B, when atribui ao recrutador da empresa A, then lança a exceção e nada é gravado;
  - given vínculo já existente, when atribui de novo, then nada é gravado e o retorno informa que já estava atribuída;
  - given três vagas da empresa e duas atribuídas, when lista as vagas do recrutador, then devolve as duas;
  - given uma vaga atribuída, when desatribui, then o repositório remove o vínculo e as curtidas daquele recrutador naquela vaga;
  - given vaga não atribuída ao recrutador, when a verificação de escopo é chamada, then lança a exceção.
- **Validação:** `./gradlew test --tests "org.example.service.RecrutadorServiceSpec"` e a suíte inteira; `CA-07`, `CA-08`, `CA-09`, `CA-15` (parte da desatribuição).
- **Anonimato:** a listagem devolve vagas da própria empresa do recrutador, o que é permitido (seção 4.1). Nenhum dado de candidato entra aqui.
- **CHANGELOG:** Adicionado — A empresa atribui e remove vagas dos recrutadores dela, e o recrutador só atua nas vagas atribuídas (spec `recrutador`, tarefa 5).
- **Situação:** concluída.

### 6. Curtida do recrutador e match [Concluída]

- **O que:** `CurtidaRepositorio`/`CurtidaDAO`/`CurtidaService` ganham a gravação da curtida do recrutador; `CurtidaService` valida o escopo pela regra da tarefa 5; `Match` passa a carregar quem fechou; `CurtidaService.houveMatch` mantém a assinatura para os três fluxos (RN-17 a RN-22). A consulta que forma match considerando curtidas de recrutadores fica na tarefa 8.
- **Arquivos:** `repositorio/CurtidaRepositorio.groovy`, `dao/CurtidaDAO.groovy`, `service/CurtidaService.groovy`, `model/Match.groovy`; `src/test/groovy/org/example/service/CurtidaServiceSpec.groovy`, `src/test/groovy/org/example/model/MatchSpec.groovy`.
- **Testes (escritos antes):**
  - given recrutador com a vaga atribuída, when curte um candidato, then o repositório grava a curtida após validar o escopo; o fluxo chama `houveMatch` para consultar o match;
  - given curtida repetida do mesmo recrutador para o mesmo candidato e vaga, when curte, then o service informa que já havia sido curtido e nada é gravado de novo;
  - given recrutador sem a vaga atribuída, when curte, then lança a exceção e o repositório de curtida **não** é chamado;
  - given o repositório de match devolvendo `true`, when a curtida é registrada, then o service informa que houve match; devolvendo `false`, informa que não houve;
  - given um `Match` com recrutador e outro sem, when é descrito, then o texto não muda em relação a hoje nem revela o recrutador (RN fora do escopo: `Match.descrever()` não é alterado).
- **Validação:** `./gradlew test --tests "org.example.service.CurtidaServiceSpec"` e a suíte inteira; `CA-10`, `CA-11`, `CA-12` e `CA-13` na parte de regra (a ordem por curtida mais antiga é SQL e fecha na tarefa 8).
- **Anonimato:** a curtida não devolve nada do candidato — só se houve match (RN-23). Os testes conferem que o retorno do service é booleano e que `Match.descrever()` não revela o recrutador.
- **CHANGELOG:** Adicionado — Curtida do recrutador em um candidato para uma vaga atribuída, valendo como interesse da empresa naquela vaga (spec `recrutador`, tarefa 6).
- **Situação:** concluída.

### 7. Persistência: `RecrutadorDAO` [Concluída]

- **O que:** implementar `RecrutadorRepositorio` em JDBC: inserir (com `RETURNING id`), buscar por id, por e-mail e por CPF, listar por empresa sem carregar CPF, data de nascimento e senha, atualizar, excluir (apagando antes vínculos e curtidas e zerando `matches.id_recrutador`), atribuir vaga (`ON CONFLICT DO NOTHING`), remover vaga (apagando as curtidas daquela vaga e zerando o recrutador dos matches dela) e listar as vagas atribuídas. `EmpresaDAO.possuiRecrutadores` foi implementado na tarefa 4 (RN-04 a RN-14, RN-24).
- **Arquivos:** `dao/RecrutadorDAO.groovy` (novo).
- **Testes (escritos antes):** não se aplica — DAO depende de banco real e o projeto não tem teste automatizado de DAO (`AGENTS.md`, "Como testar").
- **Validação:** a suíte inteira continua passando (nada deve quebrar). A conferência manual CA-17 requer aplicar o script atualizado em um banco PostgreSQL descartável: cadastrar recrutador, atribuir vaga, curtir, desatribuir (conferindo no banco que as curtidas daquela vaga saíram e o match ficou com `id_recrutador` nulo), excluir recrutador e tentar excluir a empresa com recrutadores. Todo SQL usa `conexaoBanco.executar(sql) { PreparedStatement -> }` e parâmetros `?`.
- **Anonimato:** o DAO não imprime nem formata nada; a lista da empresa seleciona somente `id`, nome, área, descrição, e-mail e `id_empresa`, e não carrega CPF, data de nascimento ou senha.
- **CHANGELOG:** Adicionado — Persistência do recrutador, das atribuições de vaga e das curtidas dele no PostgreSQL (spec `recrutador`, tarefa 7).
- **Situação:** implementação concluída; `CA-17` verificado em execução na revisão de 2026-10-14 (ver `revisao.md`, seção 7.1): o script inteiro aplicou em banco vazio e as asserções de `UNIQUE`, FK composta e coluna anulável passaram.

### 8. Persistência: curtida do recrutador e criação do match [Concluída]

- **O que:** `MatchDAO.criarMatchSePossivel` reescrito em uma consulta que aceita as curtidas da empresa e de recrutadores e grava em `id_recrutador` o recrutador da curtida mais antiga (ou nulo), mantendo `ON CONFLICT DO NOTHING` e o retorno "criado agora"; `MatchDAO.listar` passa a trazer o recrutador; `CandidatoDAO.deletar` e `VagaDAO.deletar` passam a apagar as curtidas de recrutador, e `VagaDAO` também os vínculos de vaga (RN-19, RN-20, RN-21, RN-25).
- **Arquivos:** `dao/MatchDAO.groovy`, `dao/CandidatoDAO.groovy`, `dao/VagaDAO.groovy`.
- **Testes (escritos antes):** não se aplica (DAO). A regra equivalente já está coberta nas tarefas 6 e 10 com mocks.
- **Validação:** a suíte inteira continua passando. A conferência manual de `CA-17` requer o PostgreSQL com o script atualizado e repassar os cenários de `CA-11` a `CA-14`: recrutador curte depois do candidato (match com ele); segundo recrutador curte depois (curtida gravada, nenhum match novo); dois recrutadores curtem antes e o candidato curte depois (match com o que curtiu primeiro); empresa curte e o candidato curte (match com `id_recrutador` nulo); match em duas vagas da mesma empresa com recrutadores diferentes; excluir candidato e vaga com curtidas de recrutador.
- **Anonimato:** nenhuma saída. A consulta do match seleciona ids; `Match.descrever()` não muda nesta feature.
- **CHANGELOG:** Adicionado — Match passa a ser criado também pela curtida do recrutador e registra quem o fechou (spec `recrutador`, tarefa 8).
- **Situação:** implementação concluída; `CA-17` verificado em execução na revisão de 2026-10-14 (ver `revisao.md`, seção 7.1): o script inteiro aplicou em banco vazio e as asserções de `UNIQUE`, FK composta e coluna anulável passaram.

### 9. CLI: menu de recrutadores e curtida pelo terminal [Concluída]

- **O que:** `RecrutadorView` e `RecrutadorController` (CRUD + atribuir, remover e listar vagas), opção "Recrutador curtir um candidato" no `CurtidaController`/`CurtidaView`, menu novo no `MenuController` usando o `MenuView` genérico e montagem no `Main` (RN-06, RN-07, RN-09, RN-27 e P-08).
- **Arquivos:** `view/RecrutadorView.groovy` (novo), `controller/RecrutadorController.groovy` (novo), `view/CurtidaView.groovy`, `controller/CurtidaController.groovy`, `controller/MenuController.groovy`, `Main.groovy`; `src/test/groovy/org/example/view/RecrutadorViewSpec.groovy` (nova), `controller/RecrutadorControllerSpec.groovy` (nova), `controller/MenuControllerSpec.groovy` (nova), `controller/CurtidaControllerSpec.groovy` (ampliada).
- **Testes (escritos antes):** com `EntradaFalsa.comRespostas([...])` e `SaidaConsole.capturar { ... }`:
  - given uma lista de recrutadores, when a view lista, then a saída traz id, nome, área, descrição e e-mail e **não** traz senha, CPF nem data de nascimento;
  - given as respostas de um cadastro válido, when o controller cadastra, then o service recebe os dados digitados;
  - given o service lançando `RegraDeNegocioException`, when a operação é acionada pelo menu, then a mensagem aparece na saída e o menu continua disponível;
  - given um recrutador e as vagas atribuídas a ele, when o controller lista e seleciona uma vaga, then somente as atribuídas podem ser escolhidas;
  - given recrutador, vaga atribuída e candidato, when o controller registra a curtida, then o service é chamado e a saída informa se houve match;
  - given a curtida de recrutador, when a saída é capturada, then ela usa a representação anônima do candidato e não contém nome, e-mail nem CPF do candidato.
- **Validação:** as specs direcionadas de view, controllers e menu e a suíte inteira passaram; `CA-19` (manual, com o banco) permanece pendente; `CA-16` passou nas garantias de anonimato.
- **Anonimato:** duas garantias testadas — a listagem de recrutadores sem senha, CPF e data de nascimento (RN-09) e a curtida usando `Candidato.descreverAnonimo()` (regra 5 do `AGENTS.md`).
- **CHANGELOG:** Adicionado — Menu de recrutadores no CLI, com CRUD, atribuição de vagas e a curtida do recrutador (spec `recrutador`, tarefa 9).
- **Situação:** implementação e testes automatizados concluídos; `CA-19` verificado em execução na revisão de 2026-10-14 (ver `revisao.md`, seção 7.3): CRUD, atribuição e curtida com match rodaram contra o banco real pelo CLI.

### 10. API: `Json`, `RecrutadorServlet` e as rotas de recrutador [Concluída]

- **O que:** `Json.paraRecrutador`/`deRecrutador` (sem senha, CPF e data de nascimento), `ApiServlet` com `responderSemConteudo` e `Allow` por servlet, `RecrutadorServlet` com `POST /recrutadores`, `GET /recrutadores?idEmpresa={id}`, `GET /recrutadores/{id}/vagas`, `POST /recrutadores/{id}/vagas`, `DELETE /recrutadores/{id}` e `DELETE /recrutadores/{id}/vagas/{idVaga}`, montagem em `Dependencias` e `DELETE` anunciado no `CorsFilter` (RN-05, RN-09, RN-26, P-01, P-02).
- **Arquivos:** `api/Json.groovy`, `api/ApiServlet.groovy`, `api/RecrutadorServlet.groovy` (novo), `api/Dependencias.groovy`, `api/CorsFilter.groovy`; `src/test/groovy/org/example/api/RecrutadorServletSpec.groovy` (nova), `api/JsonSpec.groovy` (ampliada), `api/CorsFilterSpec.groovy` (ampliada).
- **Testes (escritos antes):**
  - given um recrutador, when `Json.deRecrutador`, then o mapa tem id, nome, área, descrição, e-mail e idEmpresa e **não** tem senha, cpf nem dataNascimento;
  - given um corpo válido, when `POST /recrutadores`, then responde `201` com esse mapa;
  - given campo inválido ou cadastro repetido, when `POST /recrutadores`, then responde `400` com `{"erro": ...}` e a mensagem genérica no caso do repetido;
  - given uma empresa com recrutadores, when `GET /recrutadores?idEmpresa={id}`, then responde `200` com a lista; sem o parâmetro, `400`; com `id` não numérico, `400`; empresa inexistente, `404`;
  - given um recrutador, when `POST /recrutadores/{id}/vagas`, then responde `201` com as vagas atualizadas; vaga de outra empresa, `400`; recrutador inexistente, `404`;
  - given um recrutador, when `DELETE /recrutadores/{id}` e `DELETE /recrutadores/{id}/vagas/{idVaga}`, then responde `204` sem corpo;
  - given `PUT`, when chamado, then `405` com `Allow: GET, POST, DELETE`, e os outros servlets continuam com `Allow: GET, POST`;
  - given a origem permitida, when a resposta é montada, then `Access-Control-Allow-Methods` inclui `DELETE`.
- **Validação:** as specs direcionadas e a suíte inteira passaram; `.\gradlew.bat test war` gerou o WAR; `CA-01` a `CA-05` e `CA-07` a `CA-09` passaram nos testes; `CA-18` (manual, com `curl` no Tomcat 9) permanece pendente. Não foi feito deploy contra o banco local, pois não há um banco descartável definido para a conferência.
- **Anonimato:** o teste de `Json.deRecrutador` é a garantia da RN-09 na API; nenhuma resposta nova devolve dado de candidato.
- **CHANGELOG:** Adicionado — Rotas de recrutador na API REST, incluindo as primeiras rotas `DELETE` do projeto (spec `recrutador`, tarefa 10).
- **Situação:** implementação e testes automatizados concluídos; `CA-18` verificado em execução na revisão de 2026-10-14 (ver `revisao.md`, seção 7.2): WAR no Tomcat 9.0.122 e 56 asserções com `curl`.

### 11. API: login e curtida do recrutador [Concluída]

- **O que:** `LoginServlet` aceitando `tipo: "recrutador"` e devolvendo `{tipo, id, nome, idEmpresa}`; `CurtidaServlet` aceitando `tipo: "recrutador"` com `idRecrutador`, `idCandidato` e `idVaga` e respondendo `201` com `{"match": ...}` (RN-10, RN-17, RN-20, RN-23, RN-26).
- **Arquivos:** `api/LoginServlet.groovy`, `api/CurtidaServlet.groovy`; `src/test/groovy/org/example/api/LoginServletSpec.groovy`, `api/CurtidaServletSpec.groovy`.
- **Testes (escritos antes):**
  - given credenciais corretas de recrutador, when `POST /login`, then `200` com tipo, id, nome e idEmpresa, e sem senha, cpf e dataNascimento no corpo;
  - given senha errada, when `POST /login`, then `401` com a mensagem genérica, que não diz se errou e-mail ou senha;
  - given `tipo` desconhecido, when `POST /login`, then `400`;
  - given recrutador com a vaga atribuída e candidato que já curtiu, when `POST /curtidas` com `tipo: "recrutador"`, then `201` com `{"match": true}`;
  - given vaga não atribuída ao recrutador, when `POST /curtidas`, then `400` e nenhuma curtida registrada;
  - given recrutador, candidato ou vaga inexistente, when `POST /curtidas`, then `404`;
  - given a resposta da curtida, when o corpo é lido, then ele só tem `match` — nenhum dado do candidato.
- **Validação:** as specs direcionadas de `LoginServletSpec` e `CurtidaServletSpec` e a suíte inteira passaram; `.\gradlew.bat test war` gerou o WAR; `CA-06`, `CA-10` a `CA-14` na parte de API e `CA-16` passaram nos testes; a conferência manual do `CA-18` continua pendente em Tomcat 9 com banco descartável.
- **Anonimato:** o corpo do login não traz CPF, data de nascimento nem senha; o da curtida não traz nada do candidato (RN-23).
- **CHANGELOG:** Adicionado — Login do recrutador e curtida do recrutador pela API REST (spec `recrutador`, tarefa 11).
- **Situação:** implementação e testes automatizados concluídos; `CA-18` verificado em execução na revisão de 2026-10-14, incluindo login e curtida do recrutador pela API (ver `revisao.md`, seção 7.2).

### 12. Frontend: terceiro perfil, tipos e acesso à API [Concluída]

- **O que:** `TipoUsuario` ganha `recrutador`; tipos `Recrutador` (sem CPF e sem data de nascimento) e `NovoRecrutador`; funções novas em `api.ts`; `http.ts` ganha `del`; `sessao.ts` e `navegacao.ts` passam a conhecer o perfil e a página inicial do recrutador; `index.html` ganha a terceira opção de entrada e `login.html` passa a aceitar `?tipo=recrutador` (RN-10, RN-26, RN-28).
- **Arquivos:** `frontend/ts/modelos.ts`, `frontend/ts/api/http.ts`, `frontend/ts/api/api.ts`, `frontend/ts/sessao.ts`, `frontend/ts/navegacao.ts`, `frontend/ts/formularios.ts`, `frontend/ts/telas/telaAcesso.ts`, `frontend/tsconfig.json`, `frontend/index.html`, `frontend/paginas/login.html`, `frontend/paginas/recrutador.html` (destino provisório até a tela de vagas atribuídas da tarefa 14).
- **Testes (escritos antes):** o frontend não tem teste unitário e não precisa ter (`AGENTS.md`).
- **Validação:** `npx tsc --noEmit` e `npm run build` em `frontend/` passaram. O `tsconfig.json` passa a incluir o diretório `ts/`, que contém os fontes reais do frontend, para que a checagem solicitada compile o projeto. O login ponta a ponta não foi exercitado, pois depende da API e de credenciais/banco disponíveis; `CA-20` permanece parcial.
- **Anonimato:** o tipo `Recrutador` do frontend **não tem** `cpf` nem `dataNascimento`, então esses campos não podem ser renderizados por descuido. A sessão continua guardando só `tipo`, `id`, `nome` e agora `idEmpresa` do próprio usuário.
- **CHANGELOG:** Adicionado — Recrutador como terceiro perfil de acesso do site, com os tipos e as chamadas de API correspondentes (spec `recrutador`, tarefa 12).
- **Situação:** concluída; validação manual do login ponta a ponta permanece pendente. A `frontend/paginas/recrutador.html` criada aqui era provisória e foi removida na revisão de 2026-10-14: o recrutador passou a entrar direto na `minhasVagas.html`, a tela compartilhada da tarefa 14 (ver `revisao.md`, AP-02).

### 13. Frontend: tela "Recrutadores" da empresa [Concluída]

- **O que:** página e tela em que a empresa cadastra recrutador, vê a lista com a área e a contagem por área, atribui e remove vagas e exclui recrutador; link no cabeçalho, visível só para a empresa (RN-06, RN-11, RN-13, RN-15, RN-28, P-04).
- **Arquivos:** `frontend/paginas/recrutadores.html` (nova), `frontend/ts/telas/telaRecrutadores.ts` (novo), `frontend/ts/main.ts`, `frontend/ts/navegacao.ts`, `frontend/ts/dom.ts` (leitura de `textarea`), `frontend/style.css` e cabeçalhos das páginas existentes.
- **Testes (escritos antes):** não se aplica (frontend).
- **Validação:** `npx tsc --noEmit` e `npm run build` em `frontend/` passaram. A conferência manual do `CA-20` (cadastro, atribuição, remoção, exclusão e erros da API) permanece pendente, pois exige a API e o banco disponíveis.
- **Anonimato:** a tela mostra nome, área, descrição, e-mail e vagas atribuídas — nunca CPF, data de nascimento ou senha (seção 4.2). Todo texto dinâmico entra no DOM por `textContent` ou `createTextNode`, como manda o `AGENTS.md`.
- **CHANGELOG:** Adicionado — Tela de recrutadores da empresa, com cadastro, contagem por área, atribuição de vagas e exclusão (spec `recrutador`, tarefa 13).
- **Situação:** implementação concluída; `CA-20` verificado em execução na revisão de 2026-10-14 até a camada de API e de HTML servido (ver `revisao.md`, seção 7.4); a passada visual no navegador segue com o autor.

### 14. Frontend: telas do recrutador [Concluída]

- **O que:** as páginas de lista de vagas e de fila de candidatos passam a aceitar os perfis empresa e recrutador, trocando a origem dos dados (`GET /vagas?idEmpresa=` ou `GET /recrutadores/{id}/vagas`) e o corpo da curtida; o gráfico continua só para a empresa; a fila mantém afinidade decrescente e o corte dos 0% (RN-14, RN-16, RN-17, RN-23, RN-28).
- **Arquivos:** `frontend/paginas/minhasVagas.html`, `frontend/paginas/vagaCandidatos.html`, `frontend/ts/telas/telaMinhasVagas.ts`, `frontend/ts/telas/telaCandidatosDaVaga.ts`, `frontend/ts/navegacao.ts`.
- **Testes (escritos antes):** não se aplica (frontend).
- **Validação:** `npx tsc --noEmit` e `CA-20`, manual: como recrutador, ver só as vagas atribuídas, abrir a fila, curtir, ver a mensagem de match; abrir pela URL uma vaga não atribuída e conferir que nada da vaga aparece; e **conferir que o fluxo da empresa continua idêntico** ao de antes da feature.
- **Anonimato:** o card do recrutador é o mesmo da empresa (`Candidato {id}`, formação, descrição, competências, afinidade), e a vaga não atribuída não exibe dado algum. A resposta da curtida só diz se houve match.
- **CHANGELOG:** Adicionado — Telas do recrutador: vagas atribuídas e fila de candidatos com curtir e passar (spec `recrutador`, tarefa 14).
- **Situação:** concluída.

### 15. Regressão e documentação [Concluída]

- **O que:** atualizar `AGENTS.md` (recrutador no domínio, nas regras, na estrutura, nas rotas, no `DELETE` como exceção da convenção, na pendência nova de integridade das rotas de recrutador e na pendência de senha em texto puro, que passa a citar quatro cadastros), a **tabela do Princípio do Anonimato** com as linhas do recrutador (texto submetido ao autor antes de alterar o arquivo), o `README.md` (contrato com as rotas novas e o `DELETE`, e descrição do uso) e a situação da spec e das tarefas deste plano; repassar o checklist da etapa 6 da skill.
- **Arquivos:** `AGENTS.md`, `README.md`, `CHANGELOG.md`, `docs/specs/recrutador/spec.md`, `docs/specs/recrutador/plano.md`.
- **Validação:** checklist da etapa 6 preenchido:
  - `./gradlew test` passa por inteiro (com o total de testes informado) e `npx tsc --noEmit` passa em `frontend/`;
  - `CA-01` a `CA-20` repassados um a um, dizendo como cada um foi verificado;
  - Princípio do Anonimato conferido item por item, incluindo cada linha das seções 4.1 e 4.2 da spec;
  - as funcionalidades da seção 6 da spec conferidas, em especial que o fluxo da empresa (lista de vagas, fila, curtida e match) e o do candidato não mudaram;
  - nenhum arquivo fora do escopo alterado, comparando com `15e7d1a`;
  - `CHANGELOG.md` com uma entrada por tarefa, da 1 à 15.
- **Anonimato:** a tarefa inclui a revisão final das saídas tocadas e o registro das pendências; nenhuma pendência conhecida pode ter sido ampliada além do que a spec autorizou (senha em texto puro e integridade das rotas novas).
- **CHANGELOG:** Modificado — Documentação da feature do recrutador no `AGENTS.md` e no `README.md`, com a tabela do anonimato atualizada (spec `recrutador`, tarefa 15).
- **Situação:** concluída. O `AGENTS.md` e o `README.md` foram sincronizados na revisão de 2026-10-14, que também corrigiu as divergências encontradas entre o plano e o código (migração de `matches.id_recrutador`, página provisória do recrutador, gráfico restrito à empresa e a empresa na listagem do CLI). O checklist está preenchido em `revisao.md`, que na seção 7 registra a conferência em execução de 2026-10-14: `CA-17` a `CA-19` verificados e `CA-20` parcial, faltando só a passada visual no navegador.

## Pendências

- **DER (`assets/der.png`):** atualizado pelo autor depois da tarefa 1, conforme a seção 8.2 da spec.
- **Premissas P-02, P-04 e P-08** da spec continuam marcadas como a confirmar; as tarefas 10, 13 e 9, respectivamente, as implementam como estão escritas.
- **Tabela do Princípio do Anonimato no `AGENTS.md`:** aplicada na revisão de 2026-10-14, com o texto submetido ao autor e aprovado antes da edição, nos termos da seção 4 da spec.
- **Conferências manuais:** `CA-17`, `CA-18` e `CA-19` foram executados na revisão de 2026-10-14 (`revisao.md`, seção 7) contra PostgreSQL 18, Tomcat 9.0.122 e o CLI com banco real. Do `CA-20` falta só a passada visual no navegador, que depende do autor.
