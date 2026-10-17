package org.linketinder.usuarios.http

import io.micronaut.core.type.Argument
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import org.linketinder.usuarios.DadosFicticios
import org.linketinder.usuarios.banco.TransacaoJdbc
import org.linketinder.usuarios.dao.CandidatoDAO
import org.linketinder.usuarios.model.Candidato
import org.linketinder.usuarios.repositorio.CandidatoRepositorio
import org.linketinder.usuarios.repositorio.Transacao
import org.linketinder.usuarios.service.CadastroRepetidoException
import spock.lang.Specification

import java.time.LocalDate

/**
 *
 * @author Guilherme Lima Conte
 */

@MicronautTest
class CandidatoControllerSpec extends Specification {

    static final Set<String> CAMPOS_DO_CANDIDATO = ["id", "nome", "sobrenome", "dataNascimento", "email", "cpf", "pais",
                                                    "cep", "descricao", "formacao", "competencias"] as Set

    @Inject
    @Client("/")
    HttpClient cliente

    @Inject
    CandidatoRepositorio repositorio

    @MockBean(CandidatoDAO)
    CandidatoRepositorio repositorioFalso() {
        return Mock(CandidatoRepositorio)
    }

    @MockBean(TransacaoJdbc)
    Transacao transacaoFalsa() {
        return Mock(Transacao) {
            executar(_) >> { Closure bloco -> bloco() }
        }
    }

    def "POST /candidatos cadastra e responde 201 com Location e sem a senha"() {
        given:
        repositorio.inserir(_) >> { Candidato c -> c.id = 9; c }

        when:
        HttpResponse<Map> resposta = cliente.toBlocking().exchange(HttpRequest.POST("/candidatos", DadosFicticios.candidato()), Map)

        then:
        resposta.status == HttpStatus.CREATED
        resposta.header("Location") == "/candidatos/9"
        resposta.body().keySet() == CAMPOS_DO_CANDIDATO
        resposta.body().email == "candidata@example.com"
        resposta.body().competencias == []
        !resposta.body().containsKey("senha")
    }

    def "POST /candidatos com campo inválido responde 400 citando o campo sem repetir o valor"() {
        when:
        cliente.toBlocking().exchange(HttpRequest.POST("/candidatos", DadosFicticios.candidato(cpf: "98765")), Map)

        then:
        HttpClientResponseException erro = thrown()
        erro.status == HttpStatus.BAD_REQUEST
        String mensagem = erro.response.getBody(Map).get().erro
        mensagem.contains("'cpf'")
        !mensagem.contains("98765")
    }

    def "POST /candidatos repetido responde 409 genérico"() {
        given:
        repositorio.inserir(_) >> { throw new CadastroRepetidoException() }

        when:
        cliente.toBlocking().exchange(HttpRequest.POST("/candidatos", DadosFicticios.candidato()), Map)

        then:
        HttpClientResponseException erro = thrown()
        erro.status == HttpStatus.CONFLICT
        erro.response.getBody(Map).get() == [erro: "Já existe um cadastro com esses dados"]
    }

    def "GET /candidatos lista sem a senha"() {
        given:
        repositorio.listar() >> [candidatoGravado(1), candidatoGravado(2)]

        when:
        HttpResponse<List<Map>> resposta = cliente.toBlocking().exchange(HttpRequest.GET("/candidatos"), Argument.listOf(Map))

        then:
        resposta.status == HttpStatus.OK
        resposta.body()*.id == [1, 2]
        resposta.body().every { Map candidato -> candidato.keySet() == CAMPOS_DO_CANDIDATO }
    }

    def "GET /candidatos/{id} busca, responde 404 se não existir e 400 se o id não for número"() {
        given:
        repositorio.buscarPorId(1) >> candidatoGravado(1)
        repositorio.buscarPorId(99) >> null

        expect:
        cliente.toBlocking().exchange(HttpRequest.GET("/candidatos/1"), Map).body().competencias == ["Java"]
        statusDe(HttpRequest.GET("/candidatos/99")) == HttpStatus.NOT_FOUND
        statusDe(HttpRequest.GET("/candidatos/abc")) == HttpStatus.BAD_REQUEST
    }

    def "PUT /candidatos/{id} atualiza e devolve o cadastro sem a senha"() {
        given:
        repositorio.buscarPorId(1) >> candidatoGravado(1)

        when:
        HttpResponse<Map> resposta = cliente.toBlocking().exchange(HttpRequest.PUT("/candidatos/1", DadosFicticios.candidato()), Map)

        then:
        resposta.status == HttpStatus.OK
        !resposta.body().containsKey("senha")
    }

    def "PUT com o e-mail de outro candidato responde 409"() {
        given:
        repositorio.buscarPorId(1) >> candidatoGravado(1)
        repositorio.atualizar(_) >> { throw new CadastroRepetidoException() }

        expect:
        statusDe(HttpRequest.PUT("/candidatos/1", DadosFicticios.candidato())) == HttpStatus.CONFLICT
    }

    def "PUT de candidato inexistente responde 404"() {
        given:
        repositorio.buscarPorId(99) >> null

        expect:
        statusDe(HttpRequest.PUT("/candidatos/99", DadosFicticios.candidato())) == HttpStatus.NOT_FOUND
    }

    def "DELETE /candidatos/{id} responde 204 e 404 se não existir"() {
        given:
        repositorio.buscarPorId(1) >> candidatoGravado(1)
        repositorio.buscarPorId(99) >> null

        when:
        HttpResponse resposta = cliente.toBlocking().exchange(HttpRequest.DELETE("/candidatos/1"))

        then:
        resposta.status == HttpStatus.NO_CONTENT
        statusDe(HttpRequest.DELETE("/candidatos/99")) == HttpStatus.NOT_FOUND
    }

    private HttpStatus statusDe(HttpRequest requisicao) {
        try {
            return cliente.toBlocking().exchange(requisicao, Map).status
        } catch (HttpClientResponseException erro) {
            return erro.status
        }
    }

    private static Candidato candidatoGravado(Integer id) {
        return new Candidato(id: id, nome: "Candidata", sobrenome: "Exemplo", dataNascimento: LocalDate.of(2000, 1, 15),
                email: "candidata${id}@example.com", cpf: "123.456.789-00", pais: "Brasil", cep: "79000-000",
                descricao: "", formacao: "Curso de exemplo", competencias: ["Java"])
    }
}
