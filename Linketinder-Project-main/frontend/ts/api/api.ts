import type {
    Candidato,
    Competencia,
    Credenciais,
    Empresa,
    NovaEmpresa,
    NovaVaga,
    NovoCandidato,
    NovoRecrutador,
    Recrutador,
    ResultadoCurtida,
    UsuarioLogado,
    Vaga
} from "../modelos.ts";
import {msCompetencias, msCurtidas, msUsuarios, msVagas} from "./http.ts";

export function cadastrarCandidato(candidato: NovoCandidato): Promise<Candidato> {
    return msUsuarios.post<Candidato>("/candidatos", candidato);
}

export function buscarCandidato(id: number): Promise<Candidato> {
    return msUsuarios.get<Candidato>(`/candidatos/${id}`);
}

export function listarCandidatos(): Promise<Candidato[]> {
    return msUsuarios.get<Candidato[]>("/candidatos");
}

export function cadastrarEmpresa(empresa: NovaEmpresa): Promise<Empresa> {
    return msUsuarios.post<Empresa>("/empresas", empresa);
}

export function listarEmpresas(): Promise<Empresa[]> {
    return msUsuarios.get<Empresa[]>("/empresas");
}

export function cadastrarVaga(vaga: NovaVaga): Promise<Vaga> {
    return msVagas.post<Vaga>("/vagas", vaga);
}

export function listarVagas(): Promise<Vaga[]> {
    return msVagas.get<Vaga[]>("/vagas");
}

export function listarVagasDaEmpresa(idEmpresa: number): Promise<Vaga[]> {
    return msVagas.get<Vaga[]>(`/vagas?idEmpresa=${idEmpresa}`);
}

export function listarCompetencias(): Promise<Competencia[]> {
    return msCompetencias.get<Competencia[]>("/competencias");
}

export function inserirCompetencias(idCandidato: number, competencias: string[]): Promise<Candidato> {
    return msCompetencias.post<Candidato>("/competencias", {idCandidato, competencias});
}

export function cadastrarRecrutador(recrutador: NovoRecrutador): Promise<Recrutador> {
    return msUsuarios.post<Recrutador>("/recrutadores", recrutador);
}

export function listarRecrutadoresDaEmpresa(idEmpresa: number): Promise<Recrutador[]> {
    return msUsuarios.get<Recrutador[]>(`/recrutadores?idEmpresa=${idEmpresa}`);
}

export function excluirRecrutador(idRecrutador: number): Promise<void> {
    return msUsuarios.del<void>(`/recrutadores/${idRecrutador}`);
}

export function listarVagasDoRecrutador(idRecrutador: number): Promise<Vaga[]> {
    return msUsuarios.get<Vaga[]>(`/recrutadores/${idRecrutador}/vagas`);
}

export function atribuirVagaAoRecrutador(idRecrutador: number, idVaga: number): Promise<Vaga[]> {
    return msUsuarios.post<Vaga[]>(`/recrutadores/${idRecrutador}/vagas`, {idVaga});
}

export function removerVagaDoRecrutador(idRecrutador: number, idVaga: number): Promise<void> {
    return msUsuarios.del<void>(`/recrutadores/${idRecrutador}/vagas/${idVaga}`);
}

export function fazerLogin(credenciais: Credenciais): Promise<UsuarioLogado> {
    return msUsuarios.post<UsuarioLogado>("/login", credenciais);
}

export function curtirVaga(idCandidato: number, idVaga: number): Promise<ResultadoCurtida> {
    return msCurtidas.post<ResultadoCurtida>("/curtidas", {tipo: "candidato", idCandidato, idVaga});
}

export function curtirCandidato(idEmpresa: number, idCandidato: number, idVaga: number): Promise<ResultadoCurtida> {
    return msCurtidas.post<ResultadoCurtida>("/curtidas", {tipo: "empresa", idEmpresa, idCandidato, idVaga});
}

export function curtirCandidatoComoRecrutador(idRecrutador: number, idCandidato: number, idVaga: number): Promise<ResultadoCurtida> {
    return msCurtidas.post<ResultadoCurtida>("/curtidas", {tipo: "recrutador", idRecrutador, idCandidato, idVaga});
}
