package org.linketinder.cadastro.dao

import org.linketinder.cadastro.banco.ConexaoBanco
import org.linketinder.cadastro.banco.TradutorDeErroDeBanco
import org.linketinder.cadastro.model.Candidato
import org.linketinder.cadastro.repositorio.CandidatoRepositorio

import java.sql.Date
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException

/**
 *
 * @author Guilherme Lima Conte
 */

class CandidatoDAO implements CandidatoRepositorio {

    private final ConexaoBanco conexaoBanco

    CandidatoDAO(ConexaoBanco conexaoBanco) {
        this.conexaoBanco = conexaoBanco
    }

    @Override
    void inserir(Candidato candidato) {
        String sql = """
            INSERT INTO candidatos (nome, sobrenome, data_nascimento, email, cpf, pais, cep, descricao, senha, formacao)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id
        """

        try {
            conexaoBanco.executar(sql) { PreparedStatement statement ->
                preencherDados(statement, candidato)
                statement.executeQuery().withCloseable { ResultSet resultado ->
                    if (!resultado.next()) {
                        throw new SQLException("INSERT em candidatos não retornou id")
                    }
                    candidato.id = resultado.getInt("id")
                }
            }
        } catch (SQLException e) {
            throw TradutorDeErroDeBanco.traduzir(e)
        }
    }

    @Override
    boolean existe(Integer id) {
        String sql = "SELECT EXISTS (SELECT 1 FROM candidatos WHERE id = ?) AS existe"

        conexaoBanco.executar(sql) { PreparedStatement statement ->
            statement.setInt(1, id)
            statement.executeQuery().withCloseable { ResultSet resultado ->
                resultado.next() && resultado.getBoolean("existe")
            }
        }
    }

    private static void preencherDados(PreparedStatement statement, Candidato candidato) {
        statement.setString(1, candidato.nome)
        statement.setString(2, candidato.sobrenome)
        statement.setDate(3, Date.valueOf(candidato.dataNascimento))
        statement.setString(4, candidato.email)
        statement.setString(5, candidato.cpf)
        statement.setString(6, candidato.pais)
        statement.setString(7, candidato.cep)
        statement.setString(8, candidato.descricao)
        statement.setString(9, candidato.senha)
        statement.setString(10, candidato.formacao)
    }
}
