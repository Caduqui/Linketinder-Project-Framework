package org.example.controller

import org.example.model.Candidato
import org.example.service.CandidatoService
import org.example.view.CandidatoView

/**
 *
 * @author Guilherme Lima Conte
 */

class CandidatoController implements ControllerCrud {

    private final CandidatoService candidatoService
    private final CandidatoView candidatoView

    CandidatoController(CandidatoService candidatoService, CandidatoView candidatoView) {
        this.candidatoService = candidatoService
        this.candidatoView = candidatoView
    }

    @Override
    void listar() {
        candidatoView.exibir(candidatoService.listar())
    }

    void listarNomes() {
        candidatoView.exibirNomes(candidatoService.listar())
    }

    void listarAnonimos() {
        candidatoView.exibirAnonimos(candidatoService.listar())
    }

    @Override
    boolean cadastrar() {
        candidatoService.cadastrar(candidatoView.lerDados())
        candidatoView.avisarCadastrado()
        return true
    }

    @Override
    boolean atualizar() {
        Candidato candidato = selecionar("ID do candidato: ")
        if (candidato == null) {
            return false
        }

        candidatoService.atualizar(candidato.id, candidatoView.lerDados())
        candidatoView.avisarAtualizado()
        return true
    }

    @Override
    boolean excluir() {
        Candidato candidato = selecionar("ID do candidato: ")
        if (candidato == null) {
            return false
        }

        candidatoService.excluir(candidato.id)
        candidatoView.avisarExcluido()
        return true
    }

    Candidato selecionar(String mensagem) {
        Integer id = candidatoView.lerId(mensagem)
        if (id == null) {
            return null
        }

        Candidato candidato = candidatoService.buscarPorId(id)
        if (candidato == null) {
            candidatoView.avisarNaoEncontrado()
        }
        return candidato
    }

}

