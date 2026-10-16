import {obterUsuarioDoTipo} from "../sessao.ts";

export function mostrarEmpresaDaVaga(): void {
    const nomeEmpresa = document.getElementById("nome-empresa");
    const empresa = obterUsuarioDoTipo("empresa");

    if (nomeEmpresa && empresa) {
        nomeEmpresa.textContent = empresa.nome
    }
}