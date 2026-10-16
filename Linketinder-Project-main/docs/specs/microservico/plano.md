# Plano de implementação: microsserviço de cadastro

Implementa `docs/specs/microservico/spec.md`. Os códigos `RN-xx` e `CA-xx` são as regras e os critérios de aceite da spec.

Situação do plano: aprovado pelo autor em 2026-10-11; as 8 tarefas foram concluídas em 2026-10-11.

## Ponto de partida

- Branch `feature/microservico`. O commit de partida para a conferência de escopo é `bee78b8`, o último antes dos documentos desta feature.
- `./gradlew test` na raiz: 90 testes, nenhuma falha (rodado em 2026-10-11).
- Já havia alterações não commitadas em `.idea/` e em `frontend/` (`package-lock.json` e `tsconfig.json`), anteriores a esta feature. Por decisão do autor, não são tocadas nem commitadas.

## Decisões de implementação

| Tema | Decisão |
| --- | --- |
| Pasta e build | `microservico-cadastro/` com `settings.gradle`, `build.gradle` e cópia do Gradle Wrapper da raiz, para abrir e rodar sozinho no IntelliJ. Plugins `groovy` e `application`, com `mainClass` configurado para `./gradlew run` funcionar |
| Dependências | As mesmas versões do projeto principal: Groovy 5.0.8, `groovy-json` 5.0.8, driver PostgreSQL 42.7.7, Spock 2.4. Nenhuma biblioteca nova |
| Pacote | `org.linketinder.cadastro`, diferente de `org.example`, para deixar visível que nada do projeto principal é importado |
| Organização do código | As mesmas camadas do projeto, sem `view`: `http` (servidor, roteador, handlers, JSON) → `service` (validação e regras) → `repositorio` (interfaces) ← `dao` → `banco`. Só o `Main` conhece as classes concretas |
| Roteamento | Um roteador próprio que compara o caminho inteiro. O `HttpServer` do JDK casa rotas por prefixo de texto (`/candidatosX` cairia em `/candidatos`), então não dá para usar um contexto por rota |
| Banco | Uma conexão por requisição, aberta com os dados da configuração. A gravação de competências roda em transação (commit ou rollback) |
| Cadastro repetido | Detectado pela violação de unicidade do próprio banco (SQLState `23505`), traduzida para a resposta `409`. Evita a corrida entre "consultar" e "inserir" |
| Configuração | Variáveis `LINKETINDER_CADASTRO_PORTA`, `LINKETINDER_CADASTRO_BANCO_URL`, `LINKETINDER_CADASTRO_BANCO_USUARIO` e `LINKETINDER_CADASTRO_BANCO_SENHA`. As três primeiras têm os padrões da RN-13; a senha não tem padrão e é obrigatória para subir o serviço. A configuração nunca expõe a senha em texto (log, mensagem de erro, `toString`) |
| Credenciais | Nenhuma senha em código, teste, script ou documentação. O `.gitignore` do microsserviço ignora `.env`. Para rodar, a senha é definida no terminal ou na configuração de execução local do IntelliJ, que não é commitada (RN-16) |
| Dados de teste | Só fictícios, nas specs, no README e nos cadastros de conferência manual: nomes inventados, e-mails em `example.com`, CPFs e CNPJs com dígitos verificadores inválidos (RN-16) |
| Log | Uma única classe escreve no console, e só método, rota e status |
| Testes | Specs Spock em `microservico-cadastro/src/test/groovy`, espelhando os pacotes, com nomes em português. Regras e serviços com `Mock` dos repositórios. A camada HTTP é testada de verdade: a spec sobe o servidor em uma porta livre, com serviços simulados, e chama com o `HttpClient` do JDK |
| Ordem | Teste antes do código, em toda tarefa que tem teste |
| Ciclo de cada tarefa | Definido pelo autor: implementar uma tarefa por vez e, a cada uma, rodar a aplicação e os testes, conferir se o anonimato continua preservado, atualizar o `CHANGELOG.md`, marcar a tarefa como concluída no plano e fazer um commit. Se a spec se mostrar incompleta, atualizar a spec primeiro e só depois o código |
| Commits | O agente commita, um commit por tarefa, com mensagem clara no estilo do histórico recente (`tipo: descrição`, em português). Cada commit inclui apenas os arquivos da tarefa, adicionados pelo caminho; `.idea/` e `frontend/` nunca entram |

## Estrutura ao final

