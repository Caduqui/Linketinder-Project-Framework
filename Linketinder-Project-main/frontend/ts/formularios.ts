import type {Credenciais, NovaEmpresa, NovaVaga, NovoCandidato} from "./modelos";
import {cadastrarCandidato, cadastrarEmpresa, cadastrarVaga, fazerLogin} from "./api/api.ts";
import {enviarFormulario, lerCampo, mostrarMensagem, separarPorVirgula} from "./dom.ts";
import {iniciarSessao, obterUsuarioDoTipo} from "./sessao.ts";
import {lerTipoDaUrl, PAGINA_INICIAL_POR_TIPO} from "./navegacao.ts";

/**
 *
 * @author Guilherme Lima Conte
 */

function obterFormulario(id: string): HTMLFormElement | null {
    return document.getElementById(id) as HTMLFormElement | null;
}

export function configurarFormularioCandidato(): void {
    const formCandidato = obterFormulario("form-candidato");

    if (!formCandidato) {
        return;
    }

    formCandidato.addEventListener("submit", async (evento) => {
        evento.preventDefault();

        const novoCandidato: NovoCandidato = {
            nome: lerCampo("candidato_nome"),
            sobrenome: lerCampo("candidato_sobrenome"),
            dataNascimento: lerCampo("candidato_dataNascimento"),
            email: lerCampo("candidato_email"),
            cpf: lerCampo("candidato_cpf"),
            pais: lerCampo("candidato_pais"),
            cep: lerCampo("candidato_cep"),
            descricao: lerCampo("candidato_descricao"),
            senha: lerCampo("candidato_senha"),
            formacao: lerCampo("candidato_formacao")
        };

        await enviarFormulario(formCandidato, async () => {
            const candidato = await cadastrarCandidato(novoCandidato);

            iniciarSessao({tipo: "candidato", id: candidato.id, nome: candidato.nome});
            formCandidato.reset();
            window.location.href = "competencias.html";
        });
    });
}

export function configurarFormularioEmpresa(): void {
    const formEmpresa = obterFormulario("form-empresa");

    if (!formEmpresa) {
        return;
    }

    formEmpresa.addEventListener("submit", async (evento) => {
        evento.preventDefault();

        const novaEmpresa: NovaEmpresa = {
            nome: lerCampo("empresa_nome"),
            email: lerCampo("empresa_email"),
            cnpj: lerCampo("empresa_cnpj"),
            pais: lerCampo("empresa_pais"),
            cep: lerCampo("empresa_cep"),
            descricao: lerCampo("empresa_descricao"),
            senha: lerCampo("empresa_senha")
        };

        await enviarFormulario(formEmpresa, async () => {
            const empresa = await cadastrarEmpresa(novaEmpresa);

            iniciarSessao({tipo: "empresa", id: empresa.id, nome: empresa.nome});
            formEmpresa.reset();
            window.location.href = "vaga.html";
        });
    });
}

export function configurarFormularioVaga(): void {
    const formVaga = obterFormulario("form-vaga");

    if (!formVaga) {
        return;
    }

    formVaga.addEventListener("submit", async (evento) => {
        evento.preventDefault();

        const empresa = obterUsuarioDoTipo("empresa");

        if (!empresa) {
            mostrarMensagem("Faça login como empresa para cadastrar vagas.");
            return;
        }

        const novaVaga: NovaVaga = {
            nome: lerCampo("vaga_nome"),
            descricao: lerCampo("vaga_descricao"),
            estado: lerCampo("vaga_estado").toUpperCase(),
            cidade: lerCampo("vaga_cidade"),
            idEmpresa: empresa.id,
            competencias: separarPorVirgula(lerCampo("vaga_competencias"))
        };

        await enviarFormulario(formVaga, async () => {
            await cadastrarVaga(novaVaga);

            formVaga.reset();
            window.location.href = "minhasVagas.html";
        });
    });
}

export function configurarFormularioLogin(): void {
    const formLogin = obterFormulario("form-login");

    if (!formLogin) {
        return;
    }

    formLogin.addEventListener("submit", async (evento) => {
        evento.preventDefault();

        const tipo = lerTipoDaUrl();

        if (!tipo) {
            mostrarMensagem("Escolha se você é candidato, empresa ou recrutador antes de entrar.");
            return;
        }

        const credenciais: Credenciais = {
            tipo: tipo,
            email: lerCampo("login_email"),
            senha: lerCampo("login_senha")
        };

        await enviarFormulario(formLogin, async () => {
            const usuario = await fazerLogin(credenciais);

            iniciarSessao(usuario);
            window.location.href = PAGINA_INICIAL_POR_TIPO[usuario.tipo];
        });
    });
}