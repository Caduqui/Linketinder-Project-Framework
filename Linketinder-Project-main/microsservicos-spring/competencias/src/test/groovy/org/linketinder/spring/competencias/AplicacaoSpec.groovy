package org.linketinder.spring.competencias

import groovy.json.JsonSlurper
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.context.ConfigurableApplicationContext
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification

import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

/**
 *
 * @author Guilherme Lima Conte
 */

class AplicacaoSpec extends Specification {

    static final String ORIGEM_DO_FRONTEND = "http://localhost:5173"

    @Shared
    @AutoCleanup
    ConfigurableApplicationContext contexto

    @Shared
    HttpClient cliente = HttpClient.newHttpClient()

    @Shared
    String endereco

    def setupSpec() {
        contexto = new SpringApplicationBuilder(Aplicacao).profiles("test").run()
        endereco = "http://localhost:${contexto.environment.getProperty('local.server.port')}"
    }

    def "o contexto sobe com todos os repositórios e consultas"() {
        expect:
        contexto.isActive()
    }

    def "rota desconhecida responde 404 no formato de erro"() {
        when:
        HttpResponse<String> resposta = enviar("GET", "/rota-que-nao-existe")

        then:
        resposta.statusCode() == 404
        lerCorpo(resposta) == [erro: "Rota não encontrada"]
    }

    def "método não aceito responde 405 com Allow"() {
        when:
        HttpResponse<String> resposta = enviar("PUT", "/competencias", "{}")

        then:
        resposta.statusCode() == 405
        resposta.headers().firstValue("Allow").orElse(null) == "GET, POST"
        lerCorpo(resposta) == [erro: "O método PUT não é suportado. Use GET, POST"]
    }

    def "corpo que não é JSON responde 400 sem repetir o que foi enviado"() {
        when:
        HttpResponse<String> resposta = enviar("POST", "/competencias", "{nao-e-json")

        then:
        resposta.statusCode() == 400
        lerCorpo(resposta) == [erro: "O corpo da requisição deve ser um JSON válido"]
    }

    def "corpo JSON que não é objeto responde 400"() {
        when:
        HttpResponse<String> resposta = enviar("POST", "/competencias", "[1, 2]")

        then:
        resposta.statusCode() == 400
        lerCorpo(resposta) == [erro: "O corpo da requisição deve ser um objeto JSON"]
    }

    def "corpo vazio responde 400"() {
        when:
        HttpResponse<String> resposta = enviar("POST", "/competencias", "")

        then:
        resposta.statusCode() == 400
        lerCorpo(resposta) == [erro: "O corpo da requisição não pode ser vazio"]
    }

    def "id que não é número responde 400 sem repetir o valor"() {
        when:
        HttpResponse<String> resposta = enviar("GET", "/competencias/abc")

        then:
        resposta.statusCode() == 400
        lerCorpo(resposta) == [erro: "O ID informado na URL não é um número"]
    }

    def "preflight da origem do frontend é liberado"() {
        when:
        HttpResponse<String> resposta = enviarPreflight(ORIGEM_DO_FRONTEND)

        then:
        resposta.statusCode() == 200
        resposta.headers().firstValue("Access-Control-Allow-Origin").orElse(null) == ORIGEM_DO_FRONTEND
    }

    def "preflight de outra origem é recusado"() {
        when:
        HttpResponse<String> resposta = enviarPreflight("http://outra-origem.example.com")

        then:
        resposta.statusCode() == 403
        !resposta.headers().firstValue("Access-Control-Allow-Origin").isPresent()
    }

    private HttpResponse<String> enviar(String metodo, String caminho, String corpo = null) {
        HttpRequest.Builder requisicao = HttpRequest.newBuilder(URI.create(endereco + caminho))
                .method(metodo, corpo == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(corpo))

        if (corpo != null) {
            requisicao.header("Content-Type", "application/json")
        }
        return cliente.send(requisicao.build(), HttpResponse.BodyHandlers.ofString())
    }

    private HttpResponse<String> enviarPreflight(String origem) {
        HttpRequest requisicao = HttpRequest.newBuilder(URI.create(endereco + "/competencias"))
                .method("OPTIONS", HttpRequest.BodyPublishers.noBody())
                .header("Origin", origem)
                .header("Access-Control-Request-Method", "POST")
                .header("Access-Control-Request-Headers", "content-type")
                .build()
        return cliente.send(requisicao, HttpResponse.BodyHandlers.ofString())
    }

    private static Object lerCorpo(HttpResponse<String> resposta) {
        return new JsonSlurper().parseText(resposta.body())
    }
}
