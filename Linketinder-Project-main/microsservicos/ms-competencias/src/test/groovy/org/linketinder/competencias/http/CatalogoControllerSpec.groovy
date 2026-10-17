package org.linketinder.competencias.http

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
import org.linketinder.competencias.banco.TransacaoJdbc
import org.linketinder.competencias.dao.CandidatoDAO
import org.linketinder.competencias.dao.CompetenciaDAO
import org.linketinder.competencias.model.Competencia
import org.linketinder.competencias.repositorio.CandidatoRepositorio
import org.linketinder.competencias.repositorio.CompetenciaRepositorio
import org.linketinder.competencias.repositorio.Transacao
import spock.lang.Specification

/**
 *
 * @author Guilherme Lima Conte
 */

@MicronautTest
class CatalogoControllerSpec extends Specification {

    @Inject
    @Client("/")
    HttpClient cliente

    @Inject
    CompetenciaRepositorio repositorio

    @MockBean(CompetenciaDAO)
    CompetenciaRepositorio repositorioFalso() {
        return Mock(CompetenciaRepositorio)
    }

    @MockBean(CandidatoDAO)
    CandidatoRepositorio candidatoRepositorioFalso() {
        return Mock(CandidatoRepositorio)
    }

    @MockBean(TransacaoJdbc)
    Transacao transacaoFalsa() {
        return Mock(Transacao)
    }

    def "GET /competencias lista id e nome"() {
        given:
        repositorio.listar() >> [new Competencia(id: 1, nome: "Java"), new Competencia(id: 2, nome: "SQL")]

        when:
        HttpResponse<List<Map>> resposta = cliente.toBlocking().exchange(HttpRequest.GET("/competencias"), Argument.listOf(Map))

        then:
        resposta.status == HttpStatus.OK
        resposta.body() == [[id: 1, nome: "Java"], [id: 2, nome: "SQL"]]
    }

    def "POST /competencias/catalogo cria e responde 201 com Location"() {
        given:
        repositorio.buscarPorNomeIgnorandoCaixa("Kotlin") >> null
        repositorio.inserir("Kotlin") >> new Competencia(id: 9, nome: "Kotlin")

        when:
        HttpResponse<Map> resposta = cliente.toBlocking().exchange(HttpRequest.POST("/competencias/catalogo", [nome: "Kotlin"]), Map)

        then:
        resposta.status == HttpStatus.CREATED
        resposta.header("Location") == "/competencias/9"
        resposta.body() == [id: 9, nome: "Kotlin"]
    }

    def "POST /competencias/catalogo com nome repetido em outra caixa responde 409"() {
        given:
        repositorio.buscarPorNomeIgnorandoCaixa("JAVA") >> new Competencia(id: 1, nome: "Java")

        when:
        cliente.toBlocking().exchange(HttpRequest.POST("/competencias/catalogo", [nome: "JAVA"]), Map)

        then:
        HttpClientResponseException erro = thrown()
        erro.status == HttpStatus.CONFLICT
        erro.response.getBody(Map).get() == [erro: "Essa competência já está cadastrada"]
    }

    def "POST /competencias/catalogo sem nome responde 400 citando o campo"() {
        when:
        cliente.toBlocking().exchange(HttpRequest.POST("/competencias/catalogo", [:]), Map)

        then:
        HttpClientResponseException erro = thrown()
        erro.status == HttpStatus.BAD_REQUEST
        erro.response.getBody(Map).get() == [erro: "O campo 'nome' é obrigatório"]
    }

    def "PUT /competencias/{id} renomeia e responde 200"() {
        given:
        repositorio.buscarPorId(4) >> new Competencia(id: 4, nome: "Cobol")
        repositorio.buscarPorNomeIgnorandoCaixa("Fortran") >> null

        when:
        HttpResponse<Map> resposta = cliente.toBlocking().exchange(HttpRequest.PUT("/competencias/4", [nome: "Fortran"]), Map)

        then:
        resposta.status == HttpStatus.OK
        resposta.body() == [id: 4, nome: "Fortran"]
    }

    def "PUT de competência inexistente responde 404"() {
        given:
        repositorio.buscarPorId(99) >> null

        when:
        cliente.toBlocking().exchange(HttpRequest.PUT("/competencias/99", [nome: "Fortran"]), Map)

        then:
        HttpClientResponseException erro = thrown()
        erro.status == HttpStatus.NOT_FOUND
    }

    def "DELETE /competencias/{id} sem vínculo responde 204"() {
        given:
        repositorio.buscarPorId(4) >> new Competencia(id: 4, nome: "Cobol")
        repositorio.estaVinculada(4) >> false

        when:
        HttpResponse resposta = cliente.toBlocking().exchange(HttpRequest.DELETE("/competencias/4"))

        then:
        resposta.status == HttpStatus.NO_CONTENT
    }

    def "DELETE de competência vinculada responde 400"() {
        given:
        repositorio.buscarPorId(1) >> new Competencia(id: 1, nome: "Java")
        repositorio.estaVinculada(1) >> true

        when:
        cliente.toBlocking().exchange(HttpRequest.DELETE("/competencias/1"))

        then:
        HttpClientResponseException erro = thrown()
        erro.status == HttpStatus.BAD_REQUEST
    }

    def "id não numérico no PUT e no DELETE responde 400"() {
        when:
        cliente.toBlocking().exchange(HttpRequest.DELETE("/competencias/abc"))

        then:
        HttpClientResponseException erro = thrown()
        erro.status == HttpStatus.BAD_REQUEST
        erro.response.getBody(Map).get() == [erro: CorpoJson.ID_INVALIDO]
    }

    def "GET /competencias/{id} não existe e responde 405 com Allow"() {
        when:
        cliente.toBlocking().exchange(HttpRequest.GET("/competencias/1"), Map)

        then:
        HttpClientResponseException erro = thrown()
        erro.status == HttpStatus.METHOD_NOT_ALLOWED
        erro.response.header("Allow") != null
    }
}
