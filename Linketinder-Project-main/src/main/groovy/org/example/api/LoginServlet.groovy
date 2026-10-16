package org.example.api

import org.example.model.Candidato
import org.example.model.Empresa
import org.example.model.Recrutador
import org.example.service.RegraDeNegocioException

import javax.servlet.annotation.WebServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 *
 * @author Guilherme Lima Conte
 */

@WebServlet(name = "login", urlPatterns = ["/login"])
class LoginServlet extends ApiServlet {
    static final String TIPO_CANDIDATO = "candidato"
    static final String TIPO_EMPRESA = "empresa"
    static final String TIPO_RECRUTADOR = "recrutador"
    static final String CREDENCIAIS_INVALIDAS = "E-mail ou senha inválidos"

    @Override
    protected void doPost(HttpServletRequest requisicao, HttpServletResponse resposta) {
        Map dados = lerCorpo(requisicao)
        String tipo = Json.exigirTexto(dados, "tipo")
        String email = Json.exigirTexto(dados, "email")
        String senha = Json.exigirTexto(dados, "senha")

        Map usuario = autenticar(tipo, email, senha)

        if (usuario == null) {
            responderErro(resposta, HttpServletResponse.SC_UNAUTHORIZED, CREDENCIAIS_INVALIDAS)
            return
        }

        responder(resposta, HttpServletResponse.SC_OK, usuario)
    }

    @Override
    protected void doGet(HttpServletRequest requisicao, HttpServletResponse resposta) {
        resposta.setHeader("Allow", "POST")
        responderErro(resposta, HttpServletResponse.SC_METHOD_NOT_ALLOWED, "O login só aceita método POST")
    }

    private Map autenticar(String tipo, String email, String senha) {
        if (tipo == TIPO_CANDIDATO) {
            Candidato candidato = dependencias.candidatoService.autenticar(email, senha)
            return candidato == null ? null : [tipo: TIPO_CANDIDATO, id: candidato.id, nome: candidato.nome]
        }

        if (tipo == TIPO_EMPRESA) {
            Empresa empresa = dependencias.empresaService.autenticar(email, senha)
            return empresa == null ? null : [tipo: TIPO_EMPRESA, id: empresa.id, nome: empresa.nome]
        }

        if (tipo == TIPO_RECRUTADOR) {
            Recrutador recrutador = dependencias.recrutadorService.autenticar(email, senha)
            return recrutador == null ? null : [
                    tipo: TIPO_RECRUTADOR,
                    id: recrutador.id,
                    nome: recrutador.nome,
                    idEmpresa: recrutador.idEmpresa
            ]
        }

        throw new RegraDeNegocioException("O campo 'tipo' deve ser '${TIPO_CANDIDATO}', '${TIPO_EMPRESA}' ou '${TIPO_RECRUTADOR}'")
    }
}
