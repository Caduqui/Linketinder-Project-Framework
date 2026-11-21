package org.linketinder.spring.competencias.controller

import org.linketinder.spring.competencias.model.Competencia
import org.linketinder.spring.competencias.model.CompetenciaDoCandidato

/**
 *
 * @author Guilherme Lima Conte
 */

class Representacao {

    static Map<String, Object> deCompetencia(Competencia competencia) {
        return [id: competencia.id, nome: competencia.nome] as Map<String, Object>
    }

    static Map<String, Object> deCompetenciasDoCandidato(Integer idCandidato, List<CompetenciaDoCandidato> competencias) {
        return [
                id          : idCandidato,
                competencias: competencias.collect { CompetenciaDoCandidato ligacao ->
                    [nome: ligacao.competencia.nome, nivel: ligacao.nivel]
                }
        ] as Map<String, Object>
    }

    static Map<String, Object> deCompetenciasDaVaga(Integer idVaga, List<String> competencias) {
        return [id: idVaga, competencias: competencias] as Map<String, Object>
    }
}
