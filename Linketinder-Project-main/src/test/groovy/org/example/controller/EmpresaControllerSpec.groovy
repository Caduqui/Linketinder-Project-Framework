package org.example.controller


import org.example.model.Empresa
import org.example.service.EmpresaService
import org.example.view.EmpresaView
import spock.lang.Specification

/**
 *
 * @author Guilherme Lima Conte
 */

class EmpresaControllerSpec extends Specification{

    EmpresaService empresaService = Mock(EmpresaService)
    EmpresaView empresaView = Mock(EmpresaView)
    EmpresaController empresaController = new EmpresaController(empresaService, empresaView)

    Empresa empresa(Integer id) {
        Empresa empresa = new Empresa(
                "CloudX",
                "contato@cloudx.com",
                "99",
                "Brasil",
                "0",
                "d",
                "123456")
        empresa.id = id
        return empresa
    }

    def "excluir repassa a decisão ao service"() {
        given:
        empresaView.lerId(_) >> 1
        empresaService.buscarPorId(1) >> empresa(1)

        when:
        boolean excluiu = empresaController.excluir()

        then:
        1 * empresaService.excluir(1)
        1 * empresaView.avisarExcluido()
        excluiu
    }

    def "selecionar avisa quando a empresa não existe"() {
        given:
        empresaView.lerId(_) >> 42
        empresaService.buscarPorId(42) >> null

        when:
        Empresa encontrada = empresaController.selecionar("ID: ")

        then:
        1 * empresaView.avisarNaoEncontrado()
        encontrada == null
    }
}
