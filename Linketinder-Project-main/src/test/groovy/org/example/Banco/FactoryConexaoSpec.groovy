package org.example.Banco

import spock.lang.Specification
import spock.lang.Unroll

/**
 *
 * @author Guilherme Lima Conte
 */

class FactoryConexaoSpec extends Specification{

    @Unroll
    def "a fábrica cria o provedor #esperado para o banco #tipo"() {
        expect:
        FactoryConexao.criar(tipo).class == esperado

        where:
        tipo | esperado
        TipoBanco.POSTGRESQL | ProvedorPostgreSQL
        TipoBanco.MYSQL | ProvedorMySQL
    }

    @Unroll
    def "o texto #nomeBanco escolhe o banco #esperado"() {
        expect:
        TipoBanco.aPartirDoNome(nomeBanco) == esperado

        where:
        nomeBanco | esperado
        null | TipoBanco.POSTGRESQL
        "" | TipoBanco.POSTGRESQL
        "mysql" | TipoBanco.MYSQL
        " PostgreSQL " | TipoBanco.POSTGRESQL
    }

    def "um banco não suportado gera erro com as opções válidas"() {
        when:
        TipoBanco.aPartirDoNome("oracle")

        then:
        IllegalArgumentException erro = thrown()
        erro.message.contains("oracle")
        erro.message.contains("POSTGRESQL, MYSQL")
    }

    def "a aplicação tem uma única instância de ConexaoBanco"() {
        expect:
        ConexaoBanco.obterInstancia().is(ConexaoBanco.obterInstancia())
    }
}
