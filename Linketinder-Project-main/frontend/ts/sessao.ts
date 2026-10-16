import type {TipoUsuario, UsuarioLogado} from "./modelos.ts";

/**
 *
 * @author Guilherme Lima Conte
 */

const CHAVE_USUARIO = "usuariologado";

export function iniciarSessao(usuario: UsuarioLogado): void {
    sessionStorage.setItem(CHAVE_USUARIO, JSON.stringify(usuario));
}

export function obterUsuarioLogado(): UsuarioLogado | null {
    const salvo = sessionStorage.getItem(CHAVE_USUARIO);

    if (!salvo) {
        return null
    }

    try {
        return JSON.parse(salvo) as UsuarioLogado;
    } catch {
        encerrarSessao();
        return null;
    }
}

export function obterUsuarioDoTipo(tipo: "candidato"): Extract<UsuarioLogado, {tipo: "candidato"}> | null;
export function obterUsuarioDoTipo(tipo: "empresa"): Extract<UsuarioLogado, {tipo: "empresa"}> | null;
export function obterUsuarioDoTipo(tipo: "recrutador"): Extract<UsuarioLogado, {tipo: "recrutador"}> | null;
export function obterUsuarioDoTipo(tipo: TipoUsuario): UsuarioLogado | null {
    const usuario = obterUsuarioLogado();
    return usuario?.tipo === tipo ? usuario : null;
}

export function encerrarSessao(): void {
    sessionStorage.removeItem(CHAVE_USUARIO);
}