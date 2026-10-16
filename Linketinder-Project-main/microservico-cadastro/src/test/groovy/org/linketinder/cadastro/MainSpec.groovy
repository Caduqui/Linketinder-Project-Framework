package org.linketinder.cadastro

import org.linketinder.cadastro.http.RegistroDoServidor
import org.linketinder.cadastro.http.ServidorCadastro
import spock.lang.Specification

import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

/**
 *
 * @author Guilherme Lima Conte
 */

class MainSpec extends Specification {

    static final String SENHA_FICTICIA = "senha-de-teste"
    static final String PORTA_LIVRE = "0"

    ByteArrayOutputStream saida = new ByteArrayOutputStream()
    RegistroDoServidor registro = new RegistroDoServidor(new PrintStream(saida, true, "UTF-8"))

    def "com a senha do banco informada o serviço sobe na porta configurada"() {
        when:
        ServidorCadastro servidor = Main.subir([
                (Configuracao.VARIAVEL_PORTA)      : PORTA_LIVRE,
                (Configuracao.VARIAVEL_BANCO_SENHA): SENHA_FICTICIA
        ], registro)

        then:
        servidor.porta > 0
        saida.toString("UTF-8").contains("${servidor.porta}")
        !saida.toString("UTF-8").contains(SENHA_FICTICIA)

        cleanup:
        servidor?.parar()
    }

    def "o serviço sobe com as rotas de cadastro ligadas"() {
        given:
        ServidorCadastro servidor = Main.subir([
                (Configuracao.VARIAVEL_PORTA)      : PORTA_LIVRE,
                (Configuracao.VARIAVEL_BANCO_SENHA): SENHA_FICTICIA
        ], registro)

        expect: "dados inválidos são recusados pela rota antes de qualquer acesso ao banco"
        enviarCorpoVazio(servidor, "/candidatos") == 400
        enviarCorpoVazio(servidor, "/empresas") == 400
        enviarCorpoVazio(servidor, "/candidatos/7/competencias") == 400

        cleanup:
        servidor?.parar()
    }

    private static int enviarCorpoVazio(ServidorCadastro servidor, String caminho) {
        HttpRequest requisicao = HttpRequest.newBuilder(URI.create("http://localhost:${servidor.porta}${caminho}"))
                .POST(HttpRequest.BodyPublishers.ofString(""))
                .build()
        return HttpClient.newHttpClient().send(requisicao, HttpResponse.BodyHandlers.discarding()).statusCode()
    }

    def "com a porta já em uso o serviço não sobe e diz qual porta e qual variável trocar"() {
        given:
        ServidorCadastro ocupante = Main.subir([
                (Configuracao.VARIAVEL_PORTA)      : PORTA_LIVRE,
                (Configuracao.VARIAVEL_BANCO_SENHA): SENHA_FICTICIA
        ], registro)

        when:
        Main.subir([
                (Configuracao.VARIAVEL_PORTA)      : "${ocupante.porta}".toString(),
                (Configuracao.VARIAVEL_BANCO_SENHA): SENHA_FICTICIA
        ], registro)

        then:
        ConfiguracaoInvalidaException erro = thrown()
        erro.message.contains("${ocupante.porta}")
        erro.message.contains(Configuracao.VARIAVEL_PORTA)

        cleanup:
        ocupante?.parar()
    }

    def "sem a senha do banco o serviço não sobe"() {
        when:
        Main.subir([(Configuracao.VARIAVEL_PORTA): PORTA_LIVRE], registro)

        then:
        ConfiguracaoInvalidaException erro = thrown()
        erro.message.contains(Configuracao.VARIAVEL_BANCO_SENHA)
    }
}
