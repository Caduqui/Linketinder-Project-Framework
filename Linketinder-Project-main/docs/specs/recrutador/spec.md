# Spec: recrutador

Branch: `feature/recrutador` · Escopo: banco + CLI + API REST + frontend · Situação: aprovada pelo autor em 2026-10-13 (o plano de implementação está em `plano.md`)

## 1. Contexto e objetivo

Hoje, no Linketinder, quem analisa candidatos e curte perfis é a **empresa**: existe um único login por empresa, e é com ele que alguém abre a vaga, vê a fila de candidatos e curte. Na prática esse trabalho é feito por pessoas da empresa — os recrutadores —, que hoje precisariam compartilhar a mesma senha e teriam acesso a tudo: cadastro da empresa, todas as vagas e todos os candidatos.

Esta feature cria o **recrutador**: um terceiro tipo de usuário, colaborador de uma empresa, com login próprio, que analisa candidatos e curte os que considerar compatíveis **apenas nas vagas que a empresa atribuiu a ele**. A empresa continua existindo como antes e segue podendo curtir candidatos diretamente; o que muda é que ela passa a poder distribuir esse trabalho.

Caso de uso de referência:

1. A empresa "Alfa" cadastra a recrutadora Ana, da área de TI, e atribui a ela as vagas "Dev Groovy" e "Dev Front";
2. Ana entra com o e-mail e a senha dela, vê as duas vagas (e nenhuma outra da Alfa) e abre a fila de candidatos de "Dev Groovy";
3. Ana curte o candidato 7. Como o candidato 7 já havia curtido "Dev Groovy", o match acontece na hora, é registrado como match da Alfa para aquela vaga e guarda que foi a Ana quem fechou;
4. O recrutador Bruno, que também administra "Dev Groovy", curte o candidato 7 depois. A curtida dele é gravada, mas não existe match novo: aquela vaga já deu match com o candidato 7.

## 2. Modelo de dados

### 2.1 Entidades novas

**`recrutadores`** — o colaborador da empresa.

| Coluna | Tipo | Restrições |
| --- | --- | --- |
| `id` | `serial` | PK |
| `nome` | `varchar(50)` | `NOT NULL` |
| `area` | `varchar(50)` | `NOT NULL` (um dos valores da lista da RN-03) |
| `descricao` | `varchar(500)` | `NOT NULL` (gravada vazia quando não informada, como em candidato e empresa) |
| `email` | `varchar(200)` | `UNIQUE NOT NULL` |
| `cpf` | `varchar(15)` | `UNIQUE NOT NULL` |
| `data_nascimento` | `date` | `NOT NULL` |
| `senha` | `varchar(50)` | `NOT NULL` |
| `id_empresa` | `int` | `NOT NULL`, FK → `empresas(id)` |

**`recrutador_vaga`** — quais vagas cada recrutador administra (N:N).

| Coluna | Tipo | Restrições |
| --- | --- | --- |
| `id` | `serial` | PK |
| `id_recrutador` | `int` | `NOT NULL`, FK → `recrutadores(id)` |
| `id_vaga` | `int` | `NOT NULL`, FK → `vagas(id)` |
|  |  | `UNIQUE (id_recrutador, id_vaga)` |

**`curtida_recrutador_candidato`** — a curtida do recrutador em um candidato, para uma vaga.

| Coluna | Tipo | Restrições |
| --- | --- | --- |
| `id` | `serial` | PK |
| `id_recrutador` | `int` | `NOT NULL` |
| `id_candidato` | `int` | `NOT NULL`, FK → `candidatos(id)` |
| `id_vaga` | `int` | `NOT NULL` |
|  |  | `UNIQUE (id_recrutador, id_candidato, id_vaga)` |
|  |  | FK composta `(id_recrutador, id_vaga)` → `recrutador_vaga(id_recrutador, id_vaga)` |

A FK composta é o mesmo recurso que já garante empresa–vaga hoje (`fk_empresa_vaga`): o banco recusa curtida em vaga que não está atribuída àquele recrutador, mesmo que o service falhe.

### 2.2 Alteração em tabela existente

**`matches`** ganha `id_recrutador int NULL`, FK → `recrutadores(id)`. Nulo significa "a curtida que fechou o match foi da própria empresa". O `UNIQUE (id_candidato, id_empresa, id_vaga)` **não muda**: continua existindo no máximo um match por trio, e é isso que faz só o primeiro recrutador fechar o match daquela vaga.

Nenhuma outra tabela muda. `candidatos`, `empresas`, `vagas`, `competencias`, `candidato_competencia`, `vaga_competencia`, `curtida_candidato_vaga` e `curtida_empresa_candidato` ficam como estão.

### 2.3 Relacionamentos

- Empresa **1:N** Recrutador (`recrutadores.id_empresa`): uma empresa tem vários recrutadores; um recrutador pertence a exatamente uma empresa.
- Recrutador **N:N** Vaga (`recrutador_vaga`): um recrutador administra várias vagas; uma vaga pode ser administrada por vários recrutadores.
- Recrutador **N:N** Candidato, por vaga (`curtida_recrutador_candidato`).
- Match **N:1** Recrutador (opcional): o match aponta para quem o fechou, ou para ninguém quando foi a empresa.

### 2.4 Classes do backend

- `model/Recrutador` — espelha a tabela, com `descrever()` e `descreverParaEmpresa()` **sem** senha, CPF e data de nascimento (RN-09).
- `repositorio/RecrutadorRepositorio` (interface) e `dao/RecrutadorDAO` (JDBC), incluindo `buscarPorEmail`, `buscarPorCpf`, `listarPorEmpresa`, `atribuirVaga`, `removerVaga` e `listarVagasDoRecrutador`.
- `service/RecrutadorService` — validação, regras de atribuição e de escopo.
- `CurtidaRepositorio`/`CurtidaDAO` ganham `recrutadorCurtirCandidato`; `MatchRepositorio`/`MatchDAO` ganham a criação de match a partir da curtida de recrutador.
- No frontend, o tipo `Recrutador` em `modelos.ts` **não tem** `cpf` nem `dataNascimento`: esses campos só existem no corpo do cadastro (`NovoRecrutador`).

