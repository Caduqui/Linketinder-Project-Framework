package org.example.api

import org.example.model.Empresa
import org.example.model.Vaga
import org.example.service.RegraDeNegocioException

import javax.servlet.annotation.WebServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 *
 * @author Guilherme Lima Conte
 */

@WebServlet(name = "vagas", urlPatterns = ["/vagas", "/vagas/*"])
class VagaServlet extends ApiServlet {

    @Override
    protected void doGet(HttpServletRequest requisicao, HttpServletResponse resposta) {
        Integer id = lerIdDoCaminho(requisicao)

        if (id == null) {
            responder(resposta, HttpServletResponse.SC_OK, listarVagas(requisicao).collect { Vaga vaga ->
                Json.deVaga(vaga)
            })
            return
        }

        Vaga vaga = dependencias.vagaService.buscarPorId(id)

        if (vaga == null) {
            responderNaoEncontrado(resposta, "Vaga ${id} não encontrada")
            return
        }

        responder(resposta, HttpServletResponse.SC_OK, Json.deVaga(vaga))
    }

    @Override
    protected void doPost(HttpServletRequest requisicao, HttpServletResponse resposta) {
        Map dados = lerCorpo(requisicao)
        Integer idEmpresa = Json.exigirInteiro(dados, "idEmpresa")

        Empresa empresa = dependencias.empresaService.buscarPorId(idEmpresa)

        if (empresa == null) {
            responderErro(resposta, HttpServletResponse.SC_BAD_REQUEST, "Empresa ${idEmpresa} não encontrada")
            return
        }

        Vaga vaga = Json.paraVaga(dados, empresa)
        dependencias.vagaService.cadastrar(vaga)

        responderCriado(requisicao, resposta, Json.deVaga(vaga))
    }

    private List<Vaga> listarVagas(HttpServletRequest requisicao) {
        String idEmpresa = requisicao.getParameter("idEmpresa")

        if (idEmpresa == null) {
            return dependencias.vagaService.listar()
        }

        try {
            return dependencias.vagaService.listarPorEmpresa(Integer.parseInt(idEmpresa))
        } catch (NumberFormatException e) {
            throw new RegraDeNegocioException("O parâmetro 'idEmpresa' deve ser um número")
        }
    }
}
