package org.linketinder.spring.nucleo.model

/**
 *
 * @author Guilherme Lima Conte
 */

class CandidatoAnonimo {

    Integer id
    String formacao
    String descricao
    List<CompetenciaDoCandidato> competencias = []

    CandidatoAnonimo(Integer id, String formacao, String descricao) {
        this.id = id
        this.formacao = formacao
        this.descricao = descricao
    }
}
