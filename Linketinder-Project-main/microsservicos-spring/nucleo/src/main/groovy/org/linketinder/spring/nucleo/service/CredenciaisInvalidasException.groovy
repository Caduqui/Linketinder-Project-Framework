package org.linketinder.spring.nucleo.service

/**
 *
 * @author Guilherme Lima Conte
 */

class CredenciaisInvalidasException extends RuntimeException {

    static final String MENSAGEM = "E-mail ou senha inválidos"

    CredenciaisInvalidasException() {
        super(MENSAGEM)
    }
}