## 3. Regras de negócio

### O recrutador

**RN-01 — Terceiro tipo de usuário.** O recrutador é colaborador de exatamente uma empresa (`id_empresa` obrigatório), tem login próprio e não é candidato nem empresa. Não existe recrutador sem empresa.

**RN-02 — Campos do cadastro.** `nome`, `area`, `email`, `cpf`, `dataNascimento`, `senha` e a empresa são obrigatórios; `descricao` é opcional e gravada vazia quando ausente.

**RN-03 — Área de uma lista fixa.** A área é um destes valores, definidos em constante no código: `TI`, `RH`, `Marketing`, `Segurança da Informação`, `Vendas`, `Financeiro`, `Outros`. Valor fora da lista é erro, e a mensagem cita o campo e lista as opções válidas. A lista fixa existe para a contagem por área (RN-15) fechar.

**RN-04 — Validação.** Antes de gravar, o service valida. Qualquer falha é erro citando **qual campo** está errado e por quê, **sem repetir o valor enviado**, e nada é gravado. Espaços das pontas são removidos dos campos, exceto da senha, que é validada e gravada exatamente como enviada; nela, espaços são usados apenas para detectar se o valor está vazio. Os demais valores são gravados sem reformatar CPF nem mudar a caixa do e-mail.

| Campo | Regra |
| --- | --- |
| `nome` | obrigatório, até 50 caracteres |
| `area` | obrigatória, um dos valores da RN-03 |
| `descricao` | opcional, até 500 caracteres |
| `email` | obrigatório, até 200 caracteres, um `@`, parte local não vazia, domínio com ponto, sem espaços |
| `cpf` | obrigatório, 11 dígitos, só os dígitos ou na pontuação padrão `000.000.000-00`; sem conferir dígito verificador |
| `dataNascimento` | obrigatória, formato `AAAA-MM-DD`, data existente e não futura |
| `senha` | obrigatória, de 6 a 50 caracteres, usada exatamente como enviada |

**RN-05 — Cadastro repetido.** Já existir recrutador com o mesmo e-mail ou o mesmo CPF é erro, com a mensagem genérica `Já existe um recrutador com esses dados`, que **não diz qual campo coincidiu**. Nada é gravado. Na API o código é `400`, e não `409`: o `ApiServlet` mapeia regra de negócio para `400`, e usar `409` exigiria um tipo novo de exceção na API; o microsserviço mantém o `409` na rota dele, diferença já registrada no `AGENTS.md`. A unicidade é só dentro de `recrutadores`: o mesmo e-mail ou CPF pode existir em `candidatos` ou `empresas`, porque o login recebe o `tipo` e sabe onde procurar.

**RN-06 — Quem cadastra e quem remove.** Somente a empresa. Ela cadastra, lista, atualiza e remove os recrutadores dela; o recrutador não se autocadastra, não se remove e não cadastra outros recrutadores. Uma empresa só enxerga e só mexe nos próprios recrutadores.

**RN-07 — Atualização de dados.** Alterar nome, área, descrição, e-mail, CPF, data de nascimento ou senha de um recrutador é feito **apenas pelo CLI** (opção "Atualizar" do menu). A API e o site não editam recrutador.

**RN-08 — Senha.** Nunca é exibida, devolvida ou registrada em log, em nenhuma camada. É gravada sem hash na coluna `senha`, como nos outros três cadastros do projeto: esta feature mantém o padrão atual e amplia a pendência conhecida "Senha em texto puro", que passa a citar quatro cadastros (decisão do autor, ver seção 8).

**RN-09 — CPF e data de nascimento nunca saem.** São validados e gravados no cadastro e, a partir daí, **nenhuma camada os exibe ou devolve**: não aparecem em `Recrutador.descrever*()`, em `Json.deRecrutador`, em nenhuma tela, em log nem em mensagem de erro. Junto com a senha, são os campos que nunca saem do banco.

**RN-10 — Login.** `POST /login` passa a aceitar `tipo: "recrutador"` e autentica por e-mail e senha do recrutador, comparando a senha em texto puro como nos outros perfis. Credenciais corretas respondem com `{tipo: "recrutador", id, nome, idEmpresa}` — nunca a senha, o CPF ou a data de nascimento. E-mail ou senha errados respondem `401` com a mensagem genérica já existente, que não diz qual dos dois falhou. O `idEmpresa` vai na resposta porque o site precisa dele para as telas do recrutador; a empresa é a do próprio recrutador, então não é dado da parte contrária.

### Atribuição de vagas

**RN-11 — A empresa atribui.** Só a empresa dona da vaga atribui vagas aos recrutadores dela, gravando em `recrutador_vaga`. Atribuir uma vaga de outra empresa, ou atribuir a um recrutador de outra empresa, é erro. Atribuição repetida não é erro: nada é gravado de novo e a operação informa que a vaga já estava atribuída.

**RN-12 — Quantidade livre.** Um recrutador pode ter zero, uma ou várias vagas atribuídas, e uma vaga pode estar atribuída a vários recrutadores. Recrutador sem vaga faz login normalmente e vê uma lista vazia.

**RN-13 — Desatribuição.** A empresa pode remover uma vaga de um recrutador. Isso apaga as curtidas daquele recrutador **naquela vaga** (as de outras vagas e de outros recrutadores não são tocadas). Matches já formados continuam valendo, porque são da empresa, e o `id_recrutador` deles passa a nulo quando apontava para esse vínculo removido.

