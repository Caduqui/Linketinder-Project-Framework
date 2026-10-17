package org.linketinder.usuarios.http

import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Specification

/**
 *
 * @author Guilherme Lima Conte
 */

@MicronautTest
class CorsSpec extends Specification {

    static final String ORIGEM_DO_FRONTEND = "http://localhost:5173"

    @Inject
    @Client("/")
    HttpClient cliente

    def "preflight da origem do frontend é liberado"() {
        when:
        HttpResponse resposta = cliente.toBlocking().exchange(preflight(ORIGEM_DO_FRONTEND))

        then:
        resposta.header("Access-Control-Allow-Origin") == ORIGEM_DO_FRONTEND
        resposta.header("Vary")?.contains("Origin")
    }

    def "requisição da origem do frontend recebe o cabeçalho de CORS"() {
        when:
        HttpResponse resposta = cliente.toBlocking().exchange(HttpRequest.GET("/teste/1").header("Origin", ORIGEM_DO_FRONTEND), Map)

        then:
        resposta.header("Access-Control-Allow-Origin") == ORIGEM_DO_FRONTEND
    }

    def "outra origem é recusada e não recebe o cabeçalho de CORS"() {
        when:
        cliente.toBlocking().exchange(preflight("http://outra-origem.example.com"))

        then:
        HttpClientResponseException erro = thrown()
        erro.status == HttpStatus.FORBIDDEN
        erro.response.header("Access-Control-Allow-Origin") == null
    }

    def "preflight de POST com Content-Type, como o navegador envia, é liberado"() {
        when:
        HttpResponse resposta = cliente.toBlocking().exchange(HttpRequest.OPTIONS("/teste")
                .header("Origin", ORIGEM_DO_FRONTEND)
                .header("Access-Control-Request-Method", "POST")
                .header("Access-Control-Request-Headers", "content-type"))

        then:
        resposta.header("Access-Control-Allow-Origin") == ORIGEM_DO_FRONTEND
    }

    def "toda resposta leva Vary: Origin, uma vez só, com ou sem a origem do frontend"() {
        when:
        HttpResponse semOrigem = cliente.toBlocking().exchange(HttpRequest.GET("/teste/1"), Map)
        HttpResponse comOrigem = cliente.toBlocking().exchange(HttpRequest.GET("/teste/1").header("Origin", ORIGEM_DO_FRONTEND), Map)

        then:
        valoresDeVary(semOrigem) == ["Origin"]
        valoresDeVary(comOrigem) == ["Origin"]
    }

    private static List<String> valoresDeVary(HttpResponse resposta) {
        return resposta.headers.getAll("Vary").collectMany { String valor -> valor.split(",")*.trim() }
    }

    private static HttpRequest preflight(String origem) {
        return HttpRequest.OPTIONS("/teste/1")
                .header("Origin", origem)
                .header("Access-Control-Request-Method", "GET")
    }
}
