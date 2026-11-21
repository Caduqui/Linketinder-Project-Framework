package org.linketinder.spring.competencias.repositorio

import org.linketinder.spring.competencias.model.CompetenciaDaVaga
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

/**
 *
 * @author Guilherme Lima Conte
 */

interface CompetenciaDaVagaRepositorio extends JpaRepository<CompetenciaDaVaga, Integer> {

    @Query("SELECT cv FROM CompetenciaDaVaga cv JOIN FETCH cv.competencia WHERE cv.idVaga IN :idsVagas ORDER BY cv.id")
    List<CompetenciaDaVaga> listarDasVagas(@Param("idsVagas") List<Integer> idsVagas)

    @Modifying(flushAutomatically = true)
    @Query("DELETE FROM CompetenciaDaVaga cv WHERE cv.idVaga = :idVaga")
    void apagarDaVaga(@Param("idVaga") Integer idVaga)
}
