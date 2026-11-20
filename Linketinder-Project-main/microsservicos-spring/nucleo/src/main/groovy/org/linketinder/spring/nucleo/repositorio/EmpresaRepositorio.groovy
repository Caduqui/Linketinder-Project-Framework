package org.linketinder.spring.nucleo.repositorio

import org.linketinder.spring.nucleo.model.Empresa
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

/**
 *
 * @author Guilherme Lima Conte
 */

interface EmpresaRepositorio extends JpaRepository<Empresa, Integer> {

    @Query("SELECT e FROM Empresa e WHERE e.email = :email")
    Empresa buscarPorEmail(@Param("email") String email)

    @Query("SELECT CASE WHEN COUNT(v) > 0 THEN true ELSE false END FROM Vaga v WHERE v.idEmpresa = :idEmpresa")
    boolean possuiVagas(@Param("idEmpresa") Integer idEmpresa)

    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM Recrutador r WHERE r.idEmpresa = :idEmpresa")
    boolean possuiRecrutadores(@Param("idEmpresa") Integer idEmpresa)
}
