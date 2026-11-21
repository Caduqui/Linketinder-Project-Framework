package org.linketinder.spring.nucleo.service

/**
 *
 * @author Guilherme Lima Conte
 */

class CadastroRepetidoException extends RuntimeException {

    static final String MENSAGEM = "Já existe um cadastro com esses dados"

    CadastroRepetidoException() {
        super(MENSAGEM)
    }
}
