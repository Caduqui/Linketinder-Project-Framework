package org.linketinder.curtidas.http

import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import org.linketinder.curtidas.dao.CurtidaDAO
import org.linketinder.curtidas.dao.MatchDAO
import org.linketinder.curtidas.dao.ParticipanteDAO
import org.linketinder.curtidas.repositorio.CurtidaRepositorio
import org.linketinder.curtidas.repositorio.MatchRepositorio
import org.linketinder.curtidas.repositorio.ParticipanteRepositorio
import spock.lang.Specification

/**
 *
 * @author Guilherme Lima Conte
 */

@MicronautTest
class CurtidaControllerSpec extends Specification {

    @Inject
    @Client("/")
    HttpClient cliente

    @Inject
    ParticipanteRepositorio participanteRepositorio

    @Inject
    MatchRepositorio matchRepositorio

    @MockBean(ParticipanteDAO)
    ParticipanteRepositorio participanteRepositorioFalso() {
        return Mock(ParticipanteRepositorio)
    }

    @MockBean(CurtidaDAO)
    CurtidaRepositorio curtidaRepositorioFalso() {
        return Mock(CurtidaRepositorio)
    }

    @MockBean(MatchDAO)
    MatchRepositorio matchRepositorioFalso() {
        return Mock(MatchRepositorio)
    }

    def setup() {
        participanteRepositorio.candidatoExiste(1) >> true
        participanteRepositorio.buscarEmpresaDaVaga(10) >> 3
    }

    def "POST /curtidas responde 201 só com o campo match"() {
        given:
        matchRepositorio.criarMatchSePossivel(1, 3, 10) >> true

        when:
        HttpResponse<Map> resposta = cliente.toBlocking().exchange(
                HttpRequest.POST("/curtidas", [tipo: "empresa", idEmpresa: 3, idCandidato: 1, idVaga: 10]), Map)

        then:
        resposta.status == HttpStatus.CREATED
        resposta.body() == [match: true]
    }

    def "POST /curtidas responde #status para #caso"() {
        when:
        cliente.toBlocking().exchange(HttpRequest.POST("/curtidas", corpo), Map)

        then:
        HttpClientResponseException erro = thrown()
        erro.status == status

        where:
        caso                     | corpo                                                               | status
        "candidato inexistente"  | [tipo: "candidato", idCandidato: 99, idVaga: 10]                     | HttpStatus.NOT_FOUND
        "vaga inexistente"       | [tipo: "candidato", idCandidato: 1, idVaga: 99]                      | HttpStatus.NOT_FOUND
        "tipo inválido"          | [tipo: "administrador", idCandidato: 1, idVaga: 10]                  | HttpStatus.BAD_REQUEST
        "vaga de outra empresa"  | [tipo: "empresa", idEmpresa: 4, idCandidato: 1, idVaga: 10]          | HttpStatus.BAD_REQUEST
        "idVaga ausente"         | [tipo: "candidato", idCandidato: 1]                                  | HttpStatus.BAD_REQUEST
        "tipo ausente"           | [idCandidato: 1, idVaga: 10]                                         | HttpStatus.BAD_REQUEST
    }

    def "GET /curtidas responde 405 com Allow: POST"() {
        when:
        cliente.toBlocking().exchange(HttpRequest.GET("/curtidas"), Map)

        then:
        HttpClientResponseException erro = thrown()
        erro.status == HttpStatus.METHOD_NOT_ALLOWED
        erro.response.header("Allow") == "POST"
    }
}
