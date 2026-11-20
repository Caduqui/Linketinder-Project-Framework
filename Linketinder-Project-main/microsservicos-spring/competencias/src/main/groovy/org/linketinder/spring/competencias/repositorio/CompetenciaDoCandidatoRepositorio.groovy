package org.linketinder.spring.competencias.repositorio

import org.linketinder.spring.competencias.model.CompetenciaDoCandidato
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

/**
 *
 * @author Guilherme Lima Conte
 */

interface CompetenciaDoCandidatoRepositorio extends JpaRepository<CompetenciaDoCandidato, Integer> {

    @Query("SELECT cc FROM CompetenciaDoCandidato cc JOIN FETCH cc.competencia WHERE cc.idCandidato = :idCandidato ORDER BY cc.id")
    List<CompetenciaDoCandidato> listarDoCandidato(@Param("idCandidato") Integer idCandidato)

    @Query("SELECT cc FROM CompetenciaDoCandidato cc JOIN FETCH cc.competencia WHERE cc.idCandidato IN :idsCandidatos ORDER BY cc.id")
    List<CompetenciaDoCandidato> listarDosCandidatos(@Param("idsCandidatos") List<Integer> idsCandidatos)

    @Query("SELECT cc FROM CompetenciaDoCandidato cc WHERE cc.idCandidato = :idCandidato AND cc.competencia.id = :idCompetencia")
    CompetenciaDoCandidato buscarLigacao(@Param("idCandidato") Integer idCandidato, @Param("idCompetencia") Integer idCompetencia)

    @Modifying
    @Query("DELETE FROM CompetenciaDoCandidato cc WHERE cc.idCandidato = :idCandidato")
    void apagarDoCandidato(@Param("idCandidato") Integer idCandidato)
}
