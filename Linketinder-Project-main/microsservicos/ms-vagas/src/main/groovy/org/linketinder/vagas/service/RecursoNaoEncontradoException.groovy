package org.linketinder.vagas.service

/**
 *
 * @author Guilherme Lima Conte
 */

class RecursoNaoEncontradoException extends RuntimeException {

    RecursoNaoEncontradoException(String mensagem) {
        super(mensagem)
    }
}
