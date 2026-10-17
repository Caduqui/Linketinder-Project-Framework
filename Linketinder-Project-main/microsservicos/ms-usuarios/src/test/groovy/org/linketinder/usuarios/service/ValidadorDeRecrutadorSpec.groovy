package org.linketinder.usuarios.service

import spock.lang.Specification
import spock.lang.Unroll

import java.time.LocalDate

/**
 *
 * @author Guilherme Lima Conte
 */
class ValidadorDeRecrutadorSpec extends Specification {

    @Unroll
    def "campo #campo é obrigatório"() {
        when:
        validar.call(null)

        then:
        RegraDeNegocioException erro = thrown()
        erro.message.contains("'${campo}'")

        where:
        campo             | validar
        "nome"             | { String valor -> ValidadorDeRecrutador.validarNome(valor) }
        "area"             | { String valor -> ValidadorDeRecrutador.validarArea(valor) }
        "email"            | { String valor -> ValidadorDeRecrutador.validarEmail(valor) }
        "cpf"              | { String valor -> ValidadorDeRecrutador.validarCpf(valor) }
        "dataNascimento"   | { String valor -> ValidadorDeRecrutador.validarDataNascimento(valor) }
        "senha"            | { String valor -> ValidadorDeRecrutador.validarSenha(valor) }
    }

    def "nome respeita o limite de tamanho"() {
        when:
        ValidadorDeRecrutador.validarNome("a" * 51)

        then:
        RegraDeNegocioException erro = thrown()
        erro.message.contains("'nome'")
    }

    def "area precisa estar na lista fixa"() {
        when:
        ValidadorDeRecrutador.validarArea("Jurídico")

        then:
        RegraDeNegocioException erro = thrown()
        erro.message.contains("'area'")
        erro.message.contains("TI")
        !erro.message.contains("Jurídico")
    }

    def "descricao respeita o limite de tamanho"() {
        when:
        ValidadorDeRecrutador.validarDescricao("a" * 501)

        then:
        RegraDeNegocioException erro = thrown()
        erro.message.contains("'descricao'")
    }

    def "email deve ter formato válido"() {
        when:
        ValidadorDeRecrutador.validarEmail("ana.example.com")

        then:
        RegraDeNegocioException erro = thrown()
        erro.message.contains("'email'")
        !erro.message.contains("ana.example.com")
    }

    def "cpf deve ter 11 dígitos"() {
        when:
        ValidadorDeRecrutador.validarCpf("123.456.789-0")

        then:
        RegraDeNegocioException erro = thrown()
        erro.message.contains("'cpf'")
        !erro.message.contains("123.456.789-0")
    }

    def "cpf aceita somente dígitos ou pontuação padrão"() {
        when:
        ValidadorDeRecrutador.validarCpf("123-456-78909")

        then:
        RegraDeNegocioException erro = thrown()
        erro.message.contains("'cpf'")
        !erro.message.contains("123-456-78909")
    }

    def "cpf com letras é inválido"() {
        when:
        ValidadorDeRecrutador.validarCpf("abc12345678")

        then:
        RegraDeNegocioException erro = thrown()
        erro.message.contains("'cpf'")
        !erro.message.contains("abc12345678")
    }

    def "email respeita o limite de tamanho"() {
        when:
        ValidadorDeRecrutador.validarEmail("${'a' * 190}@example.com")

        then:
        RegraDeNegocioException erro = thrown()
        erro.message.contains("'email'")
        !erro.message.contains("${'a' * 190}@example.com")
    }

    def "data de nascimento precisa ser válida e não futura"() {
        when:
        ValidadorDeRecrutador.validarDataNascimento("2100-01-01")

        then:
        RegraDeNegocioException erro = thrown()
        erro.message.contains("'dataNascimento'")
    }

    def "data de nascimento inexistente é inválida"() {
        when:
        ValidadorDeRecrutador.validarDataNascimento("2026-02-30")

        then:
        RegraDeNegocioException erro = thrown()
        erro.message.contains("'dataNascimento'")
        !erro.message.contains("2026-02-30")
    }

    def "data de nascimento fora do formato ISO é inválida"() {
        when:
        ValidadorDeRecrutador.validarDataNascimento("13/10/2026")

        then:
        RegraDeNegocioException erro = thrown()
        erro.message.contains("'dataNascimento'")
        !erro.message.contains("13/10/2026")
    }

    def "senha deve ter entre 6 e 50 caracteres"() {
        when:
        ValidadorDeRecrutador.validarSenha("12345")

        then:
        RegraDeNegocioException erro = thrown()
        erro.message.contains("'senha'")
    }

    def "senha acima do limite é inválida"() {
        when:
        ValidadorDeRecrutador.validarSenha("a" * 51)

        then:
        RegraDeNegocioException erro = thrown()
        erro.message.contains("'senha'")
    }

    def "valores válidos são limpos e mantidos sem reformatar"() {
        when:
        String nome = ValidadorDeRecrutador.validarNome("  Ana Paula  ")
        String area = ValidadorDeRecrutador.validarArea("  TI  ")
        String email = ValidadorDeRecrutador.validarEmail("  ANA@EXAMPLE.COM  ")
        String cpf = ValidadorDeRecrutador.validarCpf("  12345678900  ")
        LocalDate data = ValidadorDeRecrutador.validarDataNascimento("  1990-07-12  ")
        String senha = ValidadorDeRecrutador.validarSenha(" 123456 ")

        then:
        nome == "Ana Paula"
        area == "TI"
        email == "ANA@EXAMPLE.COM"
        cpf == "12345678900"
        data == LocalDate.of(1990, 7, 12)
        senha == " 123456 "
    }

    def "senha composta somente por espaços é inválida"() {
        when:
        ValidadorDeRecrutador.validarSenha("       ")

        then:
        RegraDeNegocioException erro = thrown()
        erro.message.contains("'senha'")
        !erro.message.contains("       ")
    }
}
