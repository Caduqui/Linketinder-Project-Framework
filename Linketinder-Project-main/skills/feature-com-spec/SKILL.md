---
name: feature-com-spec
description: Implementa uma feature do Linketinder pelo fluxo spec → plano → implementação (Spec-Driven Development), com AGENTS.md, spec.md e plano.md em uma pasta por feature dentro de docs/specs/, tarefas pequenas, um commit por tarefa, CHANGELOG.md atualizado a cada tarefa e validação do Princípio do Anonimato. Use sempre que o usuário pedir para implementar, adicionar ou desenvolver uma feature, funcionalidade ou requisito novo neste projeto, e também quando falar em SDD, especificação, spec, critérios de aceite, plano de implementação ou "implementação incremental", mesmo que não cite esta skill pelo nome. Não use para correções pontuais de bug, ajustes de uma linha ou perguntas sobre o código.
---

# Feature com spec

## Objetivo

Implementar uma feature do Linketinder seguindo o fluxo **spec → plano → implementação**: primeiro se decide e se escreve *o que* a feature faz, depois *como* ela será construída em passos pequenos, e só então se escreve código, um passo por vez.

A ordem existe por um motivo. Uma dúvida resolvida na spec custa uma pergunta; a mesma dúvida descoberta no meio do código custa retrabalho. E tarefas pequenas, validadas uma a uma, deixam o usuário no controle: ele sabe o que mudou, consegue revisar e pode parar ou redirecionar a qualquer momento.

Três coisas são próprias deste projeto e valem em todas as etapas:

- **O `AGENTS.md` da raiz é a fonte de verdade** sobre domínio, regras de negócio, camadas, comandos e restrições. Esta skill não repete esse conteúdo; ela diz quando consultá-lo. Se a skill e o `AGENTS.md` divergirem, vale o `AGENTS.md` e a divergência deve ser relatada.
- **O Princípio do Anonimato é restrição obrigatória.** Ele entra na spec, na validação de cada tarefa e no checklist final.
- **Toda tarefa atualiza o `CHANGELOG.md`**, no mesmo commit da tarefa.

## Entradas

1. **A descrição da feature:** o texto do usuário, com requisitos obrigatórios e exemplos de uso, se houver.
2. **O projeto onde ela será implementada:** este repositório.

Se faltar a descrição, peça antes de começar. A escolha da feature é a etapa 1, que acontece antes desta skill; por isso o passo a passo começa na etapa 2.

## Onde ficam os documentos

Cada feature tem a própria pasta:

```
docs/specs/<nome-da-feature>/
  spec.md     especificação (etapa 3)
  plano.md    plano de implementação (etapa 4)
  revisao.md  apontamentos e correções da revisão (skill `revisao-com-spec`)
```

`<nome-da-feature>` é em português, minúsculo, sem acento e separado por hífen (por exemplo, `curtida-pela-api`). Proponha o nome ao usuário e confirme antes de criar a pasta. Nunca reaproveite a pasta de outra feature; se já existir uma com o mesmo nome, pergunte.

## Passo a passo

Cada etapa termina com uma revisão do usuário. Não avance para a etapa seguinte sem a aprovação da anterior.

### Etapa 2: Contexto para o agente (`AGENTS.md`)

**O que gerar:** o `AGENTS.md` já existe na raiz. Leia-o inteiro e atualize só o que estiver desatualizado em relação ao código; se nada mudou, diga isso e siga.

Ele deve continuar contendo visão geral, stack e estrutura, domínio, regras de negócio, Princípio do Anonimato (com as pendências conhecidas), banco de dados, como executar e testar, convenções e restrições.

Como fazer bem:

- Leia o código, não só o README. O README pode estar errado ou desatualizado.
- Rode os comandos antes de confirmá-los (`./gradlew test`, `npx tsc --noEmit` em `frontend/`). Um comando que não foi executado é um palpite; se não der para rodar, diga isso no arquivo.
- Descreva o código como ele está hoje. O que ainda é só intenção vai para a spec, não para o `AGENTS.md`.
- Registre problemas encontrados como pendências ou pontos de atenção, sem corrigi-los.
- Não altere a tabela do Princípio do Anonimato por conta própria: ela é decisão do autor.

**O que revisar:** os comandos funcionam como descrito; as convenções batem com o código real; as restrições cobrem o que o usuário não quer que seja mexido.

