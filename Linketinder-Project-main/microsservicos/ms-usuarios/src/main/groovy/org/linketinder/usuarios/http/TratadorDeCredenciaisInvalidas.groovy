package org.linketinder.usuarios.http

import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.annotation.Produces
import io.micronaut.http.server.exceptions.ExceptionHandler
import jakarta.inject.Singleton
import org.linketinder.usuarios.service.CredenciaisInvalidasException

/**
 *
 * @author Guilherme Lima Conte
 */

@Produces
@Singleton
class TratadorDeCredenciaisInvalidas implements ExceptionHandler<CredenciaisInvalidasException, HttpResponse<Map<String, String>>> {

    @Override
    HttpResponse<Map<String, String>> handle(HttpRequest requisicao, CredenciaisInvalidasException excecao) {
        return RespostaDeErro.criar(HttpStatus.UNAUTHORIZED, excecao.message)
    }
}