**RN-14 — Escopo de atuação.** O recrutador só vê e só curte nas vagas atribuídas a ele. Vaga da empresa não atribuída a ele não aparece nas listas dele, e curtir usando essa vaga é erro — recusado pelo service e, em último caso, pela FK composta do banco.

**RN-15 — Para que serve a área.** A área **não** restringe atribuição: a empresa atribui a vaga que quiser a quem quiser. Ela existe para a empresa saber quantos recrutadores tem em cada área, e por isso a lista de recrutadores da empresa mostra a área de cada um e a contagem por área.

**RN-16 — O recrutador não publica vagas.** Ele não cria, não edita e não exclui vagas, nem as atribuídas a ele. Publicar e alterar vaga continua sendo da empresa.

### Curtida e match

**RN-17 — Curtida do recrutador.** Grava `(id_recrutador, id_candidato, id_vaga)` em `curtida_recrutador_candidato` e vale como interesse **da empresa naquela vaga**: é o que, somado à curtida do candidato, forma o match.

**RN-18 — Várias curtidas por vaga.** Enquanto não houver match, vários recrutadores da mesma vaga podem curtir o mesmo candidato, e cada curtida é gravada. A curtida é única por recrutador + candidato + vaga: repetir não gera erro (o `INSERT ... ON CONFLICT DO NOTHING` não insere nada) e a operação informa que já havia sido curtido. Não existe "descurtir".

**RN-19 — O match.** Continua acontecendo quando, **para a mesma vaga**, o candidato curtiu a vaga **e** o outro lado curtiu o candidato — e esse outro lado agora pode ser a empresa (como hoje) **ou** um recrutador com aquela vaga atribuída. O match segue sendo um por trio candidato–empresa–vaga, e a ordem das curtidas não importa.

**RN-20 — Só o primeiro recrutador fecha.** O match guarda em `id_recrutador` quem fechou a reciprocidade. Curtidas posteriores de outros recrutadores na mesma vaga são gravadas, **não criam match novo** e informam que não houve match. Quando quem fecha é a própria empresa, `id_recrutador` fica nulo.

**RN-21 — Quando o candidato curte depois.** Se um ou mais recrutadores já curtiram e o match só acontece quando o candidato curte a vaga, o match fica com o recrutador que **curtiu primeiro** (o menor `id` em `curtida_recrutador_candidato` para aquele candidato e vaga). Se não houver nenhuma curtida de recrutador e a curtida existente for da empresa, `id_recrutador` fica nulo.

**RN-22 — Vários matches na mesma empresa.** O mesmo candidato pode dar match em várias vagas da mesma empresa, uma por vaga, com recrutadores diferentes. O que não existe é mais de um match na mesma vaga.

**RN-23 — Curtir não revela nada.** O recrutador não fica sabendo quem curtiu a vaga antes do match: a fila de candidatos não indica se aquele candidato já curtiu. O candidato não fica sabendo que foi curtido nem por quem.

**RN-24 — Exclusão do recrutador.** Excluir um recrutador apaga antes os vínculos de vaga e as curtidas dele (feito no DAO, como já é feito com as dependências de candidato e vaga). Os matches que ele fechou continuam valendo, com `id_recrutador` nulo. Empresa com recrutadores **não pode ser excluída**, mesma proteção que já existe para empresa com vagas.

**RN-25 — Exclusão de candidato e de vaga.** Excluir um candidato ou uma vaga passa a apagar também as curtidas de recrutador relacionadas, e excluir uma vaga apaga os vínculos `recrutador_vaga` dela. Continua tudo no DAO.

### Onde a feature aparece

**RN-26 — API REST.** Rotas novas, sobre a base `http://localhost:8080/linketinder`:

| Método e rota | Função | Resposta |
| --- | --- | --- |
| `POST /recrutadores` | A empresa cadastra um recrutador (corpo com os campos da RN-02 e `idEmpresa`) | `201` com o recrutador criado, sem senha, CPF e data de nascimento |
| `GET /recrutadores?idEmpresa={id}` | Lista os recrutadores de uma empresa | `200` com `id`, `nome`, `area`, `descricao`, `email` e `idEmpresa` de cada um |
| `DELETE /recrutadores/{id}` | A empresa remove um recrutador | `204` sem corpo |
| `GET /recrutadores/{id}/vagas` | Vagas atribuídas ao recrutador | `200` com a lista de vagas |
| `POST /recrutadores/{id}/vagas` | A empresa atribui uma vaga (`{"idVaga": n}`) | `201` com a lista atualizada de vagas do recrutador |
| `DELETE /recrutadores/{id}/vagas/{idVaga}` | A empresa desatribui a vaga | `204` sem corpo |
| `POST /curtidas` com `tipo: "recrutador"` | Curtida do recrutador (`idRecrutador`, `idCandidato`, `idVaga`) | `201` com `{"match": true\|false}` |
| `POST /login` com `tipo: "recrutador"` | Login do recrutador | `200` com `{tipo, id, nome, idEmpresa}` |

Erros seguem o que a API já faz: regra de negócio e campo inválido em `400` com `{"erro": "..."}`, recurso inexistente em `404`, método não aceito em `405` com `Allow`, falha de banco em `500`. O `DELETE` é a primeira exceção à convenção "só `GET` e `POST`" da API, autorizada pelo autor (seção 8), e precisa ser registrada no `AGENTS.md` e no `README.md`.

**RN-27 — CLI.** O menu principal ganha "Recrutadores", com "Listar", "Cadastrar", "Atualizar", "Excluir", "Atribuir vaga a um recrutador", "Remover vaga de um recrutador" e "Listar vagas do recrutador". O menu de match ganha "Recrutador curtir um candidato", que pede o recrutador, a vaga (entre as atribuídas a ele) e o candidato, e informa se houve match. A listagem de recrutadores mostra `id`, nome, área, descrição, e-mail e a empresa — nunca senha, CPF ou data de nascimento.

