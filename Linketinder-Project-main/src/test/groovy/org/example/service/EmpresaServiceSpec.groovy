package org.example.service

import org.example.repositorio.EmpresaRepositorio
import spock.lang.Specification

/**
 *
 * @author Guilherme Lima Conte
 */

class EmpresaServiceSpec extends Specification{

    EmpresaRepositorio empresaRepositorio = Mock(EmpresaRepositorio)
    EmpresaService empresaService = new EmpresaService(empresaRepositorio)

    def "não exclui empresa que possui vagas"() {
        given:
        empresaRepositorio.possuiVagas(1) >> true

        when:
        empresaService.excluir(1)

        then:
        RegraDeNegocioException erro = thrown()
        erro.message.contains("possui vagas")
        0 * empresaRepositorio.deletar(_)
    }

    def "não exclui empresa que possui recrutadores"() {
        given:
        empresaRepositorio.possuiVagas(1) >> false
        empresaRepositorio.possuiRecrutadores(1) >> true

        when:
        empresaService.excluir(1)

        then:
        RegraDeNegocioException erro = thrown()
        erro.message.contains("recrutadores")
        0 * empresaRepositorio.deletar(_)
    }

    def "exclui empresa sem vagas nem recrutadores"() {
        given:
        empresaRepositorio.possuiVagas(1) >> false
        empresaRepositorio.possuiRecrutadores(1) >> false

        when:
        empresaService.excluir(1)

        then:
        1 * empresaRepositorio.deletar(1)
    }
}
