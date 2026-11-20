package org.linketinder.spring.nucleo.repositorio

import org.linketinder.spring.nucleo.model.Vaga
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

/**
 *
 * @author Guilherme Lima Conte
 */

interface VagaRepositorio extends JpaRepository<Vaga, Integer> {

    @Query("SELECT v FROM Vaga v ORDER BY v.id")
    List<Vaga> listarTodas()

    @Query("SELECT v FROM Vaga v WHERE v.idEmpresa = :idEmpresa ORDER BY v.id")
    List<Vaga> listarDaEmpresa(@Param("idEmpresa") Integer idEmpresa)

    @Query("SELECT v FROM Vaga v WHERE v.id IN (SELECT a.idVaga FROM AtribuicaoDeVaga a WHERE a.idRecrutador = :idRecrutador) ORDER BY v.id")
    List<Vaga> listarDoRecrutador(@Param("idRecrutador") Integer idRecrutador)

    @Modifying
    @Query("DELETE FROM CurtidaDoCandidato c WHERE c.idVaga = :idVaga")
    void apagarCurtidasDeCandidatos(@Param("idVaga") Integer idVaga)

    @Modifying
    @Query("DELETE FROM CurtidaDaEmpresa c WHERE c.idVaga = :idVaga")
    void apagarCurtidasDaEmpresa(@Param("idVaga") Integer idVaga)

    @Modifying
    @Query("DELETE FROM CurtidaDoRecrutador c WHERE c.idVaga = :idVaga")
    void apagarCurtidasDeRecrutadores(@Param("idVaga") Integer idVaga)

    @Modifying
    @Query("DELETE FROM Match m WHERE m.idVaga = :idVaga")
    void apagarMatches(@Param("idVaga") Integer idVaga)

    @Modifying
    @Query("DELETE FROM AtribuicaoDeVaga a WHERE a.idVaga = :idVaga")
    void apagarAtribuicoes(@Param("idVaga") Integer idVaga)

    /**
     * Competências gravadas no banco principal pelo backend legado ou pelo Micronaut: sem apagá-las, a FK impede a exclusão.
     */
    @Modifying
    @Query(value = "DELETE FROM vaga_competencia WHERE id_vaga = :idVaga", nativeQuery = true)
    void apagarCompetenciasDoBancoPrincipal(@Param("idVaga") Integer idVaga)
}
