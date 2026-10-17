package org.linketinder.competencias.dao

import jakarta.inject.Singleton
import org.linketinder.competencias.banco.ConexaoBanco
import org.linketinder.competencias.banco.TradutorDeErroDeBanco
import org.linketinder.competencias.model.Competencia
import org.linketinder.competencias.repositorio.CompetenciaRepositorio
import org.linketinder.competencias.service.CatalogoDeCompetenciasService

import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException

/**
 *
 * @author Guilherme Lima Conte
 */

@Singleton
class CompetenciaDAO implements CompetenciaRepositorio {

    private final ConexaoBanco conexaoBanco

    CompetenciaDAO(ConexaoBanco conexaoBanco) {
        this.conexaoBanco = conexaoBanco
    }

    @Override
    List<Competencia> listar() {
        return conexaoBanco.executar("SELECT id, nome FROM competencias ORDER BY id") { PreparedStatement statement ->
            mapearLista(statement.executeQuery())
        }
    }

    @Override
    Competencia buscarPorId(Integer id) {
        return conexaoBanco.executar("SELECT id, nome FROM competencias WHERE id = ?") { PreparedStatement statement ->
            statement.setInt(1, id)
            mapearPrimeira(statement.executeQuery())
        }
    }

    @Override
    Competencia buscarPorNomeIgnorandoCaixa(String nome) {
        String sql = """
            SELECT id, nome FROM competencias
            WHERE LOWER(nome) = LOWER(?)
            ORDER BY id
            LIMIT 1
        """

        return conexaoBanco.executar(sql) { PreparedStatement statement ->
            statement.setString(1, nome)
            mapearPrimeira(statement.executeQuery())
        }
    }

    @Override
    Competencia inserir(String nome) {
        try {
            return conexaoBanco.executar("INSERT INTO competencias (nome) VALUES (?) RETURNING id, nome") { PreparedStatement statement ->
                statement.setString(1, nome)
                Competencia criada = mapearPrimeira(statement.executeQuery())
                if (criada == null) {
                    throw new SQLException("INSERT em competencias não retornou id")
                }
                return criada
            }
        } catch (SQLException falha) {
            throw TradutorDeErroDeBanco.traduzir(falha, CatalogoDeCompetenciasService.COMPETENCIA_REPETIDA)
        }
    }

    @Override
    void renomear(Integer id, String nome) {
        try {
            conexaoBanco.executar("UPDATE competencias SET nome = ? WHERE id = ?") { PreparedStatement statement ->
                statement.setString(1, nome)
                statement.setInt(2, id)
                statement.executeUpdate()
            }
        } catch (SQLException falha) {
            throw TradutorDeErroDeBanco.traduzir(falha, CatalogoDeCompetenciasService.COMPETENCIA_REPETIDA)
        }
    }

    @Override
    void excluir(Integer id) {
        conexaoBanco.executar("DELETE FROM competencias WHERE id = ?") { PreparedStatement statement ->
            statement.setInt(1, id)
            statement.executeUpdate()
        }
    }

    @Override
    boolean estaVinculada(Integer id) {
        String sql = """
            SELECT EXISTS (SELECT 1 FROM candidato_competencia WHERE id_competencia = ?)
                OR EXISTS (SELECT 1 FROM vaga_competencia WHERE id_competencia = ?)
        """

        return conexaoBanco.executar(sql) { PreparedStatement statement ->
            statement.setInt(1, id)
            statement.setInt(2, id)
            statement.executeQuery().withCloseable { ResultSet resultado ->
                resultado.next() && resultado.getBoolean(1)
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

        return conexaoBanco.executar(sql) { PreparedStatement statement ->
            statement.setInt(1, idCandidato)
            mapearLista(statement.executeQuery())
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

    private static Competencia mapearPrimeira(ResultSet resultado) {
        return resultado.withCloseable {
            resultado.next() ? mapear(resultado) : null
        }
    }

    private static List<Competencia> mapearLista(ResultSet resultado) {
        return resultado.withCloseable {
            List<Competencia> competencias = []
            while (resultado.next()) {
                competencias << mapear(resultado)
            }
            return competencias
        }
    }

    private static Competencia mapear(ResultSet resultado) {
        return new Competencia(id: resultado.getInt("id"), nome: resultado.getString("nome"))
    }
}
