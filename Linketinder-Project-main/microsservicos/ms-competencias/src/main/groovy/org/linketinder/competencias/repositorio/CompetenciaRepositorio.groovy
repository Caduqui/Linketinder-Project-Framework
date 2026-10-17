package org.linketinder.competencias.repositorio

import org.linketinder.competencias.model.Competencia

/**
 *
 * @author Guilherme Lima Conte
 */

interface CompetenciaRepositorio {

    List<Competencia> listar()

    Competencia buscarPorId(Integer id)

    Competencia buscarPorNomeIgnorandoCaixa(String nome)

    Competencia inserir(String nome)

    void renomear(Integer id, String nome)

    void excluir(Integer id)

    boolean estaVinculada(Integer id)

    List<Competencia> buscarDoCandidato(Integer idCandidato)

    void vincularAoCandidato(Integer idCandidato, Integer idCompetencia)
}
