package org.example.model

import spock.lang.Specification
import spock.lang.Unroll

import java.time.LocalDate

/**
 *
 * @author Guilherme Lima Conte
 */

class CandidatoSpec extends Specification{

    Candidato criarCandidato(List<String> competencias = ["Java", "SQL"]) {
        Candidato candidato = new Candidato(
                "Ana",
                "Silva",
                LocalDate.of(2003, 4, 21),
                "ana@gmail.com",
                "111.111.111-11",
                "Brasil",
                "23134-201",
                "Dev back-end",
                "123456",
                "Computação",
                competencias
        )
        candidato.id = 7
        return candidato
    }
    def "O construtor preenche corretamente todos os campos"() {
        when:
        Candidato candidato = criarCandidato()

        then:
        candidato.nome == "Ana"
        candidato.sobrenome == "Silva"
        candidato.dataNascimento == LocalDate.of(2003, 4, 21)
        candidato.email == "ana@gmail.com"
        candidato.cpf == "111.111.111-11"
        candidato.pais == "Brasil"
        candidato.cep == "23134-201"
        candidato.descricao == "Dev back-end"
        candidato.senha == "123456"
        candidato.formacao == "Computação"
        candidato.competencias == ["Java", "SQL"]
    }

    def "Candidato é uma Pessoa e estende TipoPessoa"() {
        expect:
        criarCandidato() instanceof Pessoa
        criarCandidato() instanceof TipoPessoa
    }

    @Unroll
    def "formatarCompetencias com #competencias retorna '#esperado'"() {
        expect:
        criarCandidato(competencias).formatarCompetencias() == esperado

        where:
        competencias | esperado
        ["Java", "Docker"] | "Java, Docker"
        ["Python"] | "Python"
        [] | "Nenhuma competência cadastrada"
    }

    def "descrever mostra ID, nome, CPF e competências"() {
        when:
        String saida = criarCandidato().descrever()

        then:
        saida.contains("ID: 7")
        saida.contains("Ana Silva")
        saida.contains("111.111.111-11")
        saida.contains("Java, SQL")
    }

    def "descreverAnonimo não mostra nome, e-mail nem CPF"() {
        when:
        String saida = criarCandidato().descreverAnonimo()

        then:
        saida.contains("ID: 7")
        saida.contains("Computação")
        !saida.contains("Ana")
        !saida.contains("ana@gmail.com")
        !saida.contains("111.111.111-11")
    }
}
