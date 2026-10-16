package org.example.api

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.example.model.Empresa
import org.example.model.Recrutador
import org.example.model.Vaga
import org.example.service.CandidatoService
import org.example.service.CompetenciaService
import org.example.service.CurtidaService
import org.example.service.EmpresaService
import org.example.service.RecrutadorService
import org.example.service.RegraDeNegocioException
import org.example.service.VagaService
import spock.lang.Specification

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 *
 * @author Guilherme Lima Conte
 */
class RecrutadorServletSpec extends Specification {

    RecrutadorService recrutadorService = Mock()
    EmpresaService empresaService = Mock()
    VagaService vagaService = Mock()
    HttpServletRequest requisicao = Mock()
    HttpServletResponse resposta = Mock()
    StringWriter saida = new StringWriter()

    RecrutadorServlet servlet = new RecrutadorServlet()

    def setup() {
        servlet.dependencias = new Dependencias(
                Mock(CandidatoService),
                empresaService,
                vagaService,
                Mock(CompetenciaService),
                Mock(CurtidaService),
                recrutadorService
        )
        resposta.writer >> new PrintWriter(saida)
    }

    void enviar(Map corpo) {
        requisicao.reader >> new BufferedReader(new StringReader(JsonOutput.toJson(corpo)))
    }

    Object respostaJson() {
        return new JsonSlurper().parseText(saida.toString())
    }

    Empresa empresa() {
        Empresa empresa = new Empresa("Alfa", "alfa@example.com", "11111111111111", "Brasil", "01000000", "", "senha123")
        empresa.id = 4
        return empresa
    }

    Recrutador recrutador() {
        Recrutador recrutador = new Recrutador(
                "Ana Paula",
                "TI",
                "Recrutadora",
                "ana@example.com",
                "12012012012",
                java.time.LocalDate.of(1990, 7, 12),
                "senha123",
                4
        )
        recrutador.id = 8
        return recrutador
    }

    Vaga vaga() {
        Vaga vaga = new Vaga("Dev Groovy", "APIs", "SP", "São Paulo", empresa(), ["Groovy"])
        vaga.id = 12
        return vaga
    }

    def "POST cadastra recrutador e não expõe senha CPF nem data de nascimento"() {
        given:
        requisicao.method >> "POST"
        requisicao.pathInfo >> null
        requisicao.requestURI >> "/linketinder/recrutadores"
        enviar([
                nome: "Ana Paula",
                area: "TI",
                descricao: "Recrutadora",
                email: "ana@example.com",
                cpf: "12012012012",
                dataNascimento: "1990-07-12",
                senha: "senha123",
                idEmpresa: 4
        ])
        empresaService.buscarPorId(4) >> empresa()

        when:
        servlet.service(requisicao, resposta)

        then:
        1 * recrutadorService.cadastrar({ Recrutador item -> item.idEmpresa == 4 }) >> { Recrutador item -> item.id = 8 }
        1 * resposta.setStatus(HttpServletResponse.SC_CREATED)
        respostaJson().id == 8
        respostaJson().idEmpresa == 4
        !respostaJson().containsKey("senha")
        !respostaJson().containsKey("cpf")
        !respostaJson().containsKey("dataNascimento")
    }

    def "POST responde 404 quando a empresa não existe"() {
        given:
        requisicao.method >> "POST"
        requisicao.pathInfo >> null
        enviar([nome: "Ana", area: "TI", email: "ana@example.com", cpf: "12012012012", dataNascimento: "1990-07-12", senha: "senha123", idEmpresa: 404])
        empresaService.buscarPorId(404) >> null

        when:
        servlet.service(requisicao, resposta)

        then:
        0 * recrutadorService.cadastrar(_)
        1 * resposta.setStatus(HttpServletResponse.SC_NOT_FOUND)
    }

    def "POST responde 400 para dados inválidos e não cadastra"() {
        given:
        requisicao.method >> "POST"
        requisicao.pathInfo >> null
        enviar([nome: "Ana", area: "Inválida"])

        when:
        servlet.service(requisicao, resposta)

        then:
        0 * recrutadorService.cadastrar(_)
        1 * resposta.setStatus(HttpServletResponse.SC_BAD_REQUEST)
        respostaJson().erro.contains("'email'")
    }

