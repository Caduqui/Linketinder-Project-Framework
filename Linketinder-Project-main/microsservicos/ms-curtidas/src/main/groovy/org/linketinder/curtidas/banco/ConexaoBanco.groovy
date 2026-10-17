package org.linketinder.curtidas.banco

import jakarta.inject.Singleton

import javax.sql.DataSource
import java.sql.Connection
import java.sql.PreparedStatement

/**
 *
 * @author Guilherme Lima Conte
 */

@Singleton
class ConexaoBanco {

    private final DataSource dataSource

    ConexaoBanco(DataSource dataSource) {
        this.dataSource = dataSource
    }

    def <T> T executar(String sql, Closure<T> acao) {
        return dataSource.connection.withCloseable { Connection conexao ->
            conexao.prepareStatement(sql).withCloseable { PreparedStatement statement ->
                acao(statement)
            }
        }
    }
}
