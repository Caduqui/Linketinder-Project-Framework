package org.example.controller

import org.example.model.Empresa
import org.example.model.Recrutador
import org.example.model.Vaga
import org.example.service.RecrutadorService
import org.example.view.RecrutadorView

/**
 *
 * @author Guilherme Lima Conte
 */
class RecrutadorController implements ControllerCrud {

    private final RecrutadorService recrutadorService
    private final RecrutadorView recrutadorView
    private final EmpresaController empresaController
    private final VagaController vagaController

    RecrutadorController(RecrutadorService recrutadorService, RecrutadorView recrutadorView, EmpresaController empresaController, VagaController vagaController) {
        this.recrutadorService = recrutadorService
        this.recrutadorView = recrutadorView
        this.empresaController = empresaController
        this.vagaController = vagaController
    }

    @Override
    void listar() {
        Empresa empresa = selecionarEmpresa()
        if (empresa == null) {
            return
        }

        recrutadorView.exibir(recrutadorService.listarPorEmpresa(empresa.id))
    }

    @Override
    boolean cadastrar() {
        Empresa empresa = selecionarEmpresa()
        if (empresa == null) {
            return false
        }

        recrutadorService.cadastrar(recrutadorView.lerDados(empresa.id))
        recrutadorView.avisarCadastrado()
        return true
    }

    @Override
    boolean atualizar() {
        Recrutador recrutador = selecionarRecrutadorDaEmpresa()
        if (recrutador == null) {
            return false
        }

        recrutadorService.atualizar(recrutador.id, recrutadorView.lerDados(recrutador.idEmpresa))
        recrutadorView.avisarAtualizado()
        return true
    }

    @Override
    boolean excluir() {
        Recrutador recrutador = selecionarRecrutadorDaEmpresa()
        if (recrutador == null) {
            return false
        }

        recrutadorService.excluir(recrutador.id)
        recrutadorView.avisarExcluido()
        return true
    }

    boolean atribuirVaga() {
        Empresa empresa = selecionarEmpresa()
        if (empresa == null) {
            return false
        }

        Recrutador recrutador = selecionar(empresa)
        if (recrutador == null) {
            return false
        }

        Vaga vaga = vagaController.selecionarVagaDaEmpresa(empresa)
        if (vaga == null) {
            return false
        }

        if (!recrutadorService.atribuirVaga(recrutador.id, vaga.id)) {
            recrutadorView.avisarVagaJaAtribuida()
            return false
        }

        recrutadorView.avisarVagaAtribuida()
        return true
    }

    boolean removerVaga() {
        Recrutador recrutador = selecionarRecrutadorDaEmpresa()
        if (recrutador == null) {
            return false
        }

        Vaga vaga = selecionarVagaDoRecrutador(recrutador.id)
        if (vaga == null) {
            return false
        }

        recrutadorService.removerVaga(recrutador.id, vaga.id)
        recrutadorView.avisarVagaRemovida()
        return true
    }

    boolean listarVagas() {
        Recrutador recrutador = selecionarRecrutadorDaEmpresa()
        if (recrutador == null) {
            return false
        }

        List<Vaga> vagas = recrutadorService.listarVagasDoRecrutador(recrutador.id)
        if (vagas.isEmpty()) {
            recrutadorView.avisarSemVagasAtribuidas()
            return false
        }

        recrutadorView.exibirVagas(vagas)
        return true
    }

    Recrutador selecionar(Empresa empresa) {
        List<Recrutador> recrutadores = recrutadorService.listarPorEmpresa(empresa.id)
        recrutadorView.exibir(recrutadores)
        Integer id = recrutadorView.lerId("ID do recrutador: ")
        Recrutador recrutador = recrutadores.find { Recrutador item -> item.id == id }
        if (id != null && recrutador == null) {
            recrutadorView.avisarNaoEncontrado()
        }
        return recrutador
    }

    Vaga selecionarVagaDoRecrutador(Integer idRecrutador) {
        List<Vaga> vagas = recrutadorService.listarVagasDoRecrutador(idRecrutador)
        if (vagas.isEmpty()) {
            recrutadorView.avisarSemVagasAtribuidas()
            return null
        }

        recrutadorView.exibirVagas(vagas)
        Integer id = recrutadorView.lerId("ID da vaga: ")
        Vaga vaga = vagas.find { Vaga item -> item.id == id }
        if (id != null && vaga == null) {
            recrutadorView.avisarNaoEncontrado()
        }
        return vaga
    }

    private Recrutador selecionarRecrutadorDaEmpresa() {
        Empresa empresa = selecionarEmpresa()
        return empresa == null ? null : selecionar(empresa)
    }

    private Empresa selecionarEmpresa() {
        empresaController.listarNomes()
        return empresaController.selecionar("ID da empresa: ")
    }
}
