package org.linketinder.cadastro.service

/**
 *
 * @author Guilherme Lima Conte
 */

class CandidatoNaoEncontradoException extends RuntimeException {

    static final String MENSAGEM = "Candidato não encontrado"

    CandidatoNaoEncontradoException() {
        super(MENSAGEM)
    }
}
