package org.linketinder.cadastro.banco

import org.linketinder.cadastro.service.CadastroRepetidoException
import spock.lang.Specification

import java.sql.SQLException

/**
 *
 * @author Guilherme Lima Conte
 */

class TradutorDeErroDeBancoSpec extends Specification {

    def "violação de unicidade vira cadastro repetido"() {
        given:
        SQLException violacao = new SQLException("ERROR: duplicate key value violates unique constraint", "23505")

        expect:
        TradutorDeErroDeBanco.traduzir(violacao) instanceof CadastroRepetidoException
    }

    def "cadastro repetido não carrega a mensagem do banco, que pode trazer o valor repetido"() {
        given:
        SQLException violacao = new SQLException("ERROR: duplicate key\n  Detail: Key (email)=(candidata@example.com) already exists.", "23505")

        expect:
        !TradutorDeErroDeBanco.traduzir(violacao).message.contains("candidata@example.com")
    }

    def "outra falha de banco continua sendo a mesma SQLException"() {
        given:
        SQLException falha = new SQLException("ERROR: falha simulada", (String) estado)

        expect:
        TradutorDeErroDeBanco.traduzir(falha).is(falha)

        where:
        estado << ["08001", "23503", "22001", null]
    }
}
