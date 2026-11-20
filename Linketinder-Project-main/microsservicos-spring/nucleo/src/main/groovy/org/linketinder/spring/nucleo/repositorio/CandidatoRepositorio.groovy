package org.linketinder.spring.nucleo.repositorio

import org.linketinder.spring.nucleo.model.Candidato
import org.linketinder.spring.nucleo.model.CandidatoAnonimo
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

/**
 *
 * @author Guilherme Lima Conte
 */

interface CandidatoRepositorio extends JpaRepository<Candidato, Integer> {

    @Query("SELECT new org.linketinder.spring.nucleo.model.CandidatoAnonimo(c.id, c.formacao, c.descricao) FROM Candidato c ORDER BY c.id")
    List<CandidatoAnonimo> listarAnonimos()

    @Query("SELECT new org.linketinder.spring.nucleo.model.CandidatoAnonimo(c.id, c.formacao, c.descricao) FROM Candidato c WHERE c.id = :id")
    CandidatoAnonimo buscarAnonimo(@Param("id") Integer id)

    @Query("SELECT c FROM Candidato c WHERE c.email = :email")
    Candidato buscarPorEmail(@Param("email") String email)

    @Modifying
    @Query("DELETE FROM CurtidaDoCandidato c WHERE c.idCandidato = :idCandidato")
    void apagarCurtidasDadas(@Param("idCandidato") Integer idCandidato)

    @Modifying
    @Query("DELETE FROM CurtidaDaEmpresa c WHERE c.idCandidato = :idCandidato")
    void apagarCurtidasDeEmpresas(@Param("idCandidato") Integer idCandidato)

    @Modifying
    @Query("DELETE FROM CurtidaDoRecrutador c WHERE c.idCandidato = :idCandidato")
    void apagarCurtidasDeRecrutadores(@Param("idCandidato") Integer idCandidato)

    @Modifying
    @Query("DELETE FROM Match m WHERE m.idCandidato = :idCandidato")
    void apagarMatches(@Param("idCandidato") Integer idCandidato)

    /**
     * Competências gravadas no banco principal pelo backend legado ou pelo Micronaut: sem apagá-las, a FK impede a exclusão.
     */
    @Modifying
    @Query(value = "DELETE FROM candidato_competencia WHERE id_candidato = :idCandidato", nativeQuery = true)
    void apagarCompetenciasDoBancoPrincipal(@Param("idCandidato") Integer idCandidato)
}
