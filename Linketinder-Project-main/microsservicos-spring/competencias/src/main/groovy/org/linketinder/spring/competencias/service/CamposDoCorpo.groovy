package org.linketinder.spring.competencias.service

/**
 *
 * @author Guilherme Lima Conte
 */

class CamposDoCorpo {

    static String texto(Map<String, Object> dados, String campo) {
        Object valor = dados[campo]

        if (valor == null) {
            return ""
        }
        if (!(valor instanceof CharSequence)) {
            throw new DadosInvalidosException("O campo '${campo}' deve ser um texto")
        }
        return valor.toString()
    }

    static String exigirTexto(Map<String, Object> dados, String campo) {
        String valor = texto(dados, campo).trim()

        if (valor.isEmpty()) {
            throw new DadosInvalidosException("O campo '${campo}' é obrigatório")
        }
        return valor
    }
}
