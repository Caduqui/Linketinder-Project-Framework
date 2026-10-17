package org.linketinder.competencias.http

import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.MediaType
import io.micronaut.http.MutableHttpResponse

/**
 *
 * @author Guilherme Lima Conte
 */

class RespostaDeErro {

    static MutableHttpResponse<Map<String, String>> criar(HttpStatus status, String mensagem) {
        MutableHttpResponse<Map<String, String>> resposta = HttpResponse.status(status)
        return resposta.body(corpo(mensagem)).contentType(MediaType.APPLICATION_JSON_TYPE)
    }

    static Map<String, String> corpo(String mensagem) {
        return [erro: mensagem]
    }
}
