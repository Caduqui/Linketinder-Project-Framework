package org.example.controller


import org.example.model.Empresa
import org.example.model.Vaga
import org.example.service.VagaService
import org.example.view.VagaView
import spock.lang.Specification

/**
 *
 * @author Guilherme Lima Conte
 */

class VagaControllerSpec extends Specification{

    VagaService vagaService = Mock(VagaService)
    VagaView vagaView = Mock(VagaView)
    EmpresaController empresaController = Mock(EmpresaController)
    VagaController vagaController = new VagaController(vagaService, vagaView, empresaController)

    Empresa empresa(Integer id) {
        Empresa empresa = new Empresa(
                "Empresa ${id}",
                "e@x.com",
                "1",
                "Brasil",
                "0",
                "d",
                "123456"
        )
        empresa.id = id
        return empresa
    }

    Vaga vaga(Integer id, Empresa empresa) {
        Vaga vaga = new Vaga(
                "Vaga ${id}",
                "d",
                "MT",
                "Cuiabá",
                empresa,
                []
        )
        vaga.id = id
        return vaga
    }

    def "cadastrar pede a empresa até encontrar uma válida"() {
        given:
        Empresa empresaValida = empresa(1)
        Vaga digitada = vaga(null, empresaValida)
        vagaView.lerDados(empresaValida) >> digitada

        when:
        boolean cadastrou = vagaController.cadastrar()

        then:
        2 * empresaController.selecionar(_) >>> [null, empresaValida]
        1 * vagaService.cadastrar(digitada)
        cadastrou
    }

    def "selecionarVagaDaEmpresa avisa quando a empresa não tem vagas"() {
        given:
        vagaService.listarPorEmpresa(1) >> []

        when:
        Vaga escolhida = vagaController.selecionarVagaDaEmpresa(empresa(1))

        then:
        1 * vagaView.avisarEmpresaSemVagas()
        escolhida == null
    }

    def "selecionarVagaDaEmpresa avisa quando a vaga é de outra empresa"() {
        given:
        Empresa empresaAtual = empresa(1)
        vagaService.listarPorEmpresa(1) >> [vaga(10, empresaAtual)]
        vagaView.lerId(_) >> 20
        vagaService.buscarVagaDaEmpresa(1, 20) >> null

        when:
        Vaga escolhida = vagaController.selecionarVagaDaEmpresa(empresaAtual)

        then:
        1 * vagaView.avisarVagaDeOutraEmpresa()
        escolhida == null
    }

    def "selecionarVagaDaEmpresa devolve a vaga da própria empresa"() {
        given:
        Empresa empresaAtual = empresa(1)
        Vaga daEmpresa = vaga(10, empresaAtual)
        vagaService.listarPorEmpresa(1) >> [daEmpresa]
        vagaView.lerId(_) >> 10
        vagaService.buscarVagaDaEmpresa(1, 10) >> daEmpresa

        expect:
        vagaController.selecionarVagaDaEmpresa(empresaAtual).id == 10
    }
}
