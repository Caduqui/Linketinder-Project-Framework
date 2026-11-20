package org.linketinder.spring.nucleo.service

/**
 *
 * @author Guilherme Lima Conte
 */

class DadosInvalidosException extends RuntimeException {

    DadosInvalidosException(String mensagem) {
        super(mensagem)
    }
}
