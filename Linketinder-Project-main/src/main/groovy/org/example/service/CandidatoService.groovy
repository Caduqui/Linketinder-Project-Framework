package org.example.service

import org.example.model.Candidato
import org.example.repositorio.CandidatoRepositorio

/**
 *
 * @author Guilherme Lima Conte
 */

class CandidatoService {

    private final CandidatoRepositorio candidatoRepositorio

    CandidatoService(CandidatoRepositorio candidatoRepositorio) {
        this.candidatoRepositorio = candidatoRepositorio
    }

    List<Candidato> listar() {
        return candidatoRepositorio.listar()
    }

    Candidato buscarPorId(Integer id) {
        return candidatoRepositorio.buscarPorId(id)
    }

    void cadastrar(Candidato candidato) {
        candidatoRepositorio.inserir(candidato)
    }

    Candidato autenticar(String email, String senha) {
        Candidato candidato = candidatoRepositorio.buscarPorEmail(email)

        if (candidato == null || candidato.senha != senha) {
            return null
        }

        return candidato
    }

    Candidato adicionarCompetencias(Integer idCandidato, List<String> novasCompetencias) {
        if (!novasCompetencias) {
            throw new RegraDeNegocioException("Informe ao menos uma competência")
        }

        Candidato candidato = candidatoRepositorio.buscarPorId(idCandidato)

        if (candidato == null) {
            return null
        }

        candidato.competencias = (candidato.competencias + novasCompetencias).unique { String competencia ->
            competencia.toLowerCase()
        }
        candidatoRepositorio.atualizar(candidato)

        return candidato
    }

    void atualizar(Integer id, Candidato dados) {
        dados.id = id
        candidatoRepositorio.atualizar(dados)
    }

    void excluir(Integer id) {
        candidatoRepositorio.deletar(id)
    }
}




