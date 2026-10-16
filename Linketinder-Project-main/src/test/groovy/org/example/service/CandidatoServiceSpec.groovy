package org.example.service

import org.example.model.Candidato
import org.example.repositorio.CandidatoRepositorio
import spock.lang.Specification

import java.time.LocalDate

/**
 *
 * @author Guilherme Lima Conte
 */

class CandidatoServiceSpec extends Specification{

    CandidatoRepositorio candidatoRepositorio = Mock(CandidatoRepositorio)
    CandidatoService candidatoService = new CandidatoService(candidatoRepositorio)

    Candidato candidato() {
        return new Candidato("Ana", "Silva", LocalDate.of(2003, 4, 21), "ana@gmail.com", "111",
                "Brasil", "0", "d", "123456", "f", [])
    }

    def "cadastrar envia o candidato para o repositório"() {
        given:
        Candidato novo = candidato()

        when:
        candidatoService.cadastrar(novo)

        then:
        1 * candidatoRepositorio.inserir(novo)
    }

    def "atualizar mantém o ID do candidato escolhido"() {
        when:
        candidatoService.atualizar(7, candidato())

        then:
        1 * candidatoRepositorio.atualizar({Candidato atualizado -> atualizado.id == 7})
    }

    def "excluir repassa o ID para o repositório"() {
        when:
        candidatoService.excluir(3)

        then:
        1 * candidatoRepositorio.deletar(3)
    }

    def "autenticar devolve o candidato quando e-mail e senha conferem"() {
        given:
        Candidato existente = candidato()
        candidatoRepositorio.buscarPorEmail("ana@example.com") >> existente

        expect:
        candidatoService.autenticar("ana@example.com", "123456") == existente
    }

    def "autenticar devolve null com senha errada ou e-mail inexistente"() {
        given:
        candidatoRepositorio.buscarPorEmail("ana@example.com") >> candidato()
        candidatoRepositorio.buscarPorEmail("ninguem@example.com") >> null

        expect:
        candidatoService.autenticar("ana@example.com", "errada") == null
        candidatoService.autenticar("ninguem@example.com", "123456") == null
    }

    def "adicionarCompetencias soma às existentes sem repetir e atualiza o candidato"() {
        given:
        Candidato existente = candidato()
        existente.competencias = ["Java"]
        candidatoRepositorio.buscarPorId(1) >> existente

        when:
        Candidato atualizado = candidatoService.adicionarCompetencias(1, ["java", "SQL"])

        then:
        atualizado.competencias == ["Java", "SQL"]
        1 * candidatoRepositorio.atualizar(existente)
    }

    def "adicionarCompetencias devolve null para candidato inexistente"() {
        given:
        candidatoRepositorio.buscarPorId(99) >> null

        when:
        Candidato resultado = candidatoService.adicionarCompetencias(99, ["Java"])

        then:
        resultado == null
        0 * candidatoRepositorio.atualizar(_)
    }

    def "adicionarCompetencias com lista vazia lança RegraDeNegocioException"() {
        when:
        candidatoService.adicionarCompetencias(1, [])

        then:
        thrown(RegraDeNegocioException)
        0 * candidatoRepositorio.buscarPorId(_)
    }
}
