package org.linketinder.cadastro.http

import groovy.json.JsonSlurper
import org.linketinder.cadastro.model.Competencia
import org.linketinder.cadastro.repositorio.CandidatoRepositorio
import org.linketinder.cadastro.repositorio.CompetenciaRepositorio
import org.linketinder.cadastro.repositorio.Transacao
import org.linketinder.cadastro.service.CompetenciaDoCandidatoService
import spock.lang.Specification

import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

/**
 *
 * @author Guilherme Lima Conte
 */

class RotaDeCompetenciasSpec extends Specification {

    static final int PORTA_LIVRE = 0

    CandidatoRepositorio candidatoRepositorio = Mock(CandidatoRepositorio)
    CompetenciaRepositorio competenciaRepositorio = Mock(CompetenciaRepositorio)
    Transacao transacaoDireta = { Closure bloco -> bloco() } as Transacao

    ByteArrayOutputStream saidaDoRegistro = new ByteArrayOutputStream()
    Roteador roteador = new Roteador(new RegistroDoServidor(new PrintStream(saidaDoRegistro, true, "UTF-8")))
    ServidorCadastro servidor = new ServidorCadastro(PORTA_LIVRE, roteador)
    HttpClient cliente = HttpClient.newHttpClient()

    def setup() {
        RotasDeCadastro.registrarCompetencias(
                roteador,
                new CompetenciaDoCandidatoService(candidatoRepositorio, competenciaRepositorio, transacaoDireta)
        )
        servidor.iniciar()
    }

    def cleanup() {
        servidor.parar()
    }

    HttpResponse<String> chamar(String metodo, String caminho, String corpo = "") {
        HttpRequest requisicao = HttpRequest.newBuilder(URI.create("http://localhost:${servidor.porta}${caminho}"))
                .method(metodo, HttpRequest.BodyPublishers.ofString(corpo))
                .build()
        return cliente.send(requisicao, HttpResponse.BodyHandlers.ofString())
    }

    static Map<String, Object> json(HttpResponse<String> resposta) {
        return new JsonSlurper().parseText(resposta.body()) as Map<String, Object>
    }

    def "competências válidas respondem 200 só com id e competencias"() {
        given:
        candidatoRepositorio.existe(7) >> true
        competenciaRepositorio.buscarDoCandidato(7) >>> [[], [new Competencia(id: 1, nome: "Java"), new Competencia(id: 3, nome: "SQL")]]
        competenciaRepositorio.buscarPorNomeIgnorandoCaixa("java") >> new Competencia(id: 1, nome: "Java")
        competenciaRepositorio.buscarPorNomeIgnorandoCaixa("SQL") >> new Competencia(id: 3, nome: "SQL")

        when:
        HttpResponse<String> resposta = chamar("POST", "/candidatos/7/competencias", '{"competencias": ["java", "SQL"]}')

        then:
        resposta.statusCode() == 200
        resposta.headers().firstValue("Content-Type").get() == "application/json; charset=UTF-8"
        json(resposta) == [id: 7, competencias: ["Java", "SQL"]]
    }

    def "candidato inexistente responde 404 e nada é criado"() {
        given:
        candidatoRepositorio.existe(999) >> false

        when:
        HttpResponse<String> resposta = chamar("POST", "/candidatos/999/competencias", '{"competencias": ["Java"]}')

        then:
        0 * competenciaRepositorio.inserir(_)
        resposta.statusCode() == 404
        json(resposta) == [erro: "Candidato não encontrado"]
    }

    def "#situacao responde 400"() {
        when:
        HttpResponse<String> resposta = chamar("POST", caminho, corpo)

        then:
        0 * competenciaRepositorio.vincularAoCandidato(_, _)
        resposta.statusCode() == 400
        json(resposta).erro != null

        where:
        situacao                 | caminho                         | corpo
        "id que não é número"    | "/candidatos/abc/competencias"  | '{"competencias": ["Java"]}'
        "id grande demais"       | "/candidatos/99999999999999/competencias" | '{"competencias": ["Java"]}'
        "lista vazia"            | "/candidatos/7/competencias"    | '{"competencias": []}'
        "corpo sem a lista"      | "/candidatos/7/competencias"    | '{}'
        "corpo vazio"            | "/candidatos/7/competencias"    | ''
        "JSON inválido"          | "/candidatos/7/competencias"    | '{"competencias": ['
    }

    def "#metodo na rota de competências responde 405 com Allow: POST"() {
        when:
        HttpResponse<String> resposta = chamar(metodo, "/candidatos/7/competencias")

        then:
        resposta.statusCode() == 405
        resposta.headers().firstValue("Allow").get() == "POST"

        where:
        metodo << ["GET", "PUT", "DELETE", "OPTIONS"]
    }

    def "caminhos parecidos com a rota de competências respondem 404"() {
        expect:
        chamar("POST", caminho, "{}").statusCode() == 404

        where:
        caminho << ["/candidatos/7", "/candidatos/7/competencias/", "/candidatos/7/competencias/1", "/candidatos//competencias", "/empresas/7/competencias"]
    }

    def "o registro traz o modelo da rota, sem o id nem o corpo"() {
        given:
        candidatoRepositorio.existe(7) >> true
        competenciaRepositorio.buscarDoCandidato(7) >> [new Competencia(id: 1, nome: "Java")]
        competenciaRepositorio.buscarPorNomeIgnorandoCaixa("Java") >> new Competencia(id: 1, nome: "Java")

        when:
        chamar("POST", "/candidatos/7/competencias", '{"competencias": ["Java"]}')

        then:
        saidaDoRegistro.toString("UTF-8").contains("POST /candidatos/{id}/competencias 200")
        !saidaDoRegistro.toString("UTF-8").contains("Java")
    }
}
