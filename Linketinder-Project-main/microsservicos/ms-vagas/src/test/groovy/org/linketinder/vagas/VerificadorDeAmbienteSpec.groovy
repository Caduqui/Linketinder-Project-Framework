package org.linketinder.vagas

import spock.lang.Specification

/**
 *
 * @author Guilherme Lima Conte
 */

class VerificadorDeAmbienteSpec extends Specification {

    def "sem a senha do banco o serviço não sobe e a mensagem cita só o nome da variável"() {
        when:
        VerificadorDeAmbiente.exigirSenhaDoBanco(ambiente)

        then:
        ConfiguracaoInvalidaException erro = thrown()
        erro.message.contains("LINKETINDER_BANCO_SENHA")
        !erro.message.contains("valor-que-nao-pode-aparecer")

        where:
        ambiente << [
                [:],
                [LINKETINDER_BANCO_SENHA: ""],
                [LINKETINDER_BANCO_SENHA: "   ", LINKETINDER_BANCO_USUARIO: "valor-que-nao-pode-aparecer"]
        ]
    }

    def "com a senha do banco o serviço pode subir"() {
        when:
        VerificadorDeAmbiente.exigirSenhaDoBanco([LINKETINDER_BANCO_SENHA: "senha-ficticia"])

        then:
        noExceptionThrown()
    }
}
