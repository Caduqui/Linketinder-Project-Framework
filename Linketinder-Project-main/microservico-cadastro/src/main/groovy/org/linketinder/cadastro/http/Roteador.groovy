package org.linketinder.cadastro.http

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import org.linketinder.cadastro.service.CadastroRepetidoException
import org.linketinder.cadastro.service.CandidatoNaoEncontradoException
import org.linketinder.cadastro.service.DadosInvalidosException

import java.io.IOException
import java.sql.SQLException

/**
 *
 * @author Guilherme Lima Conte
 */

class Roteador implements HttpHandler {

    static final int REQUISICAO_INVALIDA = 400
    static final int NAO_ENCONTRADO = 404
    static final int METODO_NAO_PERMITIDO = 405
    static final int CONFLITO = 409
    static final int ERRO_INTERNO = 500
    static final String ROTA_DESCONHECIDA = "(rota desconhecida)"

    private final RegistroDoServidor registro
    private final List<Rota> rotas = []

    Roteador(RegistroDoServidor registro) {
        this.registro = registro
    }

    void registrar(String metodo, String modelo, Manipulador manipulador) {
        Rota rota = rotas.find { Rota existente -> existente.modelo == modelo }

        if (rota == null) {
            rota = new Rota(modelo)
            rotas << rota
        }
        rota.manipuladoresPorMetodo[metodo] = manipulador
    }

    @Override
    void handle(HttpExchange troca) {
        try {
            String metodo = troca.requestMethod
            String caminho = troca.requestURI.path
            byte[] corpo = troca.requestBody.bytes

            Rota rota = rotas.find { Rota candidata -> candidata.extrairParametros(caminho) != null }
            Map<String, String> parametros = rota?.extrairParametros(caminho)
            RespostaJson resposta = rota == null
                    ? RespostaJson.erro(NAO_ENCONTRADO, "Rota não encontrada")
                    : responder(rota, new Requisicao(metodo, caminho, parametros, corpo))

            resposta.enviar(troca)
            registro.registrarRequisicao(metodo, rota == null ? ROTA_DESCONHECIDA : rota.modelo, resposta.status)
        } catch (IOException e) {
            registro.registrarFalhaInesperada(e)
        } finally {
            troca.close()
        }
    }

    private RespostaJson responder(Rota rota, Requisicao requisicao) {
        Manipulador manipulador = rota.manipuladoresPorMetodo[requisicao.metodo]

        if (manipulador == null) {
            return responderMetodoNaoPermitido(requisicao, rota.metodosAceitos)
        }
        return atender(manipulador, requisicao)
    }

    private RespostaJson atender(Manipulador manipulador, Requisicao requisicao) {
        try {
            return manipulador.atender(requisicao)
        } catch (DadosInvalidosException e) {
            return RespostaJson.erro(REQUISICAO_INVALIDA, e.message)
        } catch (CandidatoNaoEncontradoException e) {
            return RespostaJson.erro(NAO_ENCONTRADO, e.message)
        } catch (CadastroRepetidoException e) {
            return RespostaJson.erro(CONFLITO, e.message)
        } catch (SQLException e) {
            registro.registrarFalhaDeBanco(e)
            return RespostaJson.erro(ERRO_INTERNO, "Erro ao acessar o banco de dados")
        } catch (Exception e) {
            registro.registrarFalhaInesperada(e)
            return RespostaJson.erro(ERRO_INTERNO, "Erro interno do servidor")
        }
    }

    private static RespostaJson responderMetodoNaoPermitido(Requisicao requisicao, Set<String> metodosAceitos) {
        String aceitos = metodosAceitos.sort().join(", ")

        return RespostaJson
                .erro(METODO_NAO_PERMITIDO, "O método ${requisicao.metodo} não é suportado nesta rota. Use ${aceitos}")
                .comCabecalho("Allow", aceitos)
    }
}
