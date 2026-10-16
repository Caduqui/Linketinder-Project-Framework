package org.linketinder.cadastro.http

import groovy.json.JsonException
import groovy.json.JsonSlurper
import org.linketinder.cadastro.service.DadosInvalidosException

/**
 *
 * @author Guilherme Lima Conte
 */

class CorpoJson {

    static Map<String, Object> ler(String corpo) {
        if (corpo == null || corpo.trim().isEmpty()) {
            throw new DadosInvalidosException("O corpo da requisição não pode ser vazio")
        }

        Object dados = interpretar(corpo)
        if (!(dados instanceof Map)) {
            throw new DadosInvalidosException("O corpo da requisição deve ser um objeto JSON")
        }
        return dados as Map<String, Object>
    }

    private static Object interpretar(String corpo) {
        try {
            return new JsonSlurper().parseText(corpo)
        } catch (JsonException e) {
            throw new DadosInvalidosException("O corpo da requisição não é um JSON válido")
        }
    }
}
