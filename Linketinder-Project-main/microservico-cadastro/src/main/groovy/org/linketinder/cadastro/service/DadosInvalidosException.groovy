package org.linketinder.cadastro.service

/**
 *
 * @author Guilherme Lima Conte
 */

class DadosInvalidosException extends RuntimeException {

    DadosInvalidosException(String mensagem) {
        super(mensagem)
    }
}
