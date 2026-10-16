import {
    configurarFormularioCandidato,
    configurarFormularioEmpresa,
    configurarFormularioLogin,
    configurarFormularioVaga
} from "./formularios";
import {descreverErro, mostrarMensagem} from "./dom.ts";
import {configurarFormularioCompetencias, mostrarCompetencias} from "./telas/telaCompetencia.ts";
import {configurarCabecalho, protegerPagina} from "./navegacao.ts";
import {configurarTelaDeAcesso} from "./telas/telaAcesso.ts";
import {mostrarEmpresaDaVaga} from "./telas/telaVaga.ts";
import {carregarCandidatosDaVaga} from "./telas/telaCandidatosDaVaga.ts";
import {carregarVagasParaCandidato} from "./telas/telaCandidato.ts";
import {carregarMinhasVagas} from "./telas/telaMinhasVagas.ts";
import {carregarRecrutadores, configurarFormularioRecrutador} from "./telas/telaRecrutadores.ts";

/**
 *
 * @author Guilherme Lima Conte
 */

function carregarSePresente(idElemento: string, carregar: () => Promise<void>): void {
    if (!document.getElementById(idElemento)) {
        return;
    }

    carregar().catch(erro => mostrarMensagem(descreverErro(erro)));
}

export function inicializarApp(): void {

    if (!protegerPagina()) {
        return;
    }

    configurarCabecalho();
    configurarTelaDeAcesso();

    configurarFormularioCandidato();
    configurarFormularioEmpresa();
    configurarFormularioVaga();
    configurarFormularioLogin();
    configurarFormularioRecrutador();
    configurarFormularioCompetencias();
    mostrarEmpresaDaVaga();

    carregarSePresente("lista-competencias", mostrarCompetencias);
    carregarSePresente("painel-candidato", carregarVagasParaCandidato);
    carregarSePresente("lista-minhas-vagas", carregarMinhasVagas);
    carregarSePresente("lista-recrutadores", carregarRecrutadores);
    carregarSePresente("painel-candidatos-da-vaga", carregarCandidatosDaVaga);
}

document.addEventListener("DOMContentLoaded", inicializarApp);