**RN-28 — Frontend.** A empresa logada ganha a tela "Recrutadores": cadastrar, listar (com área e contagem por área), atribuir e remover vagas e excluir recrutador. O recrutador entra pela tela inicial, como terceiro perfil, vê a lista das vagas atribuídas a ele e abre a fila de candidatos de cada vaga, com os botões curtir e passar. As páginas de lista de vagas e de fila de candidatos atendem **empresa e recrutador**, trocando apenas a origem dos dados e o corpo da curtida; a fila segue a regra atual (afinidade decrescente, empate por sorteio, candidatos com 0% fora). O gráfico de competências continua exclusivo da empresa.

### Casos de borda

| Caso | Comportamento esperado |
| --- | --- |
| Recrutador sem nenhuma vaga atribuída | Faz login e vê a lista vazia, com aviso de que a empresa ainda não atribuiu vagas a ele. Nenhuma fila é aberta |
| Vaga atribuída sem candidato com afinidade maior que zero | Fila vazia, com a mensagem já usada hoje na área da empresa |
| Dois recrutadores da mesma vaga curtem o mesmo candidato, antes de o candidato curtir | As duas curtidas são gravadas e nenhum match é criado |
| O candidato curte a vaga depois dessas duas curtidas | Um único match é criado, com o `id_recrutador` do que curtiu primeiro (RN-21) |
| Match já existe e outro recrutador curte o mesmo candidato na mesma vaga | A curtida é gravada e a resposta informa que não houve match |
| A empresa curte um candidato que um recrutador dela já havia curtido na mesma vaga | A curtida da empresa é gravada normalmente; se o match ainda não existia e o candidato já havia curtido, o match é criado e fica com o recrutador que curtiu primeiro |
| Recrutador tenta curtir usando vaga não atribuída a ele | Erro de regra de negócio (`400` na API, mensagem no CLI); nada é gravado, e o banco recusaria pela FK composta |
| Recrutador tenta curtir usando vaga de outra empresa | Mesmo caso acima: a vaga não está atribuída a ele |
| Empresa tenta atribuir vaga de outra empresa | Erro de regra de negócio; nada é gravado |
| Empresa tenta atribuir vaga a recrutador de outra empresa | Erro de regra de negócio; nada é gravado |
| Empresa desatribui vaga em que o recrutador já havia curtido | O vínculo e as curtidas daquele recrutador naquela vaga são apagados; matches ficam, com `id_recrutador` nulo |
| Exclusão de empresa que tem recrutadores | Recusada, com a mensagem de regra de negócio (como já acontece com vagas) |
| Exclusão de candidato ou de vaga com curtidas de recrutador | A exclusão acontece e as curtidas de recrutador são apagadas antes |
| E-mail ou CPF já cadastrado em outro recrutador | Mensagem genérica da RN-05, sem dizer qual campo coincidiu |
| E-mail ou CPF que já existe em `candidatos` | Cadastro aceito: a unicidade é só entre recrutadores (RN-05) |
| Área fora da lista, data futura, senha de 5 caracteres, e-mail sem domínio | Erro citando o campo, sem repetir o valor; nada é gravado |
| `idEmpresa`, `idRecrutador` ou `idVaga` inexistente na API | `404` com a mensagem do recurso não encontrado |
| `id` não numérico na URL | `400`, como já responde o resto da API |
| Recrutador abre a URL de uma vaga que não é dele | A tela avisa que a vaga não existe ou não está atribuída a ele, sem mostrar dado algum da vaga |

## 4. Princípio do Anonimato

O recrutador age em nome da empresa, então **herda a visão da empresa**, sem nada a mais. E, por ser interno à empresa, ele **nunca aparece para o candidato** — nem depois do match.

### 4.1 O que cada perfil enxerga

| Quem vê | Antes do match | Depois do match |
| --- | --- | --- |
| **Candidato → vaga/empresa** | Só os dados da vaga: ID, nome, descrição, cidade/estado, competências e índice de afinidade. Nada da empresa, nem o nome | Perfil completo da empresa dona da vaga, exceto a senha. **Nada do recrutador, nunca** |
| **Candidato → recrutador** | Nada. O candidato não sabe que recrutadores existem, quantos são, nem qual deles o analisou ou o curtiu | Nada. A revelação pós-match é da empresa, não da pessoa que operou a conta |
| **Empresa → candidato** | Somente ID, formação, descrição e competências (inalterado) | Perfil completo do candidato, exceto a senha (inalterado) |
| **Empresa → seus recrutadores** | A qualquer momento: `id`, nome, área, descrição, e-mail e as vagas atribuídas. **Nunca** CPF, data de nascimento ou senha | Igual: o match não muda o que a empresa vê dos próprios recrutadores |
| **Empresa → recrutadores de outra empresa** | Nada, em nenhum momento | Nada |
| **Recrutador → candidato** | O mesmo que a empresa: ID, formação, descrição, competências e o índice de afinidade com a vaga. Nada de nome, sobrenome, e-mail, CPF, data de nascimento, país ou CEP | O mesmo que a empresa. Esta feature **não cria** nenhuma tela ou rota pós-match, então na prática o recrutador não vê perfil revelado em lugar nenhum. A tela de histórico de matches é feature futura e terá a própria decisão de anonimato (seção 8.4) |
| **Recrutador → própria empresa** | Nome da empresa dele (própria parte se identificando, como o `nome` no login) | Igual |
| **Recrutador → vaga atribuída** | Todos os dados da vaga, inclusive a empresa: a vaga é da empresa dele | Igual |
| **Recrutador → outros recrutadores** | Nada: nenhuma rota, tela ou menu do recrutador lista colegas | Nada |

