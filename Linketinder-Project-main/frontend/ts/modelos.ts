/**
 *
 * @author Guilherme Lima Conte
 */

export type TipoUsuario = "candidato" | "empresa" | "recrutador";

export interface Candidato {
    id: number;
    nome: string;
    sobrenome: string;
    dataNascimento: string;
    email: string;
    cpf: string;
    pais: string;
    cep: string;
    descricao: string;
    formacao: string;
    competencias: string[];
}

export type NovoCandidato = Omit<Candidato, "id" | "competencias"> & {senha:string};

export interface Empresa {
    id: number;
    nome: string;
    email: string;
    cnpj: string;
    pais: string;
    cep: string;
    descricao: string;
}

export type NovaEmpresa = Omit<Empresa, "id"> & {senha:string};

export interface Recrutador {
    id: number;
    nome: string;
    area: string;
    descricao: string;
    email: string;
    idEmpresa: number;
}

export interface NovoRecrutador {
    nome: string;
    area: string;
    descricao: string;
    email: string;
    cpf: string;
    dataNascimento: string;
    senha: string;
    idEmpresa: number;
}

export interface Vaga {
    id: number;
    nome: string;
    descricao: string;
    estado: string;
    cidade: string;
    empresa: {id: number; nome: string};
    competencias: string[];
}

export interface NovaVaga {
    nome: string;
    descricao: string;
    estado: string;
    cidade: string;
    idEmpresa: number;
    competencias: string[];
}

export interface Competencia {
    id: number;
    nome: string;
}

export interface Credenciais {
    tipo: TipoUsuario;
    email: string;
    senha: string;
}

interface DadosUsuarioLogado {
    id: number;
    nome: string;
}

export interface UsuarioCandidatoLogado extends DadosUsuarioLogado {
    tipo: "candidato";
}

export interface UsuarioEmpresaLogado extends DadosUsuarioLogado {
    tipo: "empresa";
}

export interface UsuarioRecrutadorLogado extends DadosUsuarioLogado {
    tipo: "recrutador";
    idEmpresa: number;
}

export type UsuarioLogado =
    | UsuarioCandidatoLogado
    | UsuarioEmpresaLogado
    | UsuarioRecrutadorLogado;

export interface ResultadoCurtida {
    match: boolean;
}