# Prompts

Registro dos prompts usados para conduzir o agente de IA no desenvolvimento com spec das features deste repositório.

- **Microsserviço de cadastro** (etapas 1 a 8): prompts transcritos como foram escritos, sem correção.
- **Recrutador** (etapas 9 a 15): a sessão original do agente se perdeu antes de ser registrada, então estes prompts são uma **reconstrução** feita a partir da spec, do plano, do `revisao.md`, do `CHANGELOG.md` e das mensagens de commit da feature. Eles dizem o que foi pedido em cada etapa, mas **não são transcrição literal**.

Em cada etapa há também as decisões respondidas às perguntas do agente e o que a etapa produziu.

# Feature: microsserviço de cadastro

## Etapa 1 — Contexto para o agente (`AGENTS.md`)

**Prompt**

> Explore o repositório e gere um AGENTS.md na raiz do projeto. Além da visão geral, stack, estrutura, como executar e testar, convenções e restrições inclua o Domínio do Linketinder: as entidades (candidato, empresa, vaga, competência, curtida, match) e como se relacionam. As Regras de negócio centrais: como funciona a curtida e quando o match acontece.Princípio do Anonimato: quais dados são visíveis antes e depois do match. Este princípio deve ser respeitado em qualquer feature. Banco de dados: onde ficam os scripts e como aplicá-los. Sempre que houver dúvidas e questões a ser tratadas é melhor me perguntar do que decidir sozinho.

**Decisões respondidas ao agente**

| Pergunta | Resposta |
| --- | --- |
| Antes do match, o que a empresa vê do candidato? | Formação, competências e descrição |
| Antes do match, o candidato vê qual empresa é dona da vaga? | Não, a empresa fica oculta |
| Depois do match, o que é revelado? | Perfil completo, exceto a senha |
| Como tratar a API REST, que devolve o cadastro completo sem anonimato? | Como pendência conhecida |

**Resultado:** `AGENTS.md`, com a regra "na dúvida, pergunte", o Princípio do Anonimato como restrição obrigatória e as pendências encontradas no código.

## Etapa 2 — Skill de desenvolvimento com spec

**Prompt**

> Eu acabei de trazer a skill de um outro projeto que fiz. A etapa de agora é a skill de desenvolvimento com spec. Adapte esta skill ao Linketinder: as specs passam a ficar em docs/specs/<nome-da-feature>/, o checklist de validação deve incluir o Princípio do Anonimato e cada tarefa deve atualizar o CHANGELOG.md.

**Decisões respondidas ao agente**

| Pergunta | Resposta |
| --- | --- |
| Em que formato cada tarefa registra a mudança no CHANGELOG? | Keep a Changelog |
| Criar o `CHANGELOG.md` junto com a adaptação da skill? | Não criar agora |

**Resultado:** `skills/feature-com-spec/SKILL.md` adaptada ao projeto.

## Etapa 3 — `CHANGELOG.md`

**Prompt**

> crie um CHANGELOG.md na raiz do projeto, seguindo o padrão Keep a Changelog.

**Resultado:** `CHANGELOG.md` com a seção `[Não lançado]` e as quatro categorias; a skill foi ajustada para usar "Modificado".

## Etapa 4 — Skill de revisão

**Prompt**

> crie uma skill que revise o código com foco na aderência à spec. Registre os apontamentos e o que você corrigiu.

**Decisões respondidas ao agente**

| Pergunta | Resposta |
| --- | --- |
| Onde registrar os apontamentos e as correções? | Em `docs/specs/<feature>/revisao.md` |

**Resultado:** `skills/revisao-com-spec/SKILL.md`. O uso da skill ficou para depois da implementação da feature.

## Etapa 5 — Especificação (`docs/specs/microservico/spec.md`)

**Prompt**

> Vamos para a etapa de especificação (docs/specs/microservico/spec.md). Use a sua skill para escrever a spec.
>
> Seu desafio é implementar a parte de cadastro de empresa/candidato em forma de um microserviço sem o uso de frameworks.
>
> Imagine o microsserviço como uma nova aplicação, um programa a parte. Você pode implementá-lo no mesmo diretório do projeto de forma desacoplada e em backend. Com isso, executar esse serviço em outra instância do InteliJ, de forma que ele fique disponível em alguma porta. E pesquisar uma forma de criar os endpoints para integração do frontend com esse microsserviço.
>
> Requisitos:
>
> Implementar um microsserviço somente de cadastro sem framework;
> Explorar uma solução para criar a comunicação entre frontend e microsserviço para o cadastro;
> Faça descrições no readme.md do que foi explorado sobre a integração.
> Ser possível cadastrar candidatos ou empresas por meio do microsserviço criado.
>
> Faça-me perguntas antes de escrever, revise e valide comigo

