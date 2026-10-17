package org.linketinder.curtidas.http

import org.linketinder.curtidas.service.RegraDeNegocioException

/**
 *
 * @author Guilherme Lima Conte
 */

class CorpoJson {

    static final String CORPO_VAZIO = "O corpo da requisição não pode ser vazio"
    static final String ID_INVALIDO = "O ID informado na URL não é um número"

    static Map<String, Object> exigir(Map<String, Object> corpo) {
        if (corpo == null) {
            throw new RegraDeNegocioException(CORPO_VAZIO)
        }
        return corpo
    }

    static Integer exigirInteiro(Map<String, Object> corpo, String campo) {
        Object valor = corpo[campo]

        if (valor instanceof Number) {
            return ((Number) valor).intValue()
        }
        throw new RegraDeNegocioException("O campo '${campo}' é obrigatório e deve ser um número")
    }

    static Integer lerId(String valor) {
        try {
            return Integer.parseInt(valor)
        } catch (NumberFormatException e) {
            throw new RegraDeNegocioException(ID_INVALIDO)
        }
    }
}
