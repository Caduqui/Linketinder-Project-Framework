package org.linketinder.cadastro.banco

import spock.lang.Specification

import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.SQLException

/**
 *
 * @author Guilherme Lima Conte
 */

class TransacaoJdbcSpec extends Specification {

    Connection conexao = Mock(Connection)
    ConexaoBanco conexaoBanco = Spy(ConexaoBanco, constructorArgs: [null]) {
        abrir() >> conexao
    }
    TransacaoJdbc transacao = new TransacaoJdbc(conexaoBanco)

    def "bloco que termina bem é confirmado e a conexão é fechada"() {
        when:
        Object resultado = transacao.executar { "resultado do bloco" }

        then:
        resultado == "resultado do bloco"
        1 * conexao.setAutoCommit(false)
        1 * conexao.commit()
        0 * conexao.rollback()
        1 * conexao.close()
    }

    def "bloco que falha é desfeito, a falha sobe e a conexão é fechada"() {
        when:
        transacao.executar { throw new IllegalStateException("falha no meio da gravação") }

        then:
        IllegalStateException falha = thrown()
        falha.message == "falha no meio da gravação"
        0 * conexao.commit()
        1 * conexao.rollback()
        1 * conexao.close()
    }

    def "consultas feitas dentro do bloco usam a conexão da transação, sem fechá-la antes do fim"() {
        given:
        PreparedStatement statement = Mock(PreparedStatement)

        when:
        transacao.executar {
            conexaoBanco.executar("SELECT 1") { PreparedStatement recebido -> recebido.execute() }
            conexaoBanco.executar("SELECT 2") { PreparedStatement recebido -> recebido.execute() }
        }

        then:
        1 * conexaoBanco.abrir() >> conexao
        1 * conexao.prepareStatement("SELECT 1") >> statement
        1 * conexao.prepareStatement("SELECT 2") >> statement
        2 * statement.execute()

        then:
        1 * conexao.commit()

        then:
        1 * conexao.close()
    }

    def "rollback que falha não perde a exceção original — a falha do rollback fica suprimida"() {
        given:
        SQLException falhaDeRollback = new SQLException("conexão encerrada")
        conexao.rollback() >> { throw falhaDeRollback }

        when:
        transacao.executar { throw new IllegalStateException("falha no meio da gravação") }

        then:
        IllegalStateException falha = thrown()
        falha.message == "falha no meio da gravação"
        falha.suppressed.contains(falhaDeRollback)
    }

    def "depois da transação, uma consulta volta a abrir e fechar a própria conexão"() {
        given:
        Connection outraConexao = Mock(Connection)
        PreparedStatement statement = Mock(PreparedStatement)
        transacao.executar { "nada" }

        when:
        conexaoBanco.executar("SELECT 3") { PreparedStatement recebido -> recebido.execute() }

        then:
        1 * conexaoBanco.abrir() >> outraConexao
        1 * outraConexao.prepareStatement("SELECT 3") >> statement
        1 * outraConexao.close()
    }
}
