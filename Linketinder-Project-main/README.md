autor: Guilherme Lima Conte

**Como executar:**

# Na raiz do projeto, execute:

`groovy -cp src/main/groovy src/main/groovy/org/example/Main.groovy`

Caso esteja utilizando IntelliJ, é possível executar o projeto apertando o botão de play enquanto está no diretório Main.groovy.

# Informações do projeto:

Optei por explorar o máximo que dava dos benefícios que o groovy proporciona. Para deixar o mais limpo possível não utilizei tipo de visibilidade nas classes, atributos, métodos e construtores e também deixei a maioria dos tipos como def, com exceção das competências que tem como tipo uma lista genérica de strings.
Utilizei do conceito de "Groovy Truth" do curso da udemy no método formatarCompetencias, como listas vazias já possuem valor false me permitiu já iniciar o operador ternário validando a verdade.

É possível adicionar novos candidatos e novas empresas, não foi feito nenhuma validação para confirmar se os tipos estão corretos e no modelo que deve ser seguido no cadastro, mas já é possível.

Devido a diversidade que eu quis deixar nas empresas, não deixei as competências já pré-determinadas para quem vai fazer o cadastro, possibilitando ser qualquer nova competência.

# Front-end

Foi implementando a listagem dos candidatos disponíveis na visão da empresa e as vagas disponíveis na visão do candidato, mostrando todas as informações do candidato/vaga deixando anônimo apenas o nome.
Foi adicionado um cadastro ao lado, da empresa e do candidato.
Por fim da listagem dos candidatos disponíveis há um gráfico em barras adicionado usando a biblioteca chart.js que indica o número de candidatos por competências

## Perfil de recrutador

A feature de recrutador adiciona um terceiro tipo de usuário em `frontend/` e na API REST. O recrutador entra com seu próprio login, vê apenas as vagas atribuídas pela empresa e pode curtir candidatos dentro desse escopo, sem ver dados pessoais sensíveis do candidato fora do que a própria empresa teria acesso.

O recrutador não se autocadastra: a empresa cadastra, atribui e remove vagas dele e pode excluí-lo. Atualizar os dados de um recrutador é só pelo CLI. As rotas estão no contrato da API REST, mais abaixo, e o que cada perfil pode ver está na spec, em `docs/specs/recrutador/spec.md`.

# Banco de dados

O banco de dados foi desenvolvido utilizando PostgreSQL, graças a ele pude observar um problema crítico em meu projeto que não considerava Empresa e Vaga como classes diferentes, ccom o desenvolvimento do banco de dados, pude perceber esse erro e corrigi-lo. O arquivo localizado em `database/Linketinder.sql` contém todas as criações, inserções e busca realizadas desde sua criação.
Inicialmente o diagrama entidade relacionamento foi feito no site dbdiagram.io, depois realizei o export em Postgre e continuei no pgadmin4.

Para o Desafio ZG foi implementado a sincronização do Groovy com o banco de dados com a criação das classes dentro do diretório `dao`, foi implementado o CRUD das 4 classes exigidas: Candidato, Empresa, Competencia, Vaga. Além de algumas verificações e buscar a fim de evitar uma possível quebra no banco de dados.

![der do Linketinder](assets/der.png)

# Match e Curtida

Foi implementado duas novas classes Match e Curtida para que a mecânica de um candidato curtir uma vaga e a empresa responsável por aquela vaga curtir o candidato gere um Match para candidato-vaga. Aproveitei o fato de já ter implementado o banco de dados e criei um novo método em VagaDAO para listar todas as vagas daquela empresa e usar dessa informação durante o menu para quando estivermos sendo a "empresa" aparecer todas as vagas que aquela empresa possui.

# Índice de afinidade

Foi adicionado na Visão do Candidato o índice de afinidade que o candidato possui com aquela vaga com base nas competências do candidato/vaga. O cálculo é feito com a quantidade de competências que o candidato tem em comum com a vaga dividido pela quantidade total de competências da vaga.
Foi adicionado também durante a produção do índice um select para simularmos melhor a "experiência" que seria estar na visão naquele candidato. Por ora será assim até a implementação do sistema de login.

# Inserção da lógica de curtida no banco de dados

