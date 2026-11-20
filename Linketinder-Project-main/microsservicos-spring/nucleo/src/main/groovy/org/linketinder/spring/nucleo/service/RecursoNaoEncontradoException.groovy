package org.linketinder.spring.nucleo.service

/**
 *
 * @author Guilherme Lima Conte
 */

class RecursoNaoEncontradoException extends RuntimeException {

    RecursoNaoEncontradoException(String mensagem) {
        super(mensagem)
    }
}
