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
import org.linketinder.usuarios.dao.EmpresaDAO
import org.linketinder.usuarios.model.Empresa
import org.linketinder.usuarios.repositorio.EmpresaRepositorio
import org.linketinder.usuarios.service.CadastroRepetidoException
import spock.lang.Specification

/**
 *
 * @author Guilherme Lima Conte
 */

@MicronautTest
class EmpresaControllerSpec extends Specification {

    static final Set<String> CAMPOS_DA_EMPRESA = ["id", "nome", "email", "cnpj", "pais", "cep", "descricao"] as Set

    @Inject
    @Client("/")
    HttpClient cliente

    @Inject
    EmpresaRepositorio repositorio

    @MockBean(EmpresaDAO)
    EmpresaRepositorio repositorioFalso() {
        return Mock(EmpresaRepositorio)
    }

    def "POST /empresas cadastra e responde 201 com Location e sem a senha"() {
        given:
        repositorio.inserir(_) >> { Empresa e -> e.id = 3; e }

        when:
        HttpResponse<Map> resposta = cliente.toBlocking().exchange(HttpRequest.POST("/empresas", DadosFicticios.empresa()), Map)

        then:
        resposta.status == HttpStatus.CREATED
        resposta.header("Location") == "/empresas/3"
        resposta.body().keySet() == CAMPOS_DA_EMPRESA
    }

    def "POST /empresas repetido responde 409 e inválido responde 400"() {
        given:
        repositorio.inserir(_) >> { throw new CadastroRepetidoException() }

        expect:
        statusDe(HttpRequest.POST("/empresas", DadosFicticios.empresa())) == HttpStatus.CONFLICT
        statusDe(HttpRequest.POST("/empresas", DadosFicticios.empresa(cep: "123"))) == HttpStatus.BAD_REQUEST
    }

    def "GET /empresas e GET /empresas/{id} nunca devolvem a senha"() {
        given:
        repositorio.listar() >> [empresaGravada(1), empresaGravada(2)]
        repositorio.buscarPorId(1) >> empresaGravada(1)
        repositorio.buscarPorId(99) >> null

        when:
        HttpResponse<List<Map>> lista = cliente.toBlocking().exchange(HttpRequest.GET("/empresas"), Argument.listOf(Map))
        Map uma = cliente.toBlocking().exchange(HttpRequest.GET("/empresas/1"), Map).body()

        then:
        lista.body().every { Map empresa -> empresa.keySet() == CAMPOS_DA_EMPRESA }
        uma.keySet() == CAMPOS_DA_EMPRESA
        statusDe(HttpRequest.GET("/empresas/99")) == HttpStatus.NOT_FOUND
        statusDe(HttpRequest.GET("/empresas/abc")) == HttpStatus.BAD_REQUEST
    }

    def "PUT /empresas/{id} atualiza e responde 200"() {
        given:
        repositorio.buscarPorId(1) >> empresaGravada(1)

        when:
        HttpResponse<Map> resposta = cliente.toBlocking().exchange(HttpRequest.PUT("/empresas/1", DadosFicticios.empresa()), Map)

        then:
        resposta.status == HttpStatus.OK
        resposta.body().keySet() == CAMPOS_DA_EMPRESA
    }

    def "DELETE /empresas/{id} responde 204, 400 com vagas e 404 se não existir"() {
        given:
        repositorio.buscarPorId(1) >> empresaGravada(1)
        repositorio.buscarPorId(2) >> empresaGravada(2)
        repositorio.buscarPorId(99) >> null
        repositorio.possuiVagas(1) >> false
        repositorio.possuiVagas(2) >> true

        expect:
        statusDe(HttpRequest.DELETE("/empresas/1")) == HttpStatus.NO_CONTENT
        statusDe(HttpRequest.DELETE("/empresas/2")) == HttpStatus.BAD_REQUEST
        statusDe(HttpRequest.DELETE("/empresas/99")) == HttpStatus.NOT_FOUND
    }

    private HttpStatus statusDe(HttpRequest requisicao) {
        try {
            return cliente.toBlocking().exchange(requisicao, Map).status
        } catch (HttpClientResponseException erro) {
            return erro.status
        }
    }

    private static Empresa empresaGravada(Integer id) {
        return new Empresa(id: id, nome: "Empresa Exemplo", email: "contato${id}@example.com", cnpj: "12.345.678/0001-00",
                pais: "Brasil", cep: "79000-000", descricao: "")
    }
}