```
microservico-cadastro/
  .gitignore
  settings.gradle
  build.gradle
  gradlew, gradlew.bat, gradle/wrapper/
  src/main/groovy/org/linketinder/cadastro/
    Main.groovy
    Configuracao.groovy
    http/         ServidorCadastro, Roteador, RespostaJson, CorpoJson, RegistroDeRequisicoes,
                  CandidatoHandler, EmpresaHandler, CompetenciaHandler
    model/        Candidato, Empresa
    service/      ValidadorDeCadastro, CadastroCandidatoService, CadastroEmpresaService,
                  CompetenciaDoCandidatoService, DadosInvalidosException,
                  CadastroRepetidoException, CandidatoNaoEncontradoException
    repositorio/  CandidatoRepositorio, EmpresaRepositorio, CompetenciaRepositorio, Transacao
    dao/          CandidatoDAO, EmpresaDAO, CompetenciaDAO
    banco/        ConexaoBanco, TransacaoJdbc, TradutorDeErroDeBanco
  src/test/groovy/org/linketinder/cadastro/
    (uma spec por classe com lógica, nos mesmos pacotes)

docs/specs/microservico/   spec.md, plano.md
README.md                  alterado na tarefa 7
AGENTS.md                  alterado na tarefa 8
CHANGELOG.md               alterado em todas as tarefas
```

Os nomes de classe podem ser ajustados durante a implementação; qualquer mudança é relatada na tarefa.

## Como rodar os testes

```bash
cd microservico-cadastro
./gradlew test                                                            # suíte do microsserviço
./gradlew test --tests "org.linketinder.cadastro.service.CadastroCandidatoServiceSpec"   # uma spec
cd .. && ./gradlew test                                                   # suíte do projeto principal (90 testes)
```

Verificado com um exemplo descartável, fora do repositório: um build Gradle independente, com as mesmas dependências, compilou uma classe com o `HttpServer` do JDK e rodou 2 specs Spock que chamam o servidor por HTTP em uma porta livre. O exemplo usou o wrapper da raiz (`./gradlew -p <pasta>`); o wrapper copiado para dentro da pasta será conferido na tarefa 1.

O que os testes cobrem: validação, regras de cadastro e de competências, rotas, códigos de status, formato das respostas, ausência da senha e do valor enviado nas respostas, conteúdo do log.

O que depende de conferência manual: os DAOs e a transação contra o PostgreSQL real (`curl` + consulta ao banco), a execução em outra instância do IntelliJ e a parte da exploração que só aparece no navegador. O agente roda o `curl` se o banco estiver no ar; a confirmação final é do autor.

## Tarefas

### 1. Esqueleto do microsserviço

- **O que:** criar a pasta com build próprio e um `main` que sobe o `HttpServer` do JDK na porta configurada, com roteador, respostas de erro em JSON e log de requisições. Ainda sem rotas de cadastro (RN-01, RN-02, RN-05, RN-12, RN-13, RN-14, RN-16).
- **Arquivos:** `microservico-cadastro/.gitignore`, `settings.gradle`, `build.gradle`, wrapper; `Main`, `Configuracao`, `http/ServidorCadastro`, `http/Roteador`, `http/RespostaJson`, `http/RegistroDeRequisicoes`; specs correspondentes; `CHANGELOG.md`.
- **Testes (escritos antes):**
  - given só a senha do banco informada, when a configuração é lida, then a porta é `8081` e a URL e o usuário do banco são os padrões;
  - given a senha do banco ausente, when a configuração é lida, then o erro cita `LINKETINDER_CADASTRO_BANCO_SENHA` e o serviço não sobe;
  - given uma configuração com senha, when ela é convertida em texto, then a senha não aparece;
  - given `LINKETINDER_CADASTRO_PORTA=9090`, when a configuração é lida, then a porta é `9090`;
  - given porta que não é número, when a configuração é lida, then o erro diz qual variável está errada;
  - given o servidor sem rotas, when chega `GET /qualquer`, then a resposta é `404` com `{"erro": ...}` e `Content-Type` JSON em UTF-8;
  - given uma rota de teste que só aceita `POST`, when chega `GET` nela, then a resposta é `405` com `Allow: POST`;
  - given uma rota `/candidatos`, when chega `/candidatosX`, then a resposta é `404`;
  - given uma requisição com corpo, when ela é atendida, then o log tem método, rota e status e não tem o corpo.
