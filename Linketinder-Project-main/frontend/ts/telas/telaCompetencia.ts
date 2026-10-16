import type {Competencia} from "../modelos.ts";
import {inserirCompetencias, listarCompetencias} from "../api/api.ts";
import {obterUsuarioDoTipo} from "../sessao.ts";
import {enviarFormulario, lerCampo, mostrarMensagem, separarPorVirgula} from "../dom.ts";

let competenciasExistentes: Competencia[] = [];

export async function mostrarCompetencias(): Promise<void> {
    const lista = document.getElementById("lista-competencias");

    if (!lista) {
        return;
    }

    competenciasExistentes = await listarCompetencias();
    lista.replaceChildren(...competenciasExistentes.map(criarOpcaoDeCompetencia));
}

function criarOpcaoDeCompetencia(competencia: Competencia): HTMLLabelElement {
    const rotulo = document.createElement("label");
    const caixa = document.createElement("input");

    rotulo.className = "opcao-competencia";
    caixa.type = "checkbox";
    caixa.name = "competencia";
    caixa.value = competencia.nome;

    rotulo.append(caixa, competencia.nome);
    return rotulo;
}

function lerCompetenciasMarcadas(): string[] {
    const caixas = document.querySelectorAll<HTMLInputElement>("input[name='competencia']:checked");
    return Array.from(caixas, caixa => caixa.value);
}

function usarNomeJaCadastrado(nomeDigitado: string): string {
    const existente = competenciasExistentes.find(
        competencia => competencia.nome.toLowerCase() === nomeDigitado.toLowerCase()
    );
    return existente ? existente.nome : nomeDigitado;
}

function juntarSemRepetir(competencias: string[]): string[] {
    const vistas = new Set<string>();

    return competencias.filter(competencia => {
        const chave = competencia.toLowerCase();

        if (vistas.has(chave)) {
            return false;
        }

        vistas.add(chave);
        return true;
    });
}

export function configurarFormularioCompetencias(): void {
    const formCompetencias = document.getElementById("form-competencias") as HTMLFormElement | null;

    if (!formCompetencias) {
        return;
    }

    formCompetencias.addEventListener("submit", async (evento) => {
        evento.preventDefault();

        const candidato = obterUsuarioDoTipo("candidato");

        if (!candidato) {
            mostrarMensagem("Faça login como candidato para inserir competências!");
            return;
        }

        const digitadas = separarPorVirgula(lerCampo("competencias_novas")).map(usarNomeJaCadastrado);
        const competencias = juntarSemRepetir([...lerCompetenciasMarcadas(), ...digitadas]);

        if (competencias.length === 0) {
            mostrarMensagem("Selecione ou digite ao menos uma competência.");
            return;
        }

        await enviarFormulario(formCompetencias, async () => {
            await inserirCompetencias(candidato.id, competencias);
            window.location.href = "candidato.html";
        });
    });
}