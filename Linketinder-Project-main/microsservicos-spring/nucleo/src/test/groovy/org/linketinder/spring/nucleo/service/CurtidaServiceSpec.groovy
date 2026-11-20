package org.linketinder.spring.nucleo.service

import org.linketinder.spring.nucleo.repositorio.CurtidaRepositorio
import org.linketinder.spring.nucleo.repositorio.MatchRepositorio
import spock.lang.Specification

/**
 *
 * @author Guilherme Lima Conte
 */

class CurtidaServiceSpec extends Specification {

    CurtidaRepositorio curtidaRepositorio = Mock()
    MatchRepositorio matchRepositorio = Mock()
    CurtidaService service = new CurtidaService(curtidaRepositorio, matchRepositorio)

    def setup() {
        curtidaRepositorio.candidatoExiste(1) >> true
        curtidaRepositorio.buscarEmpresaDaVaga(10) >> 2
        curtidaRepositorio.recrutadorExiste(8) >> true
    }

    def "candidato curte a vaga e o match é tentado com a empresa dona da vaga"() {
        when:
        boolean houveMatch = service.curtir([tipo: "candidato", idCandidato: 1, idVaga: 10])

        then:
        1 * curtidaRepositorio.candidatoCurtirVaga(1, 10) >> 1
        1 * matchRepositorio.criarMatchSePossivel(1, 2, 10) >> 1
        houveMatch
    }

    def "empresa dona da vaga curte o candidato"() {
        when:
        boolean houveMatch = service.curtir([tipo: "empresa", idEmpresa: 2, idCandidato: 1, idVaga: 10])

        then:
        1 * curtidaRepositorio.empresaCurtirCandidato(2, 1, 10) >> 1
        1 * matchRepositorio.criarMatchSePossivel(1, 2, 10) >> 0
        !houveMatch
    }

    def "curtida repetida não é erro e não cria match novo"() {
        when:
        boolean houveMatch = service.curtir([tipo: "candidato", idCandidato: 1, idVaga: 10])

        then:
        1 * curtidaRepositorio.candidatoCurtirVaga(1, 10) >> 0
        1 * matchRepositorio.criarMatchSePossivel(1, 2, 10) >> 0
        !houveMatch
    }

    def "empresa que não é dona da vaga não curte"() {
        when:
        service.curtir([tipo: "empresa", idEmpresa: 7, idCandidato: 1, idVaga: 10])

        then:
        RegraDeNegocioException erro = thrown()
        erro.message == "A vaga 10 não pertence à empresa 7"
        0 * curtidaRepositorio.empresaCurtirCandidato(*_)
        0 * matchRepositorio.criarMatchSePossivel(*_)
    }

    def "recrutador curte o candidato pela vaga atribuída a ele"() {
        given:
        curtidaRepositorio.vagaAtribuidaAoRecrutador(8, 10) >> true

        when:
        boolean houveMatch = service.curtir([tipo: "recrutador", idRecrutador: 8, idCandidato: 1, idVaga: 10])

        then:
        1 * curtidaRepositorio.recrutadorCurtirCandidato(8, 1, 10) >> 1
        1 * matchRepositorio.criarMatchSePossivel(1, 2, 10) >> 1
        houveMatch
    }

    def "recrutador não curte por vaga que não está atribuída a ele"() {
        given:
        curtidaRepositorio.vagaAtribuidaAoRecrutador(8, 10) >> false

        when:
        service.curtir([tipo: "recrutador", idRecrutador: 8, idCandidato: 1, idVaga: 10])

        then:
        RegraDeNegocioException erro = thrown()
        erro.message == "A vaga não está atribuída ao recrutador"
        0 * curtidaRepositorio.recrutadorCurtirCandidato(*_)
    }

    def "curtida com #inexistente inexistente é recurso não encontrado"() {
        when:
        service.curtir(corpo)

        then:
        RecursoNaoEncontradoException erro = thrown()
        erro.message == mensagem
        0 * matchRepositorio.criarMatchSePossivel(*_)

        where:
        inexistente  | corpo                                                              | mensagem
        "candidato"  | [tipo: "candidato", idCandidato: 99, idVaga: 10]                   | "Candidato 99 não encontrado"
        "vaga"       | [tipo: "candidato", idCandidato: 1, idVaga: 99]                    | "Vaga 99 não encontrada"
        "recrutador" | [tipo: "recrutador", idRecrutador: 99, idCandidato: 1, idVaga: 10] | "Recrutador 99 não encontrado"
    }

    def "tipo fora dos três valores é dado inválido, antes de tocar no banco"() {
        when:
        service.curtir([tipo: "administrador", idCandidato: 1, idVaga: 10])

        then:
        DadosInvalidosException erro = thrown()
        erro.message == "O campo 'tipo' deve ser 'candidato', 'empresa' ou 'recrutador'"
        0 * curtidaRepositorio._
    }

    def "curtida de #tipo sem #campo é dado inválido"() {
        when:
        service.curtir([tipo: tipo, idCandidato: 1, idVaga: 10])

        then:
        DadosInvalidosException erro = thrown()
        erro.message.contains("'${campo}'")

        where:
        tipo         | campo
        "empresa"    | "idEmpresa"
        "recrutador" | "idRecrutador"
    }
}