### 4.2 Saídas que a feature cria ou altera

| Saída da feature | Quem vê | Momento | Campos exibidos |
| --- | --- | --- | --- |
| `POST /recrutadores` (resposta) | A empresa que cadastrou | — | `id`, `nome`, `area`, `descricao`, `email`, `idEmpresa`. Sem senha, CPF e data de nascimento |
| `GET /recrutadores?idEmpresa={id}` | A empresa | — | Os mesmos campos, por recrutador |
| `GET /recrutadores/{id}/vagas` | A empresa e o próprio recrutador | — | Dados da vaga, como `Json.deVaga` já devolve |
| `POST /login` com `tipo: "recrutador"` | O próprio recrutador | — | `tipo`, `id`, `nome`, `idEmpresa`. Sem senha, CPF e data de nascimento |
| `POST /curtidas` com `tipo: "recrutador"` | O recrutador que curtiu | Momento da curtida | Só `{"match": true\|false}`. Nenhum dado do candidato, nem quando há match |
| `DELETE /recrutadores/{id}` e `DELETE .../vagas/{idVaga}` | A empresa | — | Sem corpo |
| Fila de candidatos do recrutador (frontend) | O recrutador | Antes do match | `Candidato {id}`, formação, descrição, competências e afinidade — exatamente o card que a empresa já vê |
| Lista de vagas do recrutador (frontend) | O recrutador | — | Nome, cidade/estado e competências das vagas atribuídas |
| Tela "Recrutadores" (frontend) | A empresa | — | Nome, área, descrição, e-mail, vagas atribuídas e contagem por área |
| Listagem de recrutadores no CLI | Quem opera o terminal | — | `id`, nome, área, descrição, e-mail e empresa. Sem senha, CPF e data de nascimento |
| "Recrutador curtir um candidato" no CLI | Quem opera o terminal | Antes do match | Candidatos pela representação anônima (`descreverAnonimo()`) e vagas atribuídas ao recrutador |
| Mensagens de erro de validação | Quem enviou o cadastro | — | Nome do campo e o motivo. Nunca o valor enviado, e nunca CPF, e-mail ou senha |
| Log da API | Ninguém além do operador do servidor | — | Como hoje: só a falha de banco, sem corpo e sem dado pessoal |

### 4.3 Pendências conhecidas que a feature toca

| Pendência do `AGENTS.md` | O que esta feature faz |
| --- | --- |
| **Senha em texto puro** | **Amplia, com autorização do autor:** o recrutador é o quarto cadastro a gravar senha sem hash, e o login dele compara em texto puro. O `AGENTS.md` passa a registrar quatro caminhos. Aplicar hash fica como feature própria |
| **Login sem autenticação das demais rotas** | **Amplia em integridade, sem ampliar exposição:** as rotas novas aceitam qualquer `idEmpresa` e `idRecrutador`, como as rotas atuais. Quem souber o `id` de uma empresa pode cadastrar e remover recrutadores dela e ver nome, área, descrição e e-mail dos recrutadores dela. Nenhum dado de candidato ou de empresa a mais é exposto. O autor avaliou implementar autenticação aqui e decidiu **deixá-la para uma feature futura** (seção 8.4); esta feature apenas registra a nova exposição de integridade no `AGENTS.md`, sem corrigi-la |
| **`GET /candidatos` devolve o cadastro completo** | **Não altera e não amplia os campos:** a fila do recrutador usa a mesma rota que a tela da empresa já usa, então a pendência passa a servir um perfil a mais, com os mesmos campos. Corrigir é feature própria |
| **`Match.descrever()` pós-match** | **Não altera.** O `id_recrutador` é gravado, mas nenhuma saída pós-match muda nesta feature |
| **`POST /competencias` devolve o perfil completo** | Sem relação com esta feature. Não é tocada |

## 5. Critérios de aceite

Todos os critérios de `CA-01` a `CA-16` são verificados por spec Spock (`./gradlew test`, sem banco, com `Mock` das interfaces de `repositorio`). `CA-17` a `CA-20` dependem de conferência manual, porque envolvem DAO com PostgreSQL real, Tomcat e navegador — o plano dirá em qual tarefa.

**CA-01 — Cadastro válido**
- Dado uma empresa cadastrada e os dados completos de um recrutador
- Quando a empresa cadastra o recrutador
- Então ele é gravado com o vínculo da empresa e a resposta traz `id`, nome, área, descrição, e-mail e `idEmpresa`, sem senha, CPF e data de nascimento

**CA-02 — Campo obrigatório ausente**
- Dado um cadastro sem `nome` (e, em cada caso, sem `area`, `email`, `cpf`, `dataNascimento` ou `senha`)
- Quando a empresa tenta cadastrar
- Então a operação falha citando aquele campo, sem repetir o valor enviado, e nada é gravado

**CA-03 — Formatos inválidos**
- Dado um cadastro com e-mail sem domínio, CPF com 10 dígitos, data `2030-01-01`, data `2026-02-30` ou senha de 5 caracteres
- Quando a empresa tenta cadastrar
- Então cada caso falha citando o campo e o motivo, e nada é gravado

**CA-04 — Área fora da lista**
- Dado um cadastro com `area` igual a `Jurídico`
- Quando a empresa tenta cadastrar
- Então a operação falha citando o campo `area` e listando as áreas válidas

**CA-05 — Cadastro repetido**
- Dado um recrutador já cadastrado com um e-mail (e, em outro caso, um CPF)
- Quando a empresa tenta cadastrar outro com o mesmo e-mail ou o mesmo CPF
- Então a mensagem é a genérica da RN-05, não diz qual campo coincidiu, e nada é gravado

