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
import org.linketinder.usuarios.dao.EmpresaDAO
import org.linketinder.usuarios.dao.RecrutadorDAO
import org.linketinder.usuarios.dao.VagaDAO
import org.linketinder.usuarios.model.Empresa
import org.linketinder.usuarios.model.Recrutador
import org.linketinder.usuarios.model.Vaga
import org.linketinder.usuarios.repositorio.EmpresaRepositorio
import org.linketinder.usuarios.repositorio.RecrutadorRepositorio
import org.linketinder.usuarios.repositorio.Transacao
import org.linketinder.usuarios.repositorio.VagaRepositorio
import spock.lang.Specification

/**
 *
 * @author Guilherme Lima Conte
 */

@MicronautTest
class RecrutadorControllerSpec extends Specification {

    static final Set<String> CAMPOS_DO_RECRUTADOR = ["id", "nome", "area", "descricao", "email", "idEmpresa"] as Set
    static final Set<String> CAMPOS_DA_VAGA = ["id", "nome", "descricao", "estado", "cidade", "empresa", "competencias"] as Set

    @Inject
    @Client("/")
    HttpClient cliente

    @Inject
    RecrutadorRepositorio recrutadorRepositorio

    @Inject
    EmpresaRepositorio empresaRepositorio

    @Inject
    VagaRepositorio vagaRepositorio

    @MockBean(RecrutadorDAO)
    RecrutadorRepositorio recrutadorRepositorioFalso() {
        return Mock(RecrutadorRepositorio)
    }

    @MockBean(EmpresaDAO)
    EmpresaRepositorio empresaRepositorioFalso() {
        return Mock(EmpresaRepositorio)
    }

    @MockBean(VagaDAO)
    VagaRepositorio vagaRepositorioFalso() {
        return Mock(VagaRepositorio)
    }

    @MockBean(TransacaoJdbc)
    Transacao transacaoFalsa() {
        return Mock(Transacao) {
            executar(_) >> { Closure bloco -> bloco() }
        }
    }

    def setup() {
        empresaRepositorio.buscarPorId(3) >> new Empresa(id: 3)
        recrutadorRepositorio.buscarPorId(5) >> recrutadorGravado()
        vagaRepositorio.buscarPorId(10) >> vagaDaEmpresa(10, 3)
        vagaRepositorio.buscarPorId(20) >> vagaDaEmpresa(20, 4)
    }

    def "POST cadastra recrutador e não expõe senha, CPF nem data de nascimento"() {
        given:
        recrutadorRepositorio.inserir(_) >> { Recrutador r -> r.id = 8; r }

        when:
        HttpResponse<Map> resposta = cliente.toBlocking().exchange(HttpRequest.POST("/recrutadores", DadosFicticios.recrutador()), Map)

        then:
        resposta.status == HttpStatus.CREATED
        resposta.header("Location") == "/recrutadores/8"
        resposta.body().keySet() == CAMPOS_DO_RECRUTADOR
        !resposta.body().toString().contains("987.654.321-11")
        !resposta.body().toString().contains("1995-05-20")
    }

    def "POST responde 404 para empresa inexistente e 400 para dados inválidos ou repetidos"() {
        given:
        recrutadorRepositorio.buscarIdPorEmail("repetida@example.com") >> 7

        expect:
        statusDe(HttpRequest.POST("/recrutadores", DadosFicticios.recrutador(idEmpresa: 99))) == HttpStatus.NOT_FOUND
        statusDe(HttpRequest.POST("/recrutadores", DadosFicticios.recrutador(cpf: "123"))) == HttpStatus.BAD_REQUEST
        statusDe(HttpRequest.POST("/recrutadores", DadosFicticios.recrutador(email: "repetida@example.com"))) == HttpStatus.BAD_REQUEST
        statusDe(HttpRequest.POST("/recrutadores", DadosFicticios.sem(DadosFicticios.recrutador(), "idEmpresa"))) == HttpStatus.BAD_REQUEST
    }

    def "GET ?idEmpresa lista os recrutadores da empresa sem dados restritos"() {
        given:
        recrutadorRepositorio.listarPorEmpresa(3) >> [recrutadorGravado()]

        when:
        HttpResponse<List<Map>> resposta = cliente.toBlocking().exchange(HttpRequest.GET("/recrutadores?idEmpresa=3"), Argument.listOf(Map))

        then:
        resposta.body().size() == 1
        resposta.body()[0].keySet() == CAMPOS_DO_RECRUTADOR
    }

