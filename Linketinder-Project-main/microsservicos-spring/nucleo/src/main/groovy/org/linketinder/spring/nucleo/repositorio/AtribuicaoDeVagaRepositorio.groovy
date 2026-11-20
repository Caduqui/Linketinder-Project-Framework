package org.linketinder.spring.nucleo.repositorio

import org.linketinder.spring.nucleo.model.AtribuicaoDeVaga
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.Repository
import org.springframework.data.repository.query.Param

/**
 *
 * @author Guilherme Lima Conte
 */

interface AtribuicaoDeVagaRepositorio extends Repository<AtribuicaoDeVaga, Integer> {

    @Query("SELECT r.idEmpresa FROM Recrutador r WHERE r.id = :idRecrutador")
    Integer buscarEmpresaDoRecrutador(@Param("idRecrutador") Integer idRecrutador)

    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM AtribuicaoDeVaga a WHERE a.idRecrutador = :idRecrutador AND a.idVaga = :idVaga")
    boolean estaAtribuida(@Param("idRecrutador") Integer idRecrutador, @Param("idVaga") Integer idVaga)

    @Modifying
    @Query(value = "INSERT INTO recrutador_vaga (id_recrutador, id_vaga) VALUES (:idRecrutador, :idVaga) ON CONFLICT (id_recrutador, id_vaga) DO NOTHING", nativeQuery = true)
    int atribuir(@Param("idRecrutador") Integer idRecrutador, @Param("idVaga") Integer idVaga)

    @Modifying
    @Query("UPDATE Match m SET m.idRecrutador = NULL WHERE m.idRecrutador = :idRecrutador AND m.idVaga = :idVaga")
    void desvincularDosMatches(@Param("idRecrutador") Integer idRecrutador, @Param("idVaga") Integer idVaga)

    @Modifying
    @Query("DELETE FROM CurtidaDoRecrutador c WHERE c.idRecrutador = :idRecrutador AND c.idVaga = :idVaga")
    void apagarCurtidas(@Param("idRecrutador") Integer idRecrutador, @Param("idVaga") Integer idVaga)

    @Modifying
    @Query("DELETE FROM AtribuicaoDeVaga a WHERE a.idRecrutador = :idRecrutador AND a.idVaga = :idVaga")
    void remover(@Param("idRecrutador") Integer idRecrutador, @Param("idVaga") Integer idVaga)
}
