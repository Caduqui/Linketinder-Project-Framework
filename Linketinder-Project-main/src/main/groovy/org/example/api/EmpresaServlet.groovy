package org.example.api

import org.example.model.Empresa

import javax.servlet.annotation.WebServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 *
 * @author Guilherme Lima Conte
 */

@WebServlet(name = "empresas", urlPatterns = ["/empresas", "/empresas/*"])
class EmpresaServlet extends ApiServlet{

    @Override
    protected void doGet(HttpServletRequest requisicao, HttpServletResponse resposta) {
        Integer id = lerIdDoCaminho(requisicao)

        if (id == null) {
            responder(resposta, HttpServletResponse.SC_OK, dependencias.empresaService.listar().collect { Empresa empresa ->
                Json.deEmpresa(empresa)
            })
            return
        }

        Empresa empresa = dependencias.empresaService.buscarPorId(id)
        if (empresa == null) {
            responderNaoEncontrado(resposta, "Empresa ${id} não encontrada")
            return
        }

        responder(resposta, HttpServletResponse.SC_OK, Json.deEmpresa(empresa))
    }

    @Override
    protected void doPost(HttpServletRequest requisicao, HttpServletResponse resposta) {
        Empresa empresa = Json.paraEmpresa(lerCorpo(requisicao))
        dependencias.empresaService.cadastrar(empresa)

        responderCriado(requisicao, resposta, Json.deEmpresa(empresa))
    }
}