Foi criado uma tabela de curtida referente ao candidato e uma tabela de curtida referente a empresa/vaga. O match é realizado quando as duas tabelas de curtidas são validadas. Para isso acontecer adicionei uma restrição em vagas que valida se aquela vaga está relacionado com a empresa que iria curtir o candidato, algo que não pode acontecer. Por fim, inserimos algumas curtidas e inserimos a lógica do match e logo depois verificamos isso no `SELECT`. 

# MVP do match

Como já existia a lógica do match devido a inserção da lógica de curtida no bd, nada mais prático do que eu criar uma classe Match que se comunica com o bd também. Devido desde antes eu já ter feito um código para mostrar se o match ocorreu e a listagem de matches, eu aproveitei de uns trecho do código antigo, assim não tendo alteração na main sobre a listagem dos matches.

# Clean code

Agora com a curtidaDAO, a classe Curtida é desnecessário.
Alterei as variáveis que ainda estavam com o tipo def. Entre elas estava a dataNascimento. Agora é do tipo LocalDate e possui um método em Cadastro chamado lerDataNascimento().
estaEmUso era um nome de método que não dizia claramente o que fazia, troquei para estaVinculadoACandidatoOuVaga
Para manter o padrão exibirMatch se tornou exibirDados também.
Coloquei duas variáveis estáticas e constantes para representar o tamanho da senha na hora de cadastrar e o id na main.

Refatorei todas as classes DAO, agora ConexaoBanco tem o método executar que já facilita a abertura e fechamento das conexões, reduzindo muitas linhas repetidas nos DAO. 

Refatorei também as classes Cadastro e Main. Agora Cadastro foi divido em 7 novas classes. A ideia era somente diminuir a classe Cadastro que estava enorme, mas ao fazer essas mudanças notei que alguns métodos estavam muito grandes e fiz a criação de novos, como selecionarCompetencia, selecionaCandidato, etc.. lerDadosVaga, lerDadosEmpresa, etc.. Dessa forma consegui reduzir algumas classes quase pela metade e outras até mais que a metade.

Agora com a classe Cadastro refatorado, foi possível diminuir a main também. Pois agora não possui mais menuCandidato, menuEmpresa, etc.. Também apliquei os testes que haviam deixado de funcionar devido as mudanças do código.

No front-end eu separei todas as funcionalidades que estavam inseridas no main. Agora tem 6 novas classes divididas que fazem a mesma funcionalidade de antes.

Adicionei 6 novas interfaces localizadas no pacote `org.example/repositorio` que são responsáveis em dizer quais operações existem sobre um candidato, vaga, empresa.. Agora uma classe de Cadastro não conhece uma classe que abre conexão com o banco de dados, ela depende da abstração e não de uma classe DAO.
Isso resulta que agora quem instância o DAO é o Main, ele é a única classe que conhece as classes concretas. Em consequência disso agora é possível passar `CandidatoRepositorio` no lugar de `CandidatoDAO` como parâmetro de um Mock. Ou seja, os testes unitários rodam sem banco nenhum.

# Design Pattern

## Factory

ConexaoBanco estava tendo muitas funcionalidades, ele sabia qual banco usar, abria a conexão e executava as consultas, por isso eu separei essas funcionalidades em 5 classes novas aplicando o padrão Factory. O TipoBanco lê da variável de ambiente LINKETINDER_BANCO qual banco usar. 
A interface ProvedorConexao define o contrato de abrir uma conexão, e cada banco tem sua implementação (ProvedorPostgreSQL, ProvedorMySQL). 
E a FactoryConexao recebe o tipo e cria o provedor correto. Assim, trocar de banco não exige mudar o código, só a variável. Agora um banco novo é só mais um provedor.

## Singleton

Antes, o `ConexaoBanco` abria uma conexão nova a cada consulta e a fechava no final. Nas listagens isso se multiplicava, pois cada item faz as próprias consultas internas.
Apliquei o Singleton no `ConexaoBanco` para existir uma única instância dele na aplicação. O construtor é private, então ninguém cria outra com new e a instância fica num campo private static e é obtida por `ConexaoBanco.obterInstancia()`, criada só no primeiro uso. Medindo no Postgre, listar candidatos abria 6 conexões, listar vagas 11, listar matches 13 e agora cada uma usa uma conexão só.
Essa instância abre a conexão uma única vez e a reaproveita em todas as consultas. Antes de usar, `isValid()` confere se ela ainda está ativa e, se o banco tiver reiniciado, uma nova é aberta automaticamente. E ao sair, o Main chama fechar(). Os DAOs continuam recebendo `ConexaoBanco` pelo construtor, no entanto, somente a `Main` chama `obterInstancia()`. Assim o Singleton garante uma instância única sem que cada classe dependa de um método estático.


