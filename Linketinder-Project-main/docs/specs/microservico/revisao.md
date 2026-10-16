# Revisão: microsserviço de cadastro

Spec: `docs/specs/microservico/spec.md` · Base: `main` · Commit revisado: `ac0dce0` · PR: #2
Data: 2026-10-11 · Revisor: agente de IA (Claude Code), o mesmo tipo de agente que escreveu o código da feature

**Como este registro foi feito.** A revisão aconteceu em duas passadas, e a ordem não foi a que a skill pede:

1. Uma primeira revisão de código encontrou cinco problemas e os corrigiu direto no commit `ac0dce0`, **sem registrar os apontamentos antes**. Os apontamentos AP-01 a AP-05 foram reconstruídos depois, a partir do diff desse commit (`git show ac0dce0`); a evidência de cada um é o código como estava em `7d7e620`.
2. A segunda passada, que escreveu este arquivo, releu todo o código do microsserviço já no commit `ac0dce0`, rodou as duas suítes de novo e montou a matriz e a tabela do anonimato. Ela gerou os apontamentos AP-06 a AP-08, registrados antes de serem corrigidos.

As seções 2, 3 e 4 descrevem o código no commit `ac0dce0`. As correções de AP-06 a AP-08 vieram depois dele e entram no mesmo commit deste registro; o que mudou com elas está em cada apontamento e na seção 6.

## 1. Resumo

Parecer: **aprovado com pendências**. Os 8 apontamentos foram corrigidos e nenhum era bloqueante. As pendências não são apontamentos: são as conferências manuais e a pergunta listadas na seção 6, que só o autor consegue fechar.

| Severidade | Encontrados | Corrigidos | Aguardando decisão | Não procede |
| --- | --- | --- | --- | --- |
| Bloqueante | 0 | 0 | 0 | 0 |
| Importante | 4 | 4 | 0 | 0 |
| Menor | 4 | 4 | 0 | 0 |

## 2. O que foi revisado

- **Arquivos:** `git diff --stat main...HEAD` mostra 61 arquivos, 4432 linhas a mais e 5 a menos. Desde `bee78b8`, o ponto de partida da feature: `microservico-cadastro/` (53 arquivos), `docs/specs/microservico/` (2), `README.md`, `AGENTS.md`, `CHANGELOG.md` e `PROMPTS.md`.
- **Lido por inteiro:** os 35 arquivos de `microservico-cadastro/src/main` (1431 linhas) e o `.gitignore` do microsserviço. Das specs Spock foram lidos os nomes de todos os testes e os trechos citados neste registro, não cada arquivo inteiro.
- **Verificações rodadas** (2026-10-11, forçando a execução com `cleanTest`):
  - `./gradlew cleanTest test` em `microservico-cadastro/` — 146 testes, nenhuma falha, nenhum ignorado;
  - `./gradlew cleanTest test` na raiz — 90 testes, nenhuma falha, nenhum ignorado.
- **Buscas:** `org.example` dentro do microsserviço (nada); e-mails fora de `example.com` nos arquivos da feature (um, ver AP-08); arquivos `.env` versionados e senha escrita em código (nada).
- **Não verificado nesta revisão:**
  - a gravação no PostgreSQL real. Os DAOs não têm teste automatizado, e a conferência manual relatada no plano (tarefas 4 e 6) é anterior ao commit `ac0dce0`, que alterou os três DAOs;
  - a execução do `Main` em outra instância do IntelliJ e a listagem dos cadastros no CLI;
  - o experimento de CORS descrito no README, que era descartável e não está no repositório;
  - o frontend não foi tocado, então `npx tsc --noEmit` não se aplica.

## 3. Aderência à spec

Os caminhos abaixo são relativos a `microservico-cadastro/src/main/groovy/org/linketinder/cadastro/` (implementação) e a `microservico-cadastro/src/test/groovy/org/linketinder/cadastro/` (specs Spock).

