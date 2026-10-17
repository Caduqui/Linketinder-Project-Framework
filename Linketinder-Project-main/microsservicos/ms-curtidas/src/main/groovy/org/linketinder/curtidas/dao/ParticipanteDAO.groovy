package org.linketinder.curtidas.dao

import jakarta.inject.Singleton
import org.linketinder.curtidas.banco.ConexaoBanco
import org.linketinder.curtidas.repositorio.ParticipanteRepositorio

import java.sql.PreparedStatement
import java.sql.ResultSet

/**
 *
 * @author Guilherme Lima Conte
 */

@Singleton
class ParticipanteDAO implements ParticipanteRepositorio {

    private final ConexaoBanco conexaoBanco

    ParticipanteDAO(ConexaoBanco conexaoBanco) {
        this.conexaoBanco = conexaoBanco
    }

    @Override
    boolean candidatoExiste(Integer idCandidato) {
        return buscarInteiro("SELECT 1 FROM candidatos WHERE id = ?", idCandidato) != null
    }

    @Override
    Integer buscarEmpresaDaVaga(Integer idVaga) {
        return buscarInteiro("SELECT id_empresa FROM vagas WHERE id = ?", idVaga)
    }

    @Override
    Integer buscarEmpresaDoRecrutador(Integer idRecrutador) {
        return buscarInteiro("SELECT id_empresa FROM recrutadores WHERE id = ?", idRecrutador)
    }

    @Override
    boolean vagaAtribuidaAoRecrutador(Integer idRecrutador, Integer idVaga) {
        String sql = "SELECT EXISTS (SELECT 1 FROM recrutador_vaga WHERE id_recrutador = ? AND id_vaga = ?)"

        return conexaoBanco.executar(sql) { PreparedStatement statement ->
            statement.setInt(1, idRecrutador)
            statement.setInt(2, idVaga)
            statement.executeQuery().withCloseable { ResultSet resultado ->
                resultado.next() && resultado.getBoolean(1)
            }
        }
    }

    private Integer buscarInteiro(String sql, Integer parametro) {
        return conexaoBanco.executar(sql) { PreparedStatement statement ->
            statement.setInt(1, parametro)
            statement.executeQuery().withCloseable { ResultSet resultado ->
                resultado.next() ? resultado.getInt(1) : null
            }
        }
    }
}
