package org.example.dao

import org.example.model.Competencia
import org.example.Banco.ConexaoBanco
import org.example.repositorio.CompetenciaRepositorio

import java.sql.PreparedStatement
import java.sql.ResultSet

/**
 *
 * @author Guilherme Lima Conte
 */

class CompetenciaDAO implements CompetenciaRepositorio{

    final ConexaoBanco conexaoBanco

    CompetenciaDAO(ConexaoBanco conexaoBanco) {
        this.conexaoBanco = conexaoBanco
    }

    @Override
    void inserir(Competencia competencia) {
        String sql = """
            INSERT INTO competencias (nome)
            VALUES (?)
            RETURNING id
        """

        conexaoBanco.executar(sql) { PreparedStatement statement ->
            statement.setString(1, competencia.nome)
            statement.executeQuery().withCloseable { ResultSet resultado ->
                if (resultado.next()) {
                    competencia.id = resultado.getInt("id")
                }
            }
        }
    }

    @Override
    List<Competencia> listar() {
        String sql = """
            SELECT * FROM competencias ORDER BY id
        """

        conexaoBanco.executar(sql) { PreparedStatement statement ->
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
    void atualizar(Competencia competencia) {
        String sql = """
            UPDATE competencias 
                SET nome = ?
                WHERE id = ? 
        """

        conexaoBanco.executar(sql) { PreparedStatement statement ->
            statement.setString(1, competencia.nome)
            statement.setInt(2, competencia.id)
            statement.executeUpdate()
        }
    }

    @Override
    void deletar(Integer id) {
        String sql = """
            DELETE FROM competencias
                WHERE id = ?
        """

        conexaoBanco.executar(sql) { PreparedStatement statement ->
            statement.setInt(1, id)
            statement.executeUpdate()
        }
    }

    @Override
    Competencia buscarPorId(Integer id) {
        String sql = """
            SELECT * FROM competencias WHERE id = ?
        """

        conexaoBanco.executar(sql) { PreparedStatement statement ->
            statement.setInt(1, id)
            buscarUma(statement)
        }
    }

    @Override
    Competencia buscarPorNome(String nome) {
        String sql = """
            SELECT * FROM competencias WHERE nome = ?
        """

        conexaoBanco.executar(sql) { PreparedStatement statement ->
            statement.setString(1, nome)
            buscarUma(statement)
        }
    }

    @Override
    Competencia buscarOuInserir(String nome) {
        Competencia competencia = buscarPorNome(nome)

        if (competencia == null) {
            competencia = new Competencia(nome)
            inserir(competencia)
        }

        return competencia
    }

    @Override
    boolean estaVinculadoACandidatoOuVaga (Integer id) {
        String sql = """
            SELECT EXISTS (SELECT 1 FROM candidato_competencia WHERE id_competencia = ?)
                OR EXISTS (SELECT 1 FROM vaga_competencia WHERE id_competencia = ?) AS vinculada
        """

        conexaoBanco.executar(sql) { PreparedStatement statement ->
            statement.setInt(1, id)
            statement.setInt(2, id)
            statement.executeQuery().withCloseable { ResultSet resultado ->
                resultado.next() && resultado.getBoolean("vinculada")
            }
        }
    }

    private static Competencia buscarUma(PreparedStatement statement) {
        statement.executeQuery().withCloseable { ResultSet resultado ->
            resultado.next() ? mapearCompetencia(resultado) : null
        }
    }

    private static Competencia mapearCompetencia(ResultSet resultado) {
        Competencia competencia = new Competencia(resultado.getString("nome"))
        competencia.id = resultado.getInt("id")
        return competencia
    }
}
