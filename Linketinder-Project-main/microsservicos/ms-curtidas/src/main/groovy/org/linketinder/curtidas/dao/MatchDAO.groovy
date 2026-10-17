package org.linketinder.curtidas.dao

import jakarta.inject.Singleton
import org.linketinder.curtidas.banco.ConexaoBanco
import org.linketinder.curtidas.repositorio.MatchRepositorio

import java.sql.PreparedStatement

/**
 *
 * @author Guilherme Lima Conte
 */

@Singleton
class MatchDAO implements MatchRepositorio {

    static final String CRIAR_MATCH_SE_POSSIVEL = """
        WITH curtidas_compativeis AS (
            SELECT
                ccv.id_candidato,
                v.id_empresa,
                ccv.id_vaga,
                (
                    SELECT crc.id_recrutador
                    FROM curtida_recrutador_candidato AS crc
                    JOIN recrutadores AS r ON r.id = crc.id_recrutador
                    JOIN recrutador_vaga AS rv
                        ON rv.id_recrutador = crc.id_recrutador
                        AND rv.id_vaga = crc.id_vaga
                    WHERE crc.id_candidato = ccv.id_candidato
                      AND crc.id_vaga = ccv.id_vaga
                      AND r.id_empresa = v.id_empresa
                    ORDER BY crc.id
                    LIMIT 1
                ) AS id_recrutador
            FROM curtida_candidato_vaga AS ccv
            JOIN vagas AS v ON v.id = ccv.id_vaga
            WHERE ccv.id_candidato = ?
              AND ccv.id_vaga = ?
              AND v.id_empresa = ?
              AND (
                  EXISTS (
                      SELECT 1
                      FROM curtida_empresa_candidato AS cec
                      WHERE cec.id_empresa = v.id_empresa
                        AND cec.id_candidato = ccv.id_candidato
                        AND cec.id_vaga = ccv.id_vaga
                  )
                  OR EXISTS (
                      SELECT 1
                      FROM curtida_recrutador_candidato AS crc
                      JOIN recrutadores AS r ON r.id = crc.id_recrutador
                      JOIN recrutador_vaga AS rv
                          ON rv.id_recrutador = crc.id_recrutador
                          AND rv.id_vaga = crc.id_vaga
                      WHERE crc.id_candidato = ccv.id_candidato
                        AND crc.id_vaga = ccv.id_vaga
                        AND r.id_empresa = v.id_empresa
                  )
              )
        )
        INSERT INTO matches (id_candidato, id_empresa, id_vaga, id_recrutador)
        SELECT id_candidato, id_empresa, id_vaga, id_recrutador
        FROM curtidas_compativeis
        ON CONFLICT (id_candidato, id_empresa, id_vaga) DO NOTHING
    """

    private final ConexaoBanco conexaoBanco

    MatchDAO(ConexaoBanco conexaoBanco) {
        this.conexaoBanco = conexaoBanco
    }

    @Override
    boolean criarMatchSePossivel(Integer idCandidato, Integer idEmpresa, Integer idVaga) {
        return conexaoBanco.executar(CRIAR_MATCH_SE_POSSIVEL) { PreparedStatement statement ->
            statement.setInt(1, idCandidato)
            statement.setInt(2, idVaga)
            statement.setInt(3, idEmpresa)
            statement.executeUpdate() > 0
        }
    }
}