| Item | Onde está implementado | Onde está verificado | Veredito |
| --- | --- | --- | --- |
| RN-01 Aplicação independente | `microservico-cadastro/build.gradle`; `Main.groovy:29` | `MainSpec:24`; busca por `org.example` sem resultado | atende. Abrir em outra instância do IntelliJ não é verificável pelo agente |
| RN-02 Sem framework | `http/ServidorCadastro.groovy:18` (`HttpServer` do JDK); dependências do `build.gradle` | specs de `http/` sobem o servidor de verdade | atende |
| RN-03 Mesmo banco, mesmo schema | SQL em `dao/CandidatoDAO.groovy:28`, `dao/EmpresaDAO.groovy:27`, `dao/CompetenciaDAO.groovy:26,43,58,81` | leitura; `database/` sem alteração no diff | atende pela leitura. Gravação real não repetida nesta revisão |
| RN-04 Somente cadastro | `http/RotasDeCadastro.groovy:15-20` | `http/RotasDeCadastroSpec:230,247`; `http/RotaDeCompetenciasSpec:104,116` | atende |
| RN-05 JSON na entrada e na saída | `http/RespostaJson.groovy:13,33,45`; `http/Requisicao.groovy:29` | `http/RoteadorSpec:43`; `http/RotasDeCadastroSpec:160,175` | atende |
| RN-06 Cadastro de candidato | `service/ValidadorDeCadastro.groovy:39`; `http/CandidatoHandler.groovy:22-40` | `http/RotasDeCadastroSpec:69`; `service/ValidadorDeCadastroSpec:25,91` | atende |
| RN-07 Cadastro de empresa | `service/ValidadorDeCadastro.groovy:54`; `http/EmpresaHandler.groovy:22-37` | `http/RotasDeCadastroSpec:93`; `service/ValidadorDeCadastroSpec:43,103` | atende |
| RN-08 Validação | `service/ValidadorDeCadastro.groovy:18-31,66-152` | `service/ValidadorDeCadastroSpec:58,70,82,108,145,165`; `http/RotasDeCadastroSpec:113` | atende |
| RN-09 Gravação como enviado | `service/ValidadorDeCadastro.groovy:88-98,122-129`; `dao/CandidatoDAO.groovy:60` | `service/ValidadorDeCadastroSpec:176,188` | atende |
| RN-10 Cadastro repetido | `banco/TradutorDeErroDeBanco.groovy:16`; `dao/CandidatoDAO.groovy:43`; `dao/EmpresaDAO.groovy:42`; `http/Roteador.groovy:81` | `banco/TradutorDeErroDeBancoSpec:15,23`; `http/RotasDeCadastroSpec:126` | atende |
| RN-11 Competências do candidato | `service/CompetenciaDoCandidatoService.groovy:28-89`; `dao/CompetenciaDAO.groovy:25-87`; `banco/TransacaoJdbc.groovy:21`; `http/CompetenciaHandler.groovy:23-36` | `service/CompetenciaDoCandidatoServiceSpec:32-177`; `http/RotaDeCompetenciasSpec:56,72,85`; `banco/TransacaoJdbcSpec:22,34,69` | atende. `id` numérico maior que um inteiro também responde `400` (caso "id grande demais" da spec Spock) |
| RN-12 Erros de protocolo e de infraestrutura | `http/CorpoJson.groovy:14-31`; `http/Roteador.groovy:52-53,68,83-88,92-97` | `http/RotasDeCadastroSpec:142,214,230,247`; `http/RoteadorSpec:43,73,95` | atende |
| RN-13 Configuração | `Configuracao.groovy:10-17,35-68`; `Main.groovy:32-34,69-70` | `ConfiguracaoSpec:14,25,40,53,68`; `MainSpec:63,85` | atende (depois de AP-02) |
| RN-14 Nada pessoal em log | `http/RegistroDoServidor.groovy:18-28`; `http/Roteador.groovy:24,57` | `http/RoteadorSpec:110,123`; `http/RotaDeCompetenciasSpec:124`; `http/RotasDeCadastroSpec:214,252` | atende |
| RN-15 README e exploração | `README.md:175-345` | leitura | atende, com a contagem de testes desatualizada (AP-07) |
| RN-16 Dados fictícios e nenhuma credencial | `DadosFicticios.groovy` (testes); `microservico-cadastro/.gitignore:9-10` | buscas da seção 2; CPF `123.456.789-00` e CNPJ `12.345.678/0001-00` têm dígitos verificadores inválidos | atende em parte (AP-08) |
| CA-01 Serviço sobe sozinho e desacoplado | `Main.groovy:38-46` | `MainSpec:24,40`; busca por `org.example` | atende. Execução no IntelliJ fica para o autor |
| CA-02 Cadastro de candidato | `http/CandidatoHandler.groovy:22-40`; `dao/CandidatoDAO.groovy:27` | `http/RotasDeCadastroSpec:69` | atende pela lógica. Linha no banco não repetida nesta revisão |
| CA-03 Cadastro de empresa | `http/EmpresaHandler.groovy:22-37`; `dao/EmpresaDAO.groovy:26` | `http/RotasDeCadastroSpec:93` | atende pela lógica. Linha no banco não repetida nesta revisão |
| CA-04 Campo obrigatório | `service/ValidadorDeCadastro.groovy:122-129` | `service/ValidadorDeCadastroSpec:58`; `http/RotasDeCadastroSpec:113` | atende |
| CA-05 Formatos | `service/ValidadorDeCadastro.groovy:28-31,66-115` | `service/ValidadorDeCadastroSpec:108,145` | atende |
| CA-06 Gravação como enviado | `service/ValidadorDeCadastro.groovy:122-129` | `service/ValidadorDeCadastroSpec:176` | atende |
| CA-07 Cadastro repetido | `banco/TradutorDeErroDeBanco.groovy:16` | `http/RotasDeCadastroSpec:126`; `banco/TradutorDeErroDeBancoSpec:15` | atende pela lógica. Contagem de linhas no banco não repetida |
| CA-08 Competências salvas depois do cadastro | `service/CompetenciaDoCandidatoService.groovy:36-51,72-77` | `service/CompetenciaDoCandidatoServiceSpec:32`; `http/RotaDeCompetenciasSpec:56` | atende |
| CA-09 Competência existente em outra caixa | `dao/CompetenciaDAO.groovy:26-31`; `service/CompetenciaDoCandidatoService.groovy:53-56` | `service/CompetenciaDoCandidatoServiceSpec:49` | atende. O `LOWER` no SQL depende do banco real |
| CA-10 Competências somadas | `service/CompetenciaDoCandidatoService.groovy:41-48` | `service/CompetenciaDoCandidatoServiceSpec:64,80` | atende |
| CA-11 Competências inválidas | `service/CompetenciaDoCandidatoService.groovy:37-39,58-70` | `service/CompetenciaDoCandidatoServiceSpec:110,154`; `http/RotaDeCompetenciasSpec:72,85` | atende |
| CA-12 Rotas e métodos | `http/Roteador.groovy:50-54,65-72,92-97` | `http/RotasDeCadastroSpec:142,230,247` | atende |
| CA-13 Nada fora do microsserviço | — | `git diff --name-only bee78b8 HEAD`; suíte da raiz com 90 testes | atende em parte: `PROMPTS.md` entrou depois do fechamento da spec e não está na lista do critério. É só documentação |
| CA-14 Anonimato preservado | ver seção 4 | specs citadas na seção 4 | atende |
| CA-15 README | `README.md:175-345` | leitura | atende. A leitura final é do autor |
| CA-16 Sem credenciais nem dados reais | `Configuracao.groovy:57-64`; `microservico-cadastro/.gitignore:9-10` | buscas da seção 2; `ConfiguracaoSpec:53,68` | atende em parte (AP-08) |

