# Spec: microsserviço de cadastro

Branch: `feature/microservico` · Escopo: novo backend (`microservico-cadastro/`) e documentação · Situação: implementada em 2026-10-11 (aprovada pelo autor na mesma data; o checklist de validação está em `plano.md`)

## 1. Contexto e objetivo

Hoje o cadastro de candidato e de empresa existe em três lugares que não conversam entre si: nos menus do CLI, nos `POST` da API REST (WAR no Tomcat) e nos formulários do frontend, que gravam apenas no `localStorage` do navegador.

Esta feature cria o **microsserviço de cadastro**: uma aplicação nova, um programa à parte, só de backend e sem framework, com o único propósito de cadastrar candidatos e empresas no mesmo banco do Linketinder. Ele fica no mesmo repositório, desacoplado do projeto principal, e é executado em outra instância do IntelliJ, ficando disponível em uma porta.

O cadastro grava todas as informações do candidato e da empresa. As competências do candidato vêm em um segundo passo: depois de cadastrado, ele escreve as competências dele e elas são salvas no banco. Todo candidato é obrigado a ter ao menos uma competência. Esta feature entrega a rota do microsserviço que recebe e salva essas competências; a tela que aparece para o candidato é do site e fica para a próxima feature.

A comunicação entre o frontend e o microsserviço é **apenas explorada**: a feature pesquisa uma solução e a descreve no `README.md`, sem implementá-la. A integração de fato é a próxima feature.

Requisitos obrigatórios do desafio: microsserviço somente de cadastro e sem framework; explorar uma solução de comunicação entre frontend e microsserviço; descrever no `README.md` o que foi explorado; ser possível cadastrar candidatos e empresas pelo microsserviço.

## 2. Regras de negócio

### O serviço

**RN-01 — Aplicação independente.** O microsserviço fica na pasta `microservico-cadastro/`, na raiz do repositório, com build Gradle próprio e código próprio. Ele não importa nenhuma classe de `org.example` do projeto principal, abre sozinho em outra instância do IntelliJ e sobe a partir de um `main`, ficando disponível em uma porta.

**RN-02 — Sem framework.** O HTTP é atendido pelo `com.sun.net.httpserver.HttpServer`, que já vem no JDK. Nenhum framework web e nenhum contêiner (Tomcat) são usados.

**RN-03 — Mesmo banco, mesmo schema.** O serviço grava no banco `linketinder` já existente, nas tabelas `candidatos`, `empresas`, `competencias` e `candidato_competencia`. Nenhuma tabela, coluna ou constraint é criada ou alterada.

**RN-04 — Somente cadastro.** As únicas rotas são:

| Método e rota | Função |
| --- | --- |
| `POST /candidatos` | cadastra um candidato |
| `POST /empresas` | cadastra uma empresa |
| `POST /candidatos/{id}/competencias` | salva as competências de um candidato já cadastrado |

Não existe rota de leitura, listagem, alteração de cadastro ou exclusão, nem cadastro de vagas.

**RN-05 — JSON na entrada e na saída.** O corpo das requisições é um objeto JSON em UTF-8. Toda resposta com corpo é `application/json; charset=UTF-8`. Erros têm o formato `{"erro": "<mensagem>"}`, o mesmo da API atual.

### Cadastro

**RN-06 — Cadastro de candidato.** `POST /candidatos` recebe `nome`, `sobrenome`, `dataNascimento`, `email`, `cpf`, `pais`, `cep`, `senha` (obrigatórios), `descricao` e `formacao` (opcionais, gravados vazios quando ausentes). Com dados válidos, o candidato é inserido, ainda sem competências, e a resposta é `201` com o registro criado: `id` e os campos gravados, **sem a senha** e sem cabeçalho `Location`. O cadastro só fica completo com o segundo passo, o das competências (RN-11).

**RN-07 — Cadastro de empresa.** `POST /empresas` recebe `nome`, `email`, `cnpj`, `pais`, `cep`, `senha` (obrigatórios) e `descricao` (opcional). Com dados válidos, a empresa é inserida e a resposta é `201` com o registro criado, **sem a senha** e sem cabeçalho `Location`. Empresa não tem competências.

