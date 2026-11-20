package org.linketinder.spring.nucleo.service

import org.linketinder.spring.nucleo.DadosFicticios
import org.linketinder.spring.nucleo.model.Vaga
import spock.lang.Specification

/**
 *
 * @author Guilherme Lima Conte
 */

class ValidadorDeVagaSpec extends Specification {

    ValidadorDeVaga validador = new ValidadorDeVaga()

    def "vaga válida vira uma Vaga sem os espaços das pontas"() {
        when:
        Vaga vaga = validador.validar(DadosFicticios.vaga(nome: " Vaga de exemplo ", cidade: " Cidade Exemplo "))

        then:
        vaga.nome == "Vaga de exemplo"
        vaga.descricao == "Vaga inventada para os testes"
        vaga.estado == "MS"
        vaga.cidade == "Cidade Exemplo"
        vaga.idEmpresa == 2
        vaga.competencias == ["Java", "SQL"]
    }

    def "vaga sem #campo é recusada citando o campo"() {
        when:
        validador.validar(DadosFicticios.sem(DadosFicticios.vaga(), campo))

        then:
        DadosInvalidosException erro = thrown()
        erro.message.contains("'${campo}'")

        where:
        campo << ["nome", "estado", "cidade", "idEmpresa"]
    }

    def "descrição e competências ausentes ficam vazias"() {
        when:
        Vaga vaga = validador.validar(DadosFicticios.sem(DadosFicticios.sem(DadosFicticios.vaga(), "descricao"), "competencias"))

        then:
        vaga.descricao == ""
        vaga.competencias == []
    }

    def "competências em branco são descartadas e repetidas em outra caixa valem uma vez"() {
        expect:
        validador.validar(DadosFicticios.vaga(competencias: [" Java ", "", "java", "SQL"])).competencias == ["Java", "SQL"]
    }

    def "vaga com #campo inválido (#motivo) é recusada sem repetir o valor"() {
        when:
        validador.validar(DadosFicticios.vaga([(campo): valor]))

        then:
        DadosInvalidosException erro = thrown()
        erro.message.contains("'${campo}'")
        !erro.message.contains(valor.toString())

        where:
        campo          | valor          | motivo
        "estado"       | "MSX"          | "3 letras"
        "estado"       | "M1"           | "com número"
        "nome"         | "n" * 101      | "acima de 100"
        "descricao"    | "d" * 501      | "acima de 500"
        "cidade"       | "c" * 101      | "acima de 100"
        "competencias" | ["c" * 51]     | "competência acima de 50"
        "competencias" | [10]           | "competência que não é texto"
        "competencias" | "Java"         | "texto no lugar da lista"
        "idEmpresa"    | "2"            | "texto no lugar do número"
    }
}
