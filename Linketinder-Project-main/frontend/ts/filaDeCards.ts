/**
 *
 * @author Guilherme Lima Conte
 */

import {descreverErro, limparMensagem, mostrarMensagem} from "./dom.ts";


export interface DadosDoCard {
    titulo: string;
    afinidade: number;
    detalhes: Array<[string, string]>;
}

export interface ConfiguracaoDaFila<T> {
    itens: T[];
    montarCard: (item: T) => DadosDoCard;
    curtir: (item: T) => Promise<boolean>;
    mensagemDeMatch: (item: T) => string;
    mensagemFinal: string;
}

interface ElementosDaFila {
    card: HTMLElement;
    titulo: HTMLElement;
    afinidade: HTMLElement;
    detalhes: HTMLElement;
    acoes: HTMLElement;
    vazio: HTMLElement;
    botaoCurtir: HTMLButtonElement;
    botaoPassar: HTMLButtonElement;
}

function obterElemento<T extends HTMLElement>(id: string): T {
    const elemento = document.getElementById(id);

    if (!elemento) {
        throw new Error(`Elemento #${id} não encontrado na página.`);
    }

    return elemento as T;
}

function obterElementosDaFila(): ElementosDaFila {
    return {
        card: obterElemento("card"),
        titulo: obterElemento("card-titulo"),
        afinidade: obterElemento("card-afinidade"),
        detalhes: obterElemento("card-detalhes"),
        acoes: obterElemento("card-acoes"),
        vazio: obterElemento("fila-vazia"),
        botaoCurtir: obterElemento<HTMLButtonElement>("botao-curtir"),
        botaoPassar: obterElemento<HTMLButtonElement>("botao-passar")
    };
}

function preencherCard(elementos: ElementosDaFila, card: DadosDoCard): void {
    elementos.titulo.textContent = card.titulo;
    elementos.afinidade.textContent = `${card.afinidade}% de afinidade`;

    elementos.detalhes.replaceChildren(...card.detalhes.flatMap(([rotulo, valor]) => {
        const termo = document.createElement("dt");
        const definicao = document.createElement("dd");

        termo.textContent = rotulo;
        definicao.textContent = valor || "Não informado";
        return [termo, definicao];
    }));
}

function mostrarFimDaFila(elementos: ElementosDaFila, mensagem: string): void {
    elementos.card.hidden = true;
    elementos.acoes.hidden = true;
    elementos.vazio.textContent = mensagem;
    elementos.vazio.hidden = false;
}

function alternarBotoes(elementos: ElementosDaFila, habilitados: boolean): void {
    elementos.botaoCurtir.disabled = !habilitados;
    elementos.botaoPassar.disabled = !habilitados;
}

export function iniciarFilaDeCards<T>(configuracao: ConfiguracaoDaFila<T>): void {
    const elementos = obterElementosDaFila();
    let posicao = 0;

    function mostrarCardAtual(): void {
        if (posicao >= configuracao.itens.length) {
            mostrarFimDaFila(elementos, configuracao.mensagemFinal);
            return;
        }

        elementos.card.hidden = false;
        elementos.acoes.hidden = false;
        elementos.vazio.hidden = true;

        preencherCard(elementos, configuracao.montarCard(configuracao.itens[posicao]));
    }

    function passar(): void {
        limparMensagem();
        posicao++;
        mostrarCardAtual();
    }

    async function curtir(): Promise<void> {
        const item = configuracao.itens[posicao];
        alternarBotoes(elementos, false);
        limparMensagem();

        try {
            const houveMatch = await configuracao.curtir(item);
            posicao++;
            mostrarCardAtual();

            if (houveMatch) {
                mostrarMensagem(configuracao.mensagemDeMatch(item), "sucesso");
            }
        } catch (erro) {
            mostrarMensagem(descreverErro(erro));
        } finally {
            alternarBotoes(elementos, true);
        }
    }

    elementos.botaoPassar.onclick = passar;
    elementos.botaoCurtir.onclick = () => void curtir();

    mostrarCardAtual();
}