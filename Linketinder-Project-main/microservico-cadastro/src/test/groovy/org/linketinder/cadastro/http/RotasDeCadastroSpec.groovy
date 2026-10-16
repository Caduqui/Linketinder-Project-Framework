package org.linketinder.cadastro.http

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.linketinder.cadastro.DadosFicticios
import org.linketinder.cadastro.model.Candidato
import org.linketinder.cadastro.model.Empresa
import org.linketinder.cadastro.repositorio.CandidatoRepositorio
import org.linketinder.cadastro.repositorio.EmpresaRepositorio
import org.linketinder.cadastro.service.CadastroCandidatoService
import org.linketinder.cadastro.service.CadastroEmpresaService
import org.linketinder.cadastro.service.CadastroRepetidoException
import org.linketinder.cadastro.service.ValidadorDeCadastro
import spock.lang.Specification

import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.sql.SQLException
import java.time.Clock

/**
 *
 * @author Guilherme Lima Conte
 */

class RotasDeCadastroSpec extends Specification {

    static final int PORTA_LIVRE = 0
    static final String SENHA_ENVIADA = "senha-ficticia"

    CandidatoRepositorio candidatoRepositorio = Mock(CandidatoRepositorio)
    EmpresaRepositorio empresaRepositorio = Mock(EmpresaRepositorio)
    ValidadorDeCadastro validador = new ValidadorDeCadastro(Clock.systemDefaultZone())

    ByteArrayOutputStream saidaDoRegistro = new ByteArrayOutputStream()
    Roteador roteador = new Roteador(new RegistroDoServidor(new PrintStream(saidaDoRegistro, true, "UTF-8")))
    ServidorCadastro servidor = new ServidorCadastro(PORTA_LIVRE, roteador)
    HttpClient cliente = HttpClient.newHttpClient()

