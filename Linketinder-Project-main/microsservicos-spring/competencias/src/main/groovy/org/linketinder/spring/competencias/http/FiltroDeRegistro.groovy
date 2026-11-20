package org.linketinder.spring.competencias.http

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.web.servlet.HandlerMapping

/**
 *
 * @author Guilherme Lima Conte
 */

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class FiltroDeRegistro extends OncePerRequestFilter {

    private final RegistroDoServidor registro

    FiltroDeRegistro(RegistroDoServidor registro) {
        this.registro = registro
    }

    @Override
    protected void doFilterInternal(HttpServletRequest requisicao, HttpServletResponse resposta, FilterChain cadeia) {
        try {
            cadeia.doFilter(requisicao, resposta)
        } finally {
            Object rota = requisicao.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE)
            registro.registrarRequisicao(requisicao.method, rota?.toString() ?: RegistroDoServidor.ROTA_DESCONHECIDA, resposta.status)
        }
    }
}
