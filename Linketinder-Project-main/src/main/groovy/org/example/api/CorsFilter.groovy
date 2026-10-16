package org.example.api

import javax.servlet.Filter
import javax.servlet.FilterChain
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.annotation.WebFilter
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 *
 * @author Guilherme Lima Conte
 */

@WebFilter(filterName = "cors", urlPatterns = ["/*"])
class CorsFilter implements Filter {
    static final String VARIAVEL_ORIGEM = "LINKETINDER_FRONTEND_ORIGEM"
    static final String ORIGEM_PADRAO = "http://localhost:5173"
    static final String METODOS_PERMITIDOS = "GET, POST, DELETE, OPTIONS"
    static final String CABECALHOS_PERMITIDOS = "Content-Type"

    private final String origemPermitida

    CorsFilter() {
        this(System.getenv(VARIAVEL_ORIGEM) ?: ORIGEM_PADRAO)
    }

    CorsFilter(String origemPermitida) {
        this.origemPermitida = origemPermitida
    }

    @Override
    void doFilter(ServletRequest request, ServletResponse response, FilterChain cadeia) {
        HttpServletRequest requisicao = (HttpServletRequest) request
        HttpServletResponse resposta = (HttpServletResponse) response

        if (requisicao.getHeader("Origin") == origemPermitida) {
            resposta.setHeader("Access-Control-Allow-Origin", origemPermitida)
            resposta.setHeader("Access-Control-Allow-Methods", METODOS_PERMITIDOS)
            resposta.setHeader("Access-Control-Allow-Headers", CABECALHOS_PERMITIDOS)
        }
        resposta.addHeader("Vary", "Origin")

        if (requisicao.method == "OPTIONS") {
            resposta.status = HttpServletResponse.SC_NO_CONTENT
            return
        }

        cadeia.doFilter(requisicao, resposta)
    }

}