- **Validação:** `./gradlew test` na pasta do microsserviço e na raiz (90). Manual: com a variável da senha definida no terminal, `./gradlew run` na pasta e `curl -i http://localhost:8081/x` responde `404`; sem a variável, o serviço não sobe e diz qual falta; abrir a pasta em outra instância do IntelliJ e rodar o `Main` (CA-01, parte do CA-12 e do CA-16). `git status` não mostra nenhum `.env`.
- **Anonimato:** só o log é saída nova; a spec captura o console e afirma que o corpo enviado e a senha do banco não aparecem.
- **CHANGELOG:** Adicionado — Microsserviço de cadastro: aplicação independente em `microservico-cadastro/`, com servidor HTTP do JDK na porta 8081 (spec `microservico`, tarefa 1).
- **Situação:** concluída em 2026-10-11. Fica para o autor conferir a execução em outra instância do IntelliJ.
- **Além do texto do plano:**
  - a classe de log se chama `RegistroDoServidor` (e não `RegistroDeRequisicoes`), porque também escreve a mensagem de início do serviço; foram criadas ainda `Requisicao`, `Manipulador` e `ConfiguracaoInvalidaException`;
  - para rota desconhecida, o log registra `(rota desconhecida)` em vez do caminho digitado, para que um e-mail ou CPF colocado na URL nunca vá para o log (RN-14);
  - porta já em uso vira uma mensagem que diz a porta e a variável a trocar, com teste;
  - o `build.gradle` fixa a saída do console em UTF-8, porque pelo `./gradlew run` no Windows as mensagens com acento saíam ilegíveis;
  - o JSON de resposta é escrito em UTF-8 sem escapar acentos, para ficar legível no `curl`.

### 2. Regras de cadastro: validação e serviços

- **O que:** modelos de candidato e empresa, validação dos campos e os serviços que validam e mandam gravar. Lógica pura, sem HTTP e sem banco (RN-06, RN-07, RN-08, RN-09, RN-10).
- **Arquivos:** `model/Candidato`, `model/Empresa`, `service/ValidadorDeCadastro`, `service/CadastroCandidatoService`, `service/CadastroEmpresaService`, `service/DadosInvalidosException`, `service/CadastroRepetidoException`, `repositorio/CandidatoRepositorio`, `repositorio/EmpresaRepositorio`; specs; `CHANGELOG.md`.
- **Testes (escritos antes):**
  - given candidato válido, when é cadastrado, then o repositório recebe uma inserção e o retorno traz o `id`;
  - given candidato sem `email` (e cada outro obrigatório, em tabela), when é cadastrado, then `DadosInvalidosException` cita o campo e o repositório não é chamado;
  - given `descricao` e `formacao` ausentes, when é cadastrado, then são gravados vazios;
  - given senha com 5 ou 51 caracteres, data futura, `2026-02-30`, e-mail sem `@`, CPF com 10 dígitos, CPF com letra, CNPJ com 13 dígitos, CEP com 7 dígitos, texto acima do limite da coluna (em tabela), when é validado, then o erro cita o campo e a mensagem não contém o valor enviado;
  - given CPF `12345678900` e `123.456.789-00`, CNPJ e CEP com e sem pontuação, when são validados, then são aceitos;
  - given `" Candidata "`, `cpf` `12345678900` e `email` `Candidata@Example.com`, when é cadastrado, then são gravados `Candidata`, `12345678900` e `Candidata@Example.com`;
  - given senha com espaços, when é cadastrado, then é gravada como enviada;
  - given o repositório acusando cadastro repetido, when é cadastrado, then `CadastroRepetidoException` chega a quem chamou;
  - os mesmos casos para empresa, com `cnpj` e `nome` de até 100 caracteres.
- **Validação:** `./gradlew test` na pasta (CA-04, CA-05, CA-06, parte do CA-07, pela lógica).
- **Anonimato:** as mensagens de erro são saída: as specs afirmam que citam o campo e não repetem o valor, inclusive a senha.
- **CHANGELOG:** Adicionado — Validação e regras de cadastro de candidato e empresa no microsserviço (spec `microservico`, tarefa 2).
- **Situação:** concluída em 2026-10-11.
- **Além do texto do plano:** a spec foi completada antes do código em dois pontos da RN-08: CPF, CNPJ e CEP são aceitos só com os dígitos ou na pontuação padrão (pontuação fora do padrão é erro), e valor que não é texto (número, lista, objeto) é erro. Os dados dos testes ficam em `DadosFicticios`, todos inventados (RN-16).

### 3. Rotas `POST /candidatos` e `POST /empresas`

