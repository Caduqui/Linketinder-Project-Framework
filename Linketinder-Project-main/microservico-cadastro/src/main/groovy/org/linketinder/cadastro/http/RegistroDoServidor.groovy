package org.linketinder.cadastro.http

import java.sql.SQLException

/**
 *
 * @author Guilherme Lima Conte
 */

class RegistroDoServidor {

    private final PrintStream saida

    RegistroDoServidor(PrintStream saida) {
        this.saida = saida
    }

    void registrarRequisicao(String metodo, String rota, int status) {
        saida.println("${metodo} ${rota} ${status}")
    }

    void registrarFalhaDeBanco(SQLException falha) {
        saida.println("Falha de banco [${falha.SQLState}]: ${primeiraLinha(falha.message)}")
    }

    void registrarFalhaInesperada(Exception falha) {
        saida.println("Falha inesperada: ${falha.class.name}")
    }

    void informar(String mensagem) {
        saida.println(mensagem)
    }

    private static String primeiraLinha(String mensagem) {
        return mensagem == null ? "" : mensagem.readLines().first()
    }
}
