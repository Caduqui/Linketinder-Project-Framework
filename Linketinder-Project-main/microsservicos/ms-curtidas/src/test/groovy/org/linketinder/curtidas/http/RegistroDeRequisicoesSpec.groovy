package org.linketinder.curtidas.http

import io.micronaut.http.HttpRequest
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Specification

/**
 *
 * @author Guilherme Lima Conte
 */

@MicronautTest
class RegistroDeRequisicoesSpec extends Specification {

    @Inject
    @Client("/")
    HttpClient cliente

    @Inject
    RegistroDoServidor registro

    @MockBean(RegistroDoServidorEmLog)
    RegistroDoServidor registroFalso() {
        return Mock(RegistroDoServidor)
    }

    def "registra método, modelo da rota e status, sem o valor digitado no caminho"() {
        when:
        cliente.toBlocking().exchange(HttpRequest.GET("/teste/42"), Map)

        then:
        1 * registro.registrarRequisicao("GET", "/teste/{id}", 200)
    }

    def "rota desconhecida é registrada com marcador fixo, nunca com o caminho digitado"() {
        when:
        cliente.toBlocking().exchange(HttpRequest.GET("/candidata@example.com"), Map)

        then:
        thrown(HttpClientResponseException)
        1 * registro.registrarRequisicao("GET", RegistroDeRequisicoes.ROTA_DESCONHECIDA, 404)
        0 * registro.registrarRequisicao(_, { String rota -> rota.contains("example.com") }, _)
    }

    def "falha de banco registra só o código do erro"() {
        when:
        cliente.toBlocking().exchange(HttpRequest.GET("/teste/falhas/banco"), Map)

        then:
        thrown(HttpClientResponseException)
        1 * registro.registrarFalhaDeBanco("08006")
    }
}
