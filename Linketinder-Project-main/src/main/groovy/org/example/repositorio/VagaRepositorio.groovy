package org.example.repositorio

import org.example.model.Vaga

/**
 *
 * @author Guilherme Lima Conte
 */

interface VagaRepositorio {
    void inserir(Vaga vaga)
    void atualizar(Vaga vaga)
    void deletar(Integer id)
    Vaga buscarPorId(Integer id)
    List<Vaga> listar()
    List<Vaga> listarPorEmpresa(Integer idEmpresa)
}