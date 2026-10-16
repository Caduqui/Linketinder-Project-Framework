package org.example.model

import spock.lang.Specification

import java.time.LocalDate

/**
 *
 * @author Guilherme Lima Conte
 */
class MatchSpec extends Specification {

    def "descrição de match não revela o recrutador"() {
        given:
        Candidato candidato = new Candidato(
                "Ana",
                "Silva",
                LocalDate.of(1990, 1, 1),
                "ana@example.com",
                "111.111.111-11",
                "Brasil",
                "01000-000",
                "Desenvolvedora",
                "123456",
                "Superior",
                []
        )
        Empresa empresa = new Empresa("Alfa", "alfa@example.com", "11.111.111/0001-11", "Brasil", "01000-000", "Empresa", "123456")
        Vaga vaga = new Vaga("Desenvolvedora", "Vaga", "SP", "São Paulo", empresa, [])
        Match matchComRecrutador = new Match(candidato, empresa, vaga, 42)
        Match matchSemRecrutador = new Match(candidato, empresa, vaga)

        expect:
        matchComRecrutador.descrever() == matchSemRecrutador.descrever()
        !matchComRecrutador.descrever().contains("42")
    }
}
