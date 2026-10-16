package org.linketinder.cadastro.dao

import org.linketinder.cadastro.banco.ConexaoBanco
import org.linketinder.cadastro.model.Competencia
import org.linketinder.cadastro.repositorio.CompetenciaRepositorio

import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException

/**
 *
 * @author Guilherme Lima Conte
 */

class CompetenciaDAO implements CompetenciaRepositorio {

    private final ConexaoBanco conexaoBanco

    CompetenciaDAO(ConexaoBanco conexaoBanco) {
        this.conexaoBanco = conexaoBanco
    }

    @Override
    Competencia buscarPorNomeIgnorandoCaixa(String nome) {
        String sql = """
            SELECT id, nome FROM competencias
            WHERE LOWER(nome) = LOWER(?)
            ORDER BY id
            LIMIT 1
        """

        conexaoBanco.executar(sql) { PreparedStatement statement ->
            statement.setString(1, nome)
            statement.executeQuery().withCloseable { ResultSet resultado ->
                resultado.next() ? mapearCompetencia(resultado) : null
            }
        }
    }

    @Override
    Competencia inserir(String nome) {
        String sql = "INSERT INTO competencias (nome) VALUES (?) RETURNING id, nome"

        conexaoBanco.executar(sql) { PreparedStatement statement ->
            statement.setString(1, nome)
            statement.executeQuery().withCloseable { ResultSet resultado ->
                if (!resultado.next()) {
                    throw new SQLException("INSERT em competencias não retornou id")
                }
                mapearCompetencia(resultado)
            }
        }
    }

    @Override
    List<Competencia> buscarDoCandidato(Integer idCandidato) {
        String sql = """
            SELECT comp.id, comp.nome
            FROM competencias AS comp
            JOIN candidato_competencia AS cc ON cc.id_competencia = comp.id
            WHERE cc.id_candidato = ?
            GROUP BY comp.id, comp.nome
            ORDER BY MIN(cc.id)
        """

        conexaoBanco.executar(sql) { PreparedStatement statement ->
            statement.setInt(1, idCandidato)
            statement.executeQuery().withCloseable { ResultSet resultado ->
                List<Competencia> competencias = []
                while (resultado.next()) {
                    competencias << mapearCompetencia(resultado)
                }
                return competencias
            }
        }
    }

    @Override
    void vincularAoCandidato(Integer idCandidato, Integer idCompetencia) {
        String sql = "INSERT INTO candidato_competencia (id_candidato, id_competencia) VALUES (?, ?)"

        conexaoBanco.executar(sql) { PreparedStatement statement ->
            statement.setInt(1, idCandidato)
            statement.setInt(2, idCompetencia)
            statement.executeUpdate()
        }
    }

    private static Competencia mapearCompetencia(ResultSet resultado) {
        return new Competencia(id: resultado.getInt("id"), nome: resultado.getString("nome"))
    }
}
