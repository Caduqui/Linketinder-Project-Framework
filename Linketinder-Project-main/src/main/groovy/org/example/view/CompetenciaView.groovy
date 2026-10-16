package org.example.view

import org.example.model.Competencia

/**
 *
 * @author Guilherme Lima Conte
 */

class CompetenciaView {

    private final EntradaDados entrada

    CompetenciaView(EntradaDados entrada) {
        this.entrada = entrada
    }

    Integer lerId(String mensagem) {
        return entrada.lerId(mensagem)
    }

    String lerNome(String mensagem) {
        return entrada.lerTexto(mensagem)
    }

    void exibir(List<Competencia> competencias) {
        competencias.each {
            Competencia competencia -> println competencia.descrever()
        }
    }

    void avisarNaoEncontrado() {
        println "Competência não encontrada"
    }

    void avisarCadastrado() {
        println "Competência cadastrada com sucesso!"
    }

    void avisarAtualizado() {
        println "Competência atualizada com sucesso!"
    }

    void avisarExcluido() {
        println "Competência excluída com sucesso!"
    }
}