**CA-06 — Login do recrutador**
- Dado um recrutador cadastrado
- Quando ele envia `tipo: "recrutador"`, o e-mail e a senha corretos
- Então recebe `{tipo, id, nome, idEmpresa}`, sem senha, CPF e data de nascimento; e com senha errada recebe `401` com a mensagem genérica, que não diz se o errado foi o e-mail ou a senha

**CA-07 — Atribuição de vaga**
- Dado um recrutador da empresa A e uma vaga da empresa A
- Quando a empresa A atribui a vaga ao recrutador
- Então a vaga passa a aparecer nas vagas dele; atribuir a mesma vaga outra vez não grava nada e informa que já estava atribuída

**CA-08 — Atribuição fora da empresa**
- Dado um recrutador da empresa A e uma vaga da empresa B
- Quando alguém tenta atribuir essa vaga ao recrutador
- Então a operação falha com erro de regra de negócio e nada é gravado

**CA-09 — Escopo de atuação**
- Dado uma empresa com três vagas, das quais duas estão atribuídas a um recrutador
- Quando o recrutador lista as vagas dele
- Então vê exatamente as duas atribuídas

**CA-10 — Curtida em vaga não atribuída**
- Dado um recrutador sem a vaga V atribuída
- Quando ele tenta curtir um candidato usando a vaga V
- Então a operação falha com erro de regra de negócio e nenhuma curtida é gravada

**CA-11 — Match pela curtida do recrutador**
- Dado um candidato que já curtiu a vaga V, e um recrutador com V atribuída
- Quando o recrutador curte esse candidato na vaga V
- Então o match é criado para o trio candidato–empresa–vaga, com `id_recrutador` do recrutador, e a operação informa que houve match

**CA-12 — Só o primeiro recrutador fecha**
- Dado o match da vaga V com o candidato C já criado pelo recrutador R1
- Quando o recrutador R2, que também tem V atribuída, curte C
- Então a curtida de R2 é gravada, nenhum match novo é criado e a operação informa que não houve match

**CA-13 — Candidato curte depois de dois recrutadores**
- Dado que R1 e depois R2 curtiram o candidato C na vaga V, e que C ainda não havia curtido V
- Quando C curte a vaga V
- Então um único match é criado, com `id_recrutador` de R1 (o que curtiu primeiro)

**CA-14 — Matches em vagas diferentes da mesma empresa**
- Dado o candidato C com match na vaga V1 pelo recrutador R1
- Quando C curte V2, da mesma empresa, e R2 (com V2 atribuída) curte C
- Então existe um segundo match, do trio C–empresa–V2, com `id_recrutador` de R2

**CA-15 — Desatribuição e exclusão**
- Dado um recrutador com a vaga V atribuída e curtidas gravadas em V
- Quando a empresa desatribui V, e em outro caso exclui o recrutador
- Então as curtidas dele em V são apagadas, os matches continuam existindo com `id_recrutador` nulo, e a empresa que ainda tem recrutadores não pode ser excluída

**CA-16 — Anonimato preservado**
- Dado um candidato sem match e um recrutador de uma empresa qualquer
- Quando o recrutador abre a fila de candidatos de uma vaga atribuída, e quando o candidato abre a fila de vagas
- Então o recrutador vê apenas `Candidato {id}`, formação, descrição, competências e afinidade, e o candidato não vê nada da empresa nem do recrutador; nenhuma saída contém senha, CPF ou data de nascimento, e a resposta da curtida é só `{"match": ...}`

**CA-17 — Banco (conferência manual)**
- Dado o script de criação aplicado em um PostgreSQL vazio
- Quando as tabelas novas são usadas
- Então `UNIQUE (id_recrutador, id_candidato, id_vaga)` impede curtida duplicada, a FK composta recusa curtida em vaga não atribuída, e `matches.id_recrutador` aceita nulo

**CA-18 — API no Tomcat (conferência manual)**
- Dado o WAR implantado e o banco no ar
- Quando as rotas da RN-26 são chamadas com `curl`
- Então os códigos e os corpos são os da tabela, `405` traz `Allow`, e nenhuma resposta contém senha, CPF ou data de nascimento

**CA-19 — CLI (conferência manual)**
- Dado o CLI rodando contra o banco
- Quando o menu "Recrutadores" e a opção "Recrutador curtir um candidato" são usados
- Então o CRUD, a atribuição e a curtida funcionam, e a listagem não imprime senha, CPF nem data de nascimento

**CA-20 — Site (conferência manual)**
- Dado a API no ar e `npm run dev` em `frontend/`
- Quando a empresa cadastra um recrutador, atribui vagas a ele, e o recrutador entra e usa a fila
- Então os fluxos funcionam, o recrutador só vê as vagas atribuídas, e `npx tsc --noEmit` passa

## 6. Impacto no que já existe

