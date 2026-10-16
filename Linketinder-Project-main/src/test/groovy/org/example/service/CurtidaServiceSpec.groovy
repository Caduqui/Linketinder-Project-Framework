package org.example.service

import org.example.repositorio.CurtidaRepositorio
import org.example.repositorio.MatchRepositorio
import spock.lang.Specification

/**
 *
 * @author Guilherme Lima Conte
 */

class CurtidaServiceSpec extends Specification{

    CurtidaRepositorio curtidaRepositorio = Mock(CurtidaRepositorio)
    MatchRepositorio matchRepositorio = Mock(MatchRepositorio)
    RecrutadorService recrutadorService = Mock(RecrutadorService)
    CurtidaService curtidaService = new CurtidaService(curtidaRepositorio, matchRepositorio, recrutadorService)

    def "candidatoCurtirVaga informa quando a curtida é nova"() {
        given:
        curtidaRepositorio.candidatoCurtirVaga(1, 10) >> true

        expect:
        curtidaService.candidatoCurtirVaga(1, 10)
    }

    def "candidatoCurtirVaga informa quando a curtida é repetida"() {
        given:
        curtidaRepositorio.candidatoCurtirVaga(1, 10) >> false

        expect:
        !curtidaService.candidatoCurtirVaga(1, 10)
    }

    def "recrutadorCurtirCandidato informa quando a curtida é nova"() {
        given:
        curtidaRepositorio.recrutadorCurtirCandidato(7, 1, 10) >> true

        when:
        boolean nova = curtidaService.recrutadorCurtirCandidato(7, 1, 10)

        then:
        1 * recrutadorService.validarEscopo(7, 10)
        nova
    }

    def "recrutadorCurtirCandidato informa quando a curtida é repetida"() {
        given:
        curtidaRepositorio.recrutadorCurtirCandidato(7, 1, 10) >> false

        when:
        boolean nova = curtidaService.recrutadorCurtirCandidato(7, 1, 10)

        then:
        1 * recrutadorService.validarEscopo(7, 10)
        !nova
    }

    def "não registra curtida quando o recrutador não tem a vaga atribuída"() {
        when:
        curtidaService.recrutadorCurtirCandidato(7, 1, 10)

        then:
        1 * recrutadorService.validarEscopo(7, 10) >> {
            throw new RegraDeNegocioException("A vaga não está atribuída ao recrutador")
        }
        0 * curtidaRepositorio.recrutadorCurtirCandidato(_, _, _)
        thrown(RegraDeNegocioException)
    }

    def "houveMatch consulta o repositório de matches"() {
        given:
        matchRepositorio.criarMatchSePossivel(1, 5, 10) >> true

        expect:
        curtidaService.houveMatch(1, 5, 10)
    }

    def "listarMatches devolve o que o repositório tem"() {
        given:
        matchRepositorio.listar() >> []

        expect:
        curtidaService.listarMatches().isEmpty()
    }
}
