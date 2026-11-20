package org.linketinder.spring.competencias.service

/**
 *
 * @author Guilherme Lima Conte
 */

class ServicoIndisponivelException extends RuntimeException {

    static final String MENSAGEM = "O serviço núcleo está indisponível"

    ServicoIndisponivelException() {
        super(MENSAGEM)
    }
}
