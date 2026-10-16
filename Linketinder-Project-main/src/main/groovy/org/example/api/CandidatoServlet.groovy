package org.example.api

import org.example.model.Candidato

import javax.servlet.annotation.WebServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 *
 * @author Guilherme Lima Conte
 */

@WebServlet(name = "candidatos", urlPatterns = ["/candidatos", "/candidatos/*"])
class CandidatoServlet extends ApiServlet{

    @Override
    protected void doGet(HttpServletRequest requisicao, HttpServletResponse resposta) {
        Integer id = lerIdDoCaminho(requisicao)

        if (id == null) {
            responder(resposta, HttpServletResponse.SC_OK, dependencias.candidatoService.listar().collect { Candidato candidato ->
                Json.deCandidato(candidato)
            })
            return
        }

        Candidato candidato = dependencias.candidatoService.buscarPorId(id)
        if (candidato == null) {
            responderNaoEncontrado(resposta, "Candidato ${id} não encontrado")
            return
        }

        responder(resposta, HttpServletResponse.SC_OK, Json.deCandidato(candidato))
    }

    @Override
    protected void doPost(HttpServletRequest requisicao, HttpServletResponse resposta) {
        Candidato candidato = Json.paraCandidato(lerCorpo(requisicao))
        dependencias.candidatoService.cadastrar(candidato)

        responderCriado(requisicao, resposta, Json.deCandidato(candidato))
    }
}
