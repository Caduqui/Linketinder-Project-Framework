package org.linketinder.spring.competencias.repositorio

import org.linketinder.spring.competencias.model.Competencia
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

/**
 *
 * @author Guilherme Lima Conte
 */

interface CompetenciaRepositorio extends JpaRepository<Competencia, Integer> {

    @Query("SELECT c FROM Competencia c ORDER BY c.id")
    List<Competencia> listarTodas()

    @Query("SELECT c FROM Competencia c WHERE LOWER(c.nome) = LOWER(:nome)")
    Competencia buscarPorNomeIgnorandoCaixa(@Param("nome") String nome)

    @Query("""
        SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END
        FROM Competencia c
        WHERE c.id = :id
          AND (EXISTS (SELECT 1 FROM CompetenciaDoCandidato cc WHERE cc.competencia.id = c.id)
               OR EXISTS (SELECT 1 FROM CompetenciaDaVaga cv WHERE cv.competencia.id = c.id))
    """)
    boolean estaVinculada(@Param("id") Integer id)
}
