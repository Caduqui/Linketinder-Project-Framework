package org.example.service

import org.example.model.Competencia
import org.example.repositorio.CompetenciaRepositorio

/**
 *
 * @author Guilherme Lima Conte
 */

class CompetenciaService {

    private final CompetenciaRepositorio competenciaRepositorio

    CompetenciaService(CompetenciaRepositorio competenciaRepositorio) {
        this.competenciaRepositorio = competenciaRepositorio
    }

    List<Competencia> listar() {
        return competenciaRepositorio.listar()
    }

    Competencia buscarPorId(Integer id) {
        return competenciaRepositorio.buscarPorId(id)
    }

    void cadastrar(String nome) {
        String nomeValido = validarNome(nome)
        competenciaRepositorio.inserir(new Competencia(nomeValido))
    }

    void renomear(Competencia competencia, String novoNome) {
        competencia.nome = validarNome(novoNome)
        competenciaRepositorio.atualizar(competencia)
    }

    void excluir(Integer id) {
        if (competenciaRepositorio.estaVinculadoACandidatoOuVaga(id)) {
            throw new RegraDeNegocioException("Não é possível excluir a competência porque ela está relacionada a algum candidato ou vaga")
        }
        competenciaRepositorio.deletar(id)
    }

    private String validarNome(String nome) {
        String nomeLimpo = nome == null ? "" : nome.trim()

        if (nomeLimpo.isEmpty()) {
            throw new RegraDeNegocioException("O nome da competência não pode ser vazio.")
        }

        if (competenciaRepositorio.buscarPorNome(nomeLimpo) != null) {
            throw new RegraDeNegocioException("Essa competência já está cadastrada!")
        }

        return nomeLimpo
    }
}
