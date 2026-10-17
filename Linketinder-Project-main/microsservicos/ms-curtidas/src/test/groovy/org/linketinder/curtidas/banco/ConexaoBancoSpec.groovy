package org.linketinder.curtidas.banco

import spock.lang.Specification

import javax.sql.DataSource
import java.sql.Connection
import java.sql.PreparedStatement

/**
 *
 * @author Guilherme Lima Conte
 */

class ConexaoBancoSpec extends Specification {

    def "cada comando abre e fecha a própria conexão e devolve o resultado da ação"() {
        given:
        PreparedStatement statement = Mock()
        Connection conexao = Mock()
        DataSource dataSource = Mock() {
            getConnection() >> conexao
        }
        ConexaoBanco conexaoBanco = new ConexaoBanco(dataSource)

        when:
        String resultado = conexaoBanco.executar("SELECT 1") { PreparedStatement s -> "um" }

        then:
        resultado == "um"
        1 * conexao.prepareStatement("SELECT 1") >> statement
        1 * statement.close()
        1 * conexao.close()
    }
}