### Etapa 3: Especificação (`docs/specs/<nome-da-feature>/spec.md`)

**O que gerar:** a especificação da feature, seguindo o modelo do fim deste documento. Nenhum código é escrito nesta etapa.

Como fazer bem:

1. **Pergunte antes de escrever.** Leia a descrição da feature e o código que ela toca, e levante as decisões que mudam o comportamento: dados iniciais, valores padrão, casos de erro, interação com o que já existe, e em quais partes a feature entra (CLI, API REST, frontend, banco). Para cada uma, ofereça opções com a consequência de cada escolha e indique a que você recomenda. Não tire conclusões pelo usuário.
2. **Escreva a spec** com regras numeradas (`RN-01`) e critérios de aceite numerados (`CA-01`), para que o plano e os relatos possam citá-los.
3. **Mapeie o anonimato.** Para cada tela, saída de terminal, resposta de API, consulta ou mensagem que a feature cria ou altera, escreva na seção de anonimato da spec: quem vê (candidato, empresa ou ninguém), em que momento (antes ou depois do match) e exatamente quais campos aparecem. Confira cada linha contra a tabela do `AGENTS.md`. Se a feature parecer exigir um dado fora da tabela, ou tocar em uma das pendências conhecidas, **pare e pergunte**: não amplie a pendência nem a corrija por conta própria.
4. **Separe o que foi decidido do que foi assumido.** Toda premissa que você adotou sem perguntar vai para uma lista "a confirmar". Quando o usuário confirmar, mova para as decisões.
5. **Procure o impacto no que já existe.** Leia o código das funcionalidades vizinhas e escreva o que muda nelas, inclusive efeitos colaterais que o usuário talvez não espere. Mudança de schema é sempre impacto: envolve `database/Linketinder.sql`, os DAOs e o DER, e precisa do aval do autor.

Se durante a spec aparecer um pré-requisito que é, na prática, outra feature, trate-o como uma feature separada: ele ganha a própria pasta em `docs/specs/` e é implementado antes.

**O que revisar:** as regras cobrem os casos de borda; cada critério de aceite é verificável por alguém que não escreveu a spec; a seção de anonimato cobre toda saída da feature e tem ao menos um critério de aceite; o "fora do escopo" está explícito; não sobrou premissa sem confirmação. A spec só está aprovada quando o usuário disser isso.

### Etapa 4: Plano de implementação (`docs/specs/<nome-da-feature>/plano.md`)

**O que gerar:** o plano em tarefas pequenas e numeradas, seguindo o modelo do fim deste documento. Cada tarefa indica:

- **O que será feito**, citando as regras da spec que ela atende.
- **Quais arquivos** serão criados ou alterados.
- **Como validar** que está pronta: qual teste roda, o que conferir manualmente e o que conferir do anonimato.
- **A entrada do `CHANGELOG.md`** que ela vai acrescentar.

Como fazer bem:

- **Use o padrão de testes do projeto** (seção "Como testar" do `AGENTS.md`): specs Spock em `src/test/groovy/org/example/`, espelhando o pacote da classe testada, com nome em português e mocks das interfaces de `repositorio`, rodando com `./gradlew test` sem banco. Não adicione dependência ou ferramenta sem perguntar.
- **Diga o que não tem teste automatizado.** Mudança em DAO ou em `ConexaoBanco` depende de conferência manual com o PostgreSQL; mudança no frontend é validada com `npx tsc --noEmit` e no navegador. Escreva isso na tarefa em vez de fingir cobertura.
- **Corte cada tarefa do tamanho de um commit.** Uma tarefa boa deixa o projeto funcionando ao terminar e pode ser validada sozinha.
- **Siga as camadas.** Regra em `service`, persistência em `dao` atrás de uma interface de `repositorio`, entrada e saída só em `view`, montagem só em `Main` e `api/Dependencias`. Lógica isolada vem primeiro, com teste; a ligação com menu, servlet ou tela vem depois.
- **Verifique os comandos de teste** rodando um exemplo descartável antes de escrevê-los no plano.
- **Confira o ponto de partida:** a branch está certa e contém o que a feature pressupõe?
- **Termine com uma tarefa de regressão e documentação.**

