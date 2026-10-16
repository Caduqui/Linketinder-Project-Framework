package org.example.api

import groovy.json.JsonOutput
import org.example.model.Candidato
import org.example.service.CandidatoService
import org.example.service.CompetenciaService
import org.example.service.CurtidaService
import org.example.service.RecrutadorService
import org.example.service.EmpresaService
import org.example.service.VagaService
import spock.lang.Specification

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import java.time.LocalDate

/**
 *
 * @author Guilherme Lima Conte
 */

class CandidatoServletSpec extends Specification {

    static final String CORPO_VALIDO = JsonOutput.toJson([
            nome: "Bruno", sobrenome: "Silva", dataNascimento: "2005-02-10",
            email: "bruno@gmail.com", cpf: "99999999999", pais: "Brasil", cep: "8201230",
            descricao: "Dev", senha: "senha123", formacao: "Computação", competencias: ["Java"]
    ])

    CandidatoService candidatoService = Mock(CandidatoService)
    HttpServletRequest requisicao = Mock(HttpServletRequest)
    HttpServletResponse resposta = Mock(HttpServletResponse)
    StringWriter saida = new StringWriter()

    CandidatoServlet servlet = new CandidatoServlet()

    def setup() {
        servlet.dependencias = new Dependencias(candidatoService, Mock(EmpresaService), Mock(VagaService), Mock(CompetenciaService), Mock(CurtidaService), Mock(RecrutadorService))
        resposta.writer >> new PrintWriter(saida)
        requisicao.requestURI >> "/linketinder/candidatos"
    }

    Candidato candidatoExistente(Integer id) {
        Candidato candidato = new Candidato("Ana", "Silva", LocalDate.of(2003, 4, 21), "ana@gmail.com",
                "11111111111", "Brasil", "3254142", "focada nos deveres", "123456", "ensino médio completo", [])
        candidato.id = id
        return candidato
    }

    def "POST cadastra o candidato e responde 201"() {
        given:
        requisicao.method >> "POST"
        requisicao.reader >> new BufferedReader(new StringReader(CORPO_VALIDO))

        when:
        servlet.service(requisicao, resposta)

        then:
        1 * candidatoService.cadastrar({ Candidato candidato -> candidato.nome == "Bruno"})
        1 * resposta.setStatus(HttpServletResponse.SC_CREATED)
        saida.toString().contains('"nome":"Bruno"')
        !saida.toString().contains("senha")
    }

    def "POST sem campo obrigatório responde erro 400 e não cadastra"() {
        given:
        requisicao.method >> "POST"
        requisicao.reader >> new BufferedReader(new StringReader('{"sobrenome":"Silva"}'))

        when:
        servlet.service(requisicao, resposta)

        then:
        0 * candidatoService.cadastrar(_)
        1 * resposta.setStatus(HttpServletResponse.SC_BAD_REQUEST)
        saida.toString().contains("'nome'")
    }

    def "GET da coleção responde 200 com a lista"() {
        given:
        requisicao.method >> "GET"
        requisicao.pathInfo >> null
        candidatoService.listar() >> [candidatoExistente(1)]

        when:
        servlet.service(requisicao, resposta)

        then:
        1 * resposta.setStatus(HttpServletResponse.SC_OK)
        saida.toString().startsWith("[")
        saida.toString().contains('"id":1')
    }

    def "GET de candidato inexistente responde erro 404"() {
        given:
        requisicao.method >> "GET"
        requisicao.pathInfo >> "/999"
        candidatoService.buscarPorId(999) >> null

        when:
        servlet.service(requisicao, resposta)

        then:
        1 * resposta.setStatus(HttpServletResponse.SC_NOT_FOUND)
        saida.toString().contains("Candidato 999")
    }

    def "DELETE responde 405 informando os métodos aceitos"() {
        given:
        requisicao.method >> "DELETE"

        when:
        servlet.service(requisicao, resposta)

        then:
        1 * resposta.setHeader("Allow", "GET, POST")
        1 * resposta.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED)
    }
}
