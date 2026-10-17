package org.linketinder.competencias.http

import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.annotation.Produces
import io.micronaut.http.server.exceptions.ExceptionHandler
import jakarta.inject.Singleton

import java.sql.SQLException

/**
 *
 * @author Guilherme Lima Conte
 */

@Produces
@Singleton
class TratadorDeFalhaDeBanco implements ExceptionHandler<SQLException, HttpResponse<Map<String, String>>> {

    static final String MENSAGEM = "Erro ao acessar o banco de dados"

    private final RegistroDoServidor registro

    TratadorDeFalhaDeBanco(RegistroDoServidor registro) {
        this.registro = registro
    }

    @Override
    HttpResponse<Map<String, String>> handle(HttpRequest requisicao, SQLException excecao) {
        registro.registrarFalhaDeBanco(excecao.SQLState)
        return RespostaDeErro.criar(HttpStatus.INTERNAL_SERVER_ERROR, MENSAGEM)
    }
}
