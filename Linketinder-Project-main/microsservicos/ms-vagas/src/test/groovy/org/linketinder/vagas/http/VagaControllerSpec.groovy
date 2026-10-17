package org.linketinder.vagas.http

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
import org.linketinder.vagas.DadosFicticios
import org.linketinder.vagas.banco.TransacaoJdbc
import org.linketinder.vagas.dao.CompetenciaDAO
import org.linketinder.vagas.dao.EmpresaDAO
import org.linketinder.vagas.dao.VagaDAO
import org.linketinder.vagas.model.Competencia
import org.linketinder.vagas.model.Vaga
import org.linketinder.vagas.repositorio.CompetenciaRepositorio
import org.linketinder.vagas.repositorio.EmpresaRepositorio
import org.linketinder.vagas.repositorio.Transacao
import org.linketinder.vagas.repositorio.VagaRepositorio
import spock.lang.Specification

/**
 *
 * @author Guilherme Lima Conte
 */

@MicronautTest
class VagaControllerSpec extends Specification {

    static final Set<String> CAMPOS_DA_VAGA = ["id", "nome", "descricao", "estado", "cidade", "empresa", "competencias"] as Set

    @Inject
    @Client("/")
    HttpClient cliente

    @Inject
    VagaRepositorio vagaRepositorio

    @Inject
    EmpresaRepositorio empresaRepositorio

    @Inject
    CompetenciaRepositorio competenciaRepositorio

    @MockBean(VagaDAO)
    VagaRepositorio vagaRepositorioFalso() {
        return Mock(VagaRepositorio)
    }

    @MockBean(CompetenciaDAO)
    CompetenciaRepositorio competenciaRepositorioFalso() {
        return Mock(CompetenciaRepositorio)
    }

    @MockBean(EmpresaDAO)
    EmpresaRepositorio empresaRepositorioFalso() {
        return Mock(EmpresaRepositorio)
    }

    @MockBean(TransacaoJdbc)
    Transacao transacaoFalsa() {
        return Mock(Transacao) {
            executar(_) >> { Closure bloco -> bloco() }
        }
    }

    def setup() {
        empresaRepositorio.existe(3) >> true
        vagaRepositorio.buscarPorId(10) >> DadosFicticios.vagaGravada(10)
        competenciaRepositorio.buscarPorNomeIgnorandoCaixa(_) >> { String nome -> new Competencia(id: nome.length(), nome: nome) }
    }

    def "POST /vagas cadastra e responde 201 com Location e a empresa {id, nome}"() {
        given:
        vagaRepositorio.inserir(_) >> { Vaga v -> v.id = 10; v }

        when:
        HttpResponse<Map> resposta = cliente.toBlocking().exchange(HttpRequest.POST("/vagas", DadosFicticios.vaga()), Map)

        then:
        resposta.status == HttpStatus.CREATED
        resposta.header("Location") == "/vagas/10"
        resposta.body().keySet() == CAMPOS_DA_VAGA
        resposta.body().empresa == [id: 3, nome: "Empresa Exemplo"]
    }

    def "POST /vagas responde 400 para empresa inexistente, idEmpresa ausente ou campo obrigatório vazio"() {
        given:
        empresaRepositorio.existe(99) >> false

        expect:
        statusDe(HttpRequest.POST("/vagas", DadosFicticios.vaga(idEmpresa: 99))) == HttpStatus.BAD_REQUEST
        statusDe(HttpRequest.POST("/vagas", DadosFicticios.vaga(idEmpresa: "3"))) == HttpStatus.BAD_REQUEST
        statusDe(HttpRequest.POST("/vagas", DadosFicticios.vaga(cidade: ""))) == HttpStatus.BAD_REQUEST
    }

    def "GET /vagas lista todas ou as de uma empresa"() {
        given:
        vagaRepositorio.listar() >> [DadosFicticios.vagaGravada(10), DadosFicticios.vagaGravada(11, 4)]
        vagaRepositorio.listarPorEmpresa(3) >> [DadosFicticios.vagaGravada(10)]

        expect:
        cliente.toBlocking().exchange(HttpRequest.GET("/vagas"), Argument.listOf(Map)).body()*.id == [10, 11]
        cliente.toBlocking().exchange(HttpRequest.GET("/vagas?idEmpresa=3"), Argument.listOf(Map)).body()*.id == [10]
        statusDe(HttpRequest.GET("/vagas?idEmpresa=abc")) == HttpStatus.BAD_REQUEST
    }

    def "GET /vagas/{id} busca, responde 404 se não existir e 400 se o id não for número"() {
        given:
        vagaRepositorio.buscarPorId(99) >> null

        expect:
        cliente.toBlocking().exchange(HttpRequest.GET("/vagas/10"), Map).body().keySet() == CAMPOS_DA_VAGA
        statusDe(HttpRequest.GET("/vagas/99")) == HttpStatus.NOT_FOUND
        statusDe(HttpRequest.GET("/vagas/abc")) == HttpStatus.BAD_REQUEST
    }

    def "PUT /vagas/{id} atualiza e responde 200; inexistente responde 404"() {
        given:
        vagaRepositorio.buscarPorId(99) >> null

        expect:
        statusDe(HttpRequest.PUT("/vagas/10", DadosFicticios.vaga())) == HttpStatus.OK
        statusDe(HttpRequest.PUT("/vagas/99", DadosFicticios.vaga())) == HttpStatus.NOT_FOUND
    }

    def "DELETE /vagas/{id} responde 204; inexistente responde 404"() {
        given:
        vagaRepositorio.buscarPorId(99) >> null

        expect:
        statusDe(HttpRequest.DELETE("/vagas/10")) == HttpStatus.NO_CONTENT
        statusDe(HttpRequest.DELETE("/vagas/99")) == HttpStatus.NOT_FOUND
    }

    private HttpStatus statusDe(HttpRequest requisicao) {
        try {
            return cliente.toBlocking().exchange(requisicao, String).status
        } catch (HttpClientResponseException erro) {
            return erro.status
        }
    }
}
