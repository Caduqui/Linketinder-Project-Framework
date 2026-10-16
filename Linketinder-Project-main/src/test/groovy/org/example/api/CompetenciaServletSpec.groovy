package org.example.api

import groovy.json.JsonOutput
import org.example.model.Candidato
import org.example.model.Competencia
import org.example.service.CandidatoService
import org.example.service.EmpresaService
import org.example.service.VagaService
import org.example.service.CompetenciaService
import org.example.service.CurtidaService
import org.example.service.RecrutadorService
import spock.lang.Specification

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import java.time.LocalDate

/**
 *
 * @author Guilherme Lima Conte
 */

class CompetenciaServletSpec extends Specification{
    CandidatoService candidatoService = Mock(CandidatoService)
    CompetenciaService competenciaService = Mock(CompetenciaService)
    HttpServletRequest requisicao = Mock(HttpServletRequest)
    HttpServletResponse resposta = Mock(HttpServletResponse)
    StringWriter saida = new StringWriter()

    CompetenciaServlet servlet = new CompetenciaServlet()

    def setup() {
        servlet.dependencias = new Dependencias(candidatoService, Mock(EmpresaService), Mock(VagaService), competenciaService, Mock(CurtidaService), Mock(RecrutadorService))
        resposta.writer >> new PrintWriter(saida)
    }

    Competencia competencia(Integer id, String nome) {
        Competencia competencia = new Competencia(nome)
        competencia.id = id
        return competencia
    }

    def "GET responde 200 com a lista de competências"() {
        given:
        requisicao.method >> "GET"
        competenciaService.listar() >> [competencia(1, "Java"), competencia(2, "SQL")]

        when:
        servlet.service(requisicao, resposta)

        then:
        1 * resposta.setStatus(HttpServletResponse.SC_OK)
        saida.toString() == '[{"id":1,"nome":"Java"},{"id":2,"nome":"SQL"}]'
    }

    def "POST relaciona as competências ao candidato e responde 201"() {
        given:
        Candidato candidato = new Candidato("Fernando", "Silva", LocalDate.of(2000, 1, 15), "f@example.com", "12345678900", "Brasil", "79000000", "", "senha123", "", ["Java", "SQL"])
        candidato.id = 9
        requisicao.method >> "POST"
        requisicao.reader >> new BufferedReader(new StringReader(JsonOutput.toJson([idCandidato: 9, competencias: ["Java", " SQL "]])))

        when:
        servlet.service(requisicao, resposta)

        then:
        1 * candidatoService.adicionarCompetencias(9, ["Java", "SQL"]) >> candidato
        1 * resposta.setStatus(HttpServletResponse.SC_CREATED)
        saida.toString().contains('"competencias":["Java","SQL"]')
    }

    def "POST para candidato inexistente responde 404"() {
        given:
        requisicao.method >> "POST"
        requisicao.reader >> new BufferedReader(new StringReader('{"idCandidato":99,"competencias":["Java"]}'))
        candidatoService.adicionarCompetencias(99, ["Java"]) >> null

        when:
        servlet.service(requisicao, resposta)

        then:
        1 * resposta.setStatus(HttpServletResponse.SC_NOT_FOUND)
    }

    def "POST sem idCandidato responde 400"() {
        given:
        requisicao.method >> "POST"
        requisicao.reader >> new BufferedReader(new StringReader('{"competencias":["Java"]}'))

        when:
        servlet.service(requisicao, resposta)

        then:
        0 * candidatoService.adicionarCompetencias(_, _)
        1 * resposta.setStatus(HttpServletResponse.SC_BAD_REQUEST)
    }
}
