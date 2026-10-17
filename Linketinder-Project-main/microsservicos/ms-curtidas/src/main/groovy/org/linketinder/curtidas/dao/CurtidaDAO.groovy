package org.linketinder.curtidas.dao

import jakarta.inject.Singleton
import org.linketinder.curtidas.banco.ConexaoBanco
import org.linketinder.curtidas.repositorio.CurtidaRepositorio

import java.sql.PreparedStatement

/**
 *
 * @author Guilherme Lima Conte
 */

@Singleton
class CurtidaDAO implements CurtidaRepositorio {

    private final ConexaoBanco conexaoBanco

    CurtidaDAO(ConexaoBanco conexaoBanco) {
        this.conexaoBanco = conexaoBanco
    }

    @Override
    boolean candidatoCurtirVaga(Integer idCandidato, Integer idVaga) {
        String sql = """
            INSERT INTO curtida_candidato_vaga (id_candidato, id_vaga) VALUES (?, ?)
            ON CONFLICT (id_candidato, id_vaga) DO NOTHING
        """

        return inserir(sql, [idCandidato, idVaga])
    }

    @Override
    boolean empresaCurtirCandidato(Integer idEmpresa, Integer idCandidato, Integer idVaga) {
        String sql = """
            INSERT INTO curtida_empresa_candidato (id_empresa, id_candidato, id_vaga) VALUES (?, ?, ?)
            ON CONFLICT (id_empresa, id_candidato, id_vaga) DO NOTHING
        """

        return inserir(sql, [idEmpresa, idCandidato, idVaga])
    }

    @Override
    boolean recrutadorCurtirCandidato(Integer idRecrutador, Integer idCandidato, Integer idVaga) {
        String sql = """
            INSERT INTO curtida_recrutador_candidato (id_recrutador, id_candidato, id_vaga) VALUES (?, ?, ?)
            ON CONFLICT (id_recrutador, id_candidato, id_vaga) DO NOTHING
        """

        return inserir(sql, [idRecrutador, idCandidato, idVaga])
    }

    private boolean inserir(String sql, List<Integer> ids) {
        return conexaoBanco.executar(sql) { PreparedStatement statement ->
            ids.eachWithIndex { Integer id, int posicao ->
                statement.setInt(posicao + 1, id)
            }
            statement.executeUpdate() > 0
        }
    }
}