**RN-08 — Validação.** Antes de gravar, o serviço valida os dados. Qualquer falha responde `400` com uma mensagem que diz **qual campo** está errado e por quê, sem repetir o valor enviado, e nada é gravado.

| Campo | Regra |
| --- | --- |
| Textos em geral | todo campo é texto: número, lista ou objeto no lugar é erro; espaços das pontas são removidos; campo obrigatório vazio é erro |
| Tamanho máximo | o das colunas: `nome` 50 (candidato) ou 100 (empresa), `sobrenome` 50, `email` 200, `pais` 100, `descricao` 500, `formacao` 100 |
| `senha` | de 6 a 50 caracteres, usada exatamente como enviada |
| `dataNascimento` | formato `AAAA-MM-DD`, data existente e não futura |
| `email` | um `@`, parte local não vazia, domínio com ponto, sem espaços |
| `cpf` | 11 dígitos, só os dígitos ou na pontuação padrão `000.000.000-00`; sem conferência de dígito verificador |
| `cnpj` | 14 dígitos, só os dígitos ou na pontuação padrão `00.000.000/0000-00`; sem conferência de dígito verificador |
| `cep` | 8 dígitos, só os dígitos ou como `00000-000`, qualquer que seja o país |

**RN-09 — Gravação como no CLI.** Os valores validados são gravados e devolvidos como foram enviados, apenas sem os espaços das pontas. O serviço não reformata CPF, CNPJ ou CEP nem muda a caixa do e-mail.

**RN-10 — Cadastro repetido.** Se já existir candidato com o mesmo e-mail ou CPF, ou empresa com o mesmo e-mail ou CNPJ, a resposta é `409` com a mensagem genérica `Já existe um cadastro com esses dados`, sem dizer qual campo coincidiu. Nada é gravado. "Mesmo" é a igualdade exata do banco, como no CLI: grafias diferentes do mesmo documento não são reconhecidas como repetidas.

**RN-11 — Competências do candidato.** `POST /candidatos/{id}/competencias` recebe `{"competencias": ["Java", "SQL"]}`, a lista que o candidato escreveu, e a salva no banco. O candidato é obrigado a ter ao menos uma competência, então a lista precisa trazer pelo menos uma:

- cada nome tem os espaços das pontas removidos; nomes vazios são descartados; repetidos, ignorando maiúsculas e minúsculas, contam uma vez;
- nome com mais de 50 caracteres é erro `400`; lista ausente, ou que fica vazia depois da limpeza, é erro `400`;
- a busca na tabela `competencias` **ignora maiúsculas e minúsculas**: `java` reaproveita a `Java` já cadastrada. Ao reaproveitar, vale a grafia que está no banco (a mais antiga, se houver mais de uma); competência que não existe em nenhuma grafia é criada com a primeira grafia enviada;
- as competências enviadas são **somadas** às que o candidato já tem; a que ele já tem não é vinculada de novo. Esta rota não remove nem troca competências;
- a gravação é tudo ou nada: se qualquer parte falhar, nada muda;
- sucesso responde `200` com `{"id": <id>, "competencias": [...]}`, a lista completa do candidato depois da gravação, com os nomes como estão no banco;
- `id` que não é número responde `400`; candidato inexistente responde `404`.

**RN-12 — Erros de protocolo e de infraestrutura.** Corpo vazio, JSON inválido ou JSON que não é objeto respondem `400`. Rota desconhecida responde `404`. Método não aceito em uma rota conhecida responde `405` com o cabeçalho `Allow`. Falha de banco responde `500` com `Erro ao acessar o banco de dados`, sem detalhe técnico.

### Transversais

**RN-13 — Configuração.** Porta e dados de conexão do banco vêm de variáveis de ambiente, sem recompilar. Porta, URL e usuário do banco têm valor padrão: porta `8081`, URL `jdbc:postgresql://localhost:5432/linketinder` e usuário `postgres`. A **senha do banco não tem valor padrão**: vem só da variável de ambiente e não é escrita em código, teste, script ou documentação. Sem ela, o serviço não sobe e informa qual variável está faltando, sem exibir nenhum valor.

