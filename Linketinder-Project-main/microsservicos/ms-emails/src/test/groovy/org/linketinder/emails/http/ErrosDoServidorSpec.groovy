package org.linketinder.emails.http

import io.micronaut.http.HttpRequest
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
class ErrosDoServidorSpec extends Specification {

    @Inject
    @Client("/")
    HttpClient cliente

    def "o MS não tem rotas: #requisicao.methodName #requisicao.path responde 404 no formato de erro do projeto"() {
        when:
        cliente.toBlocking().exchange(requisicao, Map)

        then:
        HttpClientResponseException erro = thrown()
        erro.status == HttpStatus.NOT_FOUND
        erro.response.getBody(Map).orElse([:]) == [erro: "Rota não encontrada"]

        where:
        requisicao << [
                HttpRequest.GET("/"),
                HttpRequest.GET("/emails"),
                HttpRequest.POST("/vagas", [nome: "Vaga Exemplo"])
        ]
    }
}
