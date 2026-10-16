package org.linketinder.cadastro.banco

import org.linketinder.cadastro.Configuracao

import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement

/**
 *
 * @author Guilherme Lima Conte
 */

class ConexaoBanco {

    private final Configuracao configuracao
    private final ThreadLocal<Connection> conexaoDaTransacao = new ThreadLocal<>()

    ConexaoBanco(Configuracao configuracao) {
        this.configuracao = configuracao
    }

    Connection abrir() {
        return DriverManager.getConnection(configuracao.bancoUrl, configuracao.bancoUsuario, configuracao.bancoSenha)
    }

    def <T> T executar(String sql, Closure<T> acao) {
        Connection emTransacao = conexaoDaTransacao.get()

        if (emTransacao != null) {
            return preparar(emTransacao, sql, acao)
        }

        abrir().withCloseable { Connection conexao ->
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
        conexao.prepareStatement(sql).withCloseable { PreparedStatement statement ->
            acao(statement)
        }
    }
}