**Decisões respondidas ao agente, antes da primeira versão**

| Pergunta | Resposta |
| --- | --- |
| Como o microsserviço fica em relação ao projeto principal? | Pasta com build próprio |
| "Sem framework": qual servidor HTTP? | `HttpServer` do JDK |
| Como distinguir candidato de empresa? | Uma rota por perfil |
| O que fazer com os `POST` de cadastro da API REST atual? | Ficam como estão |
| O que responder a um cadastro repetido? | `409` com mensagem genérica |
| Quais campos são exigidos do candidato? | Os mesmos da API atual |
| Até onde vai a validação? | Senha, data e formatos |
| A senha é gravada como? | Como hoje; o texto puro vira pendência |

Nessa mesma rodada foram respondidas quatro perguntas cujas respostas mudaram depois, pelos prompts de ajuste abaixo: como entram as competências, o que o formulário do site faz após o cadastro, a solução de integração com o frontend e onde fica a tela de competências.

**Prompts de ajuste, na ordem**

> Integrar o frondend com o backend sem framework será nossa próxima feature, então foque em apenas o microservico nesta feature. Sobre a P-02 deixe como estava igual o CLI mesmo, a P-06 existe validações que verifica competencias com o mesmo nome, compara duplicatas em minusculo. As competências devem ficar junto com o cadastro do candidato e da vaga mesmo

> Essa feature é para implementar um microserviço somente de cadastro sem framework e a comunicação entre frontend e microsservico para o cadastro é apenas para explorar uma solução e não para implementar, como dito anteriormente: Imagine o microsserviço como uma nova aplicação, um programa a parte. Você pode implementá-lo no mesmo diretório do projeto Linketinder de forma desacoplada e em backend. Com isso, executar esse serviço em outra instância do InteliJ, de forma que ele fique disponível em alguma porta. Sobre a dúvida 2. Passa a ignorar maiusculas também no banco. Agora entendi a pergunta 3. Implemente apenas o cadastro candidatos/empresas e realmente competencias pode ficar separado. Sobre 4. estão confirmadas.

> P-01 O cadastro deve cadastrar todas as informações do candidato e da empresa. Mas só após o cadastro é que o candidato vai criar e escolher as competências dele. A P-05 está certo.

> Vamos deixar as competencias para a proxima feature então. P-02 como eu disse, não vai ter integração com o front-end nessa feature. P-03 a ideia é o candidato poder alterar suas competências a qualquer momeneto mas vamos deixar isso para outra feature, deixe como pendencia. P-04 Deixe como uma pendencia para o futuro. P-06 pode ser assim

> É interessante para a competencia após cadastrar o candidato, aparecer para ele escrever as competencias para ser salvo no banco

> Opção A

> 2. O candidato é obrigado a ter ao menos 1 competencia. 1. Está correto

> Aceito e a spec está aprovada

**Resultado:** `docs/specs/microservico/spec.md` aprovada. O escopo final ficou em três rotas (`POST /candidatos`, `POST /empresas` e `POST /candidatos/{id}/competencias`), sem alteração no frontend e com a integração apenas explorada.

## Etapa 6 — Plano de implementação (`docs/specs/microservico/plano.md`)

O plano foi escrito pelo agente logo após a aprovação da spec, seguindo a skill. Os prompts desta etapa foram a regra abaixo e as respostas às pendências do plano.

**Prompt**

> Um detalhe que deixei passar Use dados fictícios. Em scripts de carga e testes, nunca use CPFs, e-mails ou nomes reais.
> Também nunca faça commit de credenciais. Senhas de banco, chaves de API e arquivos .env devem ficar fora do repositório.

**Prompts de resposta às pendências, na ordem**

> 1. sim 2. não precisa do .env por enquanto, 3. Vou deixar você commitar depois. 4. Essas alterações .idea e frontend não é para mexer e nem commitar. 5. Ficam

> 1. sim 2. sim

