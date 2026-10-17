package org.linketinder.competencias.http

import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import org.linketinder.competencias.DadosFicticios
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
class CompetenciasDoCandidatoControllerSpec extends Specification {

    @Inject
    @Client("/")
    HttpClient cliente

    @Inject
    CompetenciaRepositorio competenciaRepositorio

    @Inject
    CandidatoRepositorio candidatoRepositorio

    @MockBean(CompetenciaDAO)
    CompetenciaRepositorio competenciaRepositorioFalso() {
        return Mock(CompetenciaRepositorio)
    }

    @MockBean(CandidatoDAO)
    CandidatoRepositorio candidatoRepositorioFalso() {
        return Mock(CandidatoRepositorio)
    }

    @MockBean(TransacaoJdbc)
    Transacao transacaoFalsa() {
        return Mock(Transacao) {
            executar(_) >> { Closure bloco -> bloco() }
        }
    }

    def "POST /competencias soma ao candidato e responde 201 com o cadastro sem a senha"() {
        given:
        candidatoRepositorio.existe(7) >> true
        candidatoRepositorio.buscarPorId(7) >> DadosFicticios.candidata(7)
        competenciaRepositorio.buscarDoCandidato(7) >>> [[], [new Competencia(id: 1, nome: "Java")]]
        competenciaRepositorio.buscarPorNomeIgnorandoCaixa("java") >> new Competencia(id: 1, nome: "Java")

        when:
        HttpResponse<Map> resposta = cliente.toBlocking().exchange(
                HttpRequest.POST("/competencias", [idCandidato: 7, competencias: ["java"]]), Map)

        then:
        resposta.status == HttpStatus.CREATED
        resposta.body().id == 7
        resposta.body().competencias == ["Java"]
        resposta.body().keySet() == ["id", "nome", "sobrenome", "dataNascimento", "email", "cpf", "pais", "cep",
                                     "descricao", "formacao", "competencias"] as Set
        !resposta.body().containsKey("senha")
    }

    def "lista vazia responde 400"() {
        when:
        cliente.toBlocking().exchange(HttpRequest.POST("/competencias", [idCandidato: 7, competencias: []]), Map)

        then:
        HttpClientResponseException erro = thrown()
        erro.status == HttpStatus.BAD_REQUEST
        erro.response.getBody(Map).get() == [erro: "O campo 'competencias' deve ter ao menos uma competência"]
    }

    def "idCandidato ausente ou não numérico responde 400"() {
        when:
        cliente.toBlocking().exchange(HttpRequest.POST("/competencias", [idCandidato: "7", competencias: ["Java"]]), Map)

        then:
        HttpClientResponseException erro = thrown()
        erro.status == HttpStatus.BAD_REQUEST
        erro.response.getBody(Map).get() == [erro: "O campo 'idCandidato' é obrigatório e deve ser um número"]
    }

    def "candidato inexistente responde 404"() {
        given:
        candidatoRepositorio.existe(99) >> false

        when:
        cliente.toBlocking().exchange(HttpRequest.POST("/competencias", [idCandidato: 99, competencias: ["Java"]]), Map)

        then:
        HttpClientResponseException erro = thrown()
        erro.status == HttpStatus.NOT_FOUND
        erro.response.getBody(Map).get() == [erro: "Candidato 99 não encontrado"]
    }
}