Mudanças do diff sem regra ou tarefa correspondente:

- `PROMPTS.md` (commit `7d7e620`): documento do processo, fora da lista do CA-13.
- As correções do commit `ac0dce0`: não vieram de tarefa do plano, e sim da revisão. Estão registradas como AP-01 a AP-05.

## 4. Princípio do Anonimato

| Saída | Quem vê | Momento | Campos que saem | Conforme? | Como foi verificado |
| --- | --- | --- | --- | --- | --- |
| Resposta `201` de `POST /candidatos` (`http/CandidatoHandler.groovy:27-39`) | quem acabou de se cadastrar | cadastro, sem contraparte | `id`, `nome`, `sobrenome`, `dataNascimento`, `email`, `cpf`, `pais`, `cep`, `descricao`, `formacao` | sim | teste (`http/RotasDeCadastroSpec:69`) e leitura |
| Resposta `201` de `POST /empresas` (`http/EmpresaHandler.groovy:27-36`) | quem acabou de se cadastrar | cadastro, sem contraparte | `id`, `nome`, `email`, `cnpj`, `pais`, `cep`, `descricao` | sim | teste (`http/RotasDeCadastroSpec:93`) e leitura |
| Resposta `200` de `POST /candidatos/{id}/competencias` (`http/CompetenciaHandler.groovy:27`) | quem chamou a rota | antes do match | `id`, `competencias` | sim | teste (`http/RotaDeCompetenciasSpec:56`) |
| Respostas `400` | quem chamou | — | nome do campo e o motivo; nunca o valor enviado | sim | teste (`service/ValidadorDeCadastroSpec:108,145`; `service/CompetenciaDoCandidatoServiceSpec:142`; `http/RotasDeCadastroSpec:113,191`) |
| Resposta `409` | quem chamou | — | mensagem genérica, sem o campo que coincidiu | sim | teste (`http/RotasDeCadastroSpec:126`; `banco/TradutorDeErroDeBancoSpec:23`) |
| Respostas `404` e `500` | quem chamou | — | mensagem fixa | sim | teste (`http/RoteadorSpec:43,95`; `http/RotasDeCadastroSpec:214`) |
| Resposta `405` (`http/Roteador.groovy:92-97`) | quem chamou | — | o método HTTP recebido e os métodos aceitos | sim, com a pergunta abaixo | teste (`http/RoteadorSpec:73`) e leitura |
| Log de requisição (`http/RegistroDoServidor.groovy:18`) | quem opera o servidor | — | método, modelo da rota, status | sim, com a pergunta abaixo | teste (`http/RoteadorSpec:110,123`; `http/RotaDeCompetenciasSpec:124`; `http/RotasDeCadastroSpec:252`) |
| Log de falha de banco (`http/RegistroDoServidor.groovy:22`) | quem opera o servidor | — | código do erro e primeira linha da mensagem técnica | sim | teste (`http/RotasDeCadastroSpec:214`) |
| Log de falha inesperada (`http/RegistroDoServidor.groovy:26`), agora também chamado em `http/Roteador.groovy:58` | quem opera o servidor | — | só o nome da classe da exceção | sim | leitura. O novo `catch` do `ac0dce0` não tem teste |
| Mensagem de falha ao subir (`Main.groovy:33`) | quem opera o servidor | — | nome da variável ou número da porta | sim | teste (`MainSpec:63,85`; `ConfiguracaoSpec:53,68`) |
| Mensagens das novas `SQLException` dos DAOs (`ac0dce0`) | quem opera o servidor, pelo log de falha de banco | — | texto fixo ("INSERT em candidatos não retornou id") | sim | leitura |
| Consultas dos DAOs | ninguém fora do serviço | — | `id` e `nome` da competência; booleano de existência do candidato; `id` gerado | sim | leitura (`dao/CompetenciaDAO.groovy:27,59`; `dao/CandidatoDAO.groovy:50`) |
| Exemplos do `README.md` | quem lê o repositório | — | dados fictícios em `example.com` | sim | busca |