# MVC

As 7 classes base do projeto foram para o pacote `model`

Os modelos pararam de imprimir `exibirDados()`, agora virou `descrever()` que retorna o texto em vez de imprimir. Pois, agora quem faz a função de imprimir é a view. A leitura do teclado e a impressão saíram das classes de cadastro e foram para as views.

As regras de negócio foram para o service que está entre os controllers e os repositórios.

Quando uma regra recusa a operação, o service lança `RegraDeNegocioException` com uma mensagem, onde o `MenuController` captura e manda a view exibir

O que sobrou das classes de cadastro virou os controllers, que pedem os dados à view, entregam ao service e mandam a view responder. 

O `MenuController` assumiu os menus da `Main` e `CadastroCrud` se tornou `ControllerCrud` implementada pelos quatro controllers de CRUD. 

E agora a `Main` ficou só com a montagem das dependências.



# API REST

Devido a construção do código aplicando os princípios SOLID e MVC foi possível implementar a lógica de API sem precisar alterar os códigos já existentes, somente nas classes `Provedor` eu tive que colocar o driver como uma constante pois, o driver não se registrava sozinho, mas `DriverManager` já havia sido inicializado pelo TomCat antes da aplicação subir e a varredura pelos drivers já havia acontecido

## Contrato

Base: `http://localhost:8080/linketinder`

Temos 20 endpoints. Todos usam `GET` ou `POST`, menos os dois `DELETE` das rotas de recrutador, que são a única exceção à convenção:
POST em `/candidatos` cadastra um candidato, se houver sucesso lança 201 + `Location` e se der erro apresenta 400
GET em `/candidatos` lista todos os candidatos, se houver sucesso lança 200 e se der erro apresenta 500
GET em `/candidatos/{id}` busca um candidato, se houver sucesso lança 200 e se der erro apresenta 400 se o id não for número e 404 se não existir
POST em `/empresas` cadastra uma empresa, se houver sucesso lança 201 + `Location` e se der erro apresenta 400
GET em `/empresas` lista todas as empresas, se houver sucesso lança 200 e se der erro apresenta 500
GET em `/empresas/{id}` busca uma empresa, se houver sucesso lança 200 e se der erro apresenta 400 se o id não for número e 404 se não existir
POST em `/vagas` cadastra uma vaga, se houver sucesso lança 201 + `Location` e se der erro apresenta 400
GET em `/vagas` lista todas as vagas, se houver sucesso lança 200 e se der erro apresenta 500
GET em `/vagas?idEmpresa={id}` lista todas as vagas de uma empresa, se houver sucesso lança 200 e se der erro apresenta 400
GET em `/vagas/{id}` busca uma vaga, se houver sucesso lança 200 e se houver erro apresenta 400 se o id não for número e 404 se não existir
GET em `/competencias` lista as competências já cadastradas, se houver sucesso lança 200 e se der erro apresenta 500
POST em `/competencias` soma competências a um candidato já cadastrado, se houver sucesso lança 201, apresenta 400 se a lista vier vazia e 404 se o candidato não existir
POST em `/login` autentica candidato, empresa ou recrutador conforme o campo `tipo`, devolvendo 200 com `{tipo, id, nome}` — mais `idEmpresa` para o recrutador —, 401 com mensagem genérica quando o e-mail ou a senha estão errados e 400 se o `tipo` não for um dos três. Nunca devolve a senha
POST em `/curtidas` registra a curtida do candidato na vaga, da empresa no candidato ou do recrutador no candidato conforme o campo `tipo`, se houver sucesso lança 201 com `{"match": true|false}`, apresenta 400 se o `tipo` for inválido, se a vaga não for da empresa ou se não estiver atribuída ao recrutador, e 404 se candidato, vaga ou recrutador não existir
POST em `/recrutadores` cadastra um recrutador da empresa, se houver sucesso lança 201 + `Location` com id, nome, área, descrição, e-mail e idEmpresa, e se der erro apresenta 400; nunca devolve senha, CPF nem data de nascimento
GET em `/recrutadores?idEmpresa={id}` lista os recrutadores de uma empresa, se houver sucesso lança 200, apresenta 400 sem o parâmetro ou com id não numérico e 404 se a empresa não existir
DELETE em `/recrutadores/{id}` remove o recrutador, se houver sucesso lança 204 sem corpo e apresenta 404 se ele não existir
GET em `/recrutadores/{id}/vagas` lista as vagas atribuídas ao recrutador, se houver sucesso lança 200 e apresenta 404 se o recrutador não existir
POST em `/recrutadores/{id}/vagas` atribui uma vaga ao recrutador (corpo `{"idVaga": n}`), se houver sucesso lança 201 com a lista atualizada de vagas dele, apresenta 400 se a vaga não for da empresa dele e 404 se recrutador ou vaga não existir
DELETE em `/recrutadores/{id}/vagas/{idVaga}` desatribui a vaga, se houver sucesso lança 204 sem corpo, apresenta 400 se a vaga não estiver atribuída a ele e 404 se recrutador ou vaga não existir

