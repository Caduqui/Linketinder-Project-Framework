package org.linketinder.spring.nucleo.repositorio

import org.linketinder.spring.nucleo.model.Match
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.Repository
import org.springframework.data.repository.query.Param

/**
 *
 * @author Guilherme Lima Conte
 */

interface MatchRepositorio extends Repository<Match, Integer> {

    @Modifying
    @Query(value = """
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
            WHERE ccv.id_candidato = :idCandidato
              AND ccv.id_vaga = :idVaga
              AND v.id_empresa = :idEmpresa
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
    """, nativeQuery = true)
    int criarMatchSePossivel(@Param("idCandidato") Integer idCandidato, @Param("idEmpresa") Integer idEmpresa, @Param("idVaga") Integer idVaga)
}
