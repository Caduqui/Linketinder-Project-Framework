---
name: revisao-com-spec
description: Revisa o código de uma feature do Linketinder com foco em duas perguntas - o código faz o que a spec diz, e respeita o Princípio do Anonimato - e registra os apontamentos e as correções em revisao.md, na pasta da feature dentro de docs/specs/. Use sempre que o usuário pedir para revisar um PR, uma branch, um diff ou uma feature implementada pelo fluxo feature-com-spec, inclusive quando o código revisado foi escrito pelo próprio agente, e também quando falar em code review, revisão de código, conferir aderência à spec ou conferir o anonimato, mesmo que não cite esta skill pelo nome. Não use para implementar feature nova nem para tirar dúvidas sobre o código.
---

# Revisão com spec

## Objetivo

Revisar o código de uma feature respondendo a duas perguntas, nesta ordem de importância:

1. **O código faz o que a spec diz?** Nem menos (regra não implementada), nem mais (comportamento que ninguém pediu), nem diferente.
2. **O código respeita o Princípio do Anonimato?** Nenhuma saída expõe dado fora da tabela do `AGENTS.md`.

O resultado é um registro escrito: cada apontamento com evidência, e o que foi feito com ele. Uma revisão que só diz "está bom" não pode ser conferida por ninguém; uma que lista o que foi olhado e como, pode.

Esta skill é o par da `feature-com-spec`: aquela produz `spec.md` e `plano.md`, esta produz `revisao.md` na mesma pasta. Como lá, o `AGENTS.md` da raiz é a fonte de verdade sobre o anonimato, as camadas e as restrições.

## Entradas

1. **A feature:** a pasta `docs/specs/<nome-da-feature>/`, com `spec.md` e `plano.md`.
2. **O código a revisar:** um PR, uma branch ou um intervalo de commits, e a base de comparação (em geral `main`).

Se não houver spec para o código a revisar, pare e avise: sem spec só é possível revisar o anonimato, e essa redução de escopo é decisão do usuário. Se não estiver claro qual é a base, pergunte.

## Revisando o próprio código

Quando o código foi escrito por você, a revisão tende a confirmar o que você pretendia fazer em vez de olhar o que foi feito. Para evitar isso:

- Parta do diff e da spec, não da lembrança da implementação. Releia cada arquivo alterado por inteiro.
- Trate o relato das tarefas como alegação a conferir. "Os testes passaram" só vale se você rodar de novo agora.
- Para cada regra, procure o caso que quebra, não o que funciona.
- Um apontamento contra o seu próprio código é o resultado esperado da revisão, não uma falha dela.

## Passo a passo

### 1. Delimitar o que será revisado

- Leia `spec.md` e `plano.md` da feature e as seções "Princípio do Anonimato", "Convenções" e "Restrições" do `AGENTS.md`.
- Levante o diff contra a base e a lista de arquivos alterados:

  ```bash
  git diff --stat <base>...HEAD
  git diff <base>...HEAD
  gh pr diff <número>      # quando houver PR
  ```

- Anote no registro a base, o último commit revisado e os arquivos. A revisão vale para esse commit; código que entrar depois precisa de nova passada.

### 2. Rodar as verificações

- `./gradlew test` (informe total e falhas) e, se o frontend foi tocado, `npx tsc --noEmit` em `frontend/`.
- Se algo falhar, isso já é um apontamento bloqueante; registre a saída e continue a revisão.

### 3. Aderência à spec

Monte a matriz de rastreabilidade, uma linha por `RN-xx` e por `CA-xx`:

| Item | Onde está implementado | Onde está verificado | Veredito |
| --- | --- | --- | --- |
| RN-01 | arquivo:linha | spec Spock, conferência manual ou nada | atende, atende em parte, não atende ou não verificável |

Depois confira nos dois sentidos:

- **Da spec para o código:** toda regra, caso de borda e critério de aceite tem implementação? O comportamento é exatamente o descrito (mensagens, códigos HTTP, valores padrão, ordem)? O teste citado verifica a regra ou só exercita o código?
- **Do código para a spec:** cada mudança do diff se explica por alguma regra ou tarefa? O que não se explica é comportamento a mais ou alteração fora do escopo. Confira a lista "Fora do escopo" da spec e os arquivos previstos em cada tarefa do plano.
- **Documentos:** premissas "a confirmar" que continuam sem confirmação; tarefas do plano sem situação; tarefa sem entrada no `CHANGELOG.md`; `AGENTS.md` e README descrevendo a feature.
- **Convenções do projeto**, só onde o diff toca: camadas (`println` fora de `view`, SQL fora de `dao`, `new XxxDAO` fora de `Main` e `Dependencias`), SQL com parâmetros `?`, nomes em português, dependências e schema alterados sem aval.

