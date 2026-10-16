package org.example.controller


import org.example.model.Competencia
import org.example.service.CompetenciaService
import org.example.view.CompetenciaView
import spock.lang.Specification

/**
 *
 * @author Guilherme Lima Conte
 */

class CompetenciaControllerSpec extends Specification{

    CompetenciaService competenciaService = Mock(CompetenciaService)
    CompetenciaView competenciaView = Mock(CompetenciaView)
    CompetenciaController competenciaController = new CompetenciaController(competenciaService, competenciaView)

    def "cadastrar entrega ao service o nome digitado"() {
        given:
        competenciaView.lerNome(_) >> "Kotlin"

        when:
        boolean cadastrou = competenciaController.cadastrar()

        then:
        1 * competenciaService.cadastrar("Kotlin")
        1 * competenciaView.avisarCadastrado()
        cadastrou
    }

    def "atualizar renomeia a competência escolhida"() {
        given:
        Competencia competencia = new Competencia("Java")
        competencia.id = 1
        competenciaView.lerId(_) >> 1
        competenciaService.buscarPorId(1) >> competencia
        competenciaView.lerNome(_) >> "Groovy"

        when:
        boolean atualizou = competenciaController.atualizar()

        then:
        1 * competenciaService.renomear(competencia, "Groovy")
        atualizou
    }
}
