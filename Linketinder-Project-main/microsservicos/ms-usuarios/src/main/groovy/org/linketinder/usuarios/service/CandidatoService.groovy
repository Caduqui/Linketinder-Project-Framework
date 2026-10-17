package org.linketinder.usuarios.service

import jakarta.inject.Singleton
import org.linketinder.usuarios.model.Candidato
import org.linketinder.usuarios.repositorio.CandidatoRepositorio
import org.linketinder.usuarios.repositorio.Transacao

/**
 *
 * @author Guilherme Lima Conte
 */

@Singleton
class CandidatoService {

    private final CandidatoRepositorio candidatoRepositorio
    private final ValidadorDeCadastro validador
    private final Transacao transacao

    CandidatoService(CandidatoRepositorio candidatoRepositorio, ValidadorDeCadastro validador, Transacao transacao) {
        this.candidatoRepositorio = candidatoRepositorio
        this.validador = validador
        this.transacao = transacao
    }

    List<Candidato> listar() {
        return candidatoRepositorio.listar()
    }

    Candidato buscar(Integer id) {
        Candidato candidato = candidatoRepositorio.buscarPorId(id)

        if (candidato == null) {
            throw new RecursoNaoEncontradoException("Candidato ${id} não encontrado")
        }
        return candidato
    }

    Candidato cadastrar(Map<String, Object> dados) {
        Candidato candidato = validador.validarCandidato(dados)
        return candidatoRepositorio.inserir(candidato)
    }

    Candidato atualizar(Integer id, Map<String, Object> dados) {
        buscar(id)

        Candidato candidato = validador.validarCandidato(dados)
        candidato.id = id
        candidatoRepositorio.atualizar(candidato)

        return buscar(id)
    }

    void excluir(Integer id) {
        buscar(id)
        transacao.executar {
            candidatoRepositorio.excluir(id)
        }
    }
}
