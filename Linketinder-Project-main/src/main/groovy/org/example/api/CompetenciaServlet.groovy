package org.example.api

import org.example.model.Candidato
import org.example.model.Competencia

import javax.servlet.annotation.WebServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 *
 * @author Guilherme Lima Conte
 */

@WebServlet(name = "competencias", urlPatterns = ["/competencias"])
class CompetenciaServlet extends ApiServlet {

    @Override
    protected void doGet(HttpServletRequest requisicao, HttpServletResponse resposta) {
        responder (resposta, HttpServletResponse.SC_OK, dependencias.competenciaService.listar().collect { Competencia competencia ->
            Json.deCompetencia(competencia)
        })
    }

    @Override
    protected void doPost(HttpServletRequest requisicao, HttpServletResponse resposta) {
        Map dados = lerCorpo(requisicao)
        Integer idCandidato = Json.exigirInteiro(dados, "idCandidato")
        List<String> competencias = Json.competencias(dados)

        Candidato candidato = dependencias.candidatoService.adicionarCompetencias(idCandidato, competencias)

        if (candidato == null) {
            responderNaoEncontrado(resposta, "Candidato ${idCandidato} não encontrado")
            return
        }

        responder(resposta, HttpServletResponse.SC_CREATED, Json.deCandidato(candidato))
    }
}
