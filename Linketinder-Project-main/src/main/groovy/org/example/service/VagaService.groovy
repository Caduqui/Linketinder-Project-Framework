package org.example.service

import org.example.model.Vaga
import org.example.repositorio.VagaRepositorio

/**
 *
 * @author Guilherme Lima Conte
 */

class VagaService {

    private final VagaRepositorio vagaRepositorio

    VagaService(VagaRepositorio vagaRepositorio) {
        this.vagaRepositorio = vagaRepositorio
    }

    List<Vaga> listar() {
        return vagaRepositorio.listar()
    }

    List<Vaga> listarPorEmpresa(Integer idEmpresa) {
        return vagaRepositorio.listarPorEmpresa(idEmpresa)
    }

    Vaga buscarPorId(Integer id) {
        return vagaRepositorio.buscarPorId(id)
    }

    Vaga buscarVagaDaEmpresa(Integer idEmpresa, Integer idVaga) {
        return listarPorEmpresa(idEmpresa).find() {
            it.id == idVaga
        }
    }

    void cadastrar(Vaga vaga) {
        vagaRepositorio.inserir(vaga)
    }

    void atualizar(Integer id, Vaga dados) {
        dados.id = id
        vagaRepositorio.atualizar(dados)
    }

    void excluir(Integer id) {
        vagaRepositorio.deletar(id)
    }
}
