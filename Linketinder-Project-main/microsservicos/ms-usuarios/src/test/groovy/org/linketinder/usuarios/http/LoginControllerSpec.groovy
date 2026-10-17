package org.linketinder.usuarios.http

import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import org.linketinder.usuarios.dao.CandidatoDAO
import org.linketinder.usuarios.dao.EmpresaDAO
import org.linketinder.usuarios.dao.RecrutadorDAO
import org.linketinder.usuarios.model.Recrutador
import org.linketinder.usuarios.repositorio.CandidatoRepositorio
import org.linketinder.usuarios.repositorio.EmpresaRepositorio
import org.linketinder.usuarios.repositorio.RecrutadorRepositorio
import spock.lang.Specification

/**
 *
 * @author Guilherme Lima Conte
 */

@MicronautTest
class LoginControllerSpec extends Specification {

    @Inject
    @Client("/")
    HttpClient cliente

    @Inject
    RecrutadorRepositorio recrutadorRepositorio

    @MockBean(CandidatoDAO)
    CandidatoRepositorio candidatoRepositorioFalso() {
        return Mock(CandidatoRepositorio)
    }

    @MockBean(EmpresaDAO)
    EmpresaRepositorio empresaRepositorioFalso() {
        return Mock(EmpresaRepositorio)
    }

    @MockBean(RecrutadorDAO)
    RecrutadorRepositorio recrutadorRepositorioFalso() {
        return Mock(RecrutadorRepositorio)
    }

    def "POST /login responde 200 com tipo, id, nome e idEmpresa do recrutador, sem a senha"() {
        given:
        recrutadorRepositorio.buscarParaLogin("recrutadora@example.com") >> new Recrutador(id: 3, nome: "Recrutadora", senha: "senha-ficticia", idEmpresa: 2)

        when:
        HttpResponse<Map> resposta = cliente.toBlocking().exchange(HttpRequest.POST("/login",
                [tipo: "recrutador", email: " recrutadora@example.com ", senha: "senha-ficticia"]), Map)

        then:
        resposta.status == HttpStatus.OK
        resposta.body() == [tipo: "recrutador", id: 3, nome: "Recrutadora", idEmpresa: 2]
    }

    def "credenciais erradas respondem 401 genérico"() {
        when:
        cliente.toBlocking().exchange(HttpRequest.POST("/login", [tipo: "candidato", email: "ninguem@example.com", senha: "qualquer"]), Map)

        then:
        HttpClientResponseException erro = thrown()
        erro.status == HttpStatus.UNAUTHORIZED
        erro.response.getBody(Map).get() == [erro: "E-mail ou senha inválidos"]
    }

    def "tipo inválido ou campo ausente respondem 400"() {
        when:
        cliente.toBlocking().exchange(HttpRequest.POST("/login", corpo), Map)

        then:
        HttpClientResponseException erro = thrown()
        erro.status == HttpStatus.BAD_REQUEST

        where:
        corpo << [
                [tipo: "administrador", email: "a@example.com", senha: "qualquer"],
                [tipo: "candidato", senha: "qualquer"],
                [tipo: "candidato", email: "a@example.com"]
        ]
    }

    def "GET /login responde 405 com Allow: POST"() {
        when:
        cliente.toBlocking().exchange(HttpRequest.GET("/login"), Map)

        then:
        HttpClientResponseException erro = thrown()
        erro.status == HttpStatus.METHOD_NOT_ALLOWED
        erro.response.header("Allow") == "POST"
    }
}
