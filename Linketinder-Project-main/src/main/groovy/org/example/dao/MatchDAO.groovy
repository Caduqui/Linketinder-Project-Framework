package org.example.dao

import org.example.Banco.ConexaoBanco
import org.example.model.Match
import org.example.repositorio.CandidatoRepositorio
import org.example.repositorio.EmpresaRepositorio
import org.example.repositorio.MatchRepositorio
import org.example.repositorio.VagaRepositorio

import java.sql.PreparedStatement
import java.sql.ResultSet

/**
 *
 * @author Guilherme Lima Conte
 */

class MatchDAO implements MatchRepositorio{

    final ConexaoBanco conexaoBanco
    final CandidatoRepositorio candidatoRepositorio
    final EmpresaRepositorio empresaRepositorio
    final VagaRepositorio vagaRepositorio

    MatchDAO(ConexaoBanco conexaoBanco, CandidatoRepositorio candidatoRepositorio, EmpresaRepositorio empresaRepositorio, VagaRepositorio vagaRepositorio) {
        this.conexaoBanco = conexaoBanco
        this.candidatoRepositorio = candidatoRepositorio
        this.empresaRepositorio = empresaRepositorio
        this.vagaRepositorio = vagaRepositorio
    }

    @Override
    boolean criarMatchSePossivel(Integer idCandidato, Integer idEmpresa, Integer idVaga) {
        String sql = """
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

        conexaoBanco.executar(sql) { PreparedStatement statement ->
            statement.setInt(1, idCandidato)
            statement.setInt(2, idVaga)
            statement.setInt(3, idEmpresa)
            statement.executeUpdate() > 0
        }
    }

    @Override
    List<Match> listar() {
        String sql = """
            SELECT * FROM matches ORDER BY id
        """

        conexaoBanco.executar(sql) { PreparedStatement statement ->
            statement.executeQuery().withCloseable { ResultSet resultado ->
                List<Match> matches = []
                while (resultado.next()) {
                    matches << mapearMatch(resultado)
                }
                return matches
            }
        }
    }

    Match mapearMatch(ResultSet resultado) {
        Integer idRecrutador = resultado.getInt("id_recrutador")
        if (resultado.wasNull()) {
            idRecrutador = null
        }

        return new Match(
                candidatoRepositorio.buscarPorId(resultado.getInt("id_candidato")),
                empresaRepositorio.buscarPorId(resultado.getInt("id_empresa")),
                vagaRepositorio.buscarPorId(resultado.getInt("id_vaga")),
                idRecrutador
        )
    }
}