**RN-14 — Nada pessoal em log.** O serviço registra no máximo método, rota e status de cada requisição, e, nas falhas de banco, o código do erro e a primeira linha da mensagem técnica no console do servidor, sem a linha de detalhe (que pode trazer valores gravados). Nunca registra corpo de requisição, senha, e-mail, CPF ou CNPJ. Como alguém pode digitar um desses dados na própria URL, a rota registrada é a rota conhecida do serviço; para rota desconhecida, o registro traz o marcador fixo `(rota desconhecida)` em vez do caminho digitado, e parâmetros de consulta nunca são registrados.

**RN-15 — README e exploração da integração.** O `README.md` ganha:

- a descrição do microsserviço: o que é, como executá-lo em outra instância do IntelliJ, as rotas e exemplos de chamada;
- o relato do que foi explorado sobre a comunicação entre o frontend e o microsserviço: como o formulário enviaria os dados (HTTP com JSON), por que o navegador bloqueia chamadas entre origens diferentes, as soluções consideradas (CORS com preflight no serviço, proxy do servidor de desenvolvimento) e qual fica indicada para a próxima feature. O relato separa o que foi testado do que foi apenas pesquisado.

Nada dessa comunicação é implementado: o frontend não é alterado e o serviço não envia cabeçalhos de CORS. A exploração pode usar um experimento descartável (por exemplo, uma página de teste chamando o serviço), que não é commitado.

**RN-16 — Dados fictícios e nenhuma credencial no repositório.** Testes, exemplos do README e cadastros feitos em conferência manual usam apenas dados fictícios: nomes inventados, e-mails no domínio reservado `example.com` e CPFs e CNPJs com dígitos verificadores inválidos, que não pertencem a ninguém. Nenhuma senha de banco, chave ou arquivo `.env` entra no repositório; o `.gitignore` do microsserviço ignora arquivos `.env`.

### Casos de borda

| Caso | Comportamento esperado |
| --- | --- |
| Campo desconhecido no JSON, inclusive `competencias` no `POST /candidatos` | Ignorado; o cadastro segue normalmente |
| Corpo que não está em UTF-8 (ex.: acentos enviados em outra codificação) | `400` dizendo que o corpo deve estar em UTF-8; nada é gravado, para não guardar texto corrompido |
| Corpo em UTF-8 com acentos (ex.: `"formacao": "Ciência da Computação"`) | Gravado e devolvido com os acentos intactos |
| CPF `11111111111` com `111.111.111-11` já cadastrado | Aceito: são textos diferentes para o banco, como no CLI |
| E-mail `Candidata@Example.com` com `candidata@example.com` já cadastrado | Aceito, pelo mesmo motivo |
| Mesmo CPF, CNPJ ou e-mail, na mesma grafia | `409` genérico |
| CPF ou CNPJ com letras, ou com quantidade errada de dígitos | `400` citando o campo |
| CPF, CNPJ ou CEP com pontuação fora do padrão (ex.: `123-456-789.00`) | `400` citando o campo |
| Campo com número, lista ou objeto no lugar do texto (ex.: `"cpf": 12345678900`) | `400` citando o campo |
| CPF com dígitos verificadores inválidos (ex.: `111.111.111-11`) | Aceito |
| CNPJ alfanumérico | `400`: só o formato numérico é aceito |
| `dataNascimento` `2026-02-30` ou no futuro | `400` |
| Senha com 5 caracteres, ou com mais de 50 | `400` |
| Senha com espaços | Aceita e gravada como enviada |
| Competências `["java", "Java", " JAVA "]`, sem nenhuma no banco | Uma competência só, criada como `java` (a primeira da lista) |
| Competência `"java"` existindo `Java` no banco | Reaproveita `Java`; a resposta traz `Java` |
| Banco já com `Java` e `java` (criadas pelo CLI) | Reaproveita a mais antiga (menor `id`) |
| Candidato com `Java` e `SQL` envia `["Python"]` | Fica com `Java`, `SQL` e `Python` |
| Candidato com `Java` envia `["java", "SQL"]` | Fica com `Java` e `SQL`; `Java` não é vinculada duas vezes |
| Mesma lista enviada duas vezes | A segunda chamada responde `200` e não muda nada |
| Lista `[]` ou só com nomes vazios | `400` citando `competencias` |
| Competências para `id` de candidato que não existe | `404`; nenhuma competência é criada |
| Candidato que não chega a enviar competências (abandona o segundo passo) | Continua no banco sem competências: o serviço não tem como obrigar a segunda chamada. Quem garante o segundo passo é o fluxo do site, na próxima feature |
| `GET /candidatos`, `PUT /empresas`, `DELETE /candidatos/1/competencias`, `OPTIONS /candidatos` | `405` com `Allow` |
| `POST /vagas` ou qualquer rota fora da RN-04 | `404` |
| Chamada vinda de uma página no navegador (outra origem) | O navegador bloqueia, pois o serviço não tem CORS. É o esperado nesta feature |
| Banco fora do ar | `500` genérico |
| Porta já em uso | O serviço não sobe e informa o motivo no console |
| Senha do banco não informada | O serviço não sobe e diz qual variável de ambiente falta, sem exibir nenhum valor |

