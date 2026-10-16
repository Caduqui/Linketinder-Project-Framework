package org.linketinder.cadastro.repositorio

import org.linketinder.cadastro.model.Competencia

import java.sql.SQLException

/**
 *
 * @author Guilherme Lima Conte
 */

interface CompetenciaRepositorio {
    Competencia buscarPorNomeIgnorandoCaixa(String nome) throws SQLException
    Competencia inserir(String nome) throws SQLException
    List<Competencia> buscarDoCandidato(Integer idCandidato) throws SQLException
    void vincularAoCandidato(Integer idCandidato, Integer idCompetencia) throws SQLException
}
