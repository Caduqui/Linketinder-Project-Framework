package org.linketinder.spring.nucleo.integracao

import org.linketinder.spring.nucleo.model.CompetenciaDoCandidato

/**
 *
 * @author Guilherme Lima Conte
 */

interface ServicoDeCompetencias {

    List<String> substituirDaVaga(Integer idVaga, List<String> competencias)

    Map<Integer, List<String>> buscarDasVagas(List<Integer> idsVagas)

    void apagarDaVaga(Integer idVaga)

    Map<Integer, List<CompetenciaDoCandidato>> buscarDosCandidatos(List<Integer> idsCandidatos)

    void apagarDoCandidato(Integer idCandidato)
}
