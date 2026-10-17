package org.linketinder.curtidas.service

import org.linketinder.curtidas.model.Curtida
import org.linketinder.curtidas.repositorio.CurtidaRepositorio
import org.linketinder.curtidas.repositorio.MatchRepositorio
import org.linketinder.curtidas.repositorio.ParticipanteRepositorio
import spock.lang.Specification

/**
 *
 * @author Guilherme Lima Conte
 */

class CurtidaServiceSpec extends Specification {

    ParticipanteRepositorio participanteRepositorio = Mock()
    CurtidaRepositorio curtidaRepositorio = Mock()
    MatchRepositorio matchRepositorio = Mock()
    CurtidaService service = new CurtidaService(participanteRepositorio, curtidaRepositorio, matchRepositorio)

    def setup() {
        participanteRepositorio.candidatoExiste(1) >> true
        participanteRepositorio.buscarEmpresaDaVaga(10) >> 3
        participanteRepositorio.buscarEmpresaDoRecrutador(5) >> 3
        participanteRepositorio.buscarEmpresaDoRecrutador(6) >> 4
    }

    def "candidato curte a vaga e o service informa se fechou match"() {
        when:
        boolean match = service.curtir(new Curtida(tipo: "candidato", idCandidato: 1, idVaga: 10))

        then:
        1 * curtidaRepositorio.candidatoCurtirVaga(1, 10) >> true
        1 * matchRepositorio.criarMatchSePossivel(1, 3, 10) >> true
        match
    }

    def "empresa curte candidato para vaga própria"() {
        when:
        boolean match = service.curtir(new Curtida(tipo: "empresa", idCandidato: 1, idVaga: 10, idEmpresa: 3))

        then:
        1 * curtidaRepositorio.empresaCurtirCandidato(3, 1, 10) >> true
        1 * matchRepositorio.criarMatchSePossivel(1, 3, 10) >> false
        !match
    }

    def "empresa não curte com vaga de outra empresa"() {
        when:
        service.curtir(new Curtida(tipo: "empresa", idCandidato: 1, idVaga: 10, idEmpresa: 4))

        then:
        RegraDeNegocioException erro = thrown()
        erro.message == "A vaga 10 não pertence à empresa 4"
        0 * curtidaRepositorio.empresaCurtirCandidato(_, _, _)
        0 * matchRepositorio.criarMatchSePossivel(_, _, _)
    }

    def "recrutador curte candidato para vaga atribuída a ele"() {
        given:
        participanteRepositorio.vagaAtribuidaAoRecrutador(5, 10) >> true

        when:
        service.curtir(new Curtida(tipo: "recrutador", idCandidato: 1, idVaga: 10, idRecrutador: 5))

        then:
        1 * curtidaRepositorio.recrutadorCurtirCandidato(5, 1, 10)
        1 * matchRepositorio.criarMatchSePossivel(1, 3, 10)
    }

    def "recrutador é recusado fora do escopo: #caso"() {
        given:
        participanteRepositorio.vagaAtribuidaAoRecrutador(5, 10) >> false

        when:
        service.curtir(new Curtida(tipo: "recrutador", idCandidato: 1, idVaga: 10, idRecrutador: idRecrutador))

        then:
        RegraDeNegocioException erro = thrown()
        erro.message == mensagem
        0 * curtidaRepositorio.recrutadorCurtirCandidato(_, _, _)

        where:
        caso                   | idRecrutador | mensagem
        "vaga de outra empresa" | 6            | CurtidaService.VAGA_DE_OUTRA_EMPRESA_DO_RECRUTADOR
        "vaga não atribuída"   | 5            | CurtidaService.VAGA_NAO_ATRIBUIDA
    }

    def "candidato, vaga ou recrutador inexistente é recurso não encontrado"() {
        given:
        participanteRepositorio.candidatoExiste(99) >> false
        participanteRepositorio.buscarEmpresaDaVaga(99) >> null
        participanteRepositorio.buscarEmpresaDoRecrutador(99) >> null

        when:
        service.curtir(curtida)

        then:
        RecursoNaoEncontradoException erro = thrown()
        erro.message == mensagem

        where:
        curtida                                                                         | mensagem
        new Curtida(tipo: "candidato", idCandidato: 99, idVaga: 10)                      | "Candidato 99 não encontrado"
        new Curtida(tipo: "candidato", idCandidato: 1, idVaga: 99)                       | "Vaga 99 não encontrada"
        new Curtida(tipo: "recrutador", idCandidato: 1, idVaga: 10, idRecrutador: 99)    | "Recrutador 99 não encontrado"
    }

    def "tipo inválido ou id do autor ausente é regra de negócio"() {
        when:
        service.curtir(curtida)

        then:
        RegraDeNegocioException erro = thrown()
        erro.message == mensagem

        where:
        curtida                                                    | mensagem
        new Curtida(tipo: "administrador", idCandidato: 1, idVaga: 10) | "O campo 'tipo' deve ser 'candidato', 'empresa' ou 'recrutador'"
        new Curtida(tipo: "empresa", idCandidato: 1, idVaga: 10)       | "O campo 'idEmpresa' é obrigatório e deve ser um número"
        new Curtida(tipo: "recrutador", idCandidato: 1, idVaga: 10)    | "O campo 'idRecrutador' é obrigatório e deve ser um número"
    }

    def "curtida repetida não é erro e não fecha match novo"() {
        when:
        boolean match = service.curtir(new Curtida(tipo: "candidato", idCandidato: 1, idVaga: 10))

        then:
        1 * curtidaRepositorio.candidatoCurtirVaga(1, 10) >> false
        1 * matchRepositorio.criarMatchSePossivel(1, 3, 10) >> false
        !match
    }
}
