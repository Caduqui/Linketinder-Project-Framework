package org.linketinder.emails.http

/**
 *
 * @author Guilherme Lima Conte
 */

class RespostaDeErro {

    static Map<String, String> corpo(String mensagem) {
        return [erro: mensagem]
    }
}