- Senha em saída, JSON, log ou mensagem: não encontrada. A senha do banco não aparece no `toString` da configuração (`ConfiguracaoSpec:68`). A senha em texto puro na coluna `senha` continua sendo a pendência conhecida do `AGENTS.md`.
- Curtida revelada antes do match: não se aplica; a feature não toca em curtida nem em match.
- Pendências conhecidas ampliadas ou copiadas: nenhuma.

**Pergunta para o autor, sem evidência (não é apontamento).** O método HTTP recebido é repetido na resposta `405` e gravado no log sem tratamento. A RN-14 trocou o caminho digitado por um marcador para que um dado pessoal posto na URL não fosse para o log, mas não diz nada sobre o método, e a própria regra autoriza registrá-lo. Não foi verificado se o `HttpServer` do JDK aceita um texto qualquer no lugar do método. Se aceitar, o mesmo cuidado da URL valeria aqui.

## 5. Apontamentos

### AP-01 — Falha no rollback escondia o erro original da gravação

- **Categoria:** Spec
- **Severidade:** Importante
- **Local:** `microservico-cadastro/src/main/groovy/org/linketinder/cadastro/banco/TransacaoJdbc.groovy:31-33` (em `7d7e620`)
- **Regra:** RN-11 (gravação tudo ou nada; candidato inexistente responde `404`) e RN-12
- **Evidência:** o `catch` chamava `conexao.rollback()` sem proteção e só depois relançava. Se o rollback falhasse, a `SQLException` dele substituía a exceção original: um `CandidatoNaoEncontradoException` lançado dentro do bloco viraria `500` em vez de `404`.
- **Correção sugerida:** proteger o rollback e manter a exceção original.
- **Situação:** corrigido
- **O que foi corrigido:** a falha do rollback passou a ser anexada à exceção original com `addSuppressed`, e a original é relançada (`TransacaoJdbc.groovy:31-37`). Prova: `banco/TransacaoJdbcSpec:69`, "rollback que falha não perde a exceção original". Commit `ac0dce0`.

### AP-02 — Senha do banco só com espaços era aceita

