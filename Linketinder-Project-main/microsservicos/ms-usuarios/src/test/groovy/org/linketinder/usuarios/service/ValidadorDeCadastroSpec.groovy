package org.linketinder.usuarios.service

import org.linketinder.usuarios.DadosFicticios
import org.linketinder.usuarios.model.Candidato
import org.linketinder.usuarios.model.Empresa
import spock.lang.Specification

import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

/**
 *
 * @author Guilherme Lima Conte
 */

class ValidadorDeCadastroSpec extends Specification {

    static final String HOJE = "2026-10-11"

    Clock relogio = Clock.fixed(Instant.parse("${HOJE}T12:00:00Z"), ZoneOffset.UTC)
    ValidadorDeCadastro validador = new ValidadorDeCadastro(relogio)

    def "candidato válido vira um Candidato com todos os campos"() {
        when:
        Candidato candidato = validador.validarCandidato(DadosFicticios.candidato())

        then:
        candidato.id == null
        candidato.nome == "Candidata"
        candidato.sobrenome == "Exemplo"
        candidato.dataNascimento == LocalDate.of(2000, 1, 15)
        candidato.email == "candidata@example.com"
        candidato.cpf == "123.456.789-00"
        candidato.pais == "Brasil"
        candidato.cep == "79000-000"
        candidato.descricao == "Pessoa inventada para os testes"
        candidato.senha == "senha-ficticia"
        candidato.formacao == "Curso de exemplo"
    }

    def "empresa válida vira uma Empresa com todos os campos"() {
        when:
        Empresa empresa = validador.validarEmpresa(DadosFicticios.empresa())

        then:
        empresa.id == null
        empresa.nome == "Empresa Exemplo"
        empresa.email == "contato@example.com"
        empresa.cnpj == "12.345.678/0001-00"
        empresa.pais == "Brasil"
        empresa.cep == "79000-000"
        empresa.descricao == "Empresa inventada para os testes"
        empresa.senha == "senha-ficticia"
    }

    def "candidato sem #campo é recusado citando o campo"() {
        when:
        validador.validarCandidato(DadosFicticios.sem(DadosFicticios.candidato(), campo))

        then:
        RegraDeNegocioException erro = thrown()
        erro.message.contains("'${campo}'")

        where:
        campo << ["nome", "sobrenome", "dataNascimento", "email", "cpf", "pais", "cep", "senha"]
    }

    def "empresa sem #campo é recusada citando o campo"() {
        when:
        validador.validarEmpresa(DadosFicticios.sem(DadosFicticios.empresa(), campo))

        then:
        RegraDeNegocioException erro = thrown()
        erro.message.contains("'${campo}'")

        where:
        campo << ["nome", "email", "cnpj", "pais", "cep", "senha"]
    }

    def "campo obrigatório só com espaços conta como vazio"() {
        when:
        validador.validarCandidato(DadosFicticios.candidato(nome: "   "))

        then:
        RegraDeNegocioException erro = thrown()
        erro.message.contains("'nome'")
    }

    def "descrição e formação ausentes são gravadas vazias"() {
        given:
        Map<String, Object> dados = DadosFicticios.sem(DadosFicticios.sem(DadosFicticios.candidato(), "descricao"), "formacao")

        when:
        Candidato candidato = validador.validarCandidato(dados)

        then:
        candidato.descricao == ""
        candidato.formacao == ""
    }

    def "descrição ausente na empresa é gravada vazia"() {
        expect:
        validador.validarEmpresa(DadosFicticios.sem(DadosFicticios.empresa(), "descricao")).descricao == ""
    }