## 3. Princípio do Anonimato

| Saída da feature | Quem vê | Momento | Campos exibidos |
| --- | --- | --- | --- |
| Resposta `201` de `POST /candidatos` | Quem acabou de se cadastrar | Cadastro (não há contraparte) | `id` e os próprios dados enviados, sem a senha |
| Resposta `201` de `POST /empresas` | Quem acabou de se cadastrar | Cadastro (não há contraparte) | `id` e os próprios dados enviados, sem a senha |
| Resposta `200` de `POST .../competencias` | Quem chamou a rota | Antes do match | `id` e `competencias` (ambos permitidos antes do match) |
| Resposta `400` | Quem chamou | — | Nome do campo e o motivo; nunca o valor |
| Resposta `409` | Quem chamou | — | Mensagem genérica; não diz qual campo coincidiu |
| Respostas `404`, `405`, `500` | Quem chamou | — | Mensagem fixa, sem dados |
| Log do serviço | Quem opera o servidor | — | Método, rota, status |
| Exemplos de chamada no `README.md` | Quem lê o repositório | — | Dados fictícios |

O microsserviço **não tem rota de leitura** do cadastro: não é possível usá-lo para consultar nome, e-mail ou documento de ninguém, e a senha não sai dele em nenhuma resposta nem em log.

Pontos de atenção e pendências conhecidas do `AGENTS.md`:

- **A própria parte vendo o próprio cadastro.** A tabela do `AGENTS.md` trata do que uma parte vê da outra. Devolver a quem se cadastrou os próprios dados, sem a senha, não é exposição (confirmado pelo autor).
- **Sem login.** A rota de competências aceita qualquer `id`: quem souber o `id` de um candidato pode acrescentar competências a ele e ver a lista dele na resposta. A lista de competências é um dado que a empresa já pode ver antes do match, então não há exposição fora da tabela; o risco é de integridade, aceito pelo autor por não existir autenticação no projeto.
- **Senha em texto puro.** O serviço grava a senha como o CLI e a API fazem hoje. Passa a ser pendência conhecida, a registrar no `AGENTS.md` ao final da feature.
- **Pendências do frontend, da API e do CLI** (`localStorage` com cadastro completo, `GET` sem anonimato, nome da empresa na listagem de vagas): não são tocadas.
- **Pendências de dados e credenciais** (usuário e senha fixos em `ProvedorPostgreSQL` e `ProvedorMySQL`, e-mails em domínio real na carga inicial e nas specs existentes): não são tocadas, e o microsserviço não repete nenhuma das duas práticas (RN-13, RN-16).

## 4. Critérios de aceite

