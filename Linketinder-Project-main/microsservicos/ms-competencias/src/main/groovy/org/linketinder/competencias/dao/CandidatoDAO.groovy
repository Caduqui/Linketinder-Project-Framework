package org.linketinder.competencias.dao

import jakarta.inject.Singleton
import org.linketinder.competencias.banco.ConexaoBanco
import org.linketinder.competencias.model.Candidato
import org.linketinder.competencias.repositorio.CandidatoRepositorio

import java.sql.PreparedStatement
import java.sql.ResultSet

/**
 *
 * @author Guilherme Lima Conte
 */

@Singleton
class CandidatoDAO implements CandidatoRepositorio {

    private final ConexaoBanco conexaoBanco

    CandidatoDAO(ConexaoBanco conexaoBanco) {
        this.conexaoBanco = conexaoBanco
    }

    @Override
    boolean existe(Integer id) {
        return conexaoBanco.executar("SELECT EXISTS (SELECT 1 FROM candidatos WHERE id = ?)") { PreparedStatement statement ->
            statement.setInt(1, id)
            statement.executeQuery().withCloseable { ResultSet resultado ->
                resultado.next() && resultado.getBoolean(1)
            }
        }
    }

    @Override
    Candidato buscarPorId(Integer id) {
        String sql = """
            SELECT id, nome, sobrenome, data_nascimento, email, cpf, pais, cep, descricao, formacao
            FROM candidatos
            WHERE id = ?
        """

        return conexaoBanco.executar(sql) { PreparedStatement statement ->
            statement.setInt(1, id)
            statement.executeQuery().withCloseable { ResultSet resultado ->
                resultado.next() ? mapear(resultado) : null
            }
        }
    }

    private static Candidato mapear(ResultSet resultado) {
        return new Candidato(
                id: resultado.getInt("id"),
                nome: resultado.getString("nome"),
                sobrenome: resultado.getString("sobrenome"),
                dataNascimento: resultado.getDate("data_nascimento").toLocalDate(),
                email: resultado.getString("email"),
                cpf: resultado.getString("cpf"),
                pais: resultado.getString("pais"),
                cep: resultado.getString("cep"),
                descricao: resultado.getString("descricao"),
                formacao: resultado.getString("formacao")
        )
    }
}