    def "GET exige idEmpresa numérico e existente"() {
        expect:
        statusDe(HttpRequest.GET("/recrutadores")) == HttpStatus.BAD_REQUEST
        statusDe(HttpRequest.GET("/recrutadores?idEmpresa=abc")) == HttpStatus.BAD_REQUEST
        statusDe(HttpRequest.GET("/recrutadores?idEmpresa=99")) == HttpStatus.NOT_FOUND
    }

    def "PUT atualiza e responde 200 sem dados restritos"() {
        when:
        HttpResponse<Map> resposta = cliente.toBlocking().exchange(HttpRequest.PUT("/recrutadores/5", DadosFicticios.recrutador()), Map)

        then:
        resposta.status == HttpStatus.OK
        resposta.body().keySet() == CAMPOS_DO_RECRUTADOR
        statusDe(HttpRequest.PUT("/recrutadores/99", DadosFicticios.recrutador())) == HttpStatus.NOT_FOUND
    }

    def "DELETE remove o recrutador com 204 e responde 404 se não existir"() {
        expect:
        statusDe(HttpRequest.DELETE("/recrutadores/5")) == HttpStatus.NO_CONTENT
        statusDe(HttpRequest.DELETE("/recrutadores/99")) == HttpStatus.NOT_FOUND
    }

    def "GET vagas devolve as vagas atribuídas no formato de GET /vagas"() {
        given:
        vagaRepositorio.listarAtribuidasAoRecrutador(5) >> [vagaDaEmpresa(10, 3)]

        when:
        HttpResponse<List<Map>> resposta = cliente.toBlocking().exchange(HttpRequest.GET("/recrutadores/5/vagas"), Argument.listOf(Map))

        then:
        resposta.body()[0].keySet() == CAMPOS_DA_VAGA
        resposta.body()[0].empresa == [id: 3, nome: "Empresa Exemplo"]
        statusDe(HttpRequest.GET("/recrutadores/99/vagas")) == HttpStatus.NOT_FOUND
        statusDe(HttpRequest.GET("/recrutadores/abc/vagas")) == HttpStatus.BAD_REQUEST
    }

    def "POST vagas atribui e responde 201 com a lista atualizada"() {
        given:
        vagaRepositorio.listarAtribuidasAoRecrutador(5) >> [vagaDaEmpresa(10, 3)]

        when:
        HttpResponse<List<Map>> resposta = cliente.toBlocking().exchange(HttpRequest.POST("/recrutadores/5/vagas", [idVaga: 10]), Argument.listOf(Map))

        then:
        resposta.status == HttpStatus.CREATED
        resposta.body()*.id == [10]
    }

    def "POST vagas responde 404 para recrutador ou vaga inexistente e 400 para vaga de outra empresa"() {
        expect:
        statusDe(HttpRequest.POST("/recrutadores/99/vagas", [idVaga: 10])) == HttpStatus.NOT_FOUND
        statusDe(HttpRequest.POST("/recrutadores/5/vagas", [idVaga: 99])) == HttpStatus.NOT_FOUND
        statusDe(HttpRequest.POST("/recrutadores/5/vagas", [idVaga: 20])) == HttpStatus.BAD_REQUEST
    }

    def "DELETE vagas desatribui com 204 e recusa vaga não atribuída com 400"() {
        given:
        recrutadorRepositorio.vagaAtribuida(5, 10) >>> [true, false]

        expect:
        statusDe(HttpRequest.DELETE("/recrutadores/5/vagas/10")) == HttpStatus.NO_CONTENT
        statusDe(HttpRequest.DELETE("/recrutadores/5/vagas/10")) == HttpStatus.BAD_REQUEST
        statusDe(HttpRequest.DELETE("/recrutadores/99/vagas/10")) == HttpStatus.NOT_FOUND
        statusDe(HttpRequest.DELETE("/recrutadores/5/vagas/99")) == HttpStatus.NOT_FOUND
    }

    private HttpStatus statusDe(HttpRequest requisicao) {
        try {
            return cliente.toBlocking().exchange(requisicao, String).status
        } catch (HttpClientResponseException erro) {
            return erro.status
        }
    }

    private static Recrutador recrutadorGravado() {
        return new Recrutador(id: 5, nome: "Recrutadora Exemplo", area: "TI", descricao: "", email: "recrutadora@example.com", idEmpresa: 3)
    }

    private static Vaga vagaDaEmpresa(Integer id, Integer idEmpresa) {
        return new Vaga(id: id, nome: "Vaga Exemplo", descricao: "", estado: "MS", cidade: "Campo Grande", idEmpresa: idEmpresa,
                nomeEmpresa: "Empresa Exemplo", competencias: ["Java"])
    }
}
