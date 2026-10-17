package org.linketinder.competencias.service

/**
 *
 * @author Guilherme Lima Conte
 */

class CadastroRepetidoException extends RuntimeException {

    static final String MENSAGEM_GENERICA = "Já existe um cadastro com esses dados"

    CadastroRepetidoException() {
        this(MENSAGEM_GENERICA)
    }

    CadastroRepetidoException(String mensagem) {
        super(mensagem)
    }
}