- **Categoria:** Spec
- **Severidade:** Importante
- **Local:** `microservico-cadastro/src/main/groovy/org/linketinder/cadastro/Configuracao.groovy:60` (em `7d7e620`)
- **Regra:** RN-13 (sem a senha do banco, o serviço não sobe e diz qual variável falta)
- **Evidência:** a checagem era `senha == null || senha.isEmpty()`. Com a variável valendo `"   "` o serviço subia e só falhava na primeira gravação, com `500`. As outras três variáveis já tratavam valor só com espaços como ausente (`valorOuPadrao`).
- **Correção sugerida:** tratar a senha só com espaços como ausente.
- **Situação:** corrigido
- **O que foi corrigido:** a checagem passou a usar `senha.trim().isEmpty()` (`Configuracao.groovy:60`). A senha válida continua sendo usada como foi informada, sem `trim`. Prova: novo caso "só com espaços" em `ConfiguracaoSpec:53`. Commit `ac0dce0`.

### AP-03 — DAOs liam o `id` sem conferir se o `INSERT ... RETURNING` devolveu linha

- **Categoria:** Convenções
- **Severidade:** Menor
- **Local:** `dao/CandidatoDAO.groovy:37`, `dao/EmpresaDAO.groovy:36` e `dao/CompetenciaDAO.groovy:47` (em `7d7e620`)
- **Regra:** `AGENTS.md`, Convenções: tratamento de erros explícito
- **Evidência:** os três métodos `inserir` chamavam `resultado.next()` e ignoravam o retorno antes de ler `id`. Sem linha, o erro apareceria como uma falha do driver, sem dizer a causa.
- **Correção sugerida:** conferir o retorno e lançar `SQLException` com mensagem clara.
- **Situação:** corrigido
- **O que foi corrigido:** os três métodos lançam `SQLException("INSERT em <tabela> não retornou id")` quando não há linha (`CandidatoDAO.groovy:37-39`, `EmpresaDAO.groovy:36-38`, `CompetenciaDAO.groovy:48-50`). A mensagem é fixa e não traz dado do cadastro. **Sem teste:** os DAOs não têm teste automatizado, e a gravação no PostgreSQL real não foi repetida depois da mudança. Commit `ac0dce0`.

### AP-04 — Falha de leitura ou de envio da requisição não era registrada

- **Categoria:** Convenções
- **Severidade:** Menor
- **Local:** `microservico-cadastro/src/main/groovy/org/linketinder/cadastro/http/Roteador.groovy:44-59` (em `7d7e620`)
- **Regra:** `AGENTS.md`, Convenções: tratamento de erros explícito; RN-14 para o conteúdo do registro
- **Evidência:** `handle` tinha só `try/finally`. Uma `IOException` ao ler o corpo ou ao enviar a resposta saía do roteador sem passar pelo `RegistroDoServidor`.
- **Correção sugerida:** capturar a `IOException` e registrá-la sem dados da requisição.
- **Situação:** corrigido
- **O que foi corrigido:** `catch (IOException e)` chama `registro.registrarFalhaInesperada(e)`, que escreve só o nome da classe da exceção (`Roteador.groovy:58-59`). **Sem teste:** nenhuma spec provoca essa falha. O `import java.io.IOException` acrescentado é redundante em Groovy, que já importa `java.io.*`. Commit `ac0dce0`.

### AP-05 — `extrairParametros` era chamado duas vezes para a mesma rota

- **Categoria:** Convenções
- **Severidade:** Menor
- **Local:** `microservico-cadastro/src/main/groovy/org/linketinder/cadastro/http/Roteador.groovy:50-53` (em `7d7e620`)
- **Regra:** `AGENTS.md`, Convenções: Clean Code (DRY)
- **Evidência:** o caminho era casado uma vez para achar a rota e outra para montar a `Requisicao`.
- **Correção sugerida:** guardar o resultado em uma variável.
- **Situação:** corrigido
- **O que foi corrigido:** os parâmetros da rota encontrada ficam em `parametros` (`Roteador.groovy:51`). O `find` ainda casa o caminho uma vez para localizar a rota, então a chamada dupla continua para a rota encontrada; o ganho foi de leitura, não de execução. Sem mudança de comportamento; coberto pelas specs de rota existentes. Commit `ac0dce0`.

### AP-06 — Correções da revisão sem registro e sem entrada no CHANGELOG