| Funcionalidade atual | Impacto |
| --- | --- |
| Schema (`database/Linketinder.sql`, DER) | **Muda:** três objetos novos (`recrutadores`, `recrutador_vaga`, `curtida_recrutador_candidato`), a coluna `matches.id_recrutador` e as FKs. Precisa de carga inicial fictícia e de atualização do DER (`assets/der.png`), que o autor faz depois (seção 8.2) |
| `matches` e `model/Match` | **Muda:** coluna e atributo novos (recrutador que fechou, podendo ser nulo). `Match.descrever()` não muda nesta feature |
| `MatchDAO.criarMatchSePossivel` | **Muda:** ganha o caminho que considera a curtida de recrutador e grava `id_recrutador`, mantendo o comportamento atual da curtida da empresa |
| `CurtidaService` / `CurtidaDAO` | **Muda:** método novo para a curtida do recrutador. Os dois fluxos existentes continuam iguais |
| Curtida da empresa (CLI e `POST /curtidas` com `tipo: "empresa"`) | **Sem mudança de comportamento,** exceto que o match criado por ela passa a gravar `id_recrutador` nulo (ou o do recrutador que curtiu primeiro, RN-21) |
| Curtida do candidato (`POST /curtidas` com `tipo: "candidato"`) | **Muda por dentro:** a criação do match passa a considerar também as curtidas de recrutador. A resposta continua `{"match": ...}` |
| `POST /login` | **Muda:** aceita um terceiro `tipo` e devolve `idEmpresa` para o recrutador. Candidato e empresa continuam com a resposta atual |
| `EmpresaService.excluir` / `EmpresaDAO` | **Muda:** passa a recusar empresa com recrutadores, além de empresa com vagas |
| `CandidatoDAO.deletar` e `VagaDAO.deletar` | **Muda:** apagam também as curtidas de recrutador, e `VagaDAO` apaga os vínculos `recrutador_vaga` |
| `api/Dependencias` e `Main` (CLI) | **Mudam:** passam a montar o repositório, o DAO, o service e o controller do recrutador |
| `MenuController` / `MenuView` | **Mudam:** menu novo com mais opções que o CRUD genérico, e a opção de curtida do recrutador |
| `api/Json` | **Muda:** `paraRecrutador` e `deRecrutador`, que nunca incluem senha, CPF e data de nascimento |
| Convenção "a API só tem `GET` e `POST`" | **Muda:** ganha `DELETE` nas rotas de recrutador, com autorização do autor. `AGENTS.md`, `README.md` e `CorsFilter` (`Access-Control-Allow-Methods`) precisam acompanhar |
| `CorsFilter` | **Muda:** passa a anunciar `DELETE` entre os métodos permitidos, senão o navegador bloqueia a remoção |
| Frontend: `index.html`, `navegacao.ts`, `sessao.ts`, `modelos.ts`, `api.ts` | **Mudam:** terceiro perfil na entrada, proteção de página para recrutador, tipos e funções novas de API |
| Frontend: lista de vagas e fila de candidatos | **Mudam:** passam a atender empresa e recrutador, trocando a origem dos dados e o corpo da curtida. O comportamento para a empresa deve continuar idêntico |
| Frontend: gráfico de competências | **Sem impacto:** continua só na tela da empresa |
| Cadastro e telas do candidato | **Sem impacto** |
| Microsserviço de cadastro | **Sem impacto:** não recebe recrutador (seção 7) |
| Competências, vagas e afinidade | **Sem impacto** nas regras; a fila do recrutador reaproveita o cálculo atual |

## 7. Fora do escopo

Esta feature **não** vai:

- aplicar hash em senha, nem só na do recrutador — fica como feature própria, e a pendência conhecida é mantida;
- criar autenticação de verdade (token, cookie ou sessão no servidor) nem exigir login nas rotas: o autor avaliou e decidiu postergar, então as rotas novas seguem o padrão atual, sem conferir quem chama (seção 8.4);
- corrigir as pendências conhecidas do `AGENTS.md`: `GET /candidatos` com cadastro completo, `POST /competencias` devolvendo o perfil, `Vaga.descrever()` no CLI, `Match.descrever()` pós-match e o `;` faltando no `database/Linketinder.sql`;
- cadastrar recrutador pelo **microsserviço de cadastro**: ele é só de candidato e empresa, não tem login e não é usado pelo site;
- permitir autocadastro do recrutador, convite ou aprovação pela empresa;
- permitir que o recrutador crie, edite ou exclua vagas, nem que edite o próprio perfil;
- permitir que o recrutador veja, cadastre ou remova outros recrutadores;
- editar dados do recrutador pela API ou pelo site (só pelo CLI, RN-07);
- colocar área na vaga, nem filtrar ou restringir atribuição por área;
- criar tela ou rota pós-match (para nenhum perfil), nem alterar o que o CLI mostra dos matches: a tela de histórico de matches do site é feature futura (seção 8.4);
- dar ao recrutador o gráfico de competências;
- avisar o candidato de que foi curtido, ou por quem;
- criar relatório, histórico ou auditoria de quem curtiu o quê, além do `id_recrutador` gravado no match.

## 8. Decisões tomadas

### 8.1 As perguntas do desafio

| Pergunta | Decisão | Discutida com o Tutor? |
| --- | --- | --- |
| **Vínculo:** como o recrutador é associado à empresa? Quem pode cadastrá-lo ou removê-lo? | `id_empresa` obrigatório na tabela: todo recrutador nasce com empresa. Só a empresa cadastra, lista, atualiza e remove os recrutadores dela; não há autocadastro (RN-01, RN-06) | _(a preencher pelo autor)_ |
| **Curtidas e match:** a curtida do recrutador conta como curtida da empresa? Como fica o match quando dois recrutadores da mesma empresa analisam o mesmo candidato? | A curtida é do recrutador para um candidato **numa vaga** e vale como interesse da empresa naquela vaga. Vários recrutadores da mesma vaga podem curtir o mesmo candidato, mas o match é um só por trio candidato–empresa–vaga e fica com o primeiro que fechou a reciprocidade (RN-17 a RN-22) | _(a preencher pelo autor)_ |
| **Escopo de atuação:** o recrutador vê todas as vagas da empresa ou apenas as da sua área? Ele pode criar ou editar vagas? | Nem uma nem outra: a empresa **atribui explicitamente** quais vagas cada recrutador administra, e ele só vê e só curte nessas. Não cria, não edita e não exclui vagas (RN-11 a RN-16) | _(a preencher pelo autor)_ |
| **Anonimato:** o que o recrutador enxerga de um candidato antes do match? Depois do match o candidato vê dados do recrutador? Quais dados pessoais do recrutador nunca devem ser exibidos? | O recrutador vê exatamente o que a empresa vê (ID, formação, descrição, competências e afinidade). O candidato **nunca** vê nada do recrutador, nem depois do match. CPF, data de nascimento e senha nunca são exibidos nem devolvidos por nenhuma camada (seção 4, RN-09) | _(a preencher pelo autor)_ |
| **Validações:** quais regras valem para e-mail, CPF, data de nascimento e senha? | O rigor do microsserviço, aplicado no service do projeto principal: e-mail com formato e único, CPF de 11 dígitos (só dígitos ou pontuado, sem dígito verificador) e único, data `AAAA-MM-DD` existente e não futura, senha de 6 a 50. A área vem de uma lista fixa (RN-03, RN-04) | _(a preencher pelo autor)_ |