    def setup() {
        RotasDeCadastro.registrar(
                roteador,
                new CadastroCandidatoService(validador, candidatoRepositorio),
                new CadastroEmpresaService(validador, empresaRepositorio)
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

    String registro() {
        return saidaDoRegistro.toString("UTF-8")
    }

    def "POST /candidatos válido responde 201 com o registro criado, sem a senha"() {
        when:
        HttpResponse<String> resposta = chamar("POST", "/candidatos", JsonOutput.toJson(DadosFicticios.candidato()))

        then:
        1 * candidatoRepositorio.inserir(_) >> { Candidato candidato -> candidato.id = 7 }
        resposta.statusCode() == 201
        resposta.headers().firstValue("Content-Type").get() == "application/json; charset=UTF-8"
        !resposta.headers().firstValue("Location").isPresent()
        json(resposta) == [
                id            : 7,
                nome          : "Candidata",
                sobrenome     : "Exemplo",
                dataNascimento: "2000-01-15",
                email         : "candidata@example.com",
                cpf           : "123.456.789-00",
                pais          : "Brasil",
                cep           : "79000-000",
                descricao     : "Pessoa inventada para os testes",
                formacao      : "Curso de exemplo"
        ]
        !resposta.body().contains("senha")
    }

    def "POST /empresas válido responde 201 com o registro criado, sem a senha"() {
        when:
        HttpResponse<String> resposta = chamar("POST", "/empresas", JsonOutput.toJson(DadosFicticios.empresa()))

        then:
        1 * empresaRepositorio.inserir(_) >> { Empresa empresa -> empresa.id = 3 }
        resposta.statusCode() == 201
        !resposta.headers().firstValue("Location").isPresent()
        json(resposta) == [
                id       : 3,
                nome     : "Empresa Exemplo",
                email    : "contato@example.com",
                cnpj     : "12.345.678/0001-00",
                pais     : "Brasil",
                cep      : "79000-000",
                descricao: "Empresa inventada para os testes"
        ]
        !resposta.body().contains("senha")
    }

    def "dados inválidos respondem 400 citando o campo, sem repetir valores, e nada é gravado"() {
        when:
        HttpResponse<String> resposta = chamar("POST", "/candidatos", JsonOutput.toJson(DadosFicticios.candidato(cpf: "1234567890")))

        then:
        0 * candidatoRepositorio.inserir(_)
        resposta.statusCode() == 400
        json(resposta).erro.contains("'cpf'")
        !resposta.body().contains("1234567890")
        !resposta.body().contains(SENHA_ENVIADA)
        !resposta.body().contains("candidata@example.com")
    }

    def "cadastro repetido responde 409 com a mensagem genérica"() {
        given:
        candidatoRepositorio.inserir(_) >> { throw new CadastroRepetidoException() }
        empresaRepositorio.inserir(_) >> { throw new CadastroRepetidoException() }

        when:
        HttpResponse<String> candidato = chamar("POST", "/candidatos", JsonOutput.toJson(DadosFicticios.candidato()))
        HttpResponse<String> empresa = chamar("POST", "/empresas", JsonOutput.toJson(DadosFicticios.empresa()))

        then:
        candidato.statusCode() == 409
        empresa.statusCode() == 409
        json(candidato) == [erro: "Já existe um cadastro com esses dados"]
        json(empresa) == [erro: "Já existe um cadastro com esses dados"]
    }

    def "corpo #situacao responde 400 e nada é gravado"() {
        when:
        HttpResponse<String> resposta = chamar("POST", "/candidatos", corpo)

        then:
        0 * candidatoRepositorio.inserir(_)
        resposta.statusCode() == 400
        json(resposta).erro != null

        where:
        situacao              | corpo
        "vazio"               | ""
        "só com espaços"      | "   "
        "que não é JSON"      | '{"nome": "Candidata"'
        "que é lista"         | '[{"nome": "Candidata"}]'
        "que é texto simples" | '"Candidata"'
    }

    def "corpo em UTF-8 com acentos é gravado e devolvido com os acentos intactos"() {
        given:
        Candidato gravado = null
        String corpo = '{"nome":"Candidata","sobrenome":"Exemplo","dataNascimento":"2000-01-15","email":"candidata@example.com",' +
                '"cpf":"12345678900","pais":"Brasil","cep":"79000000","senha":"senha-ficticia","formacao":"Ciência da Computação"}'

        when:
        HttpResponse<String> resposta = chamar("POST", "/candidatos", corpo)

        then:
        1 * candidatoRepositorio.inserir(_) >> { Candidato candidato -> gravado = candidato; candidato.id = 10 }
        gravado.formacao == "Ciência da Computação"
        json(resposta).formacao == "Ciência da Computação"
    }

    def "corpo que não está em UTF-8 responde 400 e nada é gravado"() {
        given:
        byte[] corpoEmLatin1 = '{"nome":"Candidata","formacao":"Ciência"}'.getBytes("ISO-8859-1")
        HttpRequest requisicao = HttpRequest.newBuilder(URI.create("http://localhost:${servidor.porta}/candidatos"))
                .POST(HttpRequest.BodyPublishers.ofByteArray(corpoEmLatin1))
                .build()

        when:
        HttpResponse<String> resposta = cliente.send(requisicao, HttpResponse.BodyHandlers.ofString())

        then:
        0 * candidatoRepositorio.inserir(_)
        resposta.statusCode() == 400
        json(resposta).erro.contains("UTF-8")
    }

    def "JSON inválido não tem o conteúdo repetido na resposta"() {
        when:
        HttpResponse<String> resposta = chamar("POST", "/candidatos", '{"email": "candidata@example.com"')

        then:
        resposta.statusCode() == 400
        !resposta.body().contains("candidata@example.com")
    }

    def "competencias e campos desconhecidos no cadastro são ignorados"() {
        given:
        Map<String, Object> dados = DadosFicticios.candidato(competencias: ["Java"], apelido: "qualquer")

        when:
        HttpResponse<String> resposta = chamar("POST", "/candidatos", JsonOutput.toJson(dados))

        then:
        1 * candidatoRepositorio.inserir(_) >> { Candidato candidato -> candidato.id = 8 }
        resposta.statusCode() == 201
        !json(resposta).containsKey("competencias")
        !json(resposta).containsKey("apelido")
    }

    def "falha de banco responde 500 genérico e o detalhe técnico não vai para quem chamou nem para o registro"() {
        given:
        String mensagemDoBanco = 'ERROR: falha simulada\n  Detail: Key (email)=(candidata@example.com) already exists.'
        candidatoRepositorio.inserir(_) >> { throw new SQLException(mensagemDoBanco, "XX000") }

        when:
        HttpResponse<String> resposta = chamar("POST", "/candidatos", JsonOutput.toJson(DadosFicticios.candidato()))

        then:
        resposta.statusCode() == 500
        json(resposta) == [erro: "Erro ao acessar o banco de dados"]
        registro().contains("XX000")
        registro().contains("ERROR: falha simulada")
        !registro().contains("candidata@example.com")
    }

    def "#metodo em #caminho responde 405 com Allow: POST"() {
        when:
        HttpResponse<String> resposta = chamar(metodo, caminho)

        then:
        resposta.statusCode() == 405
        resposta.headers().firstValue("Allow").get() == "POST"

        where:
        metodo    | caminho
        "GET"     | "/candidatos"
        "DELETE"  | "/candidatos"
        "OPTIONS" | "/candidatos"
        "PUT"     | "/empresas"
        "GET"     | "/empresas"
    }

    def "POST em rota fora do cadastro responde 404"() {
        expect:
        chamar("POST", "/vagas", "{}").statusCode() == 404
    }

    def "o registro de um cadastro traz método, rota e status e nenhum dado do corpo"() {
        given:
        candidatoRepositorio.inserir(_) >> { Candidato candidato -> candidato.id = 9 }

        when:
        chamar("POST", "/candidatos", JsonOutput.toJson(DadosFicticios.candidato()))

        then:
        registro().contains("POST /candidatos 201")
        !registro().contains("candidata@example.com")
        !registro().contains("123.456.789-00")
        !registro().contains(SENHA_ENVIADA)
    }
}