    def "POST responde 400 para e-mail ou CPF repetido com mensagem genérica"() {
        given:
        requisicao.method >> "POST"
        requisicao.pathInfo >> null
        enviar([
                nome: "Ana Paula",
                area: "TI",
                email: "ana@example.com",
                cpf: "12012012012",
                dataNascimento: "1990-07-12",
                senha: "senha123",
                idEmpresa: 4
        ])
        empresaService.buscarPorId(4) >> empresa()
        recrutadorService.cadastrar(_) >> {
            throw new RegraDeNegocioException("Já existe um recrutador com esses dados")
        }

        when:
        servlet.service(requisicao, resposta)

        then:
        1 * resposta.setStatus(HttpServletResponse.SC_BAD_REQUEST)
        respostaJson().erro == "Já existe um recrutador com esses dados"
    }

    def "GET lista recrutadores da empresa indicada"() {
        given:
        requisicao.method >> "GET"
        requisicao.pathInfo >> null
        requisicao.getParameter("idEmpresa") >> "4"
        empresaService.buscarPorId(4) >> empresa()
        recrutadorService.listarPorEmpresa(4) >> [recrutador()]

        when:
        servlet.service(requisicao, resposta)

        then:
        1 * resposta.setStatus(HttpServletResponse.SC_OK)
        respostaJson()[0].id == 8
        respostaJson()[0].idEmpresa == 4
        !saida.toString().contains("12012012012")
        !saida.toString().contains("senha123")
        !saida.toString().contains("1990-07-12")
    }

    def "GET exige idEmpresa numérico e existente"() {
        given:
        requisicao.method >> "GET"
        requisicao.pathInfo >> null
        requisicao.getParameter("idEmpresa") >> "nao-numero"

        when:
        servlet.service(requisicao, resposta)

        then:
        0 * recrutadorService.listarPorEmpresa(_)
        1 * resposta.setStatus(HttpServletResponse.SC_BAD_REQUEST)
    }

    def "GET sem idEmpresa responde 400"() {
        given:
        requisicao.method >> "GET"
        requisicao.pathInfo >> null
        requisicao.getParameter("idEmpresa") >> null

        when:
        servlet.service(requisicao, resposta)

        then:
        0 * empresaService.buscarPorId(_)
        1 * resposta.setStatus(HttpServletResponse.SC_BAD_REQUEST)
    }

    def "GET responde 404 para empresa inexistente"() {
        given:
        requisicao.method >> "GET"
        requisicao.pathInfo >> null
        requisicao.getParameter("idEmpresa") >> "404"
        empresaService.buscarPorId(404) >> null

        when:
        servlet.service(requisicao, resposta)

        then:
        0 * recrutadorService.listarPorEmpresa(_)
        1 * resposta.setStatus(HttpServletResponse.SC_NOT_FOUND)
    }

    def "GET vagas retorna somente vagas atribuídas ao recrutador"() {
        given:
        requisicao.method >> "GET"
        requisicao.pathInfo >> "/8/vagas"
        recrutadorService.buscarPorId(8) >> recrutador()
        recrutadorService.listarVagasDoRecrutador(8) >> [vaga()]

        when:
        servlet.service(requisicao, resposta)

        then:
        1 * resposta.setStatus(HttpServletResponse.SC_OK)
        respostaJson()[0].id == 12
        respostaJson()[0].nome == "Dev Groovy"
    }

    def "GET vagas responde 404 para recrutador inexistente"() {
        given:
        requisicao.method >> "GET"
        requisicao.pathInfo >> "/404/vagas"
        recrutadorService.buscarPorId(404) >> null

        when:
        servlet.service(requisicao, resposta)

        then:
        0 * recrutadorService.listarVagasDoRecrutador(_)
        1 * resposta.setStatus(HttpServletResponse.SC_NOT_FOUND)
    }

