package org.linketinder.spring.nucleo

import groovy.json.JsonSlurper
import org.springframework.mock.web.MockHttpServletResponse

import java.nio.charset.StandardCharsets

/**
 *
 * @author Guilherme Lima Conte
 */

class RespostaDeTeste {

    final int status
    final String texto
    private final MockHttpServletResponse resposta

    RespostaDeTeste(MockHttpServletResponse resposta) {
        this.resposta = resposta
        this.status = resposta.status
        this.texto = resposta.getContentAsString(StandardCharsets.UTF_8)
    }

    Object getCorpo() {
        return texto ? new JsonSlurper().parseText(texto) : null
    }

    String cabecalho(String nome) {
        return resposta.getHeader(nome)
    }
}