**Resultado:** as duas regras entraram em "Restrições" do `AGENTS.md`; a spec passou a exigir a senha do banco só por variável de ambiente e dados fictícios nos testes; `docs/specs/microservico/plano.md` aprovado, com 8 tarefas.

## Etapa 7 — Implementação incremental

**Prompt**

> Implemente uma tarefa por vez e para cada tarefa:
>
> Rode a aplicação e os testes.
> Confira se o anonimato continua preservado.
> atualize o CHANGELOG.md e marque a tarefa como concluída no plano.
> Faça um commit semântico por tarefa.
> Se a spec se mostrar incompleta durante a implementação, atualize a spec primeiro e só depois o código.

**Resultado:** as 8 tarefas do plano, uma por commit.

| Tarefa | Mensagem do commit |
| --- | --- |
| 1 | `feat: adiciona esqueleto do microsservico de cadastro` |
| 2 | `feat: adiciona validacao e regras de cadastro no microsservico` |
| 3 | `feat: adiciona rotas de cadastro de candidato e empresa no microsservico` |
| 4 | `feat: grava cadastros do microsservico no PostgreSQL` |
| 5 | `feat: adiciona regras e rota de competencias do candidato no microsservico` |
| 6 | `feat: grava competencias do candidato no PostgreSQL em transacao` |
| 7 | `docs: descreve o microsservico de cadastro e a exploracao da integracao no README` |
| 8 | `docs: registra o microsservico de cadastro no AGENTS.md e fecha a spec` |

## Etapa 8 — Este arquivo

**Prompt**

> Crie um PROMTPS.md que mostre todos os comandos solicitados para o agente.

**Resultado:** `PROMPTS.md`.

## O que funcionou nos prompts (microsserviço)

- **Pedir perguntas antes da escrita.** "Faça-me perguntas antes de escrever, revise e valide comigo" fez as decisões aparecerem antes do código. A spec passou por várias rodadas de ajuste e o plano foi escrito uma vez só.
- **Dar a regra e o motivo.** O Princípio do Anonimato foi pedido como "restrição explícita, pois o agente precisa respeitá-la em qualquer feature", e passou a aparecer na spec, em cada tarefa e no checklist.
- **Dizer o que fica para depois.** Frases como "vamos deixar isso para outra feature, deixe como pendencia" reduziram o escopo sem perder a ideia: as pendências ficaram registradas na spec.
- **Definir o ciclo de cada tarefa.** A lista da etapa 7 (rodar, conferir o anonimato, atualizar o CHANGELOG, marcar no plano, commitar) virou o roteiro repetido nas 8 tarefas.

## O que deu trabalho (microsserviço)

- **Termos resumidos demais do lado do agente.** As premissas "P-02", "P-03" e "P-06" precisaram ser reexplicadas com exemplos antes de serem decididas.
- **Uma mesma frase com duas leituras.** "Competências pode ficar separado" podia ser uma rota separada ou outra feature; a decisão sobre as competências mudou quatro vezes até fechar.
- **Regras lembradas no meio do caminho.** Dados fictícios e credenciais entraram depois da spec aprovada e obrigaram a rever uma decisão já tomada (a senha do banco com valor padrão).

# Feature: recrutador

> **Aviso.** A sessão em que estes prompts foram enviados se perdeu antes de ser copiada para cá. O que está abaixo foi **reconstruído** a partir do que a feature deixou registrado: `docs/specs/recrutador/spec.md` (seções 8.1 a 8.4), `docs/specs/recrutador/plano.md`, `docs/specs/recrutador/revisao.md`, o `CHANGELOG.md` e as mensagens dos commits da branch. As **decisões** e os **resultados** de cada etapa vêm desses documentos e são fiéis.

## Etapa 9 — Especificação (`docs/specs/recrutador/spec.md`)

**Prompt**

