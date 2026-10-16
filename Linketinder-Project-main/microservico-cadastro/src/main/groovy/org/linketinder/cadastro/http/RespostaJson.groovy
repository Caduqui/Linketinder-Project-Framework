package org.linketinder.cadastro.http

import com.sun.net.httpserver.HttpExchange
import groovy.json.JsonGenerator

/**
 *
 * @author Guilherme Lima Conte
 */

class RespostaJson {

    static final String TIPO_JSON = "application/json; charset=UTF-8"
    static final String CODIFICACAO = "UTF-8"
    static final String METODO_SEM_CORPO = "HEAD"
    static final long SEM_CORPO = -1

    private static final JsonGenerator GERADOR = new JsonGenerator.Options().disableUnicodeEscaping().build()

    final int status
    final Object corpo
    final Map<String, String> cabecalhos = [:]

    private RespostaJson(int status, Object corpo) {
        this.status = status
        this.corpo = corpo
    }

    static RespostaJson de(int status, Object corpo) {
        return new RespostaJson(status, corpo)
    }

    static RespostaJson erro(int status, String mensagem) {
        return new RespostaJson(status, [erro: mensagem])
    }

    RespostaJson comCabecalho(String nome, String valor) {
        cabecalhos[nome] = valor
        return this
    }

    void enviar(HttpExchange troca) {
        byte[] conteudo = GERADOR.toJson(corpo).getBytes(CODIFICACAO)

        troca.responseHeaders.set("Content-Type", TIPO_JSON)
        cabecalhos.each { String nome, String valor ->
            troca.responseHeaders.set(nome, valor)
        }

        if (troca.requestMethod == METODO_SEM_CORPO) {
            troca.sendResponseHeaders(status, SEM_CORPO)
            return
        }

        troca.sendResponseHeaders(status, conteudo.length)
        troca.responseBody.write(conteudo)
    }
}
