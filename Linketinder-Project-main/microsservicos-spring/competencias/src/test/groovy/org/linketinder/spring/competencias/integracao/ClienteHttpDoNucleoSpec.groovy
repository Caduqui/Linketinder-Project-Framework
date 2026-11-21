package org.linketinder.spring.competencias.integracao

import org.linketinder.spring.competencias.service.ServicoIndisponivelException
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.web.client.RestClient
import spock.lang.Specification

import static org.springframework.test.web.client.match.MockRestRequestMatchers.method
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus

/**
 *
 * @author Guilherme Lima Conte
 */

class ClienteHttpDoNucleoSpec extends Specification {

    static final String ENDERECO = "http://nucleo.example"

    RestClient.Builder construtor = RestClient.builder().baseUrl(ENDERECO)
    MockRestServiceServer servidor = MockRestServiceServer.bindTo(construtor).build()
    ClienteHttpDoNucleo cliente = new ClienteHttpDoNucleo(construtor.build())

    def "candidato existe quando o HEAD responde #status"() {
        given:
        servidor.expect(requestTo("${ENDERECO}/candidatos/4"))
                .andExpect(method(HttpMethod.HEAD))
                .andRespond(withStatus(status))

        expect:
        cliente.candidatoExiste(4) == existe

        where:
        status               | existe
        HttpStatus.OK        | true
        HttpStatus.NOT_FOUND | false
    }

    def "outra resposta do MS Núcleo deixa o serviço indisponível"() {
        given:
        servidor.expect(requestTo("${ENDERECO}/candidatos/4")).andRespond(withServerError())

        when:
        cliente.candidatoExiste(4)

        then:
        ServicoIndisponivelException erro = thrown()
        erro.message == "O serviço núcleo está indisponível"
    }

    def "MS Núcleo fora do ar deixa o serviço indisponível"() {
        given:
        ClienteHttpDoNucleo semServidor = new ClienteHttpDoNucleo(RestClient.builder().baseUrl("http://localhost:1").build())

        when:
        semServidor.candidatoExiste(4)

        then:
        thrown(ServicoIndisponivelException)
    }
}