> Vamos para a próxima feature: o recrutador. Use a sua skill de feature com spec e escreva a spec em `docs/specs/recrutador/spec.md`.
>
> O desafio é adicionar um terceiro tipo de usuário ao Linketinder: o recrutador, colaborador de uma empresa, que analisa candidatos e curte os que considerar compatíveis. Hoje existe um único login por empresa, e quem faz esse trabalho na prática são os recrutadores.
>
> Antes de escrever, me pergunte sobre:
>
> - **Vínculo:** como o recrutador é associado à empresa? Quem pode cadastrá-lo ou removê-lo?
> - **Curtidas e match:** a curtida do recrutador conta como curtida da empresa? Como fica o match quando dois recrutadores da mesma empresa analisam o mesmo candidato?
> - **Escopo de atuação:** o recrutador vê todas as vagas da empresa ou apenas as da sua área? Ele pode criar ou editar vagas?
> - **Anonimato:** o que o recrutador enxerga de um candidato antes do match? Depois do match o candidato vê dados do recrutador? Quais dados pessoais do recrutador nunca devem ser exibidos?
> - **Validações:** quais regras valem para e-mail, CPF, data de nascimento e senha?
>
> A feature entra nas quatro partes: banco, CLI, API REST e frontend. Faça-me perguntas antes de escrever, revise e valide comigo.

**Decisões respondidas ao agente** (as cinco perguntas do desafio, registradas na seção 8.1 da spec)

| Pergunta | Resposta |
| --- | --- |
| Vínculo | `id_empresa` obrigatório: todo recrutador nasce com empresa. Só a empresa cadastra, lista, atualiza e remove os recrutadores dela; não há autocadastro |
| Curtidas e match | A curtida é do recrutador para um candidato **numa vaga** e vale como interesse da empresa naquela vaga. Vários recrutadores da mesma vaga podem curtir o mesmo candidato, mas o match é um só por trio candidato–empresa–vaga e fica com o primeiro que fechou a reciprocidade |
| Escopo de atuação | Nem todas as vagas, nem por área: a empresa **atribui explicitamente** as vagas de cada recrutador, e ele só vê e só curte nessas. Não cria, não edita e não exclui vagas |
| Anonimato | O recrutador vê do candidato exatamente o que a empresa vê. O candidato **nunca** vê nada do recrutador, nem depois do match. CPF, data de nascimento e senha do recrutador nunca saem do banco |
| Validações | O rigor do microsserviço, aplicado no `service` do projeto principal: e-mail com formato e único, CPF de 11 dígitos sem conferir dígito verificador e único, data `AAAA-MM-DD` existente e não futura, senha de 6 a 50. A área vem de uma lista fixa |

**Outras decisões da mesma rodada** (seção 8.2 da spec)

| Pergunta | Resposta |
| --- | --- |
| Como gravar a curtida do recrutador? | Tabela própria `curtida_recrutador_candidato`, com `recrutador_vaga` para a atribuição |
| Como saber quem fechou o match? | Coluna `matches.id_recrutador`, anulável: nulo significa que foi a própria empresa |
| Para que serve a área? | Informativa: a empresa vê a contagem por área. Não restringe atribuição |
| O que acontece ao desatribuir uma vaga? | Apaga as curtidas daquele recrutador naquela vaga; os matches ficam, com `id_recrutador` nulo |
| E ao excluir um recrutador? | Apaga vínculos e curtidas dele; empresa com recrutadores não pode ser excluída |
| E-mail e CPF são únicos em todo o sistema? | Só dentro de `recrutadores`: o login recebe o `tipo` e sabe onde procurar |
| Senha com hash nesta feature? | Não: segue em texto puro como os outros cadastros, ampliando a pendência conhecida de forma documentada |

**Resultado:** `docs/specs/recrutador/spec.md`, com as regras `RN-01` a `RN-28`, os critérios `CA-01` a `CA-20` e a seção 4 reescrevendo o Princípio do Anonimato com o terceiro perfil. Commit `docs(recrutador): adiciona especificação da feature`.

## Etapa 10 — Revisão da spec antes de aprovar

**Prompt**

> Antes de aprovar, repasse comigo os pontos em aberto da spec: o que ainda é premissa sua, o que muda no `AGENTS.md` e o que você precisa de mim para fechar.

**Decisões respondidas ao agente** (seção 8.2 da spec, "Confirmadas na revisão da spec")

