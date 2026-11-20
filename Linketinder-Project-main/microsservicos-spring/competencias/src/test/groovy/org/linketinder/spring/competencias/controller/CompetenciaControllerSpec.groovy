package org.linketinder.spring.competencias.controller

import org.linketinder.spring.competencias.ClienteDeTeste
import org.linketinder.spring.competencias.RespostaDeTeste
import org.linketinder.spring.competencias.model.Competencia
import org.linketinder.spring.competencias.repositorio.CompetenciaRepositorio
import org.linketinder.spring.competencias.service.CompetenciaService
import spock.lang.Specification

/**
 *
 * @author Guilherme Lima Conte
 */

class CompetenciaControllerSpec extends Specification {

    CompetenciaRepositorio repositorio = Mock()
    ClienteDeTeste cliente = new ClienteDeTeste(new CompetenciaController(new CompetenciaService(repositorio)))

    def "GET /competencias lista id e nome"() {
        given:
        repositorio.listarTodas() >> [new Competencia(id: 1, nome: "Java"), new Competencia(id: 3, nome: "SQL")]

        when:
        RespostaDeTeste resposta = cliente.get("/competencias")

        then:
        resposta.status == 200
        resposta.corpo == [[id: 1, nome: "Java"], [id: 3, nome: "SQL"]]
    }

    def "POST /competencias cria e responde 201"() {
        when:
        RespostaDeTeste resposta = cliente.post("/competencias", [nome: "Kotlin"])

        then:
        1 * repositorio.saveAndFlush(_) >> { Competencia competencia ->
            competencia.id = 11
            return competencia
        }
        resposta.status == 201
        resposta.corpo == [id: 11, nome: "Kotlin"]
    }

    def "POST /competencias repetida responde 400"() {
        given:
        repositorio.buscarPorNomeIgnorandoCaixa("java") >> new Competencia(id: 1, nome: "Java")

        when:
        RespostaDeTeste resposta = cliente.post("/competencias", [nome: "java"])

        then:
        resposta.status == 400
        resposta.corpo == [erro: "Essa competência já está cadastrada"]
    }

    def "GET /competencias/{id} inexistente responde 404"() {
        given:
        repositorio.findById(9) >> Optional.empty()

        when:
        RespostaDeTeste resposta = cliente.get("/competencias/9")

        then:
        resposta.status == 404
        resposta.corpo == [erro: "Competência 9 não encontrada"]
    }

    def "PUT /competencias/{id} renomeia"() {
        given:
        Competencia competencia = new Competencia(id: 1, nome: "java")
        repositorio.findById(1) >> Optional.of(competencia)
        repositorio.saveAndFlush(competencia) >> competencia

        when:
        RespostaDeTeste resposta = cliente.put("/competencias/1", [nome: "Java"])

        then:
        resposta.status == 200
        resposta.corpo == [id: 1, nome: "Java"]
    }

    def "DELETE /competencias/{id} ligada responde 400 e sem ligações responde 204"() {
        given:
        repositorio.findById(_) >> { Integer id -> Optional.of(new Competencia(id: id, nome: "Qualquer")) }
        repositorio.estaVinculada(1) >> true
        repositorio.estaVinculada(2) >> false

        expect:
        cliente.delete("/competencias/1").status == 400
        cliente.delete("/competencias/2").status == 204
    }
}
