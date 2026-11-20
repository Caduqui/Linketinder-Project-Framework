package org.linketinder.spring.competencias.http

import org.springframework.dao.DataAccessResourceFailureException
import spock.lang.Specification

import java.sql.SQLException

/**
 *
 * @author Guilherme Lima Conte
 */

class RegistroDoServidorSpec extends Specification {

    def "a falha de banco registrada traz só o código e a primeira linha da mensagem"() {
        given:
        SQLException falha = new SQLException("ERROR: violação de unicidade\n  Detalhe: (email)=(pessoa@example.com)", "23505")

        expect:
        RegistroDoServidor.descreverFalhaDeBanco(falha) == "Falha de banco [23505]: ERROR: violação de unicidade"
    }

    def "a falha de banco embrulhada pelo Spring é descrita pela SQLException de dentro"() {
        given:
        SQLException causa = new SQLException("ERROR: falha qualquer\n  Detalhe: (cpf)=(123)", "08006")
        DataAccessResourceFailureException falha = new DataAccessResourceFailureException("pessoa@example.com", causa)

        expect:
        RegistroDoServidor.descreverFalhaDeBanco(falha) == "Falha de banco [08006]: ERROR: falha qualquer"
    }

    def "a falha de banco sem mensagem não quebra o registro"() {
        expect:
        RegistroDoServidor.descreverFalhaDeBanco(new SQLException()) == "Falha de banco [sem código]: sem mensagem"
    }

    def "a falha inesperada registrada traz só o tipo da exceção"() {
        expect:
        RegistroDoServidor.descreverFalhaInesperada(new IllegalStateException("pessoa@example.com")) ==
                "Falha inesperada: java.lang.IllegalStateException"
    }
}
