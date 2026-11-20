package org.linketinder.spring.nucleo.service

import org.linketinder.spring.nucleo.DadosFicticios
import org.linketinder.spring.nucleo.model.Recrutador
import org.linketinder.spring.nucleo.repositorio.EmpresaRepositorio
import org.linketinder.spring.nucleo.repositorio.RecrutadorRepositorio
import spock.lang.Specification

import java.time.Clock

/**
 *
 * @author Guilherme Lima Conte
 */

class RecrutadorServiceSpec extends Specification {

    RecrutadorRepositorio recrutadorRepositorio = Mock()
    EmpresaRepositorio empresaRepositorio = Stub()
    RecrutadorService service = new RecrutadorService(new ValidadorDeRecrutador(Clock.systemUTC()), recrutadorRepositorio, empresaRepositorio)

    def "cadastrar grava o recrutador de uma empresa existente"() {
        given:
        empresaRepositorio.existsById(2) >> true

        when:
        Recrutador recrutador = service.cadastrar(DadosFicticios.recrutador())

        then:
        1 * recrutadorRepositorio.existeComEmailOuCpf("recrutadora@example.com", "987.654.321-00") >> false
        1 * recrutadorRepositorio.save(_) >> { Recrutador gravado ->
            gravado.id = 8
            return gravado
        }
        recrutador.id == 8
    }

    def "cadastrar recrutador de empresa inexistente é recurso não encontrado"() {
        given:
        empresaRepositorio.existsById(2) >> false

        when:
        service.cadastrar(DadosFicticios.recrutador())

        then:
        RecursoNaoEncontradoException erro = thrown()
        erro.message == "Empresa 2 não encontrada"
        0 * recrutadorRepositorio.save(_)
    }

    def "cadastrar recrutador com e-mail ou CPF repetido é recusado sem dizer qual coincidiu"() {
        given:
        empresaRepositorio.existsById(2) >> true
        recrutadorRepositorio.existeComEmailOuCpf(_, _) >> true

        when:
        service.cadastrar(DadosFicticios.recrutador())

        then:
        RegraDeNegocioException erro = thrown()
        erro.message == "Já existe um recrutador com esses dados"
        0 * recrutadorRepositorio.save(_)
    }

    def "listar por empresa devolve os recrutadores dela"() {
        given:
        empresaRepositorio.existsById(2) >> true
        List<Recrutador> recrutadores = [new Recrutador(id: 1), new Recrutador(id: 2)]
        recrutadorRepositorio.listarDaEmpresa(2) >> recrutadores

        expect:
        service.listarPorEmpresa(2) == recrutadores
    }

    def "listar sem informar a empresa é dado inválido"() {
        when:
        service.listarPorEmpresa(null)

        then:
        DadosInvalidosException erro = thrown()
        erro.message == "O parâmetro 'idEmpresa' é obrigatório e deve ser um número"
    }

    def "listar de empresa inexistente é recurso não encontrado"() {
        given:
        empresaRepositorio.existsById(9) >> false

        when:
        service.listarPorEmpresa(9)

        then:
        thrown(RecursoNaoEncontradoException)
    }

    def "excluir desliga o recrutador dos matches, apaga curtidas e atribuições e depois o recrutador"() {
        given:
        recrutadorRepositorio.existsById(8) >> true

        when:
        service.excluir(8)

        then:
        1 * recrutadorRepositorio.desvincularDosMatches(8)
        1 * recrutadorRepositorio.apagarCurtidas(8)
        1 * recrutadorRepositorio.apagarAtribuicoes(8)

        then:
        1 * recrutadorRepositorio.deleteById(8)
    }

    def "excluir recrutador inexistente não apaga nada"() {
        given:
        recrutadorRepositorio.existsById(8) >> false

        when:
        service.excluir(8)

        then:
        RecursoNaoEncontradoException erro = thrown()
        erro.message == "Recrutador 8 não encontrado"
        0 * recrutadorRepositorio.deleteById(_)
    }
}