    def "GET vagas com ID não numérico responde 400"() {
        given:
        requisicao.method >> "GET"
        requisicao.pathInfo >> "/abc/vagas"

        when:
        servlet.service(requisicao, resposta)

        then:
        0 * recrutadorService.buscarPorId(_)
        1 * resposta.setStatus(HttpServletResponse.SC_BAD_REQUEST)
    }

    def "POST atribui vaga e responde com a lista atualizada"() {
        given:
        requisicao.method >> "POST"
        requisicao.pathInfo >> "/8/vagas"
        requisicao.requestURI >> "/linketinder/recrutadores/8/vagas"
        enviar([idVaga: 12])
        recrutadorService.buscarPorId(8) >> recrutador()
        recrutadorService.listarVagasDoRecrutador(8) >> [vaga()]
        vagaService.buscarPorId(12) >> vaga()

        when:
        servlet.service(requisicao, resposta)

        then:
        1 * recrutadorService.atribuirVaga(8, 12) >> true
        1 * resposta.setStatus(HttpServletResponse.SC_CREATED)
        respostaJson()[0].id == 12
    }

    def "POST atribuição responde 404 para recrutador inexistente"() {
        given:
        requisicao.method >> "POST"
        requisicao.pathInfo >> "/404/vagas"
        enviar([idVaga: 12])
        recrutadorService.buscarPorId(404) >> null

        when:
        servlet.service(requisicao, resposta)

        then:
        0 * recrutadorService.atribuirVaga(_, _)
        1 * resposta.setStatus(HttpServletResponse.SC_NOT_FOUND)
    }

    def "POST atribuição responde 400 quando a vaga pertence a outra empresa"() {
        given:
        requisicao.method >> "POST"
        requisicao.pathInfo >> "/8/vagas"
        enviar([idVaga: 12])
        recrutadorService.buscarPorId(8) >> recrutador()
        vagaService.buscarPorId(12) >> vaga()

        when:
        servlet.service(requisicao, resposta)

        then:
        1 * recrutadorService.atribuirVaga(8, 12) >> {
            throw new RegraDeNegocioException("A vaga não pertence à empresa do recrutador")
        }
        1 * resposta.setStatus(HttpServletResponse.SC_BAD_REQUEST)
    }

    def "DELETE remove recrutador com resposta sem conteúdo"() {
        given:
        requisicao.method >> "DELETE"
        requisicao.pathInfo >> "/8"
        recrutadorService.buscarPorId(8) >> recrutador()

        when:
        servlet.service(requisicao, resposta)

        then:
        1 * recrutadorService.excluir(8)
        1 * resposta.setStatus(HttpServletResponse.SC_NO_CONTENT)
        saida.toString().isEmpty()
    }

    def "DELETE desatribui vaga do recrutador com resposta sem conteúdo"() {
        given:
        requisicao.method >> "DELETE"
        requisicao.pathInfo >> "/8/vagas/12"
        recrutadorService.buscarPorId(8) >> recrutador()
        vagaService.buscarPorId(12) >> vaga()

        when:
        servlet.service(requisicao, resposta)

        then:
        1 * recrutadorService.removerVaga(8, 12)
        1 * resposta.setStatus(HttpServletResponse.SC_NO_CONTENT)
        saida.toString().isEmpty()
    }

    def "DELETE responde 404 quando o recrutador não existe"() {
        given:
        requisicao.method >> "DELETE"
        requisicao.pathInfo >> "/404"
        recrutadorService.buscarPorId(404) >> null

        when:
        servlet.service(requisicao, resposta)

        then:
        0 * recrutadorService.excluir(_)
        1 * resposta.setStatus(HttpServletResponse.SC_NOT_FOUND)
    }

    def "PUT responde 405 e anuncia métodos aceitos pelo recrutador"() {
        given:
        requisicao.method >> "PUT"

        when:
        servlet.service(requisicao, resposta)

        then:
        1 * resposta.setHeader("Allow", "GET, POST, DELETE")
        1 * resposta.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED)
    }
}