**CA-01 — Serviço sobe sozinho e desacoplado**
- Dado o PostgreSQL no ar, a senha do banco informada por variável de ambiente e o projeto principal parado
- Quando o `main` do microsserviço é executado a partir da pasta dele, em outra instância do IntelliJ
- Então ele responde em `http://localhost:8081` sem Tomcat, e nenhum arquivo dele importa `org.example`

**CA-02 — Cadastro de candidato**
- Dado um JSON válido de candidato
- Quando ele é enviado em `POST /candidatos`
- Então a resposta é `201` com `id` e os dados enviados, sem `senha`, e a linha existe na tabela `candidatos` com todos os campos

**CA-03 — Cadastro de empresa**
- Dado um JSON válido de empresa
- Quando ele é enviado em `POST /empresas`
- Então a resposta é `201` com `id` e os dados enviados, sem `senha`, e a linha existe na tabela `empresas` com todos os campos

**CA-04 — Campo obrigatório**
- Dado um JSON de candidato sem `email`
- Quando ele é enviado
- Então a resposta é `400`, a mensagem cita `email` e nada é gravado

**CA-05 — Formatos**
- Dado um JSON com senha de 5 caracteres, ou data futura, ou e-mail sem `@`, ou CPF com 10 dígitos, ou CNPJ com 13, ou CEP com 7
- Quando ele é enviado
- Então a resposta é `400` citando o campo, sem repetir o valor

**CA-06 — Gravação como enviado**
- Dado um candidato com `cpf` `12345678900`, `cep` `79000000` e `email` `Candidata@Example.com`
- Quando o cadastro é aceito
- Então esses três valores são gravados e devolvidos exatamente assim

**CA-07 — Cadastro repetido**
- Dado um candidato já cadastrado
- Quando chega outro cadastro com o mesmo CPF ou o mesmo e-mail, na mesma grafia
- Então a resposta é `409` com a mensagem genérica, que não diz qual campo coincidiu, e nada é gravado

**CA-08 — Competências salvas depois do cadastro**
- Dado um candidato recém-cadastrado, sem competências, e um banco sem `Kotlin` nem `Rust`
- Quando chega `POST /candidatos/{id}/competencias` com `["Kotlin", "kotlin", " Rust ", ""]`
- Então a resposta é `200` com `["Kotlin", "Rust"]`, as duas competências são criadas e o candidato fica vinculado a elas

**CA-09 — Competência existente em outra caixa**
- Dado o banco com a competência `Java`
- Quando chega a lista `["java"]` para um candidato
- Então nenhuma competência nova é criada, o candidato fica vinculado a `Java` e a resposta traz `Java`

**CA-10 — Competências somadas**
- Dado um candidato que já tem `Java`
- Quando chega a lista `["java", "SQL"]`
- Então ele fica com `Java` e `SQL`, sem vínculo repetido, e a resposta traz as duas

**CA-11 — Competências inválidas**
- Dado um `id` sem candidato, ou uma lista vazia
- Quando chega a chamada de competências
- Então a resposta é `404` no primeiro caso e `400` no segundo, e nada é criado

**CA-12 — Rotas e métodos**
- Dado o serviço no ar
- Quando chegam `GET /candidatos`, `POST /vagas` e um `POST` com JSON inválido
- Então as respostas são `405` com `Allow`, `404` e `400`

**CA-13 — Nada fora do microsserviço**
- Dado o repositório ao final da feature
- Quando se compara com o commit de partida
- Então só há mudanças em `microservico-cadastro/`, `docs/specs/microservico/`, `README.md`, `AGENTS.md` e `CHANGELOG.md`; o frontend e o projeto principal não mudaram e os 90 testes da raiz continuam passando

**CA-14 — Anonimato preservado**
- Dado qualquer chamada ao microsserviço
- Quando se examinam as respostas e o log do serviço
- Então a senha não aparece em nenhum deles; as mensagens de erro não repetem valores enviados; a resposta de competências só traz `id` e `competencias`; e nenhuma rota permite ler o cadastro de outra pessoa

