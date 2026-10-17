package org.linketinder.usuarios.service

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
