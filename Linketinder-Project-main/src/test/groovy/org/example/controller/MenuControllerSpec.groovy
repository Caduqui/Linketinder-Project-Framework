package org.example.controller

import org.example.service.RegraDeNegocioException
import org.example.view.MenuView
import spock.lang.Specification

/**
 *
 * @author Guilherme Lima Conte
 */
class MenuControllerSpec extends Specification {

    MenuView menuView = Mock()
    CandidatoController candidatoController = Mock()
    EmpresaController empresaController = Mock()
    CompetenciaController competenciaController = Mock()
    VagaController vagaController = Mock()
    CurtidaController curtidaController = Mock()
    RecrutadorController recrutadorController = Mock()

    MenuController menuController = new MenuController(
            menuView,
            candidatoController,
            empresaController,
            competenciaController,
            vagaController,
            curtidaController,
            recrutadorController
    )

    def "menu de recrutadores exibe erro de regra de negócio sem interromper navegação"() {
        when:
        menuController.executar()

        then:
        4 * menuView.lerOpcao(_, _, _) >> "5" >> "2" >> "0" >> "0"
        1 * recrutadorController.cadastrar() >> {
            throw new RegraDeNegocioException("Dados inválidos")
        }
        1 * menuView.exibirMensagem("Dados inválidos")
    }

    def "menu de curtidas encaminha a opção de curtida do recrutador"() {
        when:
        menuController.executar()

        then:
        4 * menuView.lerOpcao(_, _, _) >> "6" >> "3" >> "0" >> "0"
        1 * curtidaController.recrutadorCurtirCandidato()
    }
}
