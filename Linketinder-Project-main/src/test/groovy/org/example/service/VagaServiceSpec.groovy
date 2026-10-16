package org.example.service

import org.example.model.Empresa
import org.example.model.Vaga
import org.example.repositorio.VagaRepositorio
import spock.lang.Specification

/**
 *
 * @author Guilherme Lima Conte
 */

class VagaServiceSpec extends Specification{

    VagaRepositorio vagaRepositorio = Mock(VagaRepositorio)
    VagaService vagaService = new VagaService(vagaRepositorio)

    Vaga vaga(Integer id) {
        Empresa empresa = new Empresa("Empresa", "e@x.com", "1", "Brasil", "0", "d", "123456")
        empresa.id = 1
        Vaga vaga = new Vaga("Vaga ${id}", "d", "MT", "Cuiabá", empresa, [])
        vaga.id = id
        return vaga
    }

    def "buscarVagaDaEmpresa devolve a vaga quando ela pertence à empresa"() {
        given:
        vagaRepositorio.listarPorEmpresa(1) >> [vaga(10)]

        expect:
        vagaService.buscarVagaDaEmpresa(1, 10).id == 10
    }

    def "buscarVagaDaEmpresa devolve null para vaga de outra empresa"() {
        given:
        vagaRepositorio.listarPorEmpresa(1) >> [vaga(10)]

        expect:
        vagaService.buscarVagaDaEmpresa(1, 20) == null
    }

    def "atualizar mantém o ID da vaga escolhida"() {
        when:
        vagaService.atualizar(4, vaga(99))

        then:
        1 * vagaRepositorio.atualizar({ Vaga atualizada -> atualizada.id == 4 })
    }
}
