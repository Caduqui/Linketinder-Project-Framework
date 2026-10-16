package org.example.api

import spock.lang.Specification

import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 *
 * @author Guilherme Lima Conte
 */

class CorsFilterSpec extends Specification {
    static final String ORIGEM_FRONTEND = "http://localhost:5173"

    CorsFilter filtro = new CorsFilter(ORIGEM_FRONTEND)
    HttpServletRequest requisicao = Mock(HttpServletRequest)
    HttpServletResponse resposta = Mock(HttpServletResponse)
    FilterChain cadeia = Mock(FilterChain)

    def "preflight OPTIONS da origem do frontend responde 204 com as permissões e não chega ao servlet"() {
        given:
        requisicao.method >> "OPTIONS"
        requisicao.getHeader("Origin") >> ORIGEM_FRONTEND

        when:
        filtro.doFilter(requisicao, resposta, cadeia)

        then:
        1 * resposta.setHeader("Access-Control-Allow-Origin", ORIGEM_FRONTEND)
        1 * resposta.setHeader("Access-Control-Allow-Methods", "GET, POST, DELETE, OPTIONS")
        1 * resposta.setHeader("Access-Control-Allow-Headers", "Content-Type")
        1 * resposta.setStatus(HttpServletResponse.SC_NO_CONTENT)
        0 * cadeia.doFilter(_, _)
    }

    def "requisição comum da origem do frontend recebe a permissão e segue para o servlet"() {
        given:
        requisicao.method >> "POST"
        requisicao.getHeader("Origin") >> ORIGEM_FRONTEND

        when:
        filtro.doFilter(requisicao, resposta, cadeia)

        then:
        1 * resposta.setHeader("Access-Control-Allow-Origin", ORIGEM_FRONTEND)
        1 * cadeia.doFilter(requisicao, resposta)
    }

    def "requisição de outra origem não recebe permissão"() {
        given:
        requisicao.method >> "GET"
        requisicao.getHeader("Origin") >> "http://site-qualquer.example.com"

        when:
        filtro.doFilter(requisicao, resposta, cadeia)

        then:
        0 * resposta.setHeader("Access-Control-Allow-Origin", _)
        1 * cadeia.doFilter(requisicao, resposta)
    }
}
