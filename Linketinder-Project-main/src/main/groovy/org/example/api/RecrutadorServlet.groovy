package org.example.api

import org.example.model.Empresa
import org.example.model.Recrutador
import org.example.model.Vaga
import org.example.service.RegraDeNegocioException

import javax.servlet.annotation.WebServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 *
 * @author Guilherme Lima Conte
 */
@WebServlet(name = "recrutadores", urlPatterns = ["/recrutadores", "/recrutadores/*"])
class RecrutadorServlet extends ApiServlet {

    static final String METODOS_RECRUTADOR = "GET, POST, DELETE"

    @Override
    protected String metodosAceitos() {
        return METODOS_RECRUTADOR
    }

    @Override
    protected void doGet(HttpServletRequest requisicao, HttpServletResponse resposta) {
        String caminho = requisicao.pathInfo
        if (caminho == null || caminho == "" || caminho == "/") {
            listarPorEmpresa(requisicao, resposta)
            return
        }

        List<String> partes = partesDoCaminho(caminho)
        if (partes.size() == 2 && partes[1] == "vagas") {
            Integer idRecrutador = lerId(partes[0])
            if (!existeRecrutador(idRecrutador, resposta)) {
                return
            }

            List<Vaga> vagas = dependencias.recrutadorService.listarVagasDoRecrutador(idRecrutador)
            responder(resposta, HttpServletResponse.SC_OK, vagas.collect { Vaga vaga -> Json.deVaga(vaga) })
            return
        }

        responderNaoEncontrado(resposta, "Rota de recrutadores não encontrada")
    }

    @Override
    protected void doPost(HttpServletRequest requisicao, HttpServletResponse resposta) {
        String caminho = requisicao.pathInfo
        if (caminho == null || caminho == "" || caminho == "/") {
            cadastrar(requisicao, resposta)
            return
        }

        List<String> partes = partesDoCaminho(caminho)
        if (partes.size() == 2 && partes[1] == "vagas") {
            atribuirVaga(requisicao, resposta, lerId(partes[0]))
            return
        }

        responderNaoEncontrado(resposta, "Rota de recrutadores não encontrada")
    }

    @Override
    protected void doDelete(HttpServletRequest requisicao, HttpServletResponse resposta) {
        List<String> partes = partesDoCaminho(requisicao.pathInfo ?: "")
        if (partes.size() == 1) {
            Integer idRecrutador = lerId(partes[0])
            if (!existeRecrutador(idRecrutador, resposta)) {
                return
            }

            dependencias.recrutadorService.excluir(idRecrutador)
            responderSemConteudo(resposta)
            return
        }

        if (partes.size() == 3 && partes[1] == "vagas") {
            Integer idRecrutador = lerId(partes[0])
            if (!existeRecrutador(idRecrutador, resposta)) {
                return
            }

            Integer idVaga = lerId(partes[2])
            if (dependencias.vagaService.buscarPorId(idVaga) == null) {
                responderNaoEncontrado(resposta, "Vaga ${idVaga} não encontrada")
                return
            }

            dependencias.recrutadorService.removerVaga(idRecrutador, idVaga)
            responderSemConteudo(resposta)
            return
        }

        responderNaoEncontrado(resposta, "Rota de recrutadores não encontrada")
    }

    private void cadastrar(HttpServletRequest requisicao, HttpServletResponse resposta) {
        Map dados = lerCorpo(requisicao)
        Recrutador recrutador = Json.paraRecrutador(dados)
        Empresa empresa = dependencias.empresaService.buscarPorId(recrutador.idEmpresa)
        if (empresa == null) {
            responderNaoEncontrado(resposta, "Empresa ${recrutador.idEmpresa} não encontrada")
            return
        }

        dependencias.recrutadorService.cadastrar(recrutador)
        responderCriado(requisicao, resposta, Json.deRecrutador(recrutador))
    }

    private void listarPorEmpresa(HttpServletRequest requisicao, HttpServletResponse resposta) {
        String valorIdEmpresa = requisicao.getParameter("idEmpresa")
        Integer idEmpresa
        try {
            idEmpresa = Integer.parseInt(valorIdEmpresa)
        } catch (NumberFormatException e) {
            throw new RegraDeNegocioException("O campo 'idEmpresa' é obrigatório e deve ser um número")
        }

        if (dependencias.empresaService.buscarPorId(idEmpresa) == null) {
            responderNaoEncontrado(resposta, "Empresa ${idEmpresa} não encontrada")
            return
        }

        List<Recrutador> recrutadores = dependencias.recrutadorService.listarPorEmpresa(idEmpresa)
        responder(resposta, HttpServletResponse.SC_OK, recrutadores.collect { Recrutador recrutador ->
            Json.deRecrutador(recrutador)
        })
    }

    private void atribuirVaga(HttpServletRequest requisicao, HttpServletResponse resposta, Integer idRecrutador) {
        if (!existeRecrutador(idRecrutador, resposta)) {
            return
        }

        Map dados = lerCorpo(requisicao)
        Integer idVaga = Json.exigirInteiro(dados, "idVaga")
        if (dependencias.vagaService.buscarPorId(idVaga) == null) {
            responderNaoEncontrado(resposta, "Vaga ${idVaga} não encontrada")
            return
        }

        dependencias.recrutadorService.atribuirVaga(idRecrutador, idVaga)
        List<Vaga> vagas = dependencias.recrutadorService.listarVagasDoRecrutador(idRecrutador)
        responder(resposta, HttpServletResponse.SC_CREATED, vagas.collect { Vaga vaga -> Json.deVaga(vaga) })
    }

    private boolean existeRecrutador(Integer idRecrutador, HttpServletResponse resposta) {
        if (dependencias.recrutadorService.buscarPorId(idRecrutador) != null) {
            return true
        }

        responderNaoEncontrado(resposta, "Recrutador ${idRecrutador} não encontrado")
        return false
    }

    private static List<String> partesDoCaminho(String caminho) {
        return caminho.split("/").findAll { String parte -> !parte.isEmpty() }
    }

    private static Integer lerId(String valor) {
        try {
            return Integer.parseInt(valor)
        } catch (NumberFormatException e) {
            throw new RegraDeNegocioException("O ID informado na URL não é um número")
        }
    }
}
