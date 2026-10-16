package org.example.api

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.example.model.Candidato
import org.example.model.Empresa
import org.example.model.Recrutador
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

class LoginServletSpec extends Specification {
    CandidatoService candidatoService = Mock(CandidatoService)
    EmpresaService empresaService = Mock(EmpresaService)
    RecrutadorService recrutadorService = Mock(RecrutadorService)
    HttpServletRequest requisicao = Mock(HttpServletRequest)
    HttpServletResponse resposta = Mock(HttpServletResponse)
    StringWriter saida = new StringWriter()

    LoginServlet servlet = new LoginServlet()

    def setup() {
        servlet.dependencias = new Dependencias(candidatoService, empresaService, Mock(VagaService), Mock(CompetenciaService), Mock(CurtidaService), recrutadorService)
        resposta.writer >> new PrintWriter(saida)
        requisicao.method >> "POST"
    }

    void enviar(Map corpo) {
        requisicao.reader >> new BufferedReader(new StringReader(JsonOutput.toJson(corpo)))
    }

    Candidato candidato() {
        Candidato candidato = new Candidato("Fernando", "Silva", LocalDate.of(2000, 1, 15), "fernando@example.com", "12345678900", "Brasil", "79000000", "", "senha123", "", [])
        candidato.id = 9
        return candidato
    }

    Recrutador recrutador() {
        Recrutador recrutador = new Recrutador(
                "Ana Paula",
                "TI",
                "",
                "ana@example.com",
                "12012012012",
                LocalDate.of(1990, 7, 12),
                "senha-secreta",
                4
        )
        recrutador.id = 8
        return recrutador
    }

    def "login de candidato com credenciais corretas responde 200 com tipo, id e nome"() {
        given:
        enviar([tipo: "candidato", email: "fernando@example.com", senha: "senha123"])
        candidatoService.autenticar("fernando@example.com", "senha123") >> candidato()

        when:
        servlet.service(requisicao, resposta)

        then:
        1 * resposta.setStatus(HttpServletResponse.SC_OK)
        saida.toString() == '{"tipo":"candidato","id":9,"nome":"Fernando"}'
    }

    def "login de empresa com credenciais corretas responde 200"() {
        given:
        Empresa empresa = new Empresa("Empresa Exemplo", "contato@example.com", "12345678000100", "Brasil", "79000000", "", "senha123")
        empresa.id = 4
        enviar([tipo: "empresa", email: "contato@example.com", senha: "senha123"])
        empresaService.autenticar("contato@example.com", "senha123") >> empresa

        when:
        servlet.service(requisicao, resposta)

        then:
        1 * resposta.setStatus(HttpServletResponse.SC_OK)
        saida.toString().contains('"tipo":"empresa"')
    }

    def "login de recrutador retorna apenas tipo id nome e idEmpresa"() {
        given:
        enviar([tipo: "recrutador", email: "ana@example.com", senha: "senha-secreta"])
        recrutadorService.autenticar("ana@example.com", "senha-secreta") >> recrutador()

        when:
        servlet.service(requisicao, resposta)

        then:
        1 * resposta.setStatus(HttpServletResponse.SC_OK)
        saida.toString() == '{"tipo":"recrutador","id":8,"nome":"Ana Paula","idEmpresa":4}'
        !saida.toString().contains("senha")
        !saida.toString().contains("12012012012")
        !saida.toString().contains("1990-07-12")
    }

    def "login de recrutador com credenciais erradas responde 401"() {
        given:
        enviar([tipo: "recrutador", email: "ana@example.com", senha: "incorreta"])
        recrutadorService.autenticar("ana@example.com", "incorreta") >> null

        when:
        servlet.service(requisicao, resposta)

        then:
        1 * resposta.setStatus(HttpServletResponse.SC_UNAUTHORIZED)
        new JsonSlurper().parseText(saida.toString()).erro == LoginServlet.CREDENCIAIS_INVALIDAS
    }

    def "login com senha errada responde 401 sem dizer qual campo errou"() {
        given:
        enviar([tipo: "candidato", email: "fernando@example.com", senha: "errada"])
        candidatoService.autenticar(_, _) >> null

        when:
        servlet.service(requisicao, resposta)

        then:
        1 * resposta.setStatus(HttpServletResponse.SC_UNAUTHORIZED)
        new JsonSlurper().parseText(saida.toString()).erro == LoginServlet.CREDENCIAIS_INVALIDAS
    }

    def "login com tipo desconhecido responde 400"() {
        given:
        enviar([tipo: "admin", email: "a@example.com", senha: "senha123"])

        when:
        servlet.service(requisicao, resposta)

        then:
        1 * resposta.setStatus(HttpServletResponse.SC_BAD_REQUEST)
        saida.toString().contains("'tipo'")
        0 * recrutadorService.autenticar(_, _)
    }

    def "login sem senha responde 400 e não consulta o service"() {
        given:
        enviar([tipo: "candidato", email: "a@example.com"])

        when:
        servlet.service(requisicao, resposta)

        then:
        0 * candidatoService.autenticar(_, _)
        1 * resposta.setStatus(HttpServletResponse.SC_BAD_REQUEST)
        saida.toString().contains("'senha'")
    }
}