Quando o código diverge da spec, o apontamento é a divergência. Não ajuste a spec para combinar com o código: decidir qual dos dois está certo é do autor.

### 4. Princípio do Anonimato

Liste toda **saída** que o diff cria ou altera e, para cada uma, responda: quem vê, em que momento (antes ou depois do match) e quais campos saem. Compare com a seção de anonimato da spec e com a tabela do `AGENTS.md`.

Onde procurar saídas:

| Camada | O que olhar |
| --- | --- |
| Modelos | métodos `descrever*` novos ou alterados e quem os chama |
| Views e controllers | cada `println` e qual método do modelo ele usa; listagens mostradas antes de uma curtida |
| API | mapas de `Json.deX`, corpo das respostas e cabeçalhos dos servlets |
| DAO | colunas devolvidas pelas consultas e o que o mapeamento carrega para o objeto |
| Frontend | HTML montado nas telas, textos de `<option>`, o que é gravado no `localStorage` |
| Transversal | `log(...)`, mensagens de exceção, mensagens de erro da API, dados de exemplo em testes e documentos |

Perguntas que cada saída precisa responder:

- **Empresa, antes do match:** do candidato saem só ID, formação, descrição e competências?
- **Candidato, antes do match:** saem só os dados da vaga, sem nada da empresa, nem o nome?
- **Depois do match:** o perfil completo vai só para as duas partes daquele match? Um ID trocado na chamada revela o perfil a quem não tem match?
- **Senha:** aparece em alguma saída, JSON, log ou mensagem?
- **Curtida:** alguma saída permite descobrir quem curtiu antes de existir o match (uma listagem, uma contagem, uma mensagem diferente)?
- **Reuso perigoso:** algum trecho usa `descrever()` ou um `Json.deX` completo em um contexto de pré-match?
- **Pendências conhecidas:** o diff amplia alguma, ou copia o padrão de uma delas?
- **Prova:** existe teste que falharia se o dado proibido passasse a aparecer (a saída capturada não contém nome nem e-mail)? Se não existe, a regra está só confiada à leitura; registre.

Uma busca ajuda a não esquecer lugares, mas não substitui a leitura:

```bash
git diff <base>...HEAD | grep -nE "^\+.*(descrever|println|Json\.de|innerHTML|textContent|localStorage|senha|email|cpf|cnpj|log\()"
```

### 5. Registrar os apontamentos

Crie `docs/specs/<nome-da-feature>/revisao.md` pelo modelo abaixo **antes de corrigir qualquer coisa**: o registro mostra o estado encontrado, e as correções entram depois, apontamento por apontamento.

Cada apontamento tem identificador (`AP-01`), categoria, severidade, local (`arquivo:linha`), a regra violada (`RN-xx`, `CA-xx` ou a regra do anonimato), a evidência (trecho ou saída) e a correção sugerida.

| Severidade | Quando usar |
| --- | --- |
| **Bloqueante** | viola o Princípio do Anonimato; expõe senha; regra ou critério de aceite não atendido; teste falhando |
| **Importante** | regra atendida em parte; critério sem verificação; mudança fora do escopo; quebra de camada; tarefa sem entrada no `CHANGELOG.md` |
| **Menor** | clareza, nome, duplicação, documentação incompleta |

Se a revisão não encontrar nada, registre isso também, com a matriz e a lista de saídas conferidas. "Nenhum apontamento" só tem valor acompanhado do que foi olhado.

### 6. Corrigir

Apresente os apontamentos ao usuário e combine o que será corrigido. Em seguida, um apontamento por vez:

1. **Corrija só o que cabe a você decidir.** Erro de implementação dentro do escopo da feature pode ser corrigido. Peça decisão antes quando a correção muda uma regra da spec, o schema, o contrato da API ou uma dependência, mexe em uma pendência conhecida do `AGENTS.md` ou sai do escopo da feature.
2. **Se o apontamento é verificável por teste, escreva o teste primeiro** e veja falhar; depois corrija e veja passar. Em apontamento de anonimato, o teste deve afirmar que o dado proibido não aparece.
3. **Rode a suíte inteira.**
4. **Atualize o registro:** situação do apontamento, o que mudou, em quais arquivos e como foi verificado.
5. **Atualize o `CHANGELOG.md`** em `Corrigido`, com a referência `(spec <nome-da-feature>, revisão AP-xx)`, quando a correção alterar código.

