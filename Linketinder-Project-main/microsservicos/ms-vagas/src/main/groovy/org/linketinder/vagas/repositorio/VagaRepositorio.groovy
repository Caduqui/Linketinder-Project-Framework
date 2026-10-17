package org.linketinder.vagas.repositorio

import org.linketinder.vagas.model.Vaga

/**
 *
 * @author Guilherme Lima Conte
 */

interface VagaRepositorio {

    List<Vaga> listar()

    List<Vaga> listarPorEmpresa(Integer idEmpresa)

    Vaga buscarPorId(Integer id)

    Vaga inserir(Vaga vaga)

    void atualizar(Vaga vaga)

    void excluir(Integer id)

    void vincularCompetencia(Integer idVaga, Integer idCompetencia)

    void desvincularCompetencias(Integer idVaga)
}
