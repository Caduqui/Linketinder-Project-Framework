package org.example.view

import org.example.model.Empresa
import org.example.model.Vaga

/**
 *
 * @author Guilherme Lima Conte
 */

class VagaView {

    private final EntradaDados entrada

    VagaView(EntradaDados entrada) {
        this.entrada = entrada
    }

    Integer lerId(String mensagem) {
        return entrada.lerId(mensagem)
    }

    Vaga lerDados(Empresa empresa) {
        String nome = entrada.lerTexto("Nome da vaga: ")
        String descricao = entrada.lerTexto("Descrição: ")
        String estado = entrada.lerSiglaEstado()
        String cidade = entrada.lerTexto("Cidade: ")
        List<String> competencias = entrada.lerCompetencias("Competências exigidas: ")

        return new Vaga(nome, descricao, estado, cidade, empresa, competencias)
    }

    void exibir(List<Vaga> vagas) {
        vagas.each { Vaga vaga ->
            println "\n${vaga.descrever()}"
        }
    }

    void exibirVagasDaEmpresa(Empresa empresa, List<Vaga> vagas) {
        println "\nVagas da empresa ${empresa.nome}:"
        exibir(vagas)
    }

    void avisarNaoEncontrado() {
        println "Vaga não encontrada"
    }

    void avisarEmpresaSemVagas() {
        println "Essa empresa não possui vagas cadastradas"
    }

    void avisarVagaDeOutraEmpresa() {
        println "Essa vaga não pertence a esta empresa"
    }

    void avisarCadastrado() {
        println "Vaga cadastrada com sucesso"
    }

    void avisarAtualizado() {
        println "Vaga atualizada com sucesso"
    }

    void avisarExcluido() {
        println "Vaga excluída com sucesso"
    }
}