    def "candidato com #campo inválido (#motivo) é recusado sem repetir o valor"() {
        when:
        validador.validarCandidato(DadosFicticios.candidato([(campo): valor]))

        then:
        RegraDeNegocioException erro = thrown()
        erro.message.contains("'${campo}'")
        !erro.message.contains(valor.toString())

        where:
        campo            | valor                          | motivo
        "senha"          | "12345"                        | "5 caracteres"
        "senha"          | "s" * 51                       | "51 caracteres"
        "dataNascimento" | "2026-10-12"                   | "data futura"
        "dataNascimento" | "2026-02-30"                   | "data inexistente"
        "dataNascimento" | "15/01/2000"                   | "fora do formato"
        "email"          | "candidata.example.com"        | "sem arroba"
        "email"          | "candidata@example"            | "domínio sem ponto"
        "email"          | "candi data@example.com"       | "com espaço"
        "email"          | "candidata@@example.com"       | "duas arrobas"
        "cpf"            | "1234567890"                   | "10 dígitos"
        "cpf"            | "123456789000"                 | "12 dígitos"
        "cpf"            | "1234567890a"                  | "com letra"
        "cpf"            | "123-456-789.00"               | "pontuação fora do padrão"
        "cep"            | "7900000"                      | "7 dígitos"
        "cep"            | "79.000-000"                   | "pontuação fora do padrão"
        "nome"           | "n" * 51                       | "acima de 50"
        "sobrenome"      | "s" * 51                       | "acima de 50"
        "email"          | ("e" * 190) + "@example.com"   | "acima de 200"
        "pais"           | "p" * 101                      | "acima de 100"
        "descricao"      | "d" * 501                      | "acima de 500"
        "formacao"       | "f" * 101                      | "acima de 100"
        "cpf"            | 12345678900L                   | "número no lugar do texto"
        "nome"           | ["Candidata"]                  | "lista no lugar do texto"
        "senha"          | 123456                         | "número no lugar do texto"
    }

    def "empresa com #campo inválido (#motivo) é recusada sem repetir o valor"() {
        when:
        validador.validarEmpresa(DadosFicticios.empresa([(campo): valor]))

        then:
        RegraDeNegocioException erro = thrown()
        erro.message.contains("'${campo}'")
        !erro.message.contains(valor.toString())

        where:
        campo   | valor                 | motivo
        "cnpj"  | "1234567800010"       | "13 dígitos"
        "cnpj"  | "12ABC34501DE35"      | "alfanumérico"
        "cnpj"  | "12-345-678.0001/00"  | "pontuação fora do padrão"
        "nome"  | "n" * 101             | "acima de 100"
        "senha" | "12345"               | "5 caracteres"
        "email" | "contato.example.com" | "sem arroba"
        "cep"   | "7900000"             | "7 dígitos"
    }

    def "mensagem de e-mail inválido só dá exemplo no domínio reservado example.com"() {
        when:
        validador.validarCandidato(DadosFicticios.candidato(email: "candidata.example.com"))

        then:
        RegraDeNegocioException erro = thrown()
        erro.message.findAll(/@[\w.-]+/) == ["@example.com"]
    }

    def "documentos e CEP são aceitos só com dígitos ou na pontuação padrão"() {
        expect:
        validador.validarCandidato(DadosFicticios.candidato(cpf: cpf, cep: cep)).cpf == cpf
        validador.validarEmpresa(DadosFicticios.empresa(cnpj: cnpj, cep: cep)).cnpj == cnpj

        where:
        cpf              | cnpj                 | cep
        "12345678900"    | "12345678000100"     | "79000000"
        "123.456.789-00" | "12.345.678/0001-00" | "79000-000"
    }

    def "os valores são mantidos como enviados, só sem os espaços das pontas"() {
        when:
        Candidato candidato = validador.validarCandidato(DadosFicticios.candidato(
                nome: " Candidata ", cpf: "12345678900", cep: " 79000000 ", email: "Candidata@Example.com"))

        then:
        candidato.nome == "Candidata"
        candidato.cpf == "12345678900"
        candidato.cep == "79000000"
        candidato.email == "Candidata@Example.com"
    }

    def "a senha é mantida exatamente como enviada, com espaços"() {
        expect:
        validador.validarCandidato(DadosFicticios.candidato(senha: " senha com espaços ")).senha == " senha com espaços "
    }

    def "nome da empresa aceita até 100 caracteres e o do candidato até 50"() {
        expect:
        validador.validarEmpresa(DadosFicticios.empresa(nome: "n" * 100)).nome.length() == 100
        validador.validarCandidato(DadosFicticios.candidato(nome: "n" * 50)).nome.length() == 50
    }

    def "data de nascimento de hoje é aceita"() {
        expect:
        validador.validarCandidato(DadosFicticios.candidato(dataNascimento: HOJE)).dataNascimento == LocalDate.parse(HOJE)
    }
}
