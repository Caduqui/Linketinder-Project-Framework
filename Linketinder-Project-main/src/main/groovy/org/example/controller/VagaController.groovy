package org.example.controller

import org.example.model.Empresa
import org.example.model.Vaga
import org.example.service.VagaService
import org.example.view.VagaView

/**
 *
 * @author Guilherme Lima Conte
 */

class VagaController implements ControllerCrud{

    private final VagaService vagaService
    private final VagaView vagaView
    private final EmpresaController empresaController

    VagaController(VagaService vagaService, VagaView vagaView, EmpresaController empresaController) {
        this.vagaService = vagaService
        this.vagaView = vagaView
        this.empresaController = empresaController
    }

    @Override
    void listar() {
        vagaView.exibir(vagaService.listar())
    }

    @Override
    boolean cadastrar() {
        vagaService.cadastrar(vagaView.lerDados(selecionarEmpresaResponsavel()))
        vagaView.avisarCadastrado()
        return true
    }

    @Override
    boolean atualizar() {
        Vaga vaga = selecionar("ID da vaga: ")
        if (vaga == null) {
            return false
        }

        vagaService.atualizar(vaga.id, vagaView.lerDados(selecionarEmpresaResponsavel()))
        vagaView.avisarAtualizado()
        return true
    }

    @Override
    boolean excluir() {
        Vaga vaga = selecionar("ID da vaga: ")
        if (vaga == null) {
            return false
        }

        vagaService.excluir(vaga.id)
        vagaView.avisarExcluido()
        return true
    }

    Vaga selecionar(String mensagem) {
        Integer id = vagaView.lerId(mensagem)
        if (id == null) {
            return null
        }

        Vaga vaga = vagaService.buscarPorId(id)
        if (vaga == null) {
            vagaView.avisarNaoEncontrado()
        }

        return vaga
    }

    Vaga selecionarVagaDaEmpresa(Empresa empresa) {
        List<Vaga> vagasDaEmpresa = vagaService.listarPorEmpresa(empresa.id)

        if (vagasDaEmpresa.isEmpty()) {
            vagaView.avisarEmpresaSemVagas()
            return null
        }

        vagaView.exibirVagasDaEmpresa(empresa, vagasDaEmpresa)

        Vaga vaga = vagaService.buscarVagaDaEmpresa(empresa.id, vagaView.lerId("ID da vaga: "))
        if (vaga == null) {
            vagaView.avisarVagaDeOutraEmpresa()
        }
        return vaga
    }

    private Empresa selecionarEmpresaResponsavel() {
        Empresa empresa = null
        while (empresa == null) {
            empresa = empresaController.selecionar("ID da empresa responsável pela vaga: ")
        }

        return empresa
    }
}