- **O que:** handlers das duas rotas, leitura do corpo JSON e montagem das respostas, ligados aos serviços da tarefa 2 (RN-04, RN-05, RN-06, RN-07, RN-10, RN-12). O `Main` ainda não registra essas rotas, porque os repositórios reais só chegam na tarefa 4.
- **Arquivos:** `http/CandidatoHandler`, `http/EmpresaHandler`, `http/CorpoJson`, ajuste em `http/ServidorCadastro`; specs; `CHANGELOG.md`.
- **Testes (escritos antes), pelo servidor em porta livre com serviços simulados:**
  - given serviço que aceita o cadastro, when chega `POST /candidatos` válido, then `201`, corpo com `id` e os dados, sem `senha` e sem cabeçalho `Location`;
  - o mesmo para `POST /empresas`;
  - given serviço que lança `DadosInvalidosException`, when chega o `POST`, then `400` com a mensagem do serviço;
  - given serviço que lança `CadastroRepetidoException`, when chega o `POST`, then `409` com `Já existe um cadastro com esses dados`;
  - given corpo vazio, JSON inválido ou JSON que é lista, when chega o `POST`, then `400`;
  - given `competencias` e um campo desconhecido no corpo, when chega `POST /candidatos`, then o cadastro segue e o serviço não recebe esses campos;
  - given serviço que lança `SQLException`, when chega o `POST`, then `500` com `Erro ao acessar o banco de dados`, sem o detalhe técnico;
  - when chegam `GET /candidatos`, `PUT /empresas` e `OPTIONS /candidatos`, then `405` com `Allow: POST`;
  - when chega `POST /vagas`, then `404`.
- **Validação:** `./gradlew test` na pasta (CA-02 a CA-05 e CA-07 pela camada HTTP, CA-12, parte do CA-14).
- **Anonimato:** as respostas `201`, `400`, `409` e `500` são conferidas contra a tabela da seção 3 da spec: nenhuma contém `senha`, nenhum erro repete valor enviado, e a resposta `409` não diz qual campo coincidiu.
- **CHANGELOG:** Adicionado — Rotas `POST /candidatos` e `POST /empresas` do microsserviço, com respostas em JSON sem a senha (spec `microservico`, tarefa 3).
- **Situação:** concluída em 2026-10-11. Com a aplicação no ar, `POST /candidatos` ainda responde `404`, como previsto: o `Main` só registra as rotas na tarefa 4.
- **Além do texto do plano:**
  - a spec das rotas usa os serviços reais com os repositórios simulados (e não serviços simulados), o que testa junto a validação e o tratamento de campos desconhecidos;
  - as rotas são registradas pela classe `RotasDeCadastro`, em vez de um ajuste em `ServidorCadastro`;
  - a RN-14 foi completada antes do código: na falha de banco, o console recebe só o código do erro e a primeira linha da mensagem, porque a linha de detalhe do PostgreSQL pode trazer valores gravados;
  - as interfaces de repositório declaram `throws SQLException`, para a falha de banco chegar inteira ao roteador.

### 4. Gravação no PostgreSQL e ligação no `Main`

- **O que:** DAOs de inserção de candidato e empresa, conexão por requisição, tradução da violação de unicidade em cadastro repetido, e o `Main` montando tudo. Ao final desta tarefa é possível cadastrar candidatos e empresas de ponta a ponta (RN-03, RN-09, RN-10, RN-12).
- **Arquivos:** `banco/ConexaoBanco`, `banco/TradutorDeErroDeBanco`, `dao/CandidatoDAO`, `dao/EmpresaDAO`, `Main`; spec do tradutor; `CHANGELOG.md`.
- **Testes (escritos antes):**
  - given `SQLException` com SQLState `23505`, when é traduzida, then vira `CadastroRepetidoException`;
  - given `SQLException` com outro estado, when é traduzida, then continua `SQLException`.
- **Validação:** `./gradlew test` na pasta. Os DAOs não têm teste automatizado. Manual, com o PostgreSQL no ar e dados fictícios:
  - `curl` de `POST /candidatos` válido responde `201`, e `SELECT` mostra a linha com todos os campos como enviados (CA-02, CA-06);
  - o mesmo para `POST /empresas` (CA-03);
  - repetir o mesmo cadastro responde `409`, e a tabela continua com uma linha só (CA-07);
  - com o banco parado, a resposta é `500` genérica;
  - o candidato cadastrado aparece na listagem do CLI.
