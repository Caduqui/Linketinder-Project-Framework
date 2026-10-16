package org.linketinder.cadastro.dao

import org.linketinder.cadastro.banco.ConexaoBanco
import org.linketinder.cadastro.banco.TradutorDeErroDeBanco
import org.linketinder.cadastro.model.Empresa
import org.linketinder.cadastro.repositorio.EmpresaRepositorio

import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException

/**
 *
 * @author Guilherme Lima Conte
 */

class EmpresaDAO implements EmpresaRepositorio {

    private final ConexaoBanco conexaoBanco

    EmpresaDAO(ConexaoBanco conexaoBanco) {
        this.conexaoBanco = conexaoBanco
    }

    @Override
    void inserir(Empresa empresa) {
        String sql = """
            INSERT INTO empresas (nome, email, cnpj, pais, cep, descricao, senha)
            VALUES (?, ?, ?, ?, ?, ?, ?) RETURNING id
        """

        try {
            conexaoBanco.executar(sql) { PreparedStatement statement ->
                preencherDados(statement, empresa)
                statement.executeQuery().withCloseable { ResultSet resultado ->
                    if (!resultado.next()) {
                        throw new SQLException("INSERT em empresas não retornou id")
                    }
                    empresa.id = resultado.getInt("id")
                }
            }
        } catch (SQLException e) {
            throw TradutorDeErroDeBanco.traduzir(e)
        }
    }

    private static void preencherDados(PreparedStatement statement, Empresa empresa) {
        statement.setString(1, empresa.nome)
        statement.setString(2, empresa.email)
        statement.setString(3, empresa.cnpj)
        statement.setString(4, empresa.pais)
        statement.setString(5, empresa.cep)
        statement.setString(6, empresa.descricao)
        statement.setString(7, empresa.senha)
    }
}
