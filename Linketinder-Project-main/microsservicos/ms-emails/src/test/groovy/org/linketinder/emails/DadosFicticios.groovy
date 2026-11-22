package org.linketinder.emails

import org.linketinder.emails.model.VagaCriada

import java.time.OffsetDateTime

/**
 *
 * @author Guilherme Lima Conte
 */

class DadosFicticios {

    static final String DATA_HORA_CRIACAO = "2026-11-21T14:05:30.123-04:00"

    static String mensagem(Map<String, Object> alteracoes = [:]) {
        Map<String, Object> campos = [
                id             : 10,
                nome           : "Vaga Exemplo",
                descricao      : "Vaga inventada para os testes",
                competencias   : ["Java", "SQL"],
                estado         : "MS",
                cidade         : "Campo Grande",
                dataHoraCriacao: DATA_HORA_CRIACAO
        ] + alteracoes
        return paraJson(campos)
    }

    static VagaCriada vagaCriada(Map<String, Object> alteracoes = [:]) {
        Map<String, Object> campos = [
                id             : 10,
                nome           : "Vaga Exemplo",
                descricao      : "Vaga inventada para os testes",
                competencias   : ["Java", "SQL"],
                estado         : "MS",
                cidade         : "Campo Grande",
                dataHoraCriacao: OffsetDateTime.parse(DATA_HORA_CRIACAO)
        ] + alteracoes
        return new VagaCriada(campos)
    }

    static List<String> emailsDeCandidatos() {
        return ["candidata.um@example.com", "candidato.dois@example.com", "candidata.tres@example.com"]
    }

    private static String paraJson(Map<String, Object> campos) {
        return "{" + campos.collect { String chave, Object valor -> "\"${chave}\": ${valorJson(valor)}" }.join(", ") + "}"
    }

    private static String valorJson(Object valor) {
        if (valor == null || valor instanceof Number) {
            return String.valueOf(valor)
        }
        if (valor instanceof List) {
            return "[" + ((List) valor).collect { valorJson(it) }.join(", ") + "]"
        }
        return "\"${valor}\""
    }
}
