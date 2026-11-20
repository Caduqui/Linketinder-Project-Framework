package org.linketinder.spring.nucleo.service

/**
 *
 * @author Guilherme Lima Conte
 */

class ServicoIndisponivelException extends RuntimeException {

    static final String MENSAGEM = "O serviço de competências está indisponível"

    ServicoIndisponivelException() {
        super(MENSAGEM)
    }
}