**CA-15 — README**
- Dado o `README.md` ao final
- Quando alguém que não participou o lê
- Então consegue subir o microsserviço, chamar as três rotas e entender qual solução de comunicação com o frontend foi explorada, o que foi testado e o que ficou para a próxima feature

**CA-16 — Sem credenciais nem dados reais**
- Dado o repositório ao final da feature
- Quando se procura, nos arquivos criados ou alterados por ela, por senha de banco, chave, arquivo `.env`, e-mails, CPFs, CNPJs e nomes
- Então não há nenhuma credencial; os e-mails são todos de `example.com`; os CPFs e CNPJs têm dígitos verificadores inválidos; e o serviço, sem a variável da senha do banco, não sobe

Como são verificados: CA-02 a CA-12 e CA-14 por specs Spock no próprio microsserviço, sem banco (repositórios simulados); a gravação real no PostgreSQL (CA-01 a CA-03 e CA-07 a CA-11) por conferência manual com `curl` e consulta ao banco; CA-13 por `git diff` e `./gradlew test` na raiz; CA-15 por leitura do autor; CA-16 por busca nos arquivos da feature e por uma spec da configuração.

## 5. Impacto no que já existe

| Funcionalidade atual | Impacto |
| --- | --- |
| CLI (`Main`) | Sem mudança de código. Cadastros e competências gravados pelo microsserviço aparecem nas listagens do CLI, pois o banco é o mesmo. O CLI continua permitindo cadastrar candidato sem competência: a obrigatoriedade de ao menos uma vale só no microsserviço |
| API REST no Tomcat | Sem mudança. `POST /candidatos` e `POST /empresas` do WAR continuam existindo: passam a haver dois caminhos de cadastro por HTTP, com comportamentos diferentes (a API atual recebe competências no próprio cadastro, não valida tamanho de senha nem formatos, e responde `500` para cadastro repetido) |
| Competências | O microsserviço reaproveita competências ignorando maiúsculas; o CLI e a API atual continuam com busca exata. A mesma lista pode gerar resultados diferentes conforme o caminho usado |
| Build e testes do projeto principal | Sem mudança. `./gradlew test` na raiz continua rodando os 90 testes; o microsserviço tem build e testes próprios, que a raiz não executa |
| Schema (`database/Linketinder.sql`, DER) | Sem impacto |
| Frontend | Sem mudança. Os formulários continuam gravando só no `localStorage` e não chamam o microsserviço |
| Documentação | `README.md`, `AGENTS.md` (nova parte do repositório, nova pendência da senha) e `CHANGELOG.md` são atualizados |

## 6. Fora do escopo

Esta feature **não** vai:

- alterar qualquer arquivo do frontend, inclusive criar a tela de competências;
- implementar a comunicação entre frontend e microsserviço: nem `fetch` nos formulários, nem CORS ou `OPTIONS` no serviço, nem proxy no Vite;
- remover ou trocar competências de um candidato, nem listar as competências existentes;
- criar rota de leitura, listagem, alteração ou exclusão no microsserviço;
- cadastrar vagas, curtidas ou matches pelo microsserviço;
- criar competências para empresa nem alterar o schema;
- implementar login, autenticação ou autorização;
- aplicar hash na senha;
- normalizar CPF, CNPJ, CEP ou e-mail, validar dígitos verificadores ou aceitar o CNPJ alfanumérico;
- alterar ou remover os `POST` da API do Tomcat, mudar a busca de competências do CLI ou corrigir as pendências de anonimato existentes;
- corrigir o `;` faltante de `database/Linketinder.sql` nem configurar o `mainClass` do projeto principal;
- tirar o usuário e a senha fixos de `ProvedorPostgreSQL` e `ProvedorMySQL`, nem trocar os e-mails em domínio real da carga inicial e das specs existentes;
- compartilhar código entre o microsserviço e o projeto principal.

### Pendências para features futuras

Ficam registradas aqui para não se perderem; nenhuma é implementada agora.

