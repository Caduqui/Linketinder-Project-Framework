package org.linketinder.spring.competencias.service

import org.linketinder.spring.competencias.model.Competencia
import org.linketinder.spring.competencias.model.CompetenciaDaVaga
import org.linketinder.spring.competencias.repositorio.CompetenciaDaVagaRepositorio
import org.linketinder.spring.competencias.repositorio.CompetenciaRepositorio
import spock.lang.Specification

/**
 *
 * @author Guilherme Lima Conte
 */

class CompetenciaDaVagaServiceSpec extends Specification {

    CompetenciaRepositorio competenciaRepositorio = Mock()
    CompetenciaDaVagaRepositorio daVagaRepositorio = Mock()
    CompetenciaDaVagaService service = new CompetenciaDaVagaService(new CompetenciaService(competenciaRepositorio), daVagaRepositorio)

    def "substituir apaga as competências antigas, liga as novas e devolve os nomes cadastrados"() {
        given:
        competenciaRepositorio.buscarPorNomeIgnorandoCaixa("java") >> new Competencia(id: 1, nome: "Java")
        competenciaRepositorio.buscarPorNomeIgnorandoCaixa("Kotlin") >> null

        when:
        List<String> nomes = service.substituir(10, [competencias: ["java", " Kotlin ", "JAVA", ""]])

        then:
        1 * daVagaRepositorio.apagarDaVaga(10)

        then:
        1 * competenciaRepositorio.save({ Competencia nova -> nova.nome == "Kotlin" }) >> { Competencia nova ->
            nova.id = 11
            return nova
        }
        2 * daVagaRepositorio.save({ CompetenciaDaVaga ligacao -> ligacao.idVaga == 10 })
        nomes == ["Java", "Kotlin"]
    }

    def "substituir com lista vazia só apaga as competências da vaga"() {
        when:
        List<String> nomes = service.substituir(10, [competencias: []])

        then:
        1 * daVagaRepositorio.apagarDaVaga(10)
        0 * daVagaRepositorio.save(_)
        nomes == []
    }

    def "substituir com corpo inválido não apaga nada"() {
        when:
        service.substituir(10, corpo)

        then:
        DadosInvalidosException erro = thrown()
        erro.message == mensagem
        0 * daVagaRepositorio.apagarDaVaga(_)

        where:
        corpo                          | mensagem
        [:]                            | "O campo 'competencias' deve ser uma lista de nomes"
        [competencias: [1]]            | "Cada item do campo 'competencias' deve ser um texto"
        [competencias: ["x" * 51]]     | "O campo 'competencias' deve ter no máximo 50 caracteres"
    }

    def "listarDasVagas agrupa os nomes por vaga"() {
        given:
        Competencia java = new Competencia(id: 1, nome: "Java")
        Competencia sql = new Competencia(id: 3, nome: "SQL")
        daVagaRepositorio.listarDasVagas([10, 20]) >> [
                new CompetenciaDaVaga(idVaga: 10, competencia: java),
                new CompetenciaDaVaga(idVaga: 10, competencia: sql),
                new CompetenciaDaVaga(idVaga: 20, competencia: java)
        ]

        expect:
        service.listarDasVagas([10, 20]) == [10: ["Java", "SQL"], 20: ["Java"]]
    }
}