- **Categoria:** Documentação
- **Severidade:** Importante
- **Local:** `CHANGELOG.md` (seção "Corrigido" vazia) e `docs/specs/microservico/` (sem `revisao.md`), no commit `ac0dce0`
- **Regra:** skill `revisao-com-spec`: registrar antes de corrigir e atualizar o `CHANGELOG.md` em "Corrigido" quando a correção altera código
- **Evidência:** o commit `ac0dce0` alterou 8 arquivos do microsserviço e nenhum documento.
- **Correção sugerida:** escrever o registro e as entradas do CHANGELOG.
- **Situação:** corrigido
- **O que foi corrigido:** este arquivo e as entradas de "Corrigido" no `CHANGELOG.md`, com a referência a AP-01 até AP-05. A ordem "registrar antes de corrigir" não pôde ser recuperada; o registro é posterior às correções e diz isso no início.

### AP-07 — README informa 144 testes; a suíte tem 146

- **Categoria:** Documentação
- **Severidade:** Menor
- **Local:** `README.md:284`
- **Regra:** RN-15 e CA-15
- **Evidência:** o texto diz "São 144 testes em Spock". `./gradlew cleanTest test` em `microservico-cadastro/` rodou 146: o commit `ac0dce0` acrescentou um cenário em `TransacaoJdbcSpec` e um caso em `ConfiguracaoSpec`. O `plano.md:254` também traz 144, mas ali é o relato do dia do checklist e não precisa mudar.
- **Correção sugerida:** trocar o número no README, ou tirar a contagem do texto para ela não envelhecer de novo.
- **Situação:** corrigido, a pedido do autor
- **O que foi corrigido:** `README.md:284` passou a dizer 147 testes, que é o total depois do teste novo de AP-08. A contagem continua escrita no texto, então precisa ser atualizada quando a suíte mudar.

### AP-08 — Mensagem de erro do e-mail usa um domínio que não é o reservado

- **Categoria:** Spec
- **Severidade:** Importante
- **Local:** `microservico-cadastro/src/main/groovy/org/linketinder/cadastro/service/ValidadorDeCadastro.groovy:70`
- **Regra:** RN-16 e CA-16 ("os e-mails são todos de `example.com`")
- **Evidência:** `"O campo 'email' deve ser um e-mail válido, como nome@dominio.com"`. A busca por e-mails nos arquivos da feature encontrou só este fora de `example.com`. É um exemplo de formato, não o dado de uma pessoa, mas `dominio.com` é um domínio registrável e o critério fala em todos os e-mails.
- **Correção sugerida:** trocar o exemplo para `nome@example.com`. Nenhuma spec afirma o texto do exemplo, então a mudança não deve quebrar teste.
- **Situação:** corrigido, a pedido do autor
- **O que foi corrigido:** o exemplo da mensagem passou a ser `nome@example.com` (`ValidadorDeCadastro.groovy:70`). O teste foi escrito antes e falhou com o texto antigo: `service/ValidadorDeCadastroSpec`, "mensagem de e-mail inválido só dá exemplo no domínio reservado example.com". Depois da troca, a busca por `dominio.com` no microsserviço e no README não encontra nada.

## 6. Depois das correções

- **Verificações rodadas de novo:**
  - sobre o commit `ac0dce0`, já com as correções de AP-01 a AP-05: `./gradlew cleanTest test` em `microservico-cadastro/` — 146 testes, nenhuma falha; na raiz — 90 testes, nenhuma falha;
  - depois das correções de AP-07 e AP-08: em `microservico-cadastro/` — 147 testes, nenhuma falha; na raiz — 90 testes, nenhuma falha.
- **Itens repassados:** RN-11, RN-12, RN-13 e RN-14, que são as regras tocadas pelas correções de AP-01 a AP-05, e as saídas alteradas por elas: o log de falha inesperada e as mensagens das novas `SQLException`. Com AP-08, a RN-16 e o CA-16 passam a "atende"; com AP-07, a contagem do README (RN-15) fica certa. A resposta `400` do e-mail inválido continua sem repetir o valor enviado (`service/ValidadorDeCadastroSpec:108,145`).
- **Fica para o autor:**
  - responder à pergunta sobre o método HTTP no log e na resposta `405` (seção 4);
  - repetir um cadastro de candidato, um de empresa e um de competências contra o PostgreSQL real, porque os três DAOs mudaram em `ac0dce0` e não têm teste automatizado;
  - as conferências que o plano já deixava em aberto: rodar o `Main` em outra instância do IntelliJ, ver os cadastros na listagem do CLI e parar de fato o PostgreSQL.
