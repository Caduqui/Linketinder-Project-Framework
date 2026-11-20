package org.linketinder.spring.nucleo.service

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

    static String exigirTextoBruto(Map<String, Object> dados, String campo) {
        String valor = texto(dados, campo)

        if (valor.isEmpty()) {
            throw new DadosInvalidosException("O campo '${campo}' é obrigatório")
        }
        return valor
    }

    static Integer exigirInteiro(Map<String, Object> dados, String campo) {
        Object valor = dados[campo]

        if (!(valor instanceof Integer || valor instanceof Long) || !cabeEmInteiro((Number) valor)) {
            throw new DadosInvalidosException("O campo '${campo}' é obrigatório e deve ser um número inteiro")
        }
        return ((Number) valor).intValue()
    }

    private static boolean cabeEmInteiro(Number valor) {
        return valor.longValue() >= Integer.MIN_VALUE && valor.longValue() <= Integer.MAX_VALUE
    }
}
