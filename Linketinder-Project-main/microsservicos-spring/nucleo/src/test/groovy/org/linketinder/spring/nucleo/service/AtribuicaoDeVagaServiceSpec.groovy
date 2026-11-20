package org.linketinder.spring.nucleo.service

import org.linketinder.spring.nucleo.integracao.ServicoDeCompetencias
import org.linketinder.spring.nucleo.model.Vaga
import org.linketinder.spring.nucleo.repositorio.AtribuicaoDeVagaRepositorio
import org.linketinder.spring.nucleo.repositorio.EmpresaRepositorio
import org.linketinder.spring.nucleo.repositorio.VagaRepositorio
import spock.lang.Specification

/**
 *
 * @author Guilherme Lima Conte
 */

class AtribuicaoDeVagaServiceSpec extends Specification {

    AtribuicaoDeVagaRepositorio atribuicaoRepositorio = Mock()
    VagaRepositorio vagaRepositorio = Stub()
    ServicoDeCompetencias servicoDeCompetencias = Stub()
    VagaService vagaService = new VagaService(new ValidadorDeVaga(), vagaRepositorio, Stub(EmpresaRepositorio), servicoDeCompetencias)
    AtribuicaoDeVagaService service = new AtribuicaoDeVagaService(atribuicaoRepositorio, vagaService)

    def setup() {
        atribuicaoRepositorio.buscarEmpresaDoRecrutador(8) >> 2
        atribuicaoRepositorio.buscarEmpresaDoRecrutador(99) >> null
        vagaRepositorio.findById(10) >> Optional.of(new Vaga(id: 10, idEmpresa: 2))
        vagaRepositorio.findById(20) >> Optional.of(new Vaga(id: 20, idEmpresa: 7))
        vagaRepositorio.findById(30) >> Optional.empty()
    }

    def "atribuir vaga da mesma empresa grava o vínculo e devolve as vagas do recrutador com as competências"() {
        given:
        vagaRepositorio.listarDoRecrutador(8) >> [new Vaga(id: 10)]
        servicoDeCompetencias.buscarDasVagas([10]) >> [10: ["Java"]]

        when:
        List<Vaga> resultado = service.atribuir(8, [idVaga: 10])

        then:
        1 * atribuicaoRepositorio.atribuir(8, 10)
        resultado*.id == [10]
        resultado[0].competencias == ["Java"]
    }

    def "atribuir vaga de outra empresa é recusado"() {
        when:
        service.atribuir(8, [idVaga: 20])

        then:
        RegraDeNegocioException erro = thrown()
        erro.message == "A vaga não pertence à empresa do recrutador"
        0 * atribuicaoRepositorio.atribuir(_, _)
    }

    def "atribuir a recrutador ou vaga inexistente é recurso não encontrado"() {
        when:
        service.atribuir(idRecrutador, [idVaga: idVaga])

        then:
        RecursoNaoEncontradoException erro = thrown()
        erro.message == mensagem

        where:
        idRecrutador | idVaga | mensagem
        99           | 10     | "Recrutador 99 não encontrado"
        8            | 30     | "Vaga 30 não encontrada"
    }

    def "atribuir sem idVaga numérico é dado inválido"() {
        when:
        service.atribuir(8, [idVaga: "10"])

        then:
        DadosInvalidosException erro = thrown()
        erro.message.contains("'idVaga'")
    }

    def "remover vaga atribuída desliga o recrutador dos matches, apaga as curtidas dele na vaga e o vínculo"() {
        given:
        atribuicaoRepositorio.estaAtribuida(8, 10) >> true

        when:
        service.remover(8, 10)

        then:
        1 * atribuicaoRepositorio.desvincularDosMatches(8, 10)
        1 * atribuicaoRepositorio.apagarCurtidas(8, 10)

        then:
        1 * atribuicaoRepositorio.remover(8, 10)
    }

    def "remover vaga que não está atribuída é recusado"() {
        given:
        atribuicaoRepositorio.estaAtribuida(8, 10) >> false

        when:
        service.remover(8, 10)

        then:
        RegraDeNegocioException erro = thrown()
        erro.message == "A vaga não está atribuída ao recrutador"
        0 * atribuicaoRepositorio.remover(_, _)
    }

    def "listar vagas de recrutador inexistente é recurso não encontrado"() {
        when:
        service.listar(99)

        then:
        thrown(RecursoNaoEncontradoException)
    }
}
