package org.example.dao

import org.example.Banco.ConexaoBanco
import org.example.repositorio.CurtidaRepositorio

import java.sql.PreparedStatement

/**
 *
 * @author Guilherme Lima Conte
 */

class CurtidaDAO implements CurtidaRepositorio{

    final ConexaoBanco conexaoBanco

    CurtidaDAO(ConexaoBanco conexaoBanco) {
        this.conexaoBanco = conexaoBanco
    }

    @Override
    boolean candidatoCurtirVaga(Integer idCandidato, Integer idVaga) {
        String sql = """
            INSERT INTO curtida_candidato_vaga (id_candidato, id_vaga) VALUES (?,?)
            ON CONFLICT (id_candidato, id_vaga) DO NOTHING
        """

        conexaoBanco.executar(sql) { PreparedStatement statement ->
            statement.setInt(1, idCandidato)
            statement.setInt(2, idVaga)
            statement.executeUpdate() > 0
        }
    }

    @Override
    boolean empresaCurtirCandidato(Integer idEmpresa, Integer idCandidato, Integer idVaga) {
        String sql = """
            INSERT INTO curtida_empresa_candidato (id_empresa, id_candidato, id_vaga) VALUES (?,?,?)
            ON CONFLICT (id_empresa, id_candidato, id_vaga) DO NOTHING
        """

        conexaoBanco.executar(sql) { PreparedStatement statement ->
            statement.setInt(1, idEmpresa)
            statement.setInt(2, idCandidato)
            statement.setInt(3, idVaga)
            statement.executeUpdate() > 0
        }
    }

    @Override
    boolean recrutadorCurtirCandidato(Integer idRecrutador, Integer idCandidato, Integer idVaga) {
        String sql = """
            INSERT INTO curtida_recrutador_candidato (id_recrutador, id_candidato, id_vaga) VALUES (?,?,?)
            ON CONFLICT (id_recrutador, id_candidato, id_vaga) DO NOTHING
        """

        conexaoBanco.executar(sql) { PreparedStatement statement ->
            statement.setInt(1, idRecrutador)
            statement.setInt(2, idCandidato)
            statement.setInt(3, idVaga)
            statement.executeUpdate() > 0
        }
    }
}
