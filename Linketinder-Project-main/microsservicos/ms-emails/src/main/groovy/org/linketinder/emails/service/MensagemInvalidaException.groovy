package org.linketinder.emails.service

/**
 *
 * @author Guilherme Lima Conte
 */

class MensagemInvalidaException extends RuntimeException {

    MensagemInvalidaException(String mensagem) {
        super(mensagem)
    }
}