- **Anonimato:** nenhuma saída nova para quem chama. Conferir que a mensagem técnica de erro de banco vai só para o console do servidor e não traz dados do cadastro.
- **CHANGELOG:** Adicionado — Cadastro de candidatos e empresas gravado no PostgreSQL pelo microsserviço; cadastro repetido responde `409` (spec `microservico`, tarefa 4).
- **Situação:** concluída em 2026-10-11. Conferida com a aplicação no ar contra o PostgreSQL real: `201` com a linha gravada como enviada (candidato e empresa), `409` no repetido sem duplicar a linha, `400` nas validações, `405`/`404` nas rotas e `500` genérico com o banco inacessível. Fica para o autor conferir o candidato na listagem do CLI.
- **Cadastros fictícios deixados no banco:** candidatos de `id` 6 e 8 e empresa de `id` 6. O candidato 6 ficou com a descrição corrompida, porque foi gravado antes da correção do UTF-8 abaixo.
- **Além do texto do plano:**
  - a conferência manual mostrou que um corpo fora de UTF-8 (acento digitado na linha de comando do Windows) era gravado corrompido em silêncio. A spec ganhou dois casos de borda e o serviço passou a responder `400` para corpo que não está em UTF-8, com teste;
  - o `Main` ganhou um teste que sobe o serviço e confirma que as duas rotas estão ligadas, sem precisar de banco.

### 5. Competências do candidato: regras e rota

- **O que:** o serviço que recebe a lista escrita pelo candidato e a rota `POST /candidatos/{id}/competencias`, com repositórios simulados (RN-04, RN-11).
- **Arquivos:** `service/CompetenciaDoCandidatoService`, `service/CandidatoNaoEncontradoException`, `repositorio/CompetenciaRepositorio`, `repositorio/Transacao`, novos métodos em `repositorio/CandidatoRepositorio`, `http/CompetenciaHandler`, ajuste em `http/ServidorCadastro`; specs; `CHANGELOG.md`.
- **Testes (escritos antes):**
  - given candidato sem competências e banco sem `Kotlin` nem `Rust`, when chega `["Kotlin", "kotlin", " Rust ", ""]`, then as duas são criadas, vinculadas, e o retorno é `["Kotlin", "Rust"]` (CA-08);
  - given `Java` no banco, when chega `["java"]`, then nada é criado, o vínculo é com `Java` e o retorno traz `Java` (CA-09);
  - given candidato que já tem `Java`, when chega `["java", "SQL"]`, then só `SQL` é vinculada e o retorno traz `Java` e `SQL` (CA-10);
  - given a mesma lista enviada de novo, when é processada, then nenhum vínculo é criado;
  - given lista `[]`, só com vazios, ou ausente, when é processada, then `DadosInvalidosException` cita `competencias`;
  - given nome com 51 caracteres, when é processado, then `DadosInvalidosException`;
  - given `id` sem candidato, when é processado, then `CandidatoNaoEncontradoException` e nenhuma competência é criada (CA-11);
  - given qualquer chamada válida, when é processada, then tudo acontece dentro de uma `Transacao`;
  - pela rota: `200` com `{"id", "competencias"}` e nada mais; `404` para candidato inexistente; `400` para `id` que não é número e para lista vazia; `405` com `Allow: POST` para outros métodos.
- **Validação:** `./gradlew test` na pasta (CA-08 a CA-11 pela lógica, CA-12, parte do CA-14).
- **Anonimato:** a resposta `200` é saída nova: a spec afirma que o corpo tem só `id` e `competencias`, sem nenhum dado pessoal do candidato.
- **CHANGELOG:** Adicionado — Regras e rota `POST /candidatos/{id}/competencias` do microsserviço: competências somadas às do candidato, reaproveitando as existentes sem diferenciar maiúsculas (spec `microservico`, tarefa 5).
- **Situação:** concluída em 2026-10-11. Com a aplicação no ar, a rota de competências ainda responde `404`, como previsto: o `Main` só a registra na tarefa 6.
- **Além do texto do plano:**
  - as consultas e vínculos de competência do candidato ficam em `CompetenciaRepositorio` (`buscarDoCandidato`, `vincularAoCandidato`), e não em novos métodos de `CandidatoRepositorio`, que ganhou só `existe`. O `CandidatoDAO.existe` já entrou nesta tarefa, porque a interface exige; ele é conferido no banco na tarefa 6;
  - o roteador passou a aceitar rotas com trecho variável (classe `Rota`), e o log registra o modelo da rota (`/candidatos/{id}/competencias`), sem o `id`;
  - item da lista que não é texto é recusado com `400`, como os demais campos (RN-08).

