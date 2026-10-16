package org.example.controller

import org.example.model.Competencia
import org.example.service.CompetenciaService
import org.example.view.CompetenciaView

/**
 *
 * @author Guilherme Lima Conte
 */

class CompetenciaController implements ControllerCrud {

    private final CompetenciaService competenciaService
    private final CompetenciaView competenciaView

    CompetenciaController(CompetenciaService competenciaService, CompetenciaView competenciaView) {
        this.competenciaService = competenciaService
        this.competenciaView = competenciaView
    }

    @Override
    void listar() {
        competenciaView.exibir(competenciaService.listar())
    }

    @Override
    boolean cadastrar() {
        competenciaService.cadastrar(competenciaView.lerNome("Nome da competência: "))
        competenciaView.avisarCadastrado()
        return true
    }

    @Override
    boolean atualizar() {
        Competencia competencia = selecionar("ID da competência: ")
        if (competencia == null) {
            return false
        }

        competenciaService.renomear(competencia, competenciaView.lerNome("Novo nome: "))
        competenciaView.avisarAtualizado()
        return true
    }

    @Override
    boolean excluir() {
        Competencia competencia = selecionar("ID da competencia: ")
        if (competencia == null) {
            return false
        }

        competenciaService.excluir(competencia.id)
        competenciaView.avisarExcluido()
        return true
    }

    Competencia selecionar(String mensagem) {
        Integer id = competenciaView.lerId(mensagem)
        if (id == null) {
            return null
        }

        Competencia competencia = competenciaService.buscarPorId(id)
        if (competencia == null) {
            competenciaView.avisarNaoEncontrado()
        }

        return competencia
    }
}
