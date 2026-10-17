package org.linketinder.competencias.http

import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.annotation.Produces
import io.micronaut.http.server.exceptions.ExceptionHandler
import jakarta.inject.Singleton
import org.linketinder.competencias.service.RecursoNaoEncontradoException

/**
 *
 * @author Guilherme Lima Conte
 */

@Produces
@Singleton
class TratadorDeRecursoNaoEncontrado implements ExceptionHandler<RecursoNaoEncontradoException, HttpResponse<Map<String, String>>> {

    @Override
    HttpResponse<Map<String, String>> handle(HttpRequest requisicao, RecursoNaoEncontradoException excecao) {
        return RespostaDeErro.criar(HttpStatus.NOT_FOUND, excecao.message)
    }
}
