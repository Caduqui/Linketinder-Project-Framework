import {
    atribuirVagaAoRecrutador,
    cadastrarRecrutador,
    excluirRecrutador,
    listarRecrutadoresDaEmpresa,
    listarVagasDaEmpresa,
    listarVagasDoRecrutador,
    removerVagaDoRecrutador
} from "../api/api.ts";
import {descreverErro, enviarFormulario, limparMensagem, mostrarAviso, mostrarMensagem, lerCampo} from "../dom.ts";
import {obterUsuarioDoTipo} from "../sessao.ts";
import type {NovoRecrutador, Recrutador, Vaga} from "../modelos.ts";

const AREAS_RECRUTADOR: string[] = [
    "TI",
    "RH",
    "Marketing",
    "Segurança da Informação",
    "Vendas",
    "Financeiro",
    "Outros"
];

interface RecrutadorComVagas {
    recrutador: Recrutador;
    vagas: Vaga[];
}

function obterSenhaSemAlterar(): string {
    const campo = document.getElementById("recrutador_senha");

    if (!(campo instanceof HTMLInputElement)) {
        throw new Error("Campo #recrutador_senha não encontrado na página.");
    }

    return campo.value;
}

function criarContagemPorArea(recrutadores: Recrutador[]): HTMLLIElement[] {
    const contagens = new Map<string, number>(AREAS_RECRUTADOR.map(area => [area, 0]));

    recrutadores.forEach(recrutador => {
        contagens.set(recrutador.area, (contagens.get(recrutador.area) ?? 0) + 1);
    });

    return AREAS_RECRUTADOR.map(area => {
        const item = document.createElement("li");
        item.textContent = `${area}: ${contagens.get(area) ?? 0}`;
        return item;
    });
}

function criarItemDeVaga(
    recrutadorId: number,
    vaga: Vaga
): HTMLLIElement {
    const item = document.createElement("li");
    const botao = document.createElement("button");

    item.className = "vaga-atribuida";
    item.append(document.createTextNode(`${vaga.nome} — ${vaga.cidade} - ${vaga.estado} `));
    botao.type = "button";
    botao.textContent = "Remover";
    botao.addEventListener("click", () => {
        void executarAlteracao(
            botao,
            async () => {
                await removerVagaDoRecrutador(recrutadorId, vaga.id);
            },
            "Vaga removida do recrutador."
        );
    });
    item.appendChild(botao);
    return item;
}

function criarSeletorDeVagas(
    recrutadorId: number,
    recrutadorNome: string,
    vagasEmpresa: Vaga[],
    vagasAtribuidas: Vaga[]
): HTMLElement {
    const grupo = document.createElement("div");
    const seletor = document.createElement("select");
    const botao = document.createElement("button");
    const idsAtribuidos = new Set(vagasAtribuidas.map(vaga => vaga.id));
    const vagasDisponiveis = vagasEmpresa.filter(vaga => !idsAtribuidos.has(vaga.id));
    const opcaoInicial = document.createElement("option");

    grupo.className = "atribuicao-vaga";
    seletor.setAttribute("aria-label", `Selecionar vaga para ${recrutadorNome}`);
    opcaoInicial.value = "";
    opcaoInicial.disabled = true;
    opcaoInicial.selected = true;
    opcaoInicial.textContent = vagasDisponiveis.length > 0
        ? "Selecione uma vaga"
        : vagasEmpresa.length > 0
            ? "Todas as vagas já estão atribuídas"
            : "Cadastre uma vaga antes de atribuí-la";
    seletor.appendChild(opcaoInicial);

    vagasDisponiveis.forEach(vaga => {
        const opcao = document.createElement("option");
        opcao.value = String(vaga.id);
        opcao.textContent = vaga.nome;
        seletor.appendChild(opcao);
    });

    botao.type = "button";
    botao.textContent = "Atribuir vaga";
    botao.disabled = vagasDisponiveis.length === 0;
    botao.addEventListener("click", () => {
        const idVaga = Number(seletor.value);

        if (!Number.isInteger(idVaga) || idVaga <= 0) {
            mostrarMensagem("Selecione uma vaga para atribuir.");
            return;
        }

        void executarAlteracao(
            botao,
            async () => {
                await atribuirVagaAoRecrutador(recrutadorId, idVaga);
            },
            "Vaga atribuída ao recrutador."
        );
    });

    grupo.append(seletor, botao);
    return grupo;
}

