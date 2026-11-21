package org.linketinder.spring.nucleo.service

import org.linketinder.spring.nucleo.DadosFicticios
import org.linketinder.spring.nucleo.model.Recrutador
import spock.lang.Specification

import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

/**
 *
 * @author Guilherme Lima Conte
 */

class ValidadorDeRecrutadorSpec extends Specification {

    ValidadorDeRecrutador validador = new ValidadorDeRecrutador(Clock.fixed(Instant.parse("2026-10-15T12:00:00Z"), ZoneOffset.UTC))

    def "recrutador válido vira um Recrutador sem os espaços das pontas"() {
        when:
        Recrutador recrutador = validador.validar(DadosFicticios.recrutador(nome: " Recrutadora ", email: " recrutadora@example.com "))

        then:
        recrutador.nome == "Recrutadora"
        recrutador.area == "TI"
        recrutador.descricao == "Recrutadora inventada para os testes"
        recrutador.email == "recrutadora@example.com"
        recrutador.cpf == "987.654.321-00"
        recrutador.dataNascimento == LocalDate.of(1990, 5, 20)
        recrutador.senha == "senha-ficticia"
        recrutador.idEmpresa == 2
    }

    def "a área precisa ser uma das áreas da lista fixa"() {
        expect:
        validador.validar(DadosFicticios.recrutador(area: area)).area == area

        where:
        area << ["TI", "RH", "Marketing", "Segurança da Informação", "Vendas", "Financeiro", "Outros"]
    }

    def "recrutador sem #campo é recusado citando o campo"() {
        when:
        validador.validar(DadosFicticios.sem(DadosFicticios.recrutador(), campo))

        then:
        DadosInvalidosException erro = thrown()
        erro.message.contains("'${campo}'")

        where:
        campo << ["nome", "area", "email", "cpf", "dataNascimento", "senha", "idEmpresa"]
    }

    def "descrição ausente é gravada vazia"() {
        expect:
        validador.validar(DadosFicticios.sem(DadosFicticios.recrutador(), "descricao")).descricao == ""
    }

    def "recrutador com #campo inválido (#motivo) é recusado sem repetir o valor"() {
        when:
        validador.validar(DadosFicticios.recrutador([(campo): valor]))

        then:
        DadosInvalidosException erro = thrown()
        erro.message.contains("'${campo}'")
        !erro.message.contains(valor.toString())

        where:
        campo            | valor                   | motivo
        "area"           | "Jurídico"              | "fora da lista"
        "nome"           | "n" * 51                | "acima de 50"
        "descricao"      | "d" * 501               | "acima de 500"
        "email"          | "recrutadora.example"   | "sem arroba"
        "email"          | ("e" * 195) + "@ex.com" | "acima de 200"
        "cpf"            | "98765432"              | "8 dígitos"
        "dataNascimento" | "20/05/1990"            | "fora do formato"
        "dataNascimento" | "2026-10-16"            | "data futura"
        "senha"          | "12345"                 | "5 caracteres"
        "senha"          | "s" * 51                | "51 caracteres"
        "idEmpresa"      | "2"                     | "texto no lugar do número"
    }

    def "a senha é mantida exatamente como enviada"() {
        expect:
        validador.validar(DadosFicticios.recrutador(senha: " senha com espaços ")).senha == " senha com espaços "
    }
}
