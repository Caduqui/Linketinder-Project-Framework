import type {TipoUsuario, UsuarioLogado} from "./modelos.ts";
import {encerrarSessao, obterUsuarioLogado} from "./sessao.ts";

/**
 *
 * @author Guilherme Lima Conte
 */

const TIPOS_VALIDOS: TipoUsuario[] = ["candidato", "empresa", "recrutador"];
const PERFIS_VALIDOS: TipoUsuario[] = TIPOS_VALIDOS;
const PASTA_DAS_PAGINAS = "paginas";
export const PAGINA_DE_ENTRADA = "index.html";

export const PAGINA_INICIAL_POR_TIPO: Record<TipoUsuario, string> = {
    candidato: "candidato.html",
    empresa: "minhasVagas.html",
    recrutador: "minhasVagas.html"
};

const PAGINA_POR_BOTAO: { [idBotao: string]: string } = {
    buttonTelaCandidato: "candidato.html",
    buttonTelaMinhasVagas: "minhasVagas.html",
    buttonTelaVaga: "vaga.html",
    buttonTelaRecrutadores: "recrutadores.html"
};

export function lerTipoDaUrl(): TipoUsuario | null {
    const tipo = new URLSearchParams(window.location.search).get("tipo");
    return TIPOS_VALIDOS.find(tipoValido => tipoValido === tipo) ?? null;
}

function estaNaPastaDePaginas(): boolean {
    return window.location.pathname.includes(`/${PASTA_DAS_PAGINAS}/`);
}

function resolverCaminho(pagina: string): string {
    const destinoNaRaiz = pagina === PAGINA_DE_ENTRADA;

    if (estaNaPastaDePaginas()) {
        return destinoNaRaiz ? `../${pagina}` : pagina;
    }

    return destinoNaRaiz ? pagina : `${PASTA_DAS_PAGINAS}/${pagina}`;
}

export function irPara(pagina: string, tipo?: TipoUsuario): void {
    const caminho = resolverCaminho(pagina);
    window.location.href = tipo ? `${caminho}?tipo=${tipo}` : caminho;
}

function perfisExigidosDoTexto(texto: string | undefined): TipoUsuario[] {
    if (!texto) {
        return [];
    }

    return texto.split(",")
        .map(perfil => perfil.trim())
        .filter((perfil): perfil is TipoUsuario => PERFIS_VALIDOS.includes(perfil as TipoUsuario));
}

export function protegerPagina(): boolean {
    const usuario = obterUsuarioLogado();
    const {perfilExigido, somenteDeslogado} = document.body.dataset;

    if (somenteDeslogado !== undefined && usuario) {
        irPara(PAGINA_INICIAL_POR_TIPO[usuario.tipo]);
        return false;
    }

    const perfisExigidos = perfisExigidosDoTexto(perfilExigido);

    if (perfisExigidos.length > 0 && !usuario) {
        irPara("login.html", perfisExigidos[0]);
        return false;
    }

    if (perfisExigidos.length > 0 && usuario && !perfisExigidos.includes(usuario.tipo)) {
        irPara(PAGINA_INICIAL_POR_TIPO[usuario.tipo]);
        return false;
    }

    return true;
}

function deveExibir(visivelPara: string, usuario: UsuarioLogado | null): boolean {
    const perfisVisiveis = visivelPara.split(",").map(perfil => perfil.trim()).filter(Boolean);

    if (perfisVisiveis.includes("logado")) {
        return usuario !== null;
    }

    if (!usuario) {
        return false;
    }

    return perfisVisiveis.includes(usuario.tipo);
}

export function configurarCabecalho(): void {
    const usuario = obterUsuarioLogado();

    document.querySelectorAll<HTMLElement>("[data-visivel-para]").forEach(elemento => {
        elemento.hidden = !deveExibir(elemento.dataset.visivelPara ?? "", usuario);
    });

    Object.keys(PAGINA_POR_BOTAO).forEach(idBotao => {
        document.getElementById(idBotao)?.addEventListener("click", () => irPara(PAGINA_POR_BOTAO[idBotao]));
    });

    document.getElementById("buttonSair")?.addEventListener("click", () => {
        encerrarSessao();
        irPara(PAGINA_DE_ENTRADA);
    });

    const rotulo = document.getElementById("usuario-logado");
    if (rotulo) {
        rotulo.textContent = usuario ? `Logado como ${usuario.nome} (${usuario.tipo})` : "";
    }
}