### 6. Competências: gravação no PostgreSQL e ligação no `Main`

- **O que:** DAO de competências com busca sem diferenciar maiúsculas (a mais antiga primeiro), consultas e vínculos do candidato, transação com commit e rollback, e o `Main` registrando a rota (RN-03, RN-11).
- **Arquivos:** `dao/CompetenciaDAO`, `dao/CandidatoDAO`, `banco/TransacaoJdbc`, `Main`; spec da transação; `CHANGELOG.md`.
- **Testes (escritos antes), com `Mock` de `Connection`:**
  - given um bloco que termina bem, when roda na transação, then há `commit` e a conexão é fechada;
  - given um bloco que lança exceção, when roda na transação, then há `rollback`, a exceção sobe e a conexão é fechada.
- **Validação:** `./gradlew test` na pasta. Manual, com o PostgreSQL no ar: repetir por `curl` os casos de CA-08 a CA-11 e conferir por `SELECT` as tabelas `competencias` e `candidato_competencia`; conferir no CLI que o candidato lista as competências gravadas.
- **Anonimato:** nenhuma saída nova. Conferir que a consulta de competências do candidato devolve só nomes de competência.
- **CHANGELOG:** Adicionado — Competências do candidato gravadas no PostgreSQL pelo microsserviço, em transação (spec `microservico`, tarefa 6).
- **Situação:** concluída em 2026-10-11. Conferida com a aplicação no ar contra o PostgreSQL real, repetindo CA-08 a CA-11: `Kotlin` e `Rust` criadas uma vez, `java` reaproveitou a `Java` da carga inicial, a soma não repetiu vínculo, e lista vazia (`400`), candidato inexistente (`404`) e `id` inválido (`400`) não criaram nada. Fica para o autor conferir as competências do candidato na listagem do CLI.
- **Cadastros fictícios deixados no banco:** candidato de `id` 9, com quatro competências, e as competências `Kotlin` e `Rust`.
- **Além do texto do plano:** a conexão da transação fica guardada em `ConexaoBanco` enquanto o bloco roda, para que os DAOs chamados dentro dele usem a mesma conexão; a spec da transação cobre também esse uso e a volta ao modo de uma conexão por consulta.

### 7. Exploração da integração e README

- **O que:** explorar como o frontend falaria com o microsserviço e escrever o `README.md` (RN-15). Nenhum código do repositório muda nesta tarefa além do README e do CHANGELOG.
- **Como:** experimento descartável, em pasta temporária fora do repositório:
  1. uma página de teste, servida em outra origem, envia `fetch` com JSON para `POST /candidatos` do serviço real: observar o bloqueio do navegador;
  2. em uma **cópia** do microsserviço na pasta temporária, acrescentar os cabeçalhos de CORS e a resposta ao `OPTIONS`: observar o preflight e a chamada passarem;
  3. simular o preflight por `curl`, com os cabeçalhos `Origin` e `Access-Control-Request-Method`, nas duas versões;
  4. pesquisar a alternativa do proxy do servidor de desenvolvimento, sem implementá-la.
  A página e a cópia são apagadas ao final.
- **Arquivos:** `README.md`, `CHANGELOG.md`.
- **Conteúdo do README:** o que é o microsserviço; como executá-lo (outra instância do IntelliJ e `./gradlew run`), variáveis de ambiente, com a senha do banco mostrada só como marcador (`<sua-senha>`), nunca com um valor; as três rotas com exemplos de `curl` e de resposta, usando dados fictícios; como rodar os testes; e a seção de exploração, dizendo o que foi testado, o que foi só pesquisado e qual solução fica indicada para a próxima feature.
- **Validação:** `git status` mostra só `README.md` e `CHANGELOG.md` alterados e nenhum resto do experimento; os comandos do README são rodados antes de escritos. O que o agente não conseguir observar no navegador fica descrito como passo para o autor conferir. Leitura do autor (CA-15).
- **Anonimato:** os exemplos do README são saída: usar apenas dados fictícios, sem senha real e sem reaproveitar pessoas da carga inicial do banco.
- **CHANGELOG:** Adicionado — README descreve o microsserviço de cadastro e a exploração da comunicação com o frontend (spec `microservico`, tarefa 7).
- **Situação:** concluída em 2026-10-11. Fica para o autor ler o README (CA-15).
- **O que o experimento mostrou**, com a página de teste em `http://localhost:5173` aberta no Microsoft Edge sem interface:
  - `fetch` com JSON para o serviço atual: o navegador enviou o preflight `OPTIONS`, recebeu `405` sem cabeçalhos de CORS e bloqueou (`TypeError: Failed to fetch`); o `POST` não saiu;
  - `fetch` sem `Content-Type` para o serviço atual: sem preflight, o `POST` chegou ao serviço e foi processado (apareceu no registro), mas a página não recebeu a resposta;
  - `fetch` com JSON para a cópia com CORS: preflight `204` com os cabeçalhos, `POST` enviado e resposta lida pela página, inclusive na rota de competências;
  - preflight de outra origem na cópia, simulado por `curl`: sem cabeçalhos de autorização.
  O proxy do Vite foi só pesquisado. A página, a cópia do serviço e o perfil temporário do navegador foram apagados; nada do experimento está no repositório.
- **Além do texto do plano:**
  - o navegador pôde ser observado pelo agente, com o Edge em modo sem interface, então não sobrou passo de navegador para o autor;
  - as chamadas da página usaram um corpo incompleto de propósito (resposta `400`), para o experimento não gravar nada no banco;
  - os exemplos de `curl` do README foram rodados com outros e-mails e documentos fictícios, e não com os valores exatos do texto, para que o primeiro uso do exemplo pelo autor não esbarre em um `409`.

### 8. Regressão e documentação

- **O que:** atualizar `AGENTS.md` (o microsserviço como parte do repositório, como executá-lo e testá-lo, as novas pendências: senha em texto puro e busca de competência diferente entre microsserviço e CLI), marcar a spec como implementada e repassar o checklist de validação.
- **Arquivos:** `AGENTS.md`, `docs/specs/microservico/spec.md`, `docs/specs/microservico/plano.md`, `CHANGELOG.md`.
- **Validação:** checklist da etapa 6 preenchido:
  - todos os testes passam, no microsserviço e na raiz (90);
  - todos os critérios de aceite repassados, de CA-01 a CA-16;
  - busca por credenciais e dados reais nos arquivos da feature (`git grep` por senha de banco, `.env`, e-mails fora de `example.com`): nada encontrado (CA-16);
  - Princípio do Anonimato conferido item por item;
  - funcionalidades listadas em "Impacto" conferidas;
  - nenhum arquivo fora do escopo alterado: `git diff bee78b8` só mostra `microservico-cadastro/`, `docs/specs/microservico/`, `README.md`, `AGENTS.md` e `CHANGELOG.md` (CA-13);
  - `CHANGELOG.md` com uma entrada por tarefa.
- **Anonimato:** conferir que o texto novo do `AGENTS.md` não contradiz a tabela do Princípio do Anonimato e registra a pendência da senha.
- **CHANGELOG:** Modificado — `AGENTS.md` passa a descrever o microsserviço de cadastro e suas pendências (spec `microservico`, tarefa 8).
- **Situação:** concluída em 2026-10-11.

#### Checklist de validação preenchido (2026-10-11)

**Testes e critérios de aceite**

- [x] `./gradlew test` no microsserviço: 144 testes, nenhuma falha. Na raiz: 90 testes, nenhuma falha.
- [x] Frontend não foi tocado, então `npx tsc --noEmit` não se aplica.
- [x] Critérios de aceite repassados:

| Critério | Como foi verificado | Situação |
| --- | --- | --- |
| CA-01 serviço sobe sozinho e desacoplado | `./gradlew run` na pasta dele, com o projeto principal parado; busca por `org.example` no microsserviço sem resultado | Atende. A execução em outra instância do IntelliJ fica para o autor |
| CA-02 cadastro de candidato | Specs + `curl` e consulta ao PostgreSQL | Atende |
| CA-03 cadastro de empresa | Specs + `curl` e consulta ao PostgreSQL | Atende |
| CA-04 campo obrigatório | Specs + `curl` | Atende |
| CA-05 formatos | Specs (tabela de casos) + `curl` | Atende |
| CA-06 gravação como enviado | Specs + consulta ao PostgreSQL | Atende |
| CA-07 cadastro repetido | Specs + `curl` e contagem de linhas no PostgreSQL | Atende |
| CA-08 competências salvas | Specs + `curl` e consulta ao PostgreSQL | Atende |
| CA-09 competência em outra caixa | Specs + `curl` e consulta ao PostgreSQL | Atende |
| CA-10 competências somadas | Specs + `curl` e consulta ao PostgreSQL | Atende |
| CA-11 competências inválidas | Specs + `curl` e consulta ao PostgreSQL | Atende |
| CA-12 rotas e métodos | Specs + `curl` | Atende |
| CA-13 nada fora do microsserviço | `git diff bee78b8` e suíte da raiz | Atende |
| CA-14 anonimato preservado | Specs das respostas e do registro + leitura das saídas | Atende |
| CA-15 README | Comandos rodados antes de escritos; experimento no navegador | Escrito. A leitura final é do autor |
| CA-16 sem credenciais nem dados reais | Busca nos arquivos da feature + specs da configuração | Atende |

- [x] Mudanças em DAO conferidas com o PostgreSQL real (tarefas 4 e 6). O schema não mudou.

**Princípio do Anonimato**

- [x] Empresa antes do match: a feature não cria saída para empresa sobre candidato. A única saída com dado de candidato para terceiros é a resposta de competências, que traz só `id` e `competencias`, ambos permitidos (spec).
- [x] Candidato antes do match: a feature não cria saída sobre vaga ou empresa para candidato.
- [x] Depois do match: a feature não toca em match.
- [x] Senha: ausente das respostas `201` (specs afirmam que a palavra `senha` não aparece), das mensagens de erro e do registro. A senha do banco não aparece em texto da configuração nem em log.
- [x] Curtida: a feature não toca em curtida.
- [x] Camadas olhadas: respostas montadas em `CandidatoHandler`, `EmpresaHandler` e `CompetenciaHandler`; mensagens de `ValidadorDeCadastro`, `CompetenciaDoCandidatoService` e `CorpoJson`; `RegistroDoServidor`, único ponto que escreve no console além da mensagem de falha de início no `Main`; consultas dos DAOs (a de competências devolve só `id` e `nome` da competência); exemplos do README.
- [x] Pendências conhecidas: nenhuma ampliada nem usada como modelo. Duas novas foram registradas no `AGENTS.md`: senha em texto puro e rota de competências sem login.
- [x] Seção 3 da spec conferida linha a linha: respostas `201`, `200`, `400`, `409`, `404`, `405` e `500` por spec; registro do serviço por spec e na execução real; exemplos do README por busca de e-mails fora de `example.com`.

**Regressão e escopo**

- [x] Impacto (seção 5 da spec): CLI, API REST, build da raiz, schema e frontend sem alteração de arquivo; suíte da raiz passando. A listagem do CLI com os cadastros do microsserviço não foi executada pelo agente (as linhas foram conferidas por consulta ao banco).
- [x] Escopo: desde `bee78b8`, só mudaram `microservico-cadastro/`, `docs/specs/microservico/`, `README.md`, `AGENTS.md` e `CHANGELOG.md`. As alterações antigas em `.idea/` e `frontend/` continuam fora dos commits.

**Documentação**

- [x] `CHANGELOG.md` com uma entrada por tarefa (1 a 8).
- [x] `AGENTS.md` descreve o microsserviço como existente.
- [x] `README.md` com a descrição de uso e a exploração da integração.
- [x] Spec marcada como implementada; todas as tarefas do plano com situação.

**Não verificado pelo agente, fica para o autor:** rodar o `Main` em outra instância do IntelliJ; ver os cadastros na listagem do CLI; parar de fato o PostgreSQL (a falha de banco foi simulada apontando a URL para uma porta sem banco); ler o README.

## Pendências

Resolvidas pelo autor em 2026-10-11:

- **Commits:** o agente commita cada tarefa, depois da validação do autor.
- **`.gitignore` da raiz:** não ganha a linha `.env` nesta feature.
- **Alterações antigas em `.idea/` e `frontend/`:** não são tocadas nem commitadas. Parte delas já está no stage do git, então cada commit da feature é feito com os caminhos explícitos (`git commit -- <arquivos>`), para que nada de `.idea/` entre junto. Na conferência de escopo do CA-13 elas são desconsideradas.
- **Banco para a conferência manual:** os cadastros de teste, fictícios, gravados nas tarefas 4 e 6 ficam no banco.

Em aberto:

- Nenhuma além da abaixo. O `plano.md` e os ajustes da spec e do `AGENTS.md` entraram em um commit de documentação antes da tarefa 1.
- **Navegador:** a observação do bloqueio e do preflight na tarefa 7 depende de um navegador; o que o agente não conseguir ver fica para o autor.
