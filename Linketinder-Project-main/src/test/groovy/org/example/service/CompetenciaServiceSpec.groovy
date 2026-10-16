package org.example.service

import org.example.model.Competencia
import org.example.repositorio.CompetenciaRepositorio
import spock.lang.Specification
import spock.lang.Unroll

/**
 *
 * @author Guilherme Lima Conte
 */

class CompetenciaServiceSpec extends Specification {

    CompetenciaRepositorio competenciaRepositorio = Mock(CompetenciaRepositorio)
    CompetenciaService competenciaService = new CompetenciaService(competenciaRepositorio)

    def "cadastrar insere a competência sem espaços nas pontas"() {
        given:
        competenciaRepositorio.buscarPorNome("Kotlin") >> null

        when:
        competenciaService.cadastrar(" Kotlin ")

        then:
        1 * competenciaRepositorio.inserir({ Competencia competencia -> competencia.nome == "Kotlin"})
    }

    def "cadastrar recusa competência já cadastrada"() {
        given:
        Competencia existente = new Competencia("Java")
        competenciaRepositorio.buscarPorNome("Java") >> existente

        when:
        competenciaService.cadastrar("Java")

        then:
        RegraDeNegocioException erro = thrown()
        erro.message.contains("já está cadastrada")
        0 * competenciaRepositorio.inserir(_)
    }

    @Unroll
    def "cadastrar recusa o nome '#nome''"() {
        when:
        competenciaService.cadastrar(nome)

        then:
        RegraDeNegocioException erro = thrown()
        erro.message.contains("não pode ser vazio")
        0 * competenciaRepositorio.inserir(_)

        where:
        nome << ["", "   ", null]
    }

    def "não exclui competência vinculada a candidato ou vaga"() {
        given:
        competenciaRepositorio.estaVinculadoACandidatoOuVaga(1) >> true

        when:
        competenciaService.excluir(1)

        then:
        RegraDeNegocioException erro = thrown()
        erro.message.contains("relacionada")
        0 * competenciaRepositorio.deletar(_)
    }

    def "renomear troca o nome e salva"() {
        given:
        Competencia competencia = new Competencia("Java")
        competencia.id = 1
        competenciaRepositorio.buscarPorNome("Groovy") >> null

        when:
        competenciaService.renomear(competencia, "Groovy")

        then:
        1 * competenciaRepositorio.atualizar({ Competencia salva -> salva.nome == "Groovy" && salva.id == 1 })
    }

}
