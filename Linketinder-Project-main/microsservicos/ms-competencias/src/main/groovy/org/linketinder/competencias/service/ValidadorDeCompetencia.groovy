package org.linketinder.competencias.service

/**
 *
 * @author Guilherme Lima Conte
 */

class ValidadorDeCompetencia {

    static final int TAMANHO_MAXIMO_NOME = 50
    static final String CAMPO_NOME = "nome"

    static String exigirNome(Object valor) {
        if (valor != null && !(valor instanceof CharSequence)) {
            throw new RegraDeNegocioException("O campo '${CAMPO_NOME}' deve ser um texto")
        }

        String nome = valor == null ? "" : valor.toString().trim()
        if (nome.isEmpty()) {
            throw new RegraDeNegocioException("O campo '${CAMPO_NOME}' é obrigatório")
        }
        if (nome.length() > TAMANHO_MAXIMO_NOME) {
            throw new RegraDeNegocioException("O campo '${CAMPO_NOME}' deve ter no máximo ${TAMANHO_MAXIMO_NOME} caracteres")
        }
        return nome
    }
}
