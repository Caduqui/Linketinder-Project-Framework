package org.example.Banco


import java.sql.Connection
import java.sql.PreparedStatement

/**
 *
 * @author Guilherme Lima Conte
 */

class ConexaoBanco {
    static final int SEGUNDOS_PARA_VALIDAR_CONEXAO = 2

    private static ConexaoBanco instancia
    private final ProvedorConexao provedor
    private Connection conexao

    private ConexaoBanco(ProvedorConexao provedor) {
        this.provedor = provedor
    }

    static ConexaoBanco obterInstancia() {
        if (instancia == null) {
            instancia = new ConexaoBanco(FactoryConexao.criar(TipoBanco.configurado()))
        }
        return instancia
    }

    Connection conectar() {
        if (conexao == null || !conexao.isValid(SEGUNDOS_PARA_VALIDAR_CONEXAO)) {
            conexao = provedor.abrirConexao()
        }
        return conexao
    }

    def <T> T executar(String sql, Closure<T> acao) {
        conectar().prepareStatement(sql).withCloseable { PreparedStatement statement ->
            acao(statement)
        }
    }

    void fechar() {
        conexao?.close()
        conexao = null
    }
}
