import {obterUsuarioDoTipo} from "../sessao.ts";
import {listarCandidatos, listarVagasDaEmpresa, listarVagasDoRecrutador} from "../api/api.ts";
import {criarCard, mostrarAviso} from "../dom.ts";
import {desenharGraficoDeCompetencias} from "../graficoCompetencias.ts";
import type {Vaga} from "../modelos.ts";

export async function carregarMinhasVagas(): Promise<void> {
    const usuario = obterUsuarioDoTipo("empresa") ?? obterUsuarioDoTipo("recrutador");
    const lista = document.getElementById("lista-minhas-vagas");

    if (!usuario || !lista) {
        return;
    }

    if (usuario.tipo === "empresa") {
        const [vagas, candidatos] = await Promise.all([listarVagasDaEmpresa(usuario.id), listarCandidatos()]);

        if (vagas.length === 0) {
            mostrarAviso(lista, "Você ainda não cadastrou nenhuma vaga. Use \"Cadastrar vagas\" no menu.");
        } else {
            lista.replaceChildren(...vagas.map(criarItemDeVaga));
        }

        desenharGraficoDeCompetencias(candidatos);
        return;
    }

    const vagas = await listarVagasDoRecrutador(usuario.id);

    if (vagas.length === 0) {
        mostrarAviso(lista, "Você ainda não tem vagas atribuídas pela empresa.");
    } else {
        lista.replaceChildren(...vagas.map(criarItemDeVaga));
    }
}

function criarItemDeVaga(vaga: Vaga): HTMLLIElement {
    const item = criarCard(vaga.nome, [
        ["Localização", `${vaga.cidade} - ${vaga.estado}`],
        ["Competências", vaga.competencias.join(", ")]
    ]);
    const link = document.createElement("a");

    item.classList.add("item-vaga");
    link.className = "botao-link";
    link.href = `vagaCandidatos.html?id=${vaga.id}`;
    link.textContent = "Acessar vaga";

    item.appendChild(link);
    return item;
}