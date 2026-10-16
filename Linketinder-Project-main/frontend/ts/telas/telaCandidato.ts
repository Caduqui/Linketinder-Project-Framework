import type {Vaga} from "../modelos.ts";
import {obterUsuarioDoTipo} from "../sessao.ts";
import {buscarCandidato, curtirVaga, listarVagas} from "../api/api.ts";
import {calcularAfinidade, type ComAfinidade, ordenarPorAfinidade} from "../afinidade.ts";
import {iniciarFilaDeCards} from "../filaDeCards.ts";

export async function carregarVagasParaCandidato(): Promise<void> {
    const usuario = obterUsuarioDoTipo("candidato");

    if (!usuario) {
        return;
    }

    const [candidato, vagas] = await Promise.all([buscarCandidato(usuario.id), listarVagas()]);

    const saudacao = document.getElementById("saudacao-candidato");
    if (saudacao) {
        saudacao.textContent = `Olá, ${candidato.nome}! Suas competências escolhidas são: ${candidato.competencias.join(", ") || "nenhuma cadastrada"}`;
    }

    const vagasOrdenadas = ordenarPorAfinidade(vagas, vaga => calcularAfinidade(candidato.competencias, vaga.competencias));

    iniciarFilaDeCards<ComAfinidade<Vaga>>({
        itens: vagasOrdenadas,
        montarCard: ({item: vaga, afinidade}) => ({
            titulo: vaga.nome,
            afinidade: afinidade,
            detalhes: [
                ["Descrição", vaga.descricao],
                ["Localização", `${vaga.cidade} - ${vaga.estado}`],
                ["Competências", vaga.competencias.join(", ")]
            ]
        }),
        curtir: async ({item: vaga}) => (await curtirVaga(candidato.id, vaga.id)).match,
        mensagemDeMatch: ({item: vaga}) => `Deu match com a vaga "${vaga.nome}"! A empresa também se interessou por você.`,
        mensagemFinal: "Você já viu todas as vagas disponíveis. Volte mais tarde!"
    });
}