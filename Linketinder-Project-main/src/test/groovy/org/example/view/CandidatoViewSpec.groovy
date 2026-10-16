package org.example.view

import org.example.SaidaConsole
import org.example.model.Candidato
import spock.lang.Specification

import java.time.LocalDate

/**
 *
 * @author Guilherme Lima Conte
 */

class CandidatoViewSpec extends Specification{

    static final Map<String, String> DADOS_CANDIDATO = [
            "Nome: " : "Fernanda",
            "Sobrenome: " : "Costa",
            "Data de nascimento (AAAA-MM-DD): " : "1999-05-10",
            "Email: " : "fernanda@email.com",
            "CPF: " : "999.999.999-99",
            "País: " : "Brasil",
            "CEP: " : "88010-000",
            "Descrição: " : "Entusiasta em programação",
            "Senha: " : "senha123",
            "Formação: " : "Computação",
            "Competências: " : "Java, java, , Docker"
    ]

    def "lerDados monta o candidato com o que foi digitado"() {
        given:
        CandidatoView view = new CandidatoView(EntradaFalsa.comRespostas(DADOS_CANDIDATO))

        when:
        Candidato candidato = view.lerDados()

        then:
        candidato.nome == "Fernanda"
        candidato.sobrenome == "Costa"
        candidato.dataNascimento == LocalDate.of(1999, 5, 10)
        candidato.cpf == "999.999.999-99"
        candidato.competencias == ["Java", "Docker"]
    }

    def "exibir imprime a descrição de cada candidato"() {
        given:
        CandidatoView view = new CandidatoView(EntradaFalsa.comRespostas([:]))
        Candidato candidato = new Candidato("Ana", "Silva", LocalDate.of(2003, 4, 21), "ana@gmail.com", "111.111.111-11", "Brasil", "0", "d", "123456", "Computação", ["Java"])
        candidato.id = 7

        when:
        String saida = SaidaConsole.capturar {
            view.exibir([candidato])
        }

        then:
        saida.contains("ID: 7 | Candidato: Ana Silva")
        saida.contains("Competências: Java")
    }

}