function criarCartaoRecrutador(
    dados: RecrutadorComVagas,
    vagasEmpresa: Vaga[]
): HTMLLIElement {
    const cartao = document.createElement("li");
    const titulo = document.createElement("h3");
    const listaVagas = document.createElement("ul");
    const botaoExcluir = document.createElement("button");

    cartao.className = "card card-recrutador";
    titulo.textContent = dados.recrutador.nome;
    cartao.appendChild(titulo);

    const campos: Array<[string, string]> = [
        ["Área", dados.recrutador.area],
        ["Descrição", dados.recrutador.descricao],
        ["E-mail", dados.recrutador.email]
    ];
    campos.forEach(([rotulo, valor]) => {
        const linha = document.createElement("p");
        const nomeCampo = document.createElement("strong");
        nomeCampo.textContent = `${rotulo}: `;
        linha.append(nomeCampo, document.createTextNode(valor));
        cartao.appendChild(linha);
    });

    const tituloVagas = document.createElement("h4");
    tituloVagas.textContent = "Vagas atribuídas";
    cartao.appendChild(tituloVagas);

    listaVagas.className = "vagas-recrutador";
    if (dados.vagas.length === 0) {
        const aviso = document.createElement("li");
        aviso.textContent = "Nenhuma vaga atribuída.";
        listaVagas.appendChild(aviso);
    } else {
        listaVagas.replaceChildren(...dados.vagas.map(vaga =>
            criarItemDeVaga(dados.recrutador.id, vaga)
        ));
    }
    cartao.appendChild(listaVagas);
    cartao.appendChild(criarSeletorDeVagas(
        dados.recrutador.id,
        dados.recrutador.nome,
        vagasEmpresa,
        dados.vagas
    ));

    botaoExcluir.type = "button";
    botaoExcluir.className = "botao-excluir-recrutador";
    botaoExcluir.textContent = "Excluir recrutador";
    botaoExcluir.addEventListener("click", () => {
        if (!window.confirm(`Deseja excluir o recrutador ${dados.recrutador.nome}?`)) {
            return;
        }

        void executarAlteracao(
            botaoExcluir,
            async () => {
                await excluirRecrutador(dados.recrutador.id);
            },
            "Recrutador excluído."
        );
    });
    cartao.appendChild(botaoExcluir);
    return cartao;
}

async function executarAlteracao(
    botao: HTMLButtonElement,
    acao: () => Promise<void>,
    mensagemSucesso: string
): Promise<void> {
    botao.disabled = true;
    limparMensagem();

    try {
        await acao();
    } catch (erro) {
        mostrarMensagem(descreverErro(erro));
        botao.disabled = false;
        return;
    }

    mostrarMensagem(mensagemSucesso, "sucesso");
    try {
        await carregarRecrutadores();
    } catch (erro) {
        mostrarMensagem(
            `${mensagemSucesso} A lista não pôde ser atualizada: ${descreverErro(erro)}`
        );
    } finally {
        botao.disabled = false;
    }
}

async function carregarListaDeRecrutadores(): Promise<{
    recrutadores: Recrutador[];
    vagasEmpresa: Vaga[];
    dados: RecrutadorComVagas[];
}> {
    const empresa = obterUsuarioDoTipo("empresa");

    if (!empresa) {
        throw new Error("Faça login como empresa para gerenciar recrutadores.");
    }

    const [recrutadores, vagasEmpresa] = await Promise.all([
        listarRecrutadoresDaEmpresa(empresa.id),
        listarVagasDaEmpresa(empresa.id)
    ]);
    const dados = await Promise.all(recrutadores.map(async recrutador => ({
        recrutador,
        vagas: await listarVagasDoRecrutador(recrutador.id)
    })));

    return {recrutadores, vagasEmpresa, dados};
}

export async function carregarRecrutadores(): Promise<void> {
    const lista = document.getElementById("lista-recrutadores");
    const contagem = document.getElementById("contagem-recrutadores-por-area");

    if (!lista || !contagem) {
        return;
    }

    const {recrutadores, vagasEmpresa, dados} = await carregarListaDeRecrutadores();
    contagem.replaceChildren(...criarContagemPorArea(recrutadores));

    if (recrutadores.length === 0) {
        mostrarAviso(lista, "Sua empresa ainda não cadastrou recrutadores.");
        return;
    }

    lista.replaceChildren(...dados.map(dado => criarCartaoRecrutador(dado, vagasEmpresa)));
}

export function configurarFormularioRecrutador(): void {
    const formulario = document.getElementById("form-recrutador");

    if (!(formulario instanceof HTMLFormElement)) {
        return;
    }

    formulario.addEventListener("submit", async evento => {
        evento.preventDefault();
        const empresa = obterUsuarioDoTipo("empresa");

        if (!empresa) {
            mostrarMensagem("Faça login como empresa para cadastrar recrutadores.");
            return;
        }

        const novoRecrutador: NovoRecrutador = {
            nome: lerCampo("recrutador_nome"),
            area: lerCampo("recrutador_area"),
            descricao: lerCampo("recrutador_descricao"),
            email: lerCampo("recrutador_email"),
            cpf: lerCampo("recrutador_cpf"),
            dataNascimento: lerCampo("recrutador_dataNascimento"),
            senha: obterSenhaSemAlterar(),
            idEmpresa: empresa.id
        };

        await enviarFormulario(formulario, async () => {
            await cadastrarRecrutador(novoRecrutador);
            formulario.reset();

            try {
                await carregarRecrutadores();
                mostrarMensagem("Recrutador cadastrado.", "sucesso");
            } catch (erro) {
                mostrarMensagem(
                    `Recrutador cadastrado, mas a lista não pôde ser atualizada: ${descreverErro(erro)}`
                );
            }
        });
    });
}
