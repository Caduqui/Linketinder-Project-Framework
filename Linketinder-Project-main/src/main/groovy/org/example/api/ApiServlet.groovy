package org.example.api

import groovy.json.JsonException
import groovy.json.JsonSlurper
import groovy.json.JsonOutput
import org.example.service.RegraDeNegocioException

import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import java.sql.SQLException

/**
 *
 * @author Guilherme Lima Conte
 */

abstract class ApiServlet extends HttpServlet {

    static final String TIPO_JSON = "application/json; charset=UTF-8"
    static final String METODOS_ACEITOS = "GET, POST"

    protected Dependencias dependencias

    protected String metodosAceitos() {
        return METODOS_ACEITOS
    }

    @Override
    void init() {
        dependencias = Dependencias.obterInstancia()
    }

    @Override
    protected void service(HttpServletRequest requisicao, HttpServletResponse resposta) {
        try {
            super.service(requisicao, resposta)
        } catch (RegraDeNegocioException e) {
            responderErro(resposta, HttpServletResponse.SC_BAD_REQUEST, e.message)
        } catch (JsonException e){
            responderErro(resposta, HttpServletResponse.SC_BAD_REQUEST, "JSON inválido: ${e.message}")
        } catch (SQLException e) {
            log("Erro ao acessar o banco de dados", e)
            responderErro(resposta, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Erro ao acessar o banco de dados")
        }
    }

    protected Map lerCorpo(HttpServletRequest requisicao) {
        requisicao.characterEncoding = "UTF-8"
        String corpo = requisicao.reader.text

        if (corpo == null || corpo.trim().isEmpty()) {
            throw new RegraDeNegocioException("O corpo da requisição não pode ser vazio")
        }

        Object dados = new JsonSlurper().parseText(corpo)
        if (!(dados instanceof Map)) {
            throw new RegraDeNegocioException("O corpo da requisição deve ser um objeto JSON")
        }
        return dados

    }

    protected Integer lerIdDoCaminho(HttpServletRequest requisicao) {
        String caminho = requisicao.pathInfo

        if (caminho == null || caminho == "/") {
            return null
        }

        try {
            return Integer.parseInt(caminho.substring(1))
        } catch (NumberFormatException e) {
            throw new RegraDeNegocioException("O ID informado na URL não é um número")
        }
    }

    protected void responder(HttpServletResponse resposta, int status, Object corpo) {
        resposta.status = status
        resposta.contentType = TIPO_JSON
        resposta.writer.write(JsonOutput.toJson(corpo))
    }

    protected void responderCriado(HttpServletRequest requisicao, HttpServletResponse resposta, Map recurso) {
        resposta.setHeader("Location", "${requisicao.requestURI}/${recurso.id}")
        responder(resposta, HttpServletResponse.SC_CREATED, recurso)
    }

    protected void responderErro(HttpServletResponse resposta, int status, String mensagem) {
        responder(resposta, status, [erro: mensagem])
    }

    protected void responderSemConteudo(HttpServletResponse resposta) {
        resposta.status = HttpServletResponse.SC_NO_CONTENT
    }

    protected void responderNaoEncontrado(HttpServletResponse resposta, String mensagem) {
        responderErro(resposta, HttpServletResponse.SC_NOT_FOUND, mensagem)
    }

    @Override
    protected void doPut(HttpServletRequest requisicao, HttpServletResponse resposta) {
        responderMetodoNaoSuportado(requisicao, resposta)
    }

    private void responderMetodoNaoSuportado(HttpServletRequest requisicao, HttpServletResponse resposta) {
        String metodos = metodosAceitos()
        resposta.setHeader("Allow", metodos)
        responderErro(resposta, HttpServletResponse.SC_METHOD_NOT_ALLOWED, "O método ${requisicao.method} não é suportado. Use ${metodos}")
    }

    @Override
    protected void doDelete(HttpServletRequest requisicao, HttpServletResponse resposta) {
        responderMetodoNaoSuportado(requisicao, resposta)
    }
}
