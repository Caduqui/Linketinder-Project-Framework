package org.linketinder.vagas.service

import org.linketinder.vagas.model.Vaga

/**
 *
 * @author Guilherme Lima Conte
 */

class ValidadorDeVaga {

    static Vaga validar(Map<String, Object> dados) {
        return new Vaga(
                nome: exigirTexto(dados, "nome"),
                descricao: texto(dados, "descricao"),
                estado: exigirTexto(dados, "estado"),
                cidade: exigirTexto(dados, "cidade"),
                competencias: competencias(dados)
        )
    }

    private static String exigirTexto(Map<String, Object> dados, String campo) {
        String valor = texto(dados, campo)

        if (valor.isEmpty()) {
            throw new RegraDeNegocioException("O campo '${campo}' é obrigatório")
        }
        return valor
    }

    private static String texto(Map<String, Object> dados, String campo) {
        Object valor = dados[campo]
        return valor == null ? "" : valor.toString().trim()
    }

    private static List<String> competencias(Map<String, Object> dados) {
        List lista = dados.competencias instanceof List ? (List) dados.competencias : []

        return lista*.toString()*.trim()
                .findAll { String competencia -> !competencia.isEmpty() }
                .unique { String competencia -> competencia.toLowerCase() }
    }
}
