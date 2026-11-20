package org.linketinder.spring.nucleo.service

import org.linketinder.spring.nucleo.DadosFicticios
import org.linketinder.spring.nucleo.integracao.ServicoDeCompetencias
import org.linketinder.spring.nucleo.model.Vaga
import org.linketinder.spring.nucleo.repositorio.EmpresaRepositorio
import org.linketinder.spring.nucleo.repositorio.VagaRepositorio
import spock.lang.Specification

/**
 *
 * @author Guilherme Lima Conte
 */

class VagaServiceSpec extends Specification {

    VagaRepositorio vagaRepositorio = Mock()
    EmpresaRepositorio empresaRepositorio = Stub()
    ServicoDeCompetencias servicoDeCompetencias = Mock()
    VagaService service = new VagaService(new ValidadorDeVaga(), vagaRepositorio, empresaRepositorio, servicoDeCompetencias)

    def "cadastrar grava a vaga e as competências dela no MS Competências"() {
        given:
        empresaRepositorio.existsById(2) >> true

        when:
        Vaga vaga = service.cadastrar(DadosFicticios.vaga(competencias: ["java", "SQL"]))

        then:
        1 * vagaRepositorio.saveAndFlush({ Vaga gravada -> gravada.idEmpresa == 2 }) >> { Vaga gravada ->
            gravada.id = 10
            return gravada
        }

        then:
        1 * servicoDeCompetencias.substituirDaVaga(10, ["java", "SQL"]) >> ["Java", "SQL"]
        vaga.id == 10
        vaga.competencias == ["Java", "SQL"]
    }

    def "a falha do MS Competências interrompe o cadastro da vaga"() {
        given:
        empresaRepositorio.existsById(2) >> true
        vagaRepositorio.saveAndFlush(_) >> { Vaga gravada ->
            gravada.id = 10
            return gravada
        }
        servicoDeCompetencias.substituirDaVaga(*_) >> { throw new ServicoIndisponivelException() }

        when:
        service.cadastrar(DadosFicticios.vaga())

        then:
        thrown(ServicoIndisponivelException)
    }

    def "cadastrar vaga de empresa inexistente é recurso não encontrado"() {
        given:
        empresaRepositorio.existsById(2) >> false

        when:
        service.cadastrar(DadosFicticios.vaga())

        then:
        RecursoNaoEncontradoException erro = thrown()
        erro.message == "Empresa 2 não encontrada"
        0 * vagaRepositorio.saveAndFlush(_)
        0 * servicoDeCompetencias._
    }

    def "listar sem empresa devolve todas as vagas com as competências de cada uma"() {
        given:
        vagaRepositorio.listarTodas() >> [new Vaga(id: 1), new Vaga(id: 2)]
        servicoDeCompetencias.buscarDasVagas([1, 2]) >> [1: ["Java"]]

        when:
        List<Vaga> vagas = service.listar(null)

        then:
        vagas*.id == [1, 2]
        vagas*.competencias == [["Java"], []]
    }

    def "listar de uma empresa existente devolve as vagas dela"() {
        given:
        empresaRepositorio.existsById(2) >> true
        vagaRepositorio.listarDaEmpresa(2) >> [new Vaga(id: 1, idEmpresa: 2)]
        servicoDeCompetencias.buscarDasVagas([1]) >> [:]

        expect:
        service.listar(2)*.id == [1]
    }

    def "listar de empresa inexistente é recurso não encontrado"() {
        given:
        empresaRepositorio.existsById(9) >> false

        when:
        service.listar(9)

        then:
        thrown(RecursoNaoEncontradoException)
    }

    def "buscar vaga inexistente é recurso não encontrado"() {
        given:
        vagaRepositorio.findById(9) >> Optional.empty()

        when:
        service.buscar(9)

        then:
        RecursoNaoEncontradoException erro = thrown()
        erro.message == "Vaga 9 não encontrada"
    }

    def "atualizar pela empresa dona grava os dados e troca as competências"() {
        given:
        vagaRepositorio.findById(10) >> Optional.of(new Vaga(id: 10, idEmpresa: 2))

        when:
        Vaga vaga = service.atualizar(10, DadosFicticios.vaga(nome: "Outro nome", competencias: ["Java"]))

        then:
        1 * vagaRepositorio.saveAndFlush({ Vaga gravada -> gravada.id == 10 && gravada.nome == "Outro nome" }) >> { Vaga gravada -> gravada }
        1 * servicoDeCompetencias.substituirDaVaga(10, ["Java"]) >> ["Java"]
        vaga.id == 10
    }

    def "atualizar vaga de outra empresa é recusado"() {
        given:
        vagaRepositorio.findById(10) >> Optional.of(new Vaga(id: 10, idEmpresa: 7))

        when:
        service.atualizar(10, DadosFicticios.vaga())

        then:
        RegraDeNegocioException erro = thrown()
        erro.message == "A vaga 10 não pertence à empresa 2"
        0 * vagaRepositorio.saveAndFlush(_)
    }

    def "excluir pela empresa dona apaga as ligações, a vaga e as competências dela"() {
        given:
        vagaRepositorio.findById(10) >> Optional.of(new Vaga(id: 10, idEmpresa: 2))

        when:
        service.excluir(10, 2)

        then:
        1 * vagaRepositorio.apagarCompetenciasDoBancoPrincipal(10)
        1 * vagaRepositorio.apagarCurtidasDeCandidatos(10)
        1 * vagaRepositorio.apagarCurtidasDaEmpresa(10)
        1 * vagaRepositorio.apagarCurtidasDeRecrutadores(10)
        1 * vagaRepositorio.apagarMatches(10)
        1 * vagaRepositorio.apagarAtribuicoes(10)

        then:
        1 * vagaRepositorio.deleteById(10)

        then:
        1 * servicoDeCompetencias.apagarDaVaga(10)
    }

    def "excluir vaga de outra empresa é recusado"() {
        given:
        vagaRepositorio.findById(10) >> Optional.of(new Vaga(id: 10, idEmpresa: 7))

        when:
        service.excluir(10, 2)

        then:
        thrown(RegraDeNegocioException)
        0 * vagaRepositorio.deleteById(_)
        0 * servicoDeCompetencias._
    }

    def "excluir sem informar a empresa é dado inválido"() {
        when:
        service.excluir(10, null)

        then:
        DadosInvalidosException erro = thrown()
        erro.message == "O parâmetro 'idEmpresa' é obrigatório e deve ser um número"
    }
}
