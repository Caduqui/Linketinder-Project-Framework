package org.linketinder.vagas.dao

import jakarta.inject.Singleton
import org.linketinder.vagas.banco.ConexaoBanco
import org.linketinder.vagas.model.Competencia
import org.linketinder.vagas.repositorio.CompetenciaRepositorio

import java.sql.PreparedStatement
import java.sql.ResultSet

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
    Competencia buscarPorNomeIgnorandoCaixa(String nome) {
        String sql = """
            SELECT id, nome FROM competencias
            WHERE LOWER(nome) = LOWER(?)
            ORDER BY id
            LIMIT 1
        """

        return conexaoBanco.executar(sql) { PreparedStatement statement ->
            statement.setString(1, nome)
            statement.executeQuery().withCloseable { ResultSet resultado ->
                resultado.next() ? mapear(resultado) : null
            }
        }
    }

    @Override
    Competencia inserir(String nome) {
        return conexaoBanco.executar("INSERT INTO competencias (nome) VALUES (?) RETURNING id, nome") { PreparedStatement statement ->
            statement.setString(1, nome)
            statement.executeQuery().withCloseable { ResultSet resultado ->
                resultado.next()
                mapear(resultado)
            }
        }
    }

    private static Competencia mapear(ResultSet resultado) {
        return new Competencia(id: resultado.getInt("id"), nome: resultado.getString("nome"))
    }
}
