package org.linketinder.spring.competencias.service

/**
 *
 * @author Guilherme Lima Conte
 */

class RecursoNaoEncontradoException extends RuntimeException {

    RecursoNaoEncontradoException(String mensagem) {
        super(mensagem)
    }
}