- **Integração do frontend com o microsserviço** (próxima feature): os formulários do site passam a chamar o serviço, e logo depois do cadastro aparece para o candidato o campo para escrever as competências, que são enviadas à rota da RN-11. Esse passo é obrigatório no site, já que o candidato precisa ter ao menos uma competência. Inclui decidir e implementar a solução explorada na RN-15.
- **Alteração das competências a qualquer momento**: o candidato poder trocar ou remover as competências dele depois de salvas. A definir em feature própria.
- **Campo `competencias` enviado no cadastro**: decidir se o `POST /candidatos` deve recusar esse campo, avisando que competências têm rota própria. Enquanto não houver decisão, ele é ignorado como qualquer campo desconhecido.
- **Senha em texto puro**: aplicar hash exige alterar a coluna `senha` e os três caminhos de cadastro.

## 7. Decisões

Confirmadas pelo autor:

- O microsserviço fica em pasta própria, com build e código próprios, sem importar o projeto principal, e roda em outra instância do IntelliJ.
- Servidor HTTP: `com.sun.net.httpserver.HttpServer` do JDK.
- Uma rota por perfil: `POST /candidatos` e `POST /empresas`. O microsserviço cadastra apenas candidatos e empresas, não vagas.
- O cadastro grava todas as informações do candidato e da empresa; as competências vêm em um segundo passo, depois do cadastro.
- A rota do microsserviço que salva as competências entra nesta feature; a tela do site fica para a próxima.
- Busca de competência no banco ignorando maiúsculas e minúsculas. Ao reaproveitar, vale a grafia que já está no banco (a mais antiga, se houver mais de uma); ao criar, vale a primeira grafia da lista enviada.
- O candidato é obrigado a ter ao menos uma competência: a rota de competências responde `400` para lista vazia.
- Chamada repetida da rota de competências soma as novas às que o candidato já tem, sem remover nenhuma; por acrescentar, a rota usa `POST`.
- Cadastro e competências são duas chamadas separadas, então o microsserviço não impede que alguém faça só a primeira: esse candidato fica no banco sem competências. A regra de "ao menos uma" é garantida pela rota de competências e, na próxima feature, pelo site, que torna o segundo passo obrigatório.
- Trocar ou remover competências depois de salvas é pendência para outra feature.
- Sem login, a rota de competências aceita qualquer `id`.
- A comunicação com o frontend é só explorada e descrita no README; não há integração nesta feature, e o serviço não tem CORS.
- A exploração pode usar um experimento descartável, que não é commitado; o README diz o que foi testado e o que foi só pesquisado.
- A API REST do Tomcat fica como está.
- Cadastro repetido responde `409` com mensagem genérica.
- Campos obrigatórios iguais aos da API atual; descrição e formação opcionais.
- Validação de senha, data e formatos de e-mail, CPF, CNPJ e CEP.
- CPF e CNPJ validados só pela quantidade de dígitos, sem dígito verificador e sem CNPJ alfanumérico.
- CEP sempre com 8 dígitos, qualquer que seja o país.
- Valores gravados como enviados, igual ao CLI, sem normalização.
- Tamanhos máximos das colunas validados pelo serviço; senha de até 50 caracteres, sem remoção de espaços.
- Resposta `201` com o registro criado, sem a senha e sem cabeçalho `Location`; devolver os próprios dados a quem se cadastrou não fere o anonimato.
- Senha gravada como hoje; o texto puro vira pendência conhecida.
- Porta `8081`, trocável por variável de ambiente.
- Dados fictícios em testes, exemplos e conferências manuais, e nenhuma credencial no repositório (regras do autor, registradas no `AGENTS.md`).
- A senha do banco não tem valor padrão no código: vem só de variável de ambiente, e sem ela o serviço não sobe. URL e usuário (`postgres`) têm padrão.
- Dados fictícios são e-mails em `example.com` e CPFs e CNPJs com dígitos verificadores inválidos.
- O `.gitignore` da raiz não ganha a linha `.env` nesta feature; só o do microsserviço ignora `.env`.
- Mesma stack do projeto: Groovy 5.0.8, Gradle, `groovy-json`, driver do PostgreSQL e Spock.

Premissas assumidas na escrita, a confirmar:

- Nenhuma.
