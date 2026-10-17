package org.linketinder.usuarios.banco

import org.linketinder.usuarios.service.CadastroRepetidoException
import spock.lang.Specification

import java.sql.SQLException

/**
 *
 * @author Guilherme Lima Conte
 */

class TradutorDeErroDeBancoSpec extends Specification {

    def "violação de unicidade vira cadastro repetido com a mensagem informada"() {
        when:
        Exception traduzida = TradutorDeErroDeBanco.traduzir(new SQLException("detalhe", "23505"), "Mensagem genérica")

        then:
        traduzida instanceof CadastroRepetidoException
        traduzida.message == "Mensagem genérica"
    }

    def "outras falhas de banco continuam como estão"() {
        given:
        SQLException falha = new SQLException("detalhe", "08006")

        expect:
        TradutorDeErroDeBanco.traduzir(falha, "Mensagem genérica").is(falha)
    }
}
