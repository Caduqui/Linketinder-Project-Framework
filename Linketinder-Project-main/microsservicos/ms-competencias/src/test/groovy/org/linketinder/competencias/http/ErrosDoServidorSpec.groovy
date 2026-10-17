package org.linketinder.competencias.http

import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.MediaType
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

    def "rota desconhecida responde 404 no formato de erro do projeto"() {
        when:
        enviar(HttpRequest.GET("/nao-existe"))

        then:
        HttpClientResponseException erro = thrown()
        erro.status == HttpStatus.NOT_FOUND
        corpoDe(erro) == [erro: "Rota não encontrada"]
    }

    def "método não aceito responde 405 com Allow e mensagem em português"() {
        when:
        enviar(HttpRequest.DELETE("/teste/1"))

        then:
        HttpClientResponseException erro = thrown()
        erro.status == HttpStatus.METHOD_NOT_ALLOWED
        erro.response.header("Allow").contains("GET")
        corpoDe(erro).erro.startsWith("O método DELETE não é suportado nesta rota")
    }

    def "JSON inválido responde 400"() {
        when:
        enviar(HttpRequest.POST("/teste", "{invalido").contentType(MediaType.APPLICATION_JSON_TYPE))

        then:
        HttpClientResponseException erro = thrown()
        erro.status == HttpStatus.BAD_REQUEST
        corpoDe(erro) == [erro: ProcessadorDeErros.JSON_INVALIDO]
    }

    def "JSON que não é objeto responde 400"() {
        when:
        enviar(HttpRequest.POST("/teste", "[1, 2]").contentType(MediaType.APPLICATION_JSON_TYPE))

        then:
        HttpClientResponseException erro = thrown()
        erro.status == HttpStatus.BAD_REQUEST
        corpoDe(erro).containsKey("erro")
    }

    def "corpo vazio responde 400 com mensagem própria"() {
        when:
        enviar(HttpRequest.POST("/teste", "").contentType(MediaType.APPLICATION_JSON_TYPE))

        then:
        HttpClientResponseException erro = thrown()
        erro.status == HttpStatus.BAD_REQUEST
        corpoDe(erro) == [erro: CorpoJson.CORPO_VAZIO]
    }

    def "corpo válido chega ao controller"() {
        when:
        HttpResponse<Map> resposta = enviar(HttpRequest.POST("/teste", [numero: 7]))

        then:
        resposta.status == HttpStatus.CREATED
        resposta.body() == [numero: 7]
    }

    def "id não numérico na URL responde 400"() {
        when:
        enviar(HttpRequest.GET("/teste/abc"))

        then:
        HttpClientResponseException erro = thrown()
        erro.status == HttpStatus.BAD_REQUEST
        corpoDe(erro) == [erro: CorpoJson.ID_INVALIDO]
    }

    def "exceções de regra viram o status correspondente"() {
        when:
        enviar(HttpRequest.GET(caminho))

        then:
        HttpClientResponseException erro = thrown()
        erro.status == status
        corpoDe(erro) == [erro: mensagem]

        where:
        caminho                 | status                 | mensagem
        "/teste/falhas/regra"      | HttpStatus.BAD_REQUEST | "Regra violada"
        "/teste/falhas/inexistente" | HttpStatus.NOT_FOUND   | "Recurso não encontrado"
        "/teste/falhas/repetido"   | HttpStatus.CONFLICT    | "Já existe um cadastro com esses dados"
    }

    def "falha de banco responde 500 genérico sem detalhe técnico"() {
        when:
        enviar(HttpRequest.GET("/teste/falhas/banco"))

        then:
        HttpClientResponseException erro = thrown()
        erro.status == HttpStatus.INTERNAL_SERVER_ERROR
        corpoDe(erro) == [erro: "Erro ao acessar o banco de dados"]
        !erro.response.getBody(String).orElse("").contains("example.com")
    }

    private HttpResponse<Map> enviar(HttpRequest requisicao) {
        return cliente.toBlocking().exchange(requisicao, Map)
    }

    private static Map corpoDe(HttpClientResponseException erro) {
        return erro.response.getBody(Map).orElse([:])
    }
}