| Pergunta | Resposta |
| --- | --- |
| Cadastro repetido responde `409`, como no microsserviço? | Não: `400` com a mensagem genérica. O `ApiServlet` já mapeia regra de negócio para `400`, e `409` exigiria um tipo novo de exceção |
| As rotas de recrutador podem usar `DELETE`? | Podem, e é a primeira exceção à convenção "só `GET` e `POST`". Acompanham o `CorsFilter`, o `AGENTS.md` e o `README.md` |
| Quem atualiza o DER (`assets/der.png`)? | Eu, depois. A feature altera só o `database/Linketinder.sql` e aponta no plano o que mudou no diagrama |
| Implementar autenticação de verdade aqui? | Não: fica como feature própria. A falta de autenticação nas rotas novas entra como pendência registrada |
| O que o recrutador vê do candidato depois do match? | O mesmo que a empresa — mas, como esta feature não cria saída pós-match, a decisão só vale quando existir a tela de histórico |
| Posso alterar a tabela do Princípio do Anonimato no `AGENTS.md`? | Só com o texto submetido antes. O anonimato é restrição central da aplicação |
| A carga do script pode ter recrutadores? | Pode, com dados fictícios: e-mails em `example.com` e CPFs com dígito verificador inválido |

**Resultado:** spec aprovada em 2026-10-13. As premissas confirmadas passaram para a seção 8.2 e sobraram três a confirmar (`P-02`, `P-04` e `P-08`); as pendências para o futuro (autenticação, hash de senha, tela de histórico de matches) ficaram na seção 8.4.

## Etapa 11 — Plano de implementação (`docs/specs/recrutador/plano.md`)

**Prompt**

> Spec aprovada. Escreva o plano de implementação seguindo a skill: tarefa por tarefa, com os arquivos tocados, os testes escritos antes, a validação, o efeito no anonimato e a entrada do `CHANGELOG`. Lógica isolada primeiro, persistência depois, CLI, API, frontend e, por último, regressão e documentação.

**Decisões respondidas ao agente**

| Pergunta | Resposta |
| --- | --- |
| Onde fica a validação do cadastro, já que CLI e API precisam das mesmas regras? | Em `service/ValidadorDeRecrutador` — primeira validação de formato no `service` do projeto principal |
| Como representar a lista fixa de áreas? | Enum `model/AreaDeRecrutamento`, com o nome de exibição de cada área |
| Como o `DELETE` entra no `ApiServlet` sem mexer nos outros servlets? | `responderSemConteudo` (204) e `metodosAceitos()` sobreponível, com `"GET, POST"` como padrão |
| Como o match passa a considerar as duas origens de curtida? | Uma única consulta em `MatchDAO.criarMatchSePossivel`, com a mesma assinatura, gravando o recrutador da curtida mais antiga |
| Onde entra o SQL novo, já que o script não é idempotente? | Em bloco próprio no fim de `database/Linketinder.sql` |

**Resultado:** `docs/specs/recrutador/plano.md` aprovado em 2026-10-13, com 15 tarefas. Commit `docs(recrutador): adiciona plano de implementação`.

## Etapa 12 — Implementação incremental

**Prompt** (o mesmo ciclo da etapa 7, repetido para esta feature)

> Implemente uma tarefa por vez e, para cada tarefa: escreva os testes antes, rode `./gradlew test` (e `npx tsc --noEmit` quando mexer no frontend), confira o Princípio do Anonimato, atualize o `CHANGELOG.md`, marque a tarefa como concluída no plano e me mostre o diff para eu commitar. Se a spec se mostrar incompleta durante a implementação, atualize a spec primeiro e só depois o código.

**Resultado:** as 15 tarefas do plano, uma por commit (as tarefas 14 e 15 saíram juntas).

| Tarefa | Mensagem do commit |
| --- | --- |
| 1 | `feat(recrutador): adiciona estrutura ao banco` |
| 2 | `feat(recrutador): adiciona modelo e áreas` |
| 3 | `feat(recrutador): valida dados do cadastro` |
| 4 | `feat(recrutador): adiciona cadastro e service` |
| 5 | `feat(recrutador): atribui vagas e valida escopo` |
| 6 | `feat(recrutador): adiciona curtida e match` |
| 7 | `feat(recrutador): adiciona persistência JDBC` |
| 8 | `feat(recrutador): integra curtidas aos matches` |
| 9 | `feat(recrutador): adiciona menu e curtida no CLI` |
| 10 | `feat(recrutador): adiciona rotas REST` |
| 11 | `feat(recrutador): integra login e curtida na API` |
| 12 | `feat(recrutador): adiciona acesso ao frontend` |
| 13 | `feat(recrutador): adiciona gestão no frontend` |
| 14 e 15 | `feat(recrutador): conclui telas e documentação do perfil` |

