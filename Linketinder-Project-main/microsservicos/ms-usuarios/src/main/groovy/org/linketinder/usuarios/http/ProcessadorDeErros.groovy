package org.linketinder.usuarios.http

import io.micronaut.context.annotation.Replaces
import io.micronaut.http.HttpHeaders
import io.micronaut.http.HttpStatus
import io.micronaut.http.MediaType
import io.micronaut.http.MutableHttpResponse
import io.micronaut.http.server.exceptions.response.ErrorContext
import io.micronaut.http.server.exceptions.response.ErrorResponseProcessor
import jakarta.inject.Singleton

/**
 *
 * @author Guilherme Lima Conte
 */

@Singleton
@Replaces(ErrorResponseProcessor)
class ProcessadorDeErros implements ErrorResponseProcessor<Map<String, String>> {

    static final String ROTA_NAO_ENCONTRADA = "Rota não encontrada"
    static final String JSON_INVALIDO = "JSON inválido: o corpo da requisição deve ser um objeto JSON"
    static final String CORPO_NAO_JSON = "O corpo da requisição deve ser JSON (Content-Type: application/json)"
    static final String ORIGEM_NAO_PERMITIDA = "Origem não permitida"
    static final String REQUISICAO_INVALIDA = "Requisição inválida"
    static final String ERRO_INTERNO = "Erro interno do servidor"

    @Override
    MutableHttpResponse<Map<String, String>> processResponse(ErrorContext contexto, MutableHttpResponse<?> resposta) {
        String mensagem = mensagemPara(resposta.status(), contexto.request.methodName, resposta.header(HttpHeaders.ALLOW))
        MutableHttpResponse<Map<String, String>> respostaDeErro = (MutableHttpResponse<Map<String, String>>) resposta

        return respostaDeErro.body(RespostaDeErro.corpo(mensagem)).contentType(MediaType.APPLICATION_JSON_TYPE)
    }

    static String mensagemPara(HttpStatus status, String metodo, String metodosAceitos) {
        switch (status) {
            case HttpStatus.NOT_FOUND:
                return ROTA_NAO_ENCONTRADA
            case HttpStatus.METHOD_NOT_ALLOWED:
                return "O método ${metodo} não é suportado nesta rota. Use ${metodosAceitos}".toString()
            case HttpStatus.BAD_REQUEST:
                return JSON_INVALIDO
            case HttpStatus.UNSUPPORTED_MEDIA_TYPE:
                return CORPO_NAO_JSON
            case HttpStatus.FORBIDDEN:
                return ORIGEM_NAO_PERMITIDA
            default:
                return status.code >= 500 ? ERRO_INTERNO : REQUISICAO_INVALIDA
        }
    }
}
