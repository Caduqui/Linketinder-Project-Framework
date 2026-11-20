package org.linketinder.spring.nucleo.repositorio

import org.linketinder.spring.nucleo.model.CurtidaDoCandidato
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.Repository
import org.springframework.data.repository.query.Param

/**
 *
 * @author Guilherme Lima Conte
 */

interface CurtidaRepositorio extends Repository<CurtidaDoCandidato, Integer> {

    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Candidato c WHERE c.id = :idCandidato")
    boolean candidatoExiste(@Param("idCandidato") Integer idCandidato)

    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM Recrutador r WHERE r.id = :idRecrutador")
    boolean recrutadorExiste(@Param("idRecrutador") Integer idRecrutador)

    @Query("SELECT v.idEmpresa FROM Vaga v WHERE v.id = :idVaga")
    Integer buscarEmpresaDaVaga(@Param("idVaga") Integer idVaga)

    @Query("""
        SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END
        FROM AtribuicaoDeVaga a, Recrutador r, Vaga v
        WHERE a.idRecrutador = :idRecrutador AND a.idVaga = :idVaga
          AND r.id = a.idRecrutador AND v.id = a.idVaga AND r.idEmpresa = v.idEmpresa
    """)
    boolean vagaAtribuidaAoRecrutador(@Param("idRecrutador") Integer idRecrutador, @Param("idVaga") Integer idVaga)

    @Modifying
    @Query(value = """
        INSERT INTO curtida_candidato_vaga (id_candidato, id_vaga) VALUES (:idCandidato, :idVaga)
        ON CONFLICT (id_candidato, id_vaga) DO NOTHING
    """, nativeQuery = true)
    int candidatoCurtirVaga(@Param("idCandidato") Integer idCandidato, @Param("idVaga") Integer idVaga)

    @Modifying
    @Query(value = """
        INSERT INTO curtida_empresa_candidato (id_empresa, id_candidato, id_vaga) VALUES (:idEmpresa, :idCandidato, :idVaga)
        ON CONFLICT (id_empresa, id_candidato, id_vaga) DO NOTHING
    """, nativeQuery = true)
    int empresaCurtirCandidato(@Param("idEmpresa") Integer idEmpresa, @Param("idCandidato") Integer idCandidato, @Param("idVaga") Integer idVaga)

    @Modifying
    @Query(value = """
        INSERT INTO curtida_recrutador_candidato (id_recrutador, id_candidato, id_vaga) VALUES (:idRecrutador, :idCandidato, :idVaga)
        ON CONFLICT (id_recrutador, id_candidato, id_vaga) DO NOTHING
    """, nativeQuery = true)
    int recrutadorCurtirCandidato(@Param("idRecrutador") Integer idRecrutador, @Param("idCandidato") Integer idCandidato, @Param("idVaga") Integer idVaga)
}
