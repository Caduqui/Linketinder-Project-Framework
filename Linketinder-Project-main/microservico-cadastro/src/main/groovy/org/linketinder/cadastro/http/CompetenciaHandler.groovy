package org.linketinder.cadastro.http

import org.linketinder.cadastro.service.CompetenciaDoCandidatoService
import org.linketinder.cadastro.service.DadosInvalidosException

/**
 *
 * @author Guilherme Lima Conte
 */

class CompetenciaHandler implements Manipulador {

    static final int SUCESSO = 200
    static final String PARAMETRO_ID = "id"

    private final CompetenciaDoCandidatoService competenciaDoCandidatoService

    CompetenciaHandler(CompetenciaDoCandidatoService competenciaDoCandidatoService) {
        this.competenciaDoCandidatoService = competenciaDoCandidatoService
    }

    @Override
    RespostaJson atender(Requisicao requisicao) {
        Integer idCandidato = lerId(requisicao.parametros[PARAMETRO_ID])
        List<String> competencias = competenciaDoCandidatoService.adicionar(idCandidato, CorpoJson.ler(requisicao.corpo))

        return RespostaJson.de(SUCESSO, [id: idCandidato, competencias: competencias])
    }

    private static Integer lerId(String texto) {
        try {
            return Integer.parseInt(texto)
        } catch (NumberFormatException e) {
            throw new DadosInvalidosException("O id do candidato na URL deve ser um número")
        }
    }
}
