package org.example.controller

import org.example.model.Empresa
import org.example.service.EmpresaService
import org.example.view.EmpresaView

/**
 *
 * @author Guilherme Lima Conte
 */

class EmpresaController implements ControllerCrud {

    private final EmpresaService empresaService
    private final EmpresaView empresaView

    EmpresaController(EmpresaService empresaService, EmpresaView empresaView) {
        this.empresaService = empresaService
        this.empresaView = empresaView
    }

    @Override
    void listar() {
        empresaView.exibir(empresaService.listar())
    }

    void listarNomes() {
        empresaView.exibirNomes(empresaService.listar())
    }

    @Override
    boolean cadastrar() {
        empresaService.cadastrar(empresaView.lerDados())
        empresaView.avisarCadastrado()
        return true
    }

    @Override
    boolean atualizar() {
        Empresa empresa = selecionar("ID da empresa: ")
        if (empresa == null) {
            return false
        }

        empresaService.atualizar(empresa.id, empresaView.lerDados())
        empresaView.avisarAtualizado()
        return true
    }

    @Override
    boolean excluir() {
        Empresa empresa = selecionar("ID da empresa: ")
        if (empresa == null) {
            return false
        }

        empresaService.excluir(empresa.id)
        empresaView.avisarExcluido()
        return true
    }

    Empresa selecionar(String mensagem) {
        Integer id = empresaView.lerId(mensagem)
        if (id == null) {
            return null
        }

        Empresa empresa = empresaService.buscarPorId(id)

        if (empresa == null) {
            empresaView.avisarNaoEncontrado()
        }
        return empresa
    }
}