### 8.2 Outras decisões confirmadas pelo autor

- **Camadas:** a feature entra em todas — banco, CLI, API REST e frontend.
- **Banco:** tabela própria para a curtida do recrutador, `recrutador_vaga` para a atribuição e `matches.id_recrutador` anulável para registrar quem fechou.
- **Área:** informativa; existe para a empresa saber quantos recrutadores tem em cada área, e não restringe atribuição.
- **Desatribuição:** remover a vaga do recrutador apaga as curtidas dele naquela vaga, o que permite a FK composta no banco.
- **Exclusão:** excluir recrutador apaga vínculos e curtidas dele; os matches ficam, com `id_recrutador` nulo; empresa com recrutadores não pode ser excluída.
- **Unicidade:** e-mail e CPF únicos apenas dentro de `recrutadores`.
- **Login:** `POST /login` aceita `tipo: "recrutador"` e devolve `{tipo, id, nome, idEmpresa}`.
- **Senha:** segue em texto puro, como os outros cadastros, ampliando a pendência conhecida de forma documentada.
- **API:** as rotas de recrutador podem usar `DELETE`, abrindo a primeira exceção à convenção "só `GET` e `POST`".
- **Frontend:** as páginas de lista de vagas e de fila de candidatos são reaproveitadas pelos dois perfis, sem gráfico para o recrutador.
- **Git:** branch `feature/recrutador`; os commits são feitos pelo autor, uma tarefa por commit.

Confirmadas na revisão da spec, em 2026-10-13:

- **Cadastro repetido responde `400`** com a mensagem genérica, não `409` (RN-05).
- **Depois do match o recrutador vê do candidato o mesmo que a empresa.** Como esta feature não cria saída pós-match, a decisão só terá efeito quando existir a tela de histórico de matches, que é feature futura.
- **O DER (`assets/der.png`) é atualizado pelo autor, depois.** A feature altera `database/Linketinder.sql` e aponta no plano o que mudou no diagrama.
- **A carga inicial do script ganha recrutadores fictícios** (e-mails em `example.com`, CPFs com dígito verificador inválido), seguindo a regra de dados fictícios do `AGENTS.md`.
- **Autenticação fica para o futuro.** O autor avaliou implementá-la nesta feature e decidiu postergar: a falta de autenticação nas rotas novas é registrada como pendência, sem correção aqui.
- **O `DELETE` exige acompanhar o `CorsFilter`** (`Access-Control-Allow-Methods`) e tirar do `AGENTS.md` e do `README.md` a frase de que a API só tem `GET` e `POST`.
- **A tabela do Princípio do Anonimato do `AGENTS.md` ganha o recrutador** exatamente como está na seção 4 — o anonimato é restrição central da aplicação, e o texto é submetido ao autor antes de alterar o arquivo.

### 8.3 Premissas assumidas na escrita, a confirmar

As premissas confirmadas na revisão de 2026-10-13 passaram para a seção 8.2; os códigos das que sobraram foram mantidos para não invalidar as referências já feitas.

- **P-02 — As rotas `DELETE` respondem `204` sem corpo,** e `POST /recrutadores/{id}/vagas` responde `201` com a lista atualizada de vagas do recrutador.
- **P-04 — A contagem de recrutadores por área é calculada no frontend** a partir de `GET /recrutadores?idEmpresa={id}`, sem rota nem consulta própria.
- **P-08 — O CLI continua sem login:** o menu "Recrutadores" pede o `id` ou escolhe numa lista, como os menus atuais fazem com empresa e vaga.

### 8.4 Pendências para features futuras

Ficam registradas para não se perderem; nenhuma é implementada nesta feature.

- **Autenticação e autorização** (decidida como feature própria, sem data): sessão ou token no servidor, `POST /logout`, proteção das rotas e uso do id da sessão em vez do id do corpo. Três decisões ficaram abertas e serão tomadas na spec dela: o mecanismo (`HttpSession` com cookie, token opaco em memória ou JWT), o alcance (só as rotas novas ou todas) e se a rota confere apenas o login ou também se o usuário é dono do recurso. Enquanto não existir, valem as pendências de integridade registradas no `AGENTS.md`.
- **Tela de histórico de matches no site** (prevista pelo autor): será a primeira saída pós-match do frontend, então precisa decidir em spec própria o que cada parte vê depois do match — inclusive se o recrutador aparece para o candidato, que hoje é "nunca" (seção 4).
- **Hash de senha:** exige alterar a coluna `senha` e os quatro cadastros, mais o `POST /login`.
- **Exposição nas rotas de leitura da API:** `GET /candidatos` devolvendo o cadastro completo, `GET /vagas` devolvendo `empresa: {id, nome}` e `POST /competencias` devolvendo o perfil do candidato. Com autenticação no lugar, dá para devolver só os campos anônimos; sem ela, não há como saber quem pergunta.
- **`Match.descrever()` pós-match:** mostra só nome e e-mail das partes, quando a regra permite o perfil completo sem a senha. Com `matches.id_recrutador` gravado, essa feature futura também decide se a empresa vê qual recrutador fechou cada match.
