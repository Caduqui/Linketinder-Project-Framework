package org.linketinder.spring.nucleo.integracao

import org.linketinder.spring.nucleo.model.CompetenciaDoCandidato
import org.linketinder.spring.nucleo.service.DadosInvalidosException
import org.linketinder.spring.nucleo.service.ServicoIndisponivelException
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.web.client.RestClient
import spock.lang.Specification

import static org.springframework.test.web.client.match.MockRestRequestMatchers.content
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import static org.springframework.test.web.client.response.MockRestResponseCreators.withNoContent
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess

/**
 *
 * @author Guilherme Lima Conte
 */

class ClienteHttpDeCompetenciasSpec extends Specification {

    static final String ENDERECO = "http://competencias.example"

    RestClient.Builder construtor = RestClient.builder().baseUrl(ENDERECO)
    MockRestServiceServer servidor = MockRestServiceServer.bindTo(construtor).build()
    ClienteHttpDeCompetencias cliente = new ClienteHttpDeCompetencias(construtor.build())

    def "substituirDaVaga envia as competências e devolve os nomes gravados"() {
        given:
        servidor.expect(requestTo("${ENDERECO}/vagas/10/competencias"))
                .andExpect(method(HttpMethod.PUT))
                .andExpect(content().string('{"competencias":["java","SQL"]}'))
                .andRespond(withSuccess('{"id": 10, "competencias": ["Java", "SQL"]}', MediaType.APPLICATION_JSON))

        expect:
        cliente.substituirDaVaga(10, ["java", "SQL"]) == ["Java", "SQL"]
        servidor.verify()
    }

    def "buscarDasVagas pede as competências de várias vagas numa chamada só"() {
        given:
        servidor.expect(requestTo("${ENDERECO}/vagas/competencias?ids=1%2C2"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess('[{"id": 1, "competencias": ["Java"]}, {"id": 2, "competencias": []}]', MediaType.APPLICATION_JSON))

        expect:
        cliente.buscarDasVagas([1, 2]) == [1: ["Java"], 2: []]
    }

    def "buscar sem ids não chama o MS Competências"() {
        expect:
        cliente.buscarDasVagas([]) == [:]
        cliente.buscarDosCandidatos([]) == [:]
        servidor.verify()
    }

    def "buscarDosCandidatos devolve as competências com o nível"() {
        given:
        servidor.expect(requestTo("${ENDERECO}/candidatos/competencias?ids=3"))
                .andRespond(withSuccess('[{"id": 3, "competencias": [{"nome": "Java", "nivel": "AVANCADO"}]}]', MediaType.APPLICATION_JSON))

        when:
        Map<Integer, List<CompetenciaDoCandidato>> competencias = cliente.buscarDosCandidatos([3])

        then:
        competencias[3]*.nome == ["Java"]
        competencias[3]*.nivel == ["AVANCADO"]
    }

    def "apagarDaVaga e apagarDoCandidato chamam o DELETE de cada um"() {
        given:
        servidor.expect(requestTo("${ENDERECO}/vagas/10/competencias")).andExpect(method(HttpMethod.DELETE)).andRespond(withNoContent())
        servidor.expect(requestTo("${ENDERECO}/candidatos/3/competencias")).andExpect(method(HttpMethod.DELETE)).andRespond(withNoContent())

        when:
        cliente.apagarDaVaga(10)
        cliente.apagarDoCandidato(3)

        then:
        servidor.verify()
    }

    def "a recusa do MS Competências vira dado inválido com a mensagem dele"() {
        given:
        servidor.expect(requestTo("${ENDERECO}/vagas/10/competencias"))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body('{"erro": "O campo \'competencias\' deve ter no máximo 50 caracteres"}'))

        when:
        cliente.substituirDaVaga(10, ["x" * 60])

        then:
        DadosInvalidosException erro = thrown()
        erro.message == "O campo 'competencias' deve ter no máximo 50 caracteres"
    }

    def "falha do MS Competências deixa o serviço indisponível"() {
        given:
        servidor.expect(requestTo("${ENDERECO}/vagas/competencias?ids=1")).andRespond(withServerError())

        when:
        cliente.buscarDasVagas([1])

        then:
        ServicoIndisponivelException erro = thrown()
        erro.message == "O serviço de competências está indisponível"
    }

    def "MS Competências fora do ar deixa o serviço indisponível"() {
        given:
        ClienteHttpDeCompetencias semServidor = new ClienteHttpDeCompetencias(RestClient.builder().baseUrl("http://localhost:1").build())

        when:
        semServidor.apagarDaVaga(10)

        then:
        thrown(ServicoIndisponivelException)
    }
}
