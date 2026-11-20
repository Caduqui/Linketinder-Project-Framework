package org.linketinder.spring.competencias.service

/**
 *
 * @author Guilherme Lima Conte
 */

class DadosInvalidosException extends RuntimeException {

    DadosInvalidosException(String mensagem) {
        super(mensagem)
    }
}
