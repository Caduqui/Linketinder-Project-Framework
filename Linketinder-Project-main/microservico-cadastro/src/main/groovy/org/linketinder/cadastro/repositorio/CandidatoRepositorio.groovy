package org.linketinder.cadastro.repositorio

import org.linketinder.cadastro.model.Candidato

import java.sql.SQLException

/**
 *
 * @author Guilherme Lima Conte
 */

interface CandidatoRepositorio {
    void inserir(Candidato candidato) throws SQLException
    boolean existe(Integer id) throws SQLException
}
