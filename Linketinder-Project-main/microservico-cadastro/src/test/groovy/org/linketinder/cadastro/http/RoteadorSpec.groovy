package org.linketinder.cadastro.http

import groovy.json.JsonSlurper
import spock.lang.Specification

import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

/**
 *
 * @author Guilherme Lima Conte
 */

class RoteadorSpec extends Specification {

    static final int PORTA_LIVRE = 0

    ByteArrayOutputStream saidaDoRegistro = new ByteArrayOutputStream()
    Roteador roteador = new Roteador(new RegistroDoServidor(new PrintStream(saidaDoRegistro, true, "UTF-8")))
    ServidorCadastro servidor = new ServidorCadastro(PORTA_LIVRE, roteador)
    HttpClient cliente = HttpClient.newHttpClient()

    def setup() {
        servidor.iniciar()
    }

    def cleanup() {
        servidor.parar()
    }

    HttpResponse<String> chamar(String metodo, String caminho, String corpo = "") {
        HttpRequest requisicao = HttpRequest.newBuilder(URI.create("http://localhost:${servidor.porta}${caminho}"))
                .method(metodo, HttpRequest.BodyPublishers.ofString(corpo))
                .build()
        return cliente.send(requisicao, HttpResponse.BodyHandlers.ofString())
    }

    String registro() {
        return saidaDoRegistro.toString("UTF-8")
    }

    def "rota desconhecida responde 404 com erro em JSON"() {
        when:
        HttpResponse<String> resposta = chamar("GET", "/qualquer")

        then:
        resposta.statusCode() == 404
        resposta.headers().firstValue("Content-Type").get() == "application/json; charset=UTF-8"
        new JsonSlurper().parseText(resposta.body()) == [erro: "Rota não encontrada"]
    }

    def "rota conhecida entrega a requisição ao manipulador e devolve a resposta dele"() {
        given:
        Requisicao recebida = null
        roteador.registrar("POST", "/teste") { Requisicao requisicao ->
            recebida = requisicao
            RespostaJson.de(201, [id: 7])
        }

        when:
        HttpResponse<String> resposta = chamar("POST", "/teste", '{"nome":"Candidata"}')

        then:
        resposta.statusCode() == 201
        resposta.headers().firstValue("Content-Type").get() == "application/json; charset=UTF-8"
        new JsonSlurper().parseText(resposta.body()) == [id: 7]
        recebida.metodo == "POST"
        recebida.caminho == "/teste"
        recebida.corpo == '{"nome":"Candidata"}'
    }

    def "método não aceito em rota conhecida responde 405 com Allow"() {
        given:
        roteador.registrar("POST", "/teste") { Requisicao requisicao -> RespostaJson.de(201, [:]) }

        when:
        HttpResponse<String> resposta = chamar("GET", "/teste")

        then:
        resposta.statusCode() == 405
        resposta.headers().firstValue("Allow").get() == "POST"
        new JsonSlurper().parseText(resposta.body()).erro.contains("GET")
    }

    def "caminho que só começa igual a uma rota responde 404"() {
        given:
        roteador.registrar("POST", "/candidatos") { Requisicao requisicao -> RespostaJson.de(201, [:]) }

        expect:
        chamar("POST", "/candidatosX").statusCode() == 404
        chamar("POST", "/candidatos/").statusCode() == 404
    }

    def "falha inesperada do manipulador responde 500 sem o detalhe técnico"() {
        given:
        roteador.registrar("POST", "/teste") { Requisicao requisicao ->
            throw new IllegalStateException("detalhe interno")
        }

        when:
        HttpResponse<String> resposta = chamar("POST", "/teste", "{}")

        then:
        resposta.statusCode() == 500
        new JsonSlurper().parseText(resposta.body()) == [erro: "Erro interno do servidor"]
        !resposta.body().contains("detalhe interno")
    }

    def "o registro traz método, rota e status e não traz o corpo"() {
        given:
        roteador.registrar("POST", "/teste") { Requisicao requisicao -> RespostaJson.de(201, [:]) }

        when:
        chamar("POST", "/teste", '{"email":"candidata@example.com","senha":"senha-ficticia"}')

        then:
        registro().contains("POST /teste 201")
        !registro().contains("candidata@example.com")
        !registro().contains("senha-ficticia")
    }

    def "o registro de rota desconhecida não repete o caminho digitado"() {
        when:
        chamar("GET", "/candidatos/candidata@example.com?cpf=12345678900")

        then:
        registro().contains("GET (rota desconhecida) 404")
        !registro().contains("candidata@example.com")
        !registro().contains("12345678900")
    }
}
