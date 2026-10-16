package org.example.api

import groovy.json.JsonOutput
import org.example.model.Empresa
import org.example.model.Vaga
import org.example.service.CandidatoService
import org.example.service.EmpresaService
import org.example.service.VagaService
import org.example.service.CompetenciaService
import org.example.service.CurtidaService
import org.example.service.RecrutadorService
import spock.lang.Specification

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 *
 * @author Guilherme Lima Conte
 */

class VagaServletSpec extends Specification {

    static final String CORPO_VALIDO = JsonOutput.toJson([
            nome        : "Dev Groovy",
            descricao   : "APIs",
            estado      : "MS",
            cidade      : "Campo Grande",
            idEmpresa   : 5,
            competencias: ["Groovy"]
    ])

    EmpresaService empresaService = Mock(EmpresaService)
    VagaService vagaService = Mock(VagaService)
    HttpServletRequest requisicao = Mock(HttpServletRequest)
    HttpServletResponse resposta = Mock(HttpServletResponse)
    StringWriter saida = new StringWriter()
    VagaServlet servlet = new VagaServlet()

    def setup() {
        servlet.dependencias = new Dependencias(Mock(CandidatoService), empresaService, vagaService, Mock(CompetenciaService), Mock(CurtidaService), Mock(RecrutadorService))
        resposta.writer >> new PrintWriter(saida)
        requisicao.requestURI >> "/linketinder/vagas"
    }

    Empresa empresaExistente(Integer id) {
        Empresa empresa = new Empresa(
                "CloudX",
                "cloudx@gmail.com",
                "99888988900",
                "Brasil",
                "79091246",
                "SaaS",
                "senha123"
        )
        empresa.id = id
        return empresa
    }


    Vaga vagaExistente(Integer id, Empresa empresa) {
        Vaga vaga = new Vaga(
                "Dev",
                "APIs",
                "MS",
                "Campo Grande",
                empresa,
                ["Groovy"])
        vaga.id = id
        return vaga
    }

    def "GET sem filtro responde 200 com todas as vagas"() {
        given:
        requisicao.method >> "GET"
        requisicao.pathInfo >> null
        requisicao.getParameter("idEmpresa") >> null
        vagaService.listar() >> [vagaExistente(1, empresaExistente(5))]

        when:
        servlet.service(requisicao, resposta)

        then:
        0 * vagaService.listarPorEmpresa(_)
        1 * resposta.setStatus(HttpServletResponse.SC_OK)
        saida.toString().contains('"id":1')
    }

    def "GET com idEmpresa filtra as vagas pela empresa"() {
        given:
        requisicao.method >> "GET"
        requisicao.pathInfo >> null
        requisicao.getParameter("idEmpresa") >> "5"

        when:
        servlet.service(requisicao, resposta)

        then:
        1 * vagaService.listarPorEmpresa(5) >> [vagaExistente(1, empresaExistente(5))]
        0 * vagaService.listar()
        1 * resposta.setStatus(HttpServletResponse.SC_OK)
    }

    def "GET com idEmpresa que não é número responde com erro 400"() {
        given:
        requisicao.method >> "GET"
        requisicao.pathInfo >> null
        requisicao.getParameter("idEmpresa") >> "abc"

        when:
        servlet.service(requisicao, resposta)

        then:
        0 * vagaService.listarPorEmpresa(_)
        1 * resposta.setStatus(HttpServletResponse.SC_BAD_REQUEST)
        saida.toString().contains("idEmpresa")
    }

    def "GET de vaga inexistente responde com erro 404"() {
        given:
        requisicao.method >> "GET"
        requisicao.pathInfo >> "/999"
        vagaService.buscarPorId(999) >> null

        when:
        servlet.service(requisicao, resposta)

        then:
        1 * resposta.setStatus(HttpServletResponse.SC_NOT_FOUND)
        saida.toString().contains("Vaga 999")
    }

    def "POST cadastra a vaga vinculada à empresa informada e responde 201"() {
        given:
        requisicao.method >> "POST"
        requisicao.reader >> new BufferedReader(new StringReader(CORPO_VALIDO))
        empresaService.buscarPorId(5) >> empresaExistente(5)

        when:
        servlet.service(requisicao, resposta)

        then:
        1 * vagaService.cadastrar({ Vaga vaga -> vaga.nome == "Dev Groovy" && vaga.empresa.id == 5 })
        1 * resposta.setStatus(HttpServletResponse.SC_CREATED)
        saida.toString().contains('"empresa":{"id":5,"nome":"CloudX"}')
    }

    def "POST com empresa inexistente responde erro 400 e não cadastra"() {
        given:
        requisicao.method >> "POST"
        requisicao.reader >> new BufferedReader(new StringReader(CORPO_VALIDO))
        empresaService.buscarPorId(5) >> null

        when:
        servlet.service(requisicao, resposta)

        then:
        0 * vagaService.cadastrar(_)
        1 * resposta.setStatus(HttpServletResponse.SC_BAD_REQUEST)
        saida.toString().contains("Empresa 5")
    }

    def "POST sem idEmpresa responde erro 400 e não busca a empresa"() {
        given:
        requisicao.method >> "POST"
        requisicao.reader >> new BufferedReader(new StringReader('{"nome":"Dev","estado":"MS","cidade":"Campo Grande"}'))

        when:
        servlet.service(requisicao, resposta)

        then:
        0 * empresaService.buscarPorId(_)
        0 * vagaService.cadastrar(_)
        1 * resposta.setStatus(HttpServletResponse.SC_BAD_REQUEST)
        saida.toString().contains("'idEmpresa'")
    }
}
