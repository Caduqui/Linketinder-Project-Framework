package org.linketinder.emails.service

import io.micronaut.json.JsonMapper
import jakarta.inject.Singleton
import org.linketinder.emails.model.VagaCriada

import java.time.OffsetDateTime
import java.time.format.DateTimeParseException

/**
 *
 * @author Guilherme Lima Conte
 */

@Singleton
class LeitorDeMensagem {

    static final String MENSAGEM_VAZIA = "A mensagem está vazia"
    static final String JSON_INVALIDO = "A mensagem não é um JSON válido"
    static final String NAO_E_OBJETO = "A mensagem não é um objeto JSON"
    static final String SEM_ID_OU_NOME = "A mensagem não tem o id ou o nome da vaga"

    private final JsonMapper jsonMapper

    LeitorDeMensagem(JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper
    }

    VagaCriada ler(String mensagem) {
        Map<String, Object> campos = lerObjeto(mensagem)
        Integer id = inteiroDe(campos.id)
        String nome = textoDe(campos.nome)

        if (id == null || !nome) {
            throw new MensagemInvalidaException(SEM_ID_OU_NOME)
        }
        return new VagaCriada(
                id: id,
                nome: nome,
                descricao: textoDe(campos.descricao),
                competencias: listaDe(campos.competencias),
                estado: textoDe(campos.estado),
                cidade: textoDe(campos.cidade),
                dataHoraCriacao: dataHoraDe(campos.dataHoraCriacao)
        )
    }

    private Map<String, Object> lerObjeto(String mensagem) {
        if (mensagem == null || mensagem.trim().isEmpty()) {
            throw new MensagemInvalidaException(MENSAGEM_VAZIA)
        }

        Object lido
        try {
            lido = jsonMapper.readValue(mensagem, Object)
        } catch (IOException ignorada) {
            throw new MensagemInvalidaException(JSON_INVALIDO)
        }

        if (!(lido instanceof Map)) {
            throw new MensagemInvalidaException(NAO_E_OBJETO)
        }
        return (Map<String, Object>) lido
    }

    private static Integer inteiroDe(Object valor) {
        boolean inteiro = valor instanceof Integer || valor instanceof Long || valor instanceof BigInteger
        return inteiro ? ((Number) valor).intValue() : null
    }

    private static String textoDe(Object valor) {
        return valor == null ? null : valor.toString().trim()
    }

    private static List<String> listaDe(Object valor) {
        if (!(valor instanceof List)) {
            return []
        }
        return ((List) valor).findAll { Object item -> item != null }.collect { Object item -> item.toString() }
    }

    private static OffsetDateTime dataHoraDe(Object valor) {
        if (valor == null) {
            return null
        }
        try {
            return OffsetDateTime.parse(valor.toString())
        } catch (DateTimeParseException ignorada) {
            return null
        }
    }
}
