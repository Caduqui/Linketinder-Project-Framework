package org.linketinder.cadastro

import spock.lang.Specification

/**
 *
 * @author Guilherme Lima Conte
 */

class ConfiguracaoSpec extends Specification {

    static final String SENHA_FICTICIA = "senha-de-teste"

    def "só com a senha do banco informada usa os valores padrão"() {
        when:
        Configuracao configuracao = Configuracao.aPartirDe([(Configuracao.VARIAVEL_BANCO_SENHA): SENHA_FICTICIA])

        then:
        configuracao.porta == 8081
        configuracao.bancoUrl == "jdbc:postgresql://localhost:5432/linketinder"
        configuracao.bancoUsuario == "postgres"
        configuracao.bancoSenha == SENHA_FICTICIA
    }

    def "porta, URL e usuário do banco podem ser trocados por variável de ambiente"() {
        when:
        Configuracao configuracao = Configuracao.aPartirDe([
                (Configuracao.VARIAVEL_PORTA)        : "9090",
                (Configuracao.VARIAVEL_BANCO_URL)    : "jdbc:postgresql://localhost:5433/outro",
                (Configuracao.VARIAVEL_BANCO_USUARIO): "usuario-de-teste",
                (Configuracao.VARIAVEL_BANCO_SENHA)  : SENHA_FICTICIA
        ])

        then:
        configuracao.porta == 9090
        configuracao.bancoUrl == "jdbc:postgresql://localhost:5433/outro"
        configuracao.bancoUsuario == "usuario-de-teste"
    }

    def "porta que não é número é recusada dizendo qual variável está errada"() {
        when:
        Configuracao.aPartirDe([
                (Configuracao.VARIAVEL_PORTA)      : "abc",
                (Configuracao.VARIAVEL_BANCO_SENHA): SENHA_FICTICIA
        ])

        then:
        ConfiguracaoInvalidaException erro = thrown()
        erro.message.contains(Configuracao.VARIAVEL_PORTA)
        !erro.message.contains(SENHA_FICTICIA)
    }

    def "senha do banco #situacao é recusada dizendo qual variável falta"() {
        when:
        Configuracao.aPartirDe(ambiente)

        then:
        ConfiguracaoInvalidaException erro = thrown()
        erro.message.contains(Configuracao.VARIAVEL_BANCO_SENHA)

        where:
        situacao           | ambiente
        "ausente"          | [:]
        "vazia"            | [(Configuracao.VARIAVEL_BANCO_SENHA): ""]
        "só com espaços"   | [(Configuracao.VARIAVEL_BANCO_SENHA): "   "]
    }

    def "a configuração convertida em texto não mostra a senha do banco"() {
        given:
        Configuracao configuracao = Configuracao.aPartirDe([(Configuracao.VARIAVEL_BANCO_SENHA): SENHA_FICTICIA])

        expect:
        !configuracao.toString().contains(SENHA_FICTICIA)
        configuracao.toString().contains("8081")
    }
}
