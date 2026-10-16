package org.example.view


import spock.lang.Specification
import spock.lang.Unroll

import java.time.LocalDate

/**
 *
 * @author Guilherme Lima Conte
 */

class EntradaDadosSpec extends Specification{

    LeitorEntrada leitor = Mock(LeitorEntrada)
    EntradaDados entrada = new EntradaDados(leitor)

    def "lerTexto remove os espaços das pontas"() {
        given:
        leitor.lerLinha("Nome: ") >> "  Ana  "

        expect:
        entrada.lerTexto("Nome: ") == "Ana"
    }

    @Unroll
    def "lerId com '#digitado' retorna #esperado"() {
        given:
        leitor.lerLinha("ID: ") >> digitado

        expect:
        entrada.lerId("ID: ") == esperado

        where:
        digitado | esperado
        "7" | 7
        " 12 " | 12
        "abc" | null
        "" | null
    }

    def "lerSenha pergunta de novo até ter o tamanho mínimo"() {
        when:
        String senha = entrada.lerSenha()

        then:
        2 * leitor.lerLinha("Senha: ") >>> ["123", "senha123"]
        senha == "senha123"
    }

    def "lerDataNascimento pergunta de novo quando o formato é inválido"() {
        when:
        LocalDate data = entrada.lerDataNascimento()

        then:
        2 * leitor.lerLinha(_) >>> ["31/12/2000", "2000-12-31"]
        data == LocalDate.of(2000, 12, 31)
    }

    def "lerSiglaEstado só aceita 2 letras e devolve em maiúsculas"() {
        when:
        String estado = entrada.lerSiglaEstado()

        then:
        3 * leitor.lerLinha(_) >>> ["Mato Grosso", "M1", "mt"]
        estado == "MT"
    }

    @Unroll
    def "lerCompetencias com '#digitado' retorna #esperado"() {
        given:
        leitor.lerLinha("Competências: ") >> digitado

        expect:
        entrada.lerCompetencias("Competências: ") == esperado

        where:
        digitado | esperado
        "Java, SQL" | ["Java", "SQL"]
        "Java, java, , SQL" | ["Java", "SQL"]
        "" | []
    }

}