**O que revisar:** toda regra da spec aparece em alguma tarefa; nenhuma tarefa mexe em arquivo fora do escopo; a validação de cada tarefa é concreta; toda tarefa tem a entrada do `CHANGELOG.md` prevista. O plano só está aprovado quando o usuário disser isso.

### Etapa 5: Implementação incremental

**O que gerar:** o código de **uma tarefa por vez**, na ordem do plano.

Para cada tarefa:

1. **Confira o ponto de partida.** Veja o estado do repositório: a tarefa anterior já foi commitada? Se não, não misture as duas; avise e espere.
2. **Releia a tarefa no plano e as regras da spec que ela cita**, inclusive a seção de anonimato.
3. **Se a tarefa tem teste, escreva o teste primeiro**, rode e veja falhar. Depois implemente e veja passar.
4. **Rode a suíte inteira** (`./gradlew test`), não só o teste novo. Se a tarefa tocou o frontend, rode também `npx tsc --noEmit` em `frontend/`.
5. **Confira o anonimato da tarefa.** Olhe o que ela passou a exibir, devolver ou registrar e compare com a seção de anonimato da spec. Onde for possível, prove com teste (por exemplo, a saída capturada não contém o nome nem o e-mail da outra parte).
6. **Atualize o `CHANGELOG.md`** com a entrada da tarefa (ver "CHANGELOG" abaixo).
7. **Marque a situação da tarefa no plano:** concluída, ou implementada e aguardando a conferência manual do usuário.
8. **Relate**, citando a tarefa do plano e as regras da spec:
   - o que mudou e em quais arquivos;
   - o que você verificou e como, incluindo o anonimato;
   - a entrada que entrou no `CHANGELOG.md`;
   - o que falta o usuário conferir, em passos que ele possa seguir;
   - qualquer decisão que você tomou além do texto do plano.
9. **Pare.** Só comece a tarefa seguinte quando o usuário pedir.

"Conferi" ou "está funcionando" sobre uma tarefa não é pedido para começar a próxima: o usuário pode ainda querer revisar ou commitar. Na dúvida, pergunte.

Combine os commits no início da etapa: quem commita (o usuário ou o agente) e em que estilo de mensagem. Se for o agente, faça o commit só depois que o usuário validar a tarefa. O commit da tarefa leva junto o código, os testes, a situação no plano e a entrada do `CHANGELOG.md`.

**O que revisar:** a cada tarefa, o usuário confere a validação descrita no plano, lê a entrada do `CHANGELOG.md` e faz (ou autoriza) o commit.

### Etapa 6: Validação

**O que gerar:** a confirmação de que a feature atende à spec, respeita o anonimato e não quebrou o que existia. Preencha o checklist abaixo no relato final, dizendo para cada item como foi verificado: por teste, manualmente ou ainda não verificado.

#### Checklist de validação

**Testes e critérios de aceite**

- [ ] `./gradlew test` passa por inteiro (informe o total de testes e as falhas).
- [ ] `npx tsc --noEmit` passa em `frontend/`, se o frontend foi tocado.
- [ ] Cada critério de aceite da spec foi repassado, um por um.
- [ ] Mudanças em DAO ou schema foram conferidas com o PostgreSQL real.

**Princípio do Anonimato** (tabela e regras no `AGENTS.md`)

- [ ] **Empresa, antes do match:** do candidato aparecem somente ID, formação, descrição e competências. Nada de nome, sobrenome, e-mail, CPF, data de nascimento, país ou CEP.
- [ ] **Candidato, antes do match:** aparecem somente os dados da vaga (ID, nome, descrição, cidade/estado, competências, índice de afinidade). Nada da empresa, nem o nome.
- [ ] **Depois do match:** o perfil completo é revelado apenas entre as duas partes daquele match, e sem a senha.
- [ ] **Senha:** não aparece em nenhuma saída, JSON, log, mensagem de erro ou texto de teste.
- [ ] **Curtida:** nenhuma parte descobre quem a curtiu antes de existir o match.
- [ ] **Todas as camadas tocadas foram olhadas:** textos de `descrever*` e views no CLI, mapas de `Json.deX` na API, HTML renderizado e dados gravados no frontend, colunas devolvidas pelas consultas, logs e mensagens de exceção.
- [ ] **Pendências conhecidas:** nenhuma foi ampliada nem usada como modelo; se alguma foi alterada, houve autorização do autor e o `AGENTS.md` foi atualizado.
- [ ] Cada linha da seção de anonimato da spec foi conferida, com a forma de verificação.

