package org.linketinder.vagas.http

import io.micronaut.http.HttpHeaders
import io.micronaut.http.MutableHttpResponse
import io.micronaut.http.annotation.ResponseFilter
import io.micronaut.http.annotation.ServerFilter

/**
 *
 * @author Guilherme Lima Conte
 */

@ServerFilter(ServerFilter.MATCH_ALL_PATTERN)
class CabecalhoVaryOrigin {

    static final String ORIGIN = "Origin"

    @ResponseFilter
    void acrescentar(MutableHttpResponse<?> resposta) {
        boolean jaVaria = resposta.headers.getAll(HttpHeaders.VARY).any { String valor ->
            valor.split(",")*.trim().contains(ORIGIN)
        }

        if (!jaVaria) {
            resposta.headers.add(HttpHeaders.VARY, ORIGIN)
        }
    }
}