Commits seguem o `AGENTS.md`: só com aval do usuário. Combine se será um commit por correção ou um para a revisão.

### 7. Repassar e concluir

- Rode de novo as verificações do passo 2 e refaça a conferência dos itens afetados pelas correções: uma correção pode abrir outro problema.
- Feche o registro com o resultado: quantos apontamentos por severidade, quantos corrigidos, quantos aguardam decisão, e o parecer.
- Relate ao usuário o resumo, o que ficou para ele decidir e o que só ele consegue conferir (banco real, navegador, Tomcat).

O parecer é **aprovado** só quando não resta apontamento bloqueante em aberto e todo item do anonimato foi verificado. Item não verificado não conta como aprovado.

## Regras

- **Revise contra a spec, não contra o gosto.** Preferência de estilo sem regra do projeto que a sustente não é apontamento.
- **Todo apontamento tem evidência:** arquivo e linha, trecho ou saída de comando. Sem evidência é suspeita, e vai como pergunta.
- **Registre antes de corrigir**, e nunca apague um apontamento: se não procede, marque "não procede" e diga por quê.
- **Não ajuste a spec para combinar com o código**, nem o contrário, sem o autor.
- **Não corrija o que está fora do escopo**, inclusive pendências conhecidas do `AGENTS.md`. Relate e deixe a decisão com o usuário.
- **Relate com fidelidade.** Diga o que foi rodado e o que foi só lido. Se um teste falhou, mostre a saída.
- **Operações de git precisam de autorização.**

## Modelo

### `docs/specs/<nome-da-feature>/revisao.md`

```markdown
# Revisão: <nome da feature>

Spec: `docs/specs/<nome-da-feature>/spec.md` · Base: `<base>` · Commit revisado: `<hash>` · PR: <número ou "sem PR">
Data: <AAAA-MM-DD> · Revisor: <quem revisou; diga se é o autor do código>

## 1. Resumo

Parecer: <aprovado | aprovado com pendências | reprovado>

| Severidade | Encontrados | Corrigidos | Aguardando decisão | Não procede |
| --- | --- | --- | --- | --- |
| Bloqueante | | | | |
| Importante | | | | |
| Menor | | | | |

## 2. O que foi revisado

- Arquivos: <lista ou saída de `git diff --stat`>
- Verificações rodadas: <comando> — <resultado, com total de testes e falhas>
- Não verificado: <o que ficou de fora e por quê>

## 3. Aderência à spec

| Item | Onde está implementado | Onde está verificado | Veredito |
| --- | --- | --- | --- |
| RN-01 | <arquivo:linha> | <teste ou conferência manual> | <atende | atende em parte | não atende | não verificável> |
| CA-01 | | | |

Mudanças do diff sem regra ou tarefa correspondente: <lista ou "nenhuma">.

## 4. Princípio do Anonimato

| Saída | Quem vê | Momento | Campos que saem | Conforme? | Como foi verificado |
| --- | --- | --- | --- | --- | --- |
| <método, endpoint, tela, consulta, log> | <candidato, empresa ou ninguém> | <antes ou depois do match> | <lista exata> | <sim | não> | <teste | leitura | manual> |

- Senha em saída, JSON, log ou mensagem: <não encontrada | onde>.
- Curtida revelada antes do match: <não | onde>.
- Pendências conhecidas ampliadas ou copiadas: <nenhuma | quais>.

## 5. Apontamentos

### AP-01 — <título>

- **Categoria:** <Spec | Anonimato | Escopo | Testes | Convenções | Documentação>
- **Severidade:** <Bloqueante | Importante | Menor>
- **Local:** `<arquivo:linha>`
- **Regra:** <RN-xx, CA-xx ou regra do anonimato>
- **Evidência:** <trecho de código ou saída>
- **Correção sugerida:** <o que fazer>
- **Situação:** <aberto | corrigido | aguardando decisão do autor | não procede>
- **O que foi corrigido:** <mudança, arquivos, teste que prova, commit> ou <motivo de não corrigir>

## 6. Depois das correções

- Verificações rodadas de novo: <comando> — <resultado>
- Itens repassados: <RN, CA e saídas conferidos de novo>
- Fica para o autor: <decisões e conferências manuais>
```
