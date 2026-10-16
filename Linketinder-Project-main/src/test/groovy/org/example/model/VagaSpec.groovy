package org.example.model

import spock.lang.Specification
import spock.lang.Unroll

/**
 *
 * @author Guilherme Lima Conte
 */

class VagaSpec extends Specification {

    Vaga criarVaga(List<String> competencias = ["Groovy", "SQL"]) {
        Empresa empresa = new Empresa(
                "TechVision",
                "contato@techvision.com",
                "33.333.333/3333-33",
                "Brasil",
                "79020-150",
                "Sistemas web",
                "123456")
        Vaga vaga = new Vaga("Dev Backend", "APIs em Groovy", "MS", "Campo Grande", empresa, competencias)
        vaga.id = 4
        return vaga
    }

    @Unroll
    def "formatarCompetencias com #competencias retorna '#esperado'"() {
        expect:
        criarVaga(competencias).formatarCompetencias() == esperado

        where:
        competencias | esperado
        ["Groovy", "SQL"] | "Groovy, SQL"
        [] | "Nenhuma competência cadastrada"
    }

    def "descrever mostra ID, empresa, local e competências"() {
        when:
        String saida = criarVaga().descrever()

        then:
        saida.contains("ID: 4")
        saida.contains("TechVision")
        saida.contains("Campo Grande - MS")
        saida.contains("Groovy, SQL")
    }

}
