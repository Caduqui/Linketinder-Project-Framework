package org.linketinder.spring.nucleo.repositorio

import org.linketinder.spring.nucleo.model.Recrutador
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

/**
 *
 * @author Guilherme Lima Conte
 */

interface RecrutadorRepositorio extends JpaRepository<Recrutador, Integer> {

    @Query("SELECT r FROM Recrutador r WHERE r.email = :email")
    Recrutador buscarPorEmail(@Param("email") String email)

    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM Recrutador r WHERE r.email = :email OR r.cpf = :cpf")
    boolean existeComEmailOuCpf(@Param("email") String email, @Param("cpf") String cpf)

    @Query("SELECT r FROM Recrutador r WHERE r.idEmpresa = :idEmpresa ORDER BY r.id")
    List<Recrutador> listarDaEmpresa(@Param("idEmpresa") Integer idEmpresa)

    @Modifying
    @Query("UPDATE Match m SET m.idRecrutador = NULL WHERE m.idRecrutador = :idRecrutador")
    void desvincularDosMatches(@Param("idRecrutador") Integer idRecrutador)

    @Modifying
    @Query("DELETE FROM CurtidaDoRecrutador c WHERE c.idRecrutador = :idRecrutador")
    void apagarCurtidas(@Param("idRecrutador") Integer idRecrutador)

    @Modifying
    @Query("DELETE FROM AtribuicaoDeVaga a WHERE a.idRecrutador = :idRecrutador")
    void apagarAtribuicoes(@Param("idRecrutador") Integer idRecrutador)
}