`PUT` é respondido com erro 405 e o cabeçalho `Allow`, e o mesmo acontece com `DELETE` fora das rotas de recrutador. Qualquer requisição pode dar erro 500 caso o banco falhe.

## Recursos utilizados

`javax.servlet.api` versão 4.0.1 para compilar o servlet
Apache Tomcat versão 9.0.122 para servir como servidor que executa os servlets
`groovy-json` versão 5.0.8 
`JsonSlurper` e `JsonOutput`

O servlet recebe um `HttpServletRequest`, escreve em uma `HttpServletResponde` e o Tomcat cuida das portase threads.

No `build.gradle` tive que adicionar esses recursos então agora

`plugins` passou a ter od id `groovy` `application` e `war`

e nas dependências:

```groovy
dependencies {
    compileOnly 'javax.servlet:javax.servlet-api:4.0.1'
    testImplementation 'javax.servlet:javax.servlet-api:4.0.1'
    implementation 'org.apache.groovy:groovy-json:5.0.8'
}
```

É necessário `compileOnly` no servlet-api pois quem fornece essa biblioteca em tempo de execução é o Tomcat, então se ela fosse junto no WAR, haveria duas cópias da mesma classe e o Tomcat recusaria a aplicação.

Já o driver do PostGreSQL e o Groovy precisam ir no WAR (`implementation), porque o Tomcat não os tem.

`compileOnly` não vale para os testes. Como os specs criam mocks de `HttpServletRequest`, e o servlet-api precisa estar no classpath de teste então utilizei `testImplementation` porque o Spock precisa da classe em tempo de execução para gerar o mock. Ele também não entra no WAR, porque o `war` empacota `runtimeClasspath`

## endpoints

Todas as classes servlets tem o caminho declarado com @WebServlet pois o Tomcat varre as anotações do WAR ao subir a aplicação.

`HttpServlet` chama `doGet` para requisição GET e `doPost` para requisição POST, as demais requisições responde com 405.

## observação

O `POST` aceita o campo `senha` para o cadastro mas `Json.deCandidato` e `Json.deEmpresa` não o incluem, dessa forma um cliente que lista candidatos nunca vai receber as senhas.


## como rodar

1. Baixe o **Tomcat 9** e descompacte
2. Gere o WAR `./gradlew war` -> `build/libs/linketinder.war`
3. Copie o WAR para `apache-tomcat-9.0.122/webapps`
4. Suba o servidor: `bin/startup.sh`
5. O banco de dados é necessário estar rodando, caso contrário aparecerá erro 500 ao executar o passo 6.
6. Entre em `curl http://localhost:8080/linketinder/candidatos`

# Microsserviço de cadastro

O cadastro de candidatos e de empresas também existe como um **microsserviço**: uma aplicação à parte, só de backend, que fica na pasta `microservico-cadastro/` e grava no mesmo banco do Linketinder.

Ele foi feito **sem framework**: o HTTP é atendido pelo `com.sun.net.httpserver.HttpServer`, que já vem no JDK, sem Tomcat e sem servlet. A pasta tem o próprio build Gradle e o próprio código (pacote `org.linketinder.cadastro`), e não importa nenhuma classe do projeto principal. O propósito dele é único: cadastrar. Não há rota de leitura, listagem, alteração ou exclusão.

## Como executar

1. Deixe o PostgreSQL no ar, com o banco `linketinder` criado pelo script `database/Linketinder.sql`.
2. Abra a pasta `microservico-cadastro/` em **outra janela do IntelliJ** (`File > Open`). Ela é um projeto Gradle independente.
3. Na configuração de execução da classe `org.linketinder.cadastro.Main`, em *Environment variables*, defina `LINKETINDER_CADASTRO_BANCO_SENHA=<sua-senha>`.
4. Rode o `Main`. O console mostra `Microsserviço de cadastro disponível na porta 8081`.

Pelo terminal, a partir da pasta `microservico-cadastro/`:

```bash
# Git Bash / Linux / macOS
LINKETINDER_CADASTRO_BANCO_SENHA=<sua-senha> ./gradlew run
```

```powershell
# PowerShell
$env:LINKETINDER_CADASTRO_BANCO_SENHA = "<sua-senha>"; .\gradlew.bat run
```

A configuração vem de variáveis de ambiente:

| Variável | Padrão | Para que serve |
| --- | --- | --- |
| `LINKETINDER_CADASTRO_BANCO_SENHA` | não tem | Senha do banco. É obrigatória: sem ela o serviço não sobe e avisa qual variável falta |
| `LINKETINDER_CADASTRO_PORTA` | `8081` | Porta em que o serviço fica disponível |
| `LINKETINDER_CADASTRO_BANCO_URL` | `jdbc:postgresql://localhost:5432/linketinder` | Endereço do banco |
| `LINKETINDER_CADASTRO_BANCO_USUARIO` | `postgres` | Usuário do banco |

A senha do banco não tem valor padrão de propósito: ela não fica escrita no código nem em nenhum arquivo do repositório.

## Rotas

Base: `http://localhost:8081`. Requisições e respostas são JSON em UTF-8.

| Método e rota | O que faz | Sucesso |
| --- | --- | --- |
| `POST /candidatos` | Cadastra um candidato | `201` com o registro criado, sem a senha |
| `POST /empresas` | Cadastra uma empresa | `201` com o registro criado, sem a senha |
| `POST /candidatos/{id}/competencias` | Salva as competências de um candidato já cadastrado | `200` com `id` e a lista completa de competências |

O cadastro do candidato acontece em dois passos: primeiro os dados pessoais, depois as competências.

Os dados dos exemplos abaixo são fictícios.

**Cadastrar candidato.** Obrigatórios: `nome`, `sobrenome`, `dataNascimento` (`AAAA-MM-DD`), `email`, `cpf`, `pais`, `cep` e `senha` (de 6 a 50 caracteres). Opcionais: `descricao` e `formacao`.

```bash
curl -i -X POST http://localhost:8081/candidatos \
  -H "Content-Type: application/json" \
  -d '{"nome":"Candidata","sobrenome":"Exemplo","dataNascimento":"2000-01-15","email":"candidata@example.com","cpf":"123.456.789-00","pais":"Brasil","cep":"79000-000","senha":"<uma-senha>","formacao":"Curso de exemplo"}'
```

```json
{"id":9,"nome":"Candidata","sobrenome":"Exemplo","dataNascimento":"2000-01-15","email":"candidata@example.com","cpf":"123.456.789-00","pais":"Brasil","cep":"79000-000","descricao":"","formacao":"Curso de exemplo"}
```

**Cadastrar empresa.** Obrigatórios: `nome`, `email`, `cnpj`, `pais`, `cep` e `senha`. Opcional: `descricao`.

```bash
curl -i -X POST http://localhost:8081/empresas \
  -H "Content-Type: application/json" \
  -d '{"nome":"Empresa Exemplo","email":"contato@example.com","cnpj":"12.345.678/0001-00","pais":"Brasil","cep":"79000-000","senha":"<uma-senha>"}'
```

```json
{"id":6,"nome":"Empresa Exemplo","email":"contato@example.com","cnpj":"12.345.678/0001-00","pais":"Brasil","cep":"79000-000","descricao":""}
```

**Salvar as competências do candidato.** Usa o `id` devolvido no cadastro. A lista precisa ter ao menos uma competência.

```bash
curl -i -X POST http://localhost:8081/candidatos/9/competencias \
  -H "Content-Type: application/json" \
  -d '{"competencias":["java","SQL"]}'
```

```json
{"id":9,"competencias":["Java","SQL"]}
```

Regras das competências: nomes vazios e repetidos são descartados; uma competência que já existe no banco é reaproveitada mesmo que a caixa seja diferente (`java` usa a `Java` já cadastrada); a que não existe é criada; chamar a rota de novo **soma** as novas às que o candidato já tem.

Respostas de erro, sempre no formato `{"erro": "mensagem"}`:

| Status | Quando |
| --- | --- |
| `400` | Campo obrigatório ausente, formato inválido, JSON inválido, corpo vazio ou corpo fora de UTF-8. A mensagem diz qual campo, sem repetir o valor enviado |
| `404` | Rota desconhecida, ou candidato inexistente na rota de competências |
| `405` | Método não aceito na rota (a resposta traz o cabeçalho `Allow`) |
| `409` | Já existe cadastro com o mesmo e-mail, CPF ou CNPJ. A mensagem é genérica e não diz qual campo coincidiu |
| `500` | Falha de banco, com mensagem genérica |

Validações: CPF com 11 dígitos, CNPJ com 14 e CEP com 8, só os dígitos ou na pontuação padrão; e-mail com `@` e domínio; data de nascimento existente e não futura. Os valores são gravados como foram enviados, só sem os espaços das pontas.

Atenção no Windows: um acento digitado direto na linha de comando do `curl` pode sair em outra codificação, e o serviço responde `400` pedindo UTF-8. Nesse caso, salve o JSON em um arquivo UTF-8 e envie com `--data-binary @arquivo.json`.

## Testes

```bash
cd microservico-cadastro
./gradlew test
```

São 147 testes em Spock, que não precisam de banco: as regras são testadas com repositórios simulados, e as rotas são testadas subindo o servidor de verdade em uma porta livre e chamando por HTTP. A gravação no PostgreSQL foi conferida manualmente.

## Exploração: comunicação entre o frontend e o microsserviço

Esta parte foi **explorada, não implementada**. O frontend continua gravando no `localStorage` e o microsserviço não tem CORS. O que segue é o que foi testado e pesquisado para decidir como fazer a integração na próxima feature.

### Como o formulário enviaria os dados

O formulário de cadastro coletaria os campos em um objeto, converteria em JSON e enviaria por HTTP com `fetch`, lendo de volta o `id` do registro criado:

```ts
const resposta = await fetch("http://localhost:8081/candidatos", {
    method: "POST",
    headers: {"Content-Type": "application/json"},
    body: JSON.stringify(dadosDoFormulario)
});
const candidato = await resposta.json();
```

Com o `id` em mãos, a tela seguinte enviaria as competências para `POST /candidatos/{id}/competencias`.

### O obstáculo: origens diferentes

O navegador só deixa uma página chamar livremente a própria **origem** (protocolo, endereço e porta). O site roda em `http://localhost:5173` (Vite) e o microsserviço em `http://localhost:8081`: a porta muda, então são origens diferentes, e a chamada só é liberada se o serviço autorizar por cabeçalhos de CORS.

### O que foi testado

Em um experimento descartável, que não está no repositório, uma página de teste servida em `http://localhost:5173` fez as chamadas abaixo, abertas em um navegador de verdade (Microsoft Edge). Do outro lado ficaram o microsserviço como ele é e uma cópia temporária dele com CORS acrescentado.

| Tentativa | O que aconteceu |
| --- | --- |
| `fetch` com JSON para o serviço atual, sem CORS | Antes do `POST`, o navegador enviou sozinho um `OPTIONS` (o *preflight*). O serviço respondeu `405`, sem cabeçalhos de CORS, e o navegador bloqueou: o `POST` nem chegou a sair, e a página recebeu `TypeError: Failed to fetch` |
| `fetch` sem `Content-Type` para o serviço atual | Não houve *preflight*, e o `POST` **chegou ao serviço e foi processado**. Mesmo assim, o navegador não entregou a resposta à página |
| `fetch` com JSON para a cópia com CORS | O `OPTIONS` voltou `204` com os cabeçalhos de autorização, o `POST` saiu em seguida e a página leu a resposta normalmente |
| Mesma chamada vinda de outra origem (simulada com `curl`) | A cópia não enviou os cabeçalhos de autorização, então o navegador manteria o bloqueio |

Duas conclusões desses testes:

- O *preflight* só acontece porque o corpo vai como `application/json`. Ele precisa ser respondido: o serviço tem de aceitar `OPTIONS` nas rotas de cadastro.
- CORS não protege o serviço, só a página. A segunda tentativa mostra que uma página qualquer consegue fazer o serviço processar um `POST`, apenas não consegue ler a resposta. E o `curl` nem passa por essa regra. Proteção de verdade é autenticação, que o projeto ainda não tem.

### Soluções consideradas

**1. CORS no microsserviço (testada).** O serviço passa a:

- responder ao `OPTIONS` das rotas de cadastro com `204` e os cabeçalhos `Access-Control-Allow-Origin`, `Access-Control-Allow-Methods` e `Access-Control-Allow-Headers: Content-Type`;
- incluir `Access-Control-Allow-Origin` (e `Vary: Origin`) nas demais respostas, inclusive nas de erro, para a página conseguir ler a mensagem;
- fazer isso só para a origem do frontend, configurada por variável de ambiente.

Funciona em qualquer ambiente e mantém o frontend chamando o serviço direto pela porta dele. O custo é o serviço conhecer a origem do site.

**2. Proxy do servidor de desenvolvimento (pesquisada, não testada).** O Vite pode ser configurado (`server.proxy`) para repassar, por exemplo, `/api` para `http://localhost:8081`. A página chama a própria origem e o navegador não vê cruzamento nenhum, então o serviço não precisa de CORS. A limitação é valer só no servidor de desenvolvimento: fora dele seria preciso outro proxy na frente das duas aplicações.

### Indicação para a próxima feature

A indicada é o **CORS no microsserviço**, com a origem permitida em variável de ambiente: foi a que funcionou no teste, não depende da ferramenta do frontend e deixa explícito, no próprio serviço, quem pode chamá-lo pelo navegador. O proxy do Vite fica como alternativa para o ambiente de desenvolvimento.

Fica para a próxima feature: implementar o CORS, trocar a gravação no `localStorage` dos formulários pelo `fetch` e criar a tela em que o candidato escreve as competências logo depois do cadastro.

### Sobre fazer sem framework

O `HttpServer` do JDK cuida do protocolo HTTP e entrega cada requisição a um manipulador. O que um framework faria a mais foi escrito à mão no serviço: o roteamento (o `HttpServer` casa caminhos por prefixo de texto, então `/candidatosX` cairia em `/candidatos`; por isso há um roteador próprio que compara o caminho inteiro), a leitura e a escrita de JSON, a tradução de erros em códigos de status e a montagem das dependências no `Main`.


### Integração Frontend com Backend

Como sugerido pela IA na feature anterior eu adotei o uso do CORS filter devido as portas diferentes sendo utilizadas no frontend e backend. Para o login, as interfaces `CandidatoRepositorio` e `EmpresaRepositorio` ganharam o método `buscarPorEmail` e como os DAOs implementam essas interfaces, implementei o método neles também.
O `LoginServlet` recebe o `POST` e usa o método `autenticar` dos services de candidato e empresa que buscam o usuário pelo email e confere a senha.
Adicionei a classe CompetenciaServlet que lista as competências cadastradas e associa as competências escolhidas a um candidato já cadastrado.

No frontend não temos mais o uso do localStorage como banco de dados e agora passou a consumir a API REST do Tomcat. A comunicação foi dividida em `http.ts` que monta a URL e envia o corpo em JSON e `api.ts` que possui uma função para cada endpoint como `cadastrarCandidato`, `fazerLogin` e `listarVagas`.

O login envia `POST` com o tipo do usuário, email e senha. Se os dados estiverem corretos o backend devolve o `tipo`, `id` e `nome` e o frontend guarda essas informações para saber quem está usando a aplicação.

Por segurança, as informações digitadas pelos usuários passam a ser inseridos com `textContent` e não mais com `ìnnerHTML`.

### Melhoria no front-end

Com a integração feita, separei um tempo para melhorar o front-end que necessitava de muitos ajustes.

Agora que está tudo conectado, a página inicial apresenta somente a tela de login. Podendo ser um candidato ou uma empresa. 

Quando o usuário loga como um candidato aparece somente a página de vagas para ele visualizar. Agora as vagas aparecem uma por uma por ordem decrescente de afinidade. Adicionei também dois botões de Curtir e passar para a próxima vaga.

Quando o usuário loga como uma empresa aparece a opção de criar vagas e dela ver as vagas que possui juntamente do gráfico de competências. Ao selecionar a vaga aparece os candidatos em ordem decrescente de afinidade em relação à vaga. E vagas que possuem 0% de afinidade não aparecem pois fogem do escopo da vaga que a empresa procura.
