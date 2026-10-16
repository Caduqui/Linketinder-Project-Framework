import {irPara, lerTipoDaUrl, PAGINA_DE_ENTRADA} from "../navegacao.ts";

export function configurarTelaDeAcesso(): void {
    if (!document.getElementById("tela-acesso")) {
        return;
    }

    const tipo = lerTipoDaUrl();

    if (!tipo) {
        irPara(PAGINA_DE_ENTRADA);
        return;
    }

    document.querySelectorAll<HTMLElement>("[data-tipo]").forEach(elemento => {
        const tiposVisiveis = elemento.dataset.tipo?.split(",") ?? [];
        elemento.hidden = !tiposVisiveis.includes(tipo);
    });

    document.querySelectorAll<HTMLAnchorElement>("a[data-pagina]").forEach(link => {
        link.href = `${link.dataset.pagina}?tipo=${tipo}`;
    });
}