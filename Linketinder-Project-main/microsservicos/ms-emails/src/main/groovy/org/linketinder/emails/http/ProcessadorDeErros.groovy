package org.linketinder.emails.http

import io.micronaut.context.annotation.Replaces
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
    static final String REQUISICAO_INVALIDA = "Requisição inválida"
    static final String ERRO_INTERNO = "Erro interno do servidor"

    @Override
    MutableHttpResponse<Map<String, String>> processResponse(ErrorContext contexto, MutableHttpResponse<?> resposta) {
        MutableHttpResponse<Map<String, String>> respostaDeErro = (MutableHttpResponse<Map<String, String>>) resposta

        return respostaDeErro.body(RespostaDeErro.corpo(mensagemPara(resposta.status())))
                .contentType(MediaType.APPLICATION_JSON_TYPE)
    }

    static String mensagemPara(HttpStatus status) {
        if (status == HttpStatus.NOT_FOUND) {
            return ROTA_NAO_ENCONTRADA
        }
        return status.code >= 500 ? ERRO_INTERNO : REQUISICAO_INVALIDA
    }
}
