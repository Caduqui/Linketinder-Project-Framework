package org.linketinder.competencias.banco

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
    private final ThreadLocal<Connection> conexaoDaTransacao = new ThreadLocal<>()

    ConexaoBanco(DataSource dataSource) {
        this.dataSource = dataSource
    }

    Connection abrir() {
        return dataSource.connection
    }

    def <T> T executar(String sql, Closure<T> acao) {
        Connection emTransacao = conexaoDaTransacao.get()

        if (emTransacao != null) {
            return preparar(emTransacao, sql, acao)
        }

        return abrir().withCloseable { Connection conexao ->
            preparar(conexao, sql, acao)
        }
    }

    void vincularATransacao(Connection conexao) {
        conexaoDaTransacao.set(conexao)
    }

    void desvincularDaTransacao() {
        conexaoDaTransacao.remove()
    }

    private static <T> T preparar(Connection conexao, String sql, Closure<T> acao) {
        return conexao.prepareStatement(sql).withCloseable { PreparedStatement statement ->
            acao(statement)
        }
    }
}
