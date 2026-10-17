package org.linketinder.vagas.http

import io.micronaut.http.HttpRequest
import io.micronaut.http.MutableHttpResponse
import io.micronaut.http.annotation.ResponseFilter
import io.micronaut.http.annotation.ServerFilter
import io.micronaut.web.router.RouteAttributes
import io.micronaut.web.router.RouteMatch
import io.micronaut.web.router.UriRouteMatch

/**
 *
 * @author Guilherme Lima Conte
 */

@ServerFilter(ServerFilter.MATCH_ALL_PATTERN)
class RegistroDeRequisicoes {

    static final String ROTA_DESCONHECIDA = "(rota desconhecida)"

    private final RegistroDoServidor registro

    RegistroDeRequisicoes(RegistroDoServidor registro) {
        this.registro = registro
    }

    @ResponseFilter
    void registrar(HttpRequest<?> requisicao, MutableHttpResponse<?> resposta) {
        registro.registrarRequisicao(requisicao.methodName, modeloDaRota(requisicao), resposta.status().code)
    }

    private static String modeloDaRota(HttpRequest<?> requisicao) {
        Optional<RouteMatch<?>> rota = RouteAttributes.getRouteMatch(requisicao)

        if (rota.isPresent() && rota.get() instanceof UriRouteMatch) {
            return ((UriRouteMatch) rota.get()).routeInfo.uriMatchTemplate.toString()
        }
        return ROTA_DESCONHECIDA
    }
}
