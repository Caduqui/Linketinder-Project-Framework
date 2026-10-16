package org.example.view

import org.example.model.Match
import org.example.model.Vaga

/**
 *
 * @author Guilherme Lima Conte
 */

class CurtidaView {

    void exibirVagasDisponiveis() {
        println "\nVagas Disponíveis: "
    }

    void avisarVagaCurtida() {
        println "\nVaga curtida com sucesso"
    }

    void avisarCurtidaRepetidaDoCandidato() {
        println "O candidato já curtiu essa vaga"
    }

    void avisarCandidatoCurtido(Vaga vaga) {
        println "\nCandidato curtido com sucesso para a vaga ${vaga.nome}!"
    }

    void avisarCurtidaRepetidaDaEmpresa() {
        println "A empresa já curtiu esse candidato para essa vaga"
    }

    void avisarCandidatoCurtidoPorRecrutador(Vaga vaga) {
        println "\nCandidato curtido com sucesso para a vaga ${vaga.nome}!"
    }

    void avisarCurtidaRepetidaDoRecrutador() {
        println "O recrutador já curtiu esse candidato para essa vaga"
    }

    void anunciarMatch() {
        println "\nMATCH!! A curtida entre vocês dois foi recíproca!"
    }

    void anunciarSemMatch() {
        println "Ainda não houve match"
    }

    void exibirMatches(List<Match> matches) {
        if (matches.isEmpty()) {
            println "\nNenhum match ocorreu até o momento."
            return
        }

        println "\nCandidatos e vagas que deram match:"
        matches.each {
            Match match -> println "\n${match.descrever()}"
        }
    }

}
