package org.linketinder.usuarios.dao

import jakarta.inject.Singleton
import org.linketinder.usuarios.banco.ConexaoBanco
import org.linketinder.usuarios.model.Vaga
import org.linketinder.usuarios.repositorio.VagaRepositorio

import java.sql.PreparedStatement
import java.sql.ResultSet

/**
 *
 * @author Guilherme Lima Conte
 */

@Singleton
class VagaDAO implements VagaRepositorio {

    static final String CONSULTA = """
        SELECT v.id, v.nome, v.descricao, v.estado, v.cidade, v.id_empresa, e.nome AS nome_empresa,
               COALESCE(ARRAY_AGG(comp.nome ORDER BY vc.id) FILTER (WHERE comp.nome IS NOT NULL), '{}') AS competencias
        FROM vagas AS v
        JOIN empresas AS e ON e.id = v.id_empresa
        LEFT JOIN vaga_competencia AS vc ON vc.id_vaga = v.id
        LEFT JOIN competencias AS comp ON comp.id = vc.id_competencia
    """

    private final ConexaoBanco conexaoBanco

    VagaDAO(ConexaoBanco conexaoBanco) {
        this.conexaoBanco = conexaoBanco
    }

    @Override
    Vaga buscarPorId(Integer id) {
        String sql = "${CONSULTA} WHERE v.id = ? GROUP BY v.id, e.nome"

        return conexaoBanco.executar(sql.toString()) { PreparedStatement statement ->
            statement.setInt(1, id)
            statement.executeQuery().withCloseable { ResultSet resultado ->
                resultado.next() ? mapear(resultado) : null
            }
        }
    }

    @Override
    List<Vaga> listarAtribuidasAoRecrutador(Integer idRecrutador) {
        String sql = """
            ${CONSULTA}
            WHERE v.id IN (SELECT id_vaga FROM recrutador_vaga WHERE id_recrutador = ?)
            GROUP BY v.id, e.nome
            ORDER BY v.id
        """

        return conexaoBanco.executar(sql.toString()) { PreparedStatement statement ->
            statement.setInt(1, idRecrutador)
            statement.executeQuery().withCloseable { ResultSet resultado ->
                List<Vaga> vagas = []
                while (resultado.next()) {
                    vagas << mapear(resultado)
                }
                return vagas
            }
        }
    }

    private static Vaga mapear(ResultSet resultado) {
        return new Vaga(
                id: resultado.getInt("id"),
                nome: resultado.getString("nome"),
                descricao: resultado.getString("descricao"),
                estado: resultado.getString("estado"),
                cidade: resultado.getString("cidade"),
                idEmpresa: resultado.getInt("id_empresa"),
                nomeEmpresa: resultado.getString("nome_empresa"),
                competencias: (resultado.getArray("competencias").array as String[]).toList()
        )
    }
}