## Etapa 13 — Revisão com spec (`docs/specs/recrutador/revisao.md`)

**Prompt**

> Leia o `AGENTS.md`, a spec do recrutador e o plano e verifique se tem incoerências. Use a skill de revisão com spec e registre os apontamentos antes de corrigir.

**O que a revisão encontrou** (doze apontamentos, nenhum bloqueante)

| Apontamento | Situação |
| --- | --- |
| `AP-01` `matches.id_recrutador` sem migração, impedindo o `CA-17` como estava escrito | corrigido |
| `AP-02` o recrutador continuava entrando na página provisória da tarefa 12 | corrigido |
| `AP-03` o gráfico de competências aparecia na tela do recrutador | corrigido |
| `AP-04` a listagem de recrutadores no CLI não mostrava a empresa | corrigido |
| `AP-05` o `AGENTS.md` não foi atualizado pela tarefa 15 | corrigido |
| `AP-06` o `README.md` se contradizia sobre o `DELETE` e o contrato estava incompleto | corrigido |
| `AP-07` o `CHANGELOG.md` estava sem a entrada da tarefa 15 | corrigido |
| `AP-08` o `plano.md` se contradizia e não havia `revisao.md` | corrigido |
| `AP-09` lista de áreas duplicada em três lugares | postergado |
| `AP-10` `CurtidaService` com dependência opcional e `IllegalStateException` | postergado |
| `AP-11` `Location` do `POST /recrutadores` aponta para uma rota que não existe | postergado |
| `AP-12` a verificação de escopo carrega todas as vagas do recrutador | postergado |

**Decisões respondidas ao agente**

| Pergunta | Resposta |
| --- | --- |
| Corrijo os quatro apontamentos menores de qualidade agora? | Não: são melhorias sem efeito em comportamento, ficam registradas |
| Posso alterar a tabela do anonimato no `AGENTS.md`? | Sim, com o texto da seção 4 da spec, como combinado na etapa 10 |

**Resultado:** `docs/specs/recrutador/revisao.md`, com o parecer "aprovado com pendências", e o commit `fix(recrutador): corrigir pontos da revisão final`.

## Etapa 14 — Conferência em execução

**Prompt**

> Não basta ler o código: rode. Suba o PostgreSQL, aplique o script, gere o WAR no Tomcat 9 e exercite os cenários de `CA-11` a `CA-20` de verdade. Registre o resultado no `revisao.md`.

**Resultado:** seção 7 do `revisao.md` — 94 asserções de execução contra PostgreSQL 18, Tomcat 9.0.122 e o Vite, sem falhas. `CA-17`, `CA-18` e `CA-19` passaram de pendentes a verificados; o `CA-20` ficou parcial, porque faltou a passada visual no navegador.

## O que funcionou nos prompts (recrutador)

- **Entregar as perguntas do desafio dentro do prompt.** As cinco perguntas (vínculo, curtidas e match, escopo, anonimato, validações) viraram a espinha da spec, e as decisões ficaram registradas na seção 8.1 em vez de se perderem na conversa.
- **Separar "aprovar" de "escrever".** A rodada da etapa 10, só para repassar premissas e pendências antes de aprovar, evitou que `409` × `400`, o `DELETE` e a autenticação fossem decididos pelo agente no meio da implementação.
- **Exigir a revisão como etapa própria.** Pedir a conferência entre spec, plano e código encontrou doze apontamentos numa tarefa de documentação que já estava marcada como concluída.
- **Pedir execução, não leitura.** "Não basta ler o código: rode" foi o que transformou `CA-17` a `CA-19` de "pendente" em "verificado".

## O que deu trabalho (recrutador)

- **Tarefa marcada como concluída sem estar.** A tarefa 15 do plano ("Regressão e documentação") foi dada como pronta com o `AGENTS.md` ganhando cinco linhas; só a revisão pegou isso.
- **Página provisória que ficou.** O destino temporário criado na tarefa 12 (`paginas/recrutador.html`) não foi removido pela tarefa 14, e o recrutador passou a ter duas telas com o mesmo conteúdo.
- **Mudança de schema em arquivo que não é idempotente.** A coluna `matches.id_recrutador` foi escrita dentro do `CREATE TABLE` em vez de um bloco no fim, e a validação do `CA-17` ("aplicar sobre o script antigo") deixou de ser executável até a correção do `AP-01`.