**Regressão e escopo**

- [ ] As funcionalidades listadas em "Impacto no que já existe" foram conferidas.
- [ ] Comparando com o commit de partida, nenhum arquivo fora do escopo foi alterado.

**Documentação**

- [ ] `CHANGELOG.md` tem uma entrada para cada tarefa do plano.
- [ ] `AGENTS.md` descreve a feature como existente.
- [ ] `README.md` ganhou a descrição de uso.
- [ ] A spec está marcada como implementada e todas as tarefas do plano têm situação.

Um item do anonimato que não pôde ser verificado não é "ok": relate como pendente e deixe a decisão com o usuário.

**O que revisar:** o usuário faz a conferência final do que só ele consegue ver (terminal com o banco real, navegador, Tomcat) e lê a documentação atualizada.

## CHANGELOG

O arquivo é `CHANGELOG.md`, na raiz, no formato [Keep a Changelog](https://keepachangelog.com/pt-BR/1.1.0/) em português, com a seção `## [Não lançado]` no topo e as mudanças agrupadas em `Adicionado`, `Modificado`, `Corrigido` e `Removido`.

```markdown
# Changelog

## [Não lançado]

### Adicionado
- Curtida pela API: `POST /curtidas`
  (spec `curtida-pela-api`, tarefa 2).

### Modificado
- `Json.deVaga` deixa de devolver a empresa
  (spec `curtida-pela-api`, tarefa 3).
```

- **Uma entrada por tarefa**, acrescentada em `[Não lançado]` na categoria certa: `Adicionado` (comportamento novo), `Modificado` (mudança no que já existia, inclusive testes, refatoração e documentação), `Corrigido` (defeito resolvido) ou `Removido`. As quatro categorias já existem no arquivo; não crie outras.
- **A entrada descreve o efeito** para quem usa ou mantém o projeto, em uma frase, e termina com a referência à spec e à tarefa, como no exemplo acima.
- **A entrada não pode violar o anonimato nem conter dados reais** de candidatos ou empresas.
- **Não reescreva entradas antigas** nem mova itens de `[Não lançado]` para uma versão: isso só acontece a pedido do autor.
- A tarefa final de regressão e documentação também tem entrada, e aproveita para conferir se cada tarefa anterior deixou a sua.

## Regras

- **Não escreva código antes da spec aprovada.** Código escrito cedo vira argumento para não mudar a spec, e a decisão deixa de ser do usuário.
- **Uma tarefa por vez.** Implemente, relate e pare. Duas tarefas misturadas não podem ser revisadas nem commitadas separadamente.
- **Um commit por tarefa.** O histórico passa a contar a feature passo a passo, e qualquer tarefa pode ser revertida sozinha.
- **Toda tarefa atualiza o `CHANGELOG.md`.** Uma tarefa sem entrada não está concluída.
- **O Princípio do Anonimato vale em toda feature.** Nenhuma tarefa pode expor dado fora da tabela do `AGENTS.md`, e o checklist da etapa 6 não pode ser pulado.
- **Não altere o que estiver fora do escopo.** Isso inclui corrigir bugs antigos, refatorar, reformatar e resolver pendências conhecidas do `AGENTS.md`. Se encontrar um problema fora do escopo, relate e deixe a decisão com o usuário.
- **Pergunte em vez de supor.** Quando uma decisão muda o comportamento da feature, ela é do usuário. Schema, contrato da API e dependências só mudam com aval.
- **Relate com fidelidade.** Diga o que foi verificado e o que não foi. "Passou no teste com mock" não é "funciona com o banco"; se um teste falhou, mostre a saída.
- **Operações de git precisam de autorização:** commit, merge, push e troca de branch só com o aval do usuário.
- **Mantenha spec, plano e código de acordo.** Se uma decisão mudar no meio do caminho, atualize a spec e o plano antes de mudar o código.

## Modelos

### `docs/specs/<nome-da-feature>/spec.md`

```markdown
# Spec: <nome da feature>

Branch: `<branch>` · Escopo: <CLI | API REST | frontend | banco> · Situação: <em elaboração | aprovada | implementada>

## 1. Contexto e objetivo

<Qual problema a feature resolve, para quem, e como é hoje sem ela.>
<Um caso de uso de referência, se o usuário deu um.>

## 2. Regras de negócio

**RN-01 — <título>.** <Como a feature deve se comportar.>
**RN-02 — <título>.** <...>

### Casos de borda

| Caso | Comportamento esperado |
| --- | --- |
| <situação incomum> | <o que acontece> |

## 3. Princípio do Anonimato

| Saída da feature | Quem vê | Momento | Campos exibidos |
| --- | --- | --- | --- |
| <tela, texto do terminal, resposta de API, consulta, log> | <candidato, empresa ou ninguém> | <antes ou depois do match> | <lista exata de campos> |

<Se a feature não exibe nem devolve dados de candidato, empresa ou vaga, escreva isso e por quê.>
<Pendências conhecidas do AGENTS.md que a feature toca, e o que o autor decidiu sobre cada uma.>

## 4. Critérios de aceite

**CA-01 — <título>**
- Dado <estado inicial>
- Quando <ação>
- Então <resultado observável>

**CA-xx — Anonimato preservado**
- Dado <uma parte sem match com a outra>
- Quando <ela usa a feature>
- Então <vê apenas os campos da seção 3, e nunca a senha>

<Diga aqui como os critérios são verificados: spec Spock, conferência manual ou ambos.>

## 5. Impacto no que já existe

| Funcionalidade atual | Impacto |
| --- | --- |
| <funcionalidade> | <"Sem impacto" ou o que muda, incluindo efeitos colaterais> |
| Schema (`database/Linketinder.sql`, DER) | <"Sem impacto" ou tabelas e colunas que mudam> |

## 6. Fora do escopo

Esta feature **não** vai:

- <o que fica de fora, inclusive correções tentadoras de pendências já conhecidas>

## 7. Decisões

Confirmadas pelo autor:

- <decisão>

Premissas assumidas na escrita, a confirmar:

- <premissa>
```

### `docs/specs/<nome-da-feature>/plano.md`

```markdown
# Plano de implementação: <nome da feature>

Implementa `docs/specs/<nome-da-feature>/spec.md`. Os códigos `RN-xx` e `CA-xx` são as regras e os critérios de aceite da spec.

## Decisões de implementação

| Tema | Decisão |
| --- | --- |
| Testes | <specs Spock novas ou alteradas, o que cobrem> |
| Organização do código | <classes, interfaces ou módulos novos, por camada> |
| Banco | <"Sem mudança" ou o que muda no script e no DER> |
| Ordem | <por exemplo: teste antes do código> |
| Commits | <quem commita e em que estilo> |

## Estrutura ao final

<Árvore dos arquivos criados e alterados.>

## Como rodar os testes

<Comando exato, já verificado.>

<O que os testes cobrem e o que depende de conferência manual (DAO com PostgreSQL, navegador, Tomcat).>

## Tarefas

### 1. <título da tarefa>

- **O que:** <o que será feito> (RN-xx).
- **Arquivos:** <criados e alterados>.
- **Testes (escritos antes):**
  - given <estado>, when <ação>, then <resultado>;
- **Validação:** <qual teste roda e o que conferir manualmente, citando CA-xx>.
- **Anonimato:** <o que esta tarefa exibe ou devolve e como conferir, ou "não exibe dados de candidato, empresa ou vaga">.
- **CHANGELOG:** <categoria> — <texto da entrada> (spec `<nome-da-feature>`, tarefa 1).
- **Situação:** <pendente | concluída | implementada, aguardando conferência do autor>.

### 2. <título da tarefa>

<...>

### N. Regressão e documentação

- **O que:** atualizar `AGENTS.md`, README e a situação da spec; repassar o checklist de validação.
- **Arquivos:** <documentos>.
- **Validação:** checklist da etapa 6 preenchido:
  - todos os testes passam;
  - todos os critérios de aceite repassados;
  - Princípio do Anonimato conferido item por item;
  - funcionalidades listadas em "Impacto" conferidas;
  - nenhum arquivo fora do escopo alterado;
  - `CHANGELOG.md` com uma entrada por tarefa.
- **CHANGELOG:** Modificado — <documentação da feature atualizada> (spec `<nome-da-feature>`, tarefa N).
- **Situação:** <pendente | concluída>.

## Pendências

- <o que depende de decisão ou autorização do autor>
```
