package org.linketinder.vagas.banco

import spock.lang.Specification

import javax.sql.DataSource
import java.sql.Connection
import java.sql.PreparedStatement

/**
 *
 * @author Guilherme Lima Conte
 */

class TransacaoJdbcSpec extends Specification {

    Connection conexao = Mock()
    DataSource dataSource = Mock() {
        getConnection() >> conexao
    }
    ConexaoBanco conexaoBanco = new ConexaoBanco(dataSource)
    TransacaoJdbc transacao = new TransacaoJdbc(conexaoBanco)

    def "confirma a transação quando o bloco termina sem erro e devolve o resultado"() {
        when:
        String resultado = transacao.executar { "feito" }

        then:
        resultado == "feito"
        1 * conexao.setAutoCommit(false)
        1 * conexao.commit()
        0 * conexao.rollback()
        1 * conexao.close()
    }

    def "desfaz a transação e repassa a exceção quando o bloco falha"() {
        when:
        transacao.executar { throw new IllegalStateException("falhou") }

        then:
        thrown(IllegalStateException)
        0 * conexao.commit()
        1 * conexao.rollback()
        1 * conexao.close()
    }

    def "os comandos dentro do bloco usam a mesma conexão da transação"() {
        given:
        PreparedStatement statement = Mock()

        when:
        transacao.executar {
            conexaoBanco.executar("SELECT 1") { PreparedStatement s -> "um" }
            conexaoBanco.executar("SELECT 2") { PreparedStatement s -> "dois" }
        }

        then:
        2 * conexao.prepareStatement(_) >> statement
        1 * conexao.close()
    }

    def "fora de transação cada comando abre e fecha a própria conexão"() {
        given:
        PreparedStatement statement = Mock()

        when:
        conexaoBanco.executar("SELECT 1") { PreparedStatement s -> "um" }

        then:
        1 * conexao.prepareStatement("SELECT 1") >> statement
        1 * statement.close()
        1 * conexao.close()
    }
}
