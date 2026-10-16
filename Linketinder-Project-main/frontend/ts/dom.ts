/**
 *
 * @author Guilherme Lima Conte
 */

type TipoMensagem = "erro" | "sucesso";

export function lerCampo(id: string): string {
    const campo = document.getElementById(id);
    if (!(campo instanceof HTMLInputElement)
        && !(campo instanceof HTMLSelectElement)
        && !(campo instanceof HTMLTextAreaElement)) {
        throw new Error(`Campo #${id} não encontrado na página.`);
    }

    return campo.value.trim();
}

export function separarPorVirgula(texto: string): string[] {
    return texto.split(",").map(item => item.trim()).filter(item => item.length > 0);
}

export function mostrarMensagem(texto: string, tipo: TipoMensagem = "erro"): void {
    const elemento = document.getElementById("mensagem");

    if (!elemento) {
        console.error(texto);
        return;
    }

    elemento.textContent = texto;
    elemento.className = `mensagem mensagem-${tipo}`;
}

export function limparMensagem(): void {
    const elemento = document.getElementById("mensagem");

    if (elemento) {
        elemento.textContent = "";
        elemento.className = "mensagem";
    }
}

export function descreverErro(erro: unknown): string {
    return erro instanceof Error ? erro.message : "Ocorreu um erro inesperado";
}

export async function enviarFormulario(formulario: HTMLFormElement, acao: () => Promise<void>): Promise<void> {
    const botao = formulario.querySelector<HTMLButtonElement>("button[type='submit']");

    limparMensagem();
    if (botao)
        botao.disabled = true;

    try {
        await acao();
    } catch (erro) {
        mostrarMensagem(descreverErro(erro));
    } finally {
        if (botao)
            botao.disabled = false;
    }
}

export function criarCard(titulo: string, linhas: Array<[string, string]>): HTMLLIElement {
    const card = document.createElement("li");
    card.className = "card";

    const elementoTitulo = document.createElement("strong");
    elementoTitulo.textContent = titulo;
    card.appendChild(elementoTitulo);

    linhas.forEach(([rotulo, valor]) => {
        const paragrafo = document.createElement("p");
        const elementoRotulo = document.createElement("strong");

        elementoRotulo.textContent = `${rotulo}: `;
        paragrafo.append(elementoRotulo, valor);
        card.appendChild(paragrafo);
    });

    return card;
}

export function mostrarAviso(lista: HTMLElement, texto: string): void {
    const item = document.createElement("li");

    item.className = "card";
    item.textContent = texto;
    lista.replaceChildren(item);
}