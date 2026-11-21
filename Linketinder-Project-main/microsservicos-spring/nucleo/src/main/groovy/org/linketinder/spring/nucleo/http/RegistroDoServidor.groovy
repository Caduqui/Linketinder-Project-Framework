package org.linketinder.spring.nucleo.http

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

import java.sql.SQLException

/**
 *
 * @author Guilherme Lima Conte
 */

@Component
class RegistroDoServidor {

    static final String ROTA_DESCONHECIDA = "(rota desconhecida)"

    private static final Logger LOG = LoggerFactory.getLogger("linketinder.ms-spring-nucleo")

    void registrarRequisicao(String metodo, String rota, int status) {
        LOG.info("{} {} -> {}", metodo, rota, status)
    }

    void registrarFalhaDeBanco(Throwable falha) {
        LOG.error(descreverFalhaDeBanco(falha))
    }

    void registrarFalhaInesperada(Exception falha) {
        LOG.error(descreverFalhaInesperada(falha))
    }

    static String descreverFalhaDeBanco(Throwable falha) {
        SQLException falhaSql = encontrarFalhaSql(falha)
        String codigo = falhaSql?.SQLState ?: "sem código"
        String primeiraLinha = falhaSql?.message?.readLines()?.find() ?: "sem mensagem"
        return "Falha de banco [${codigo}]: ${primeiraLinha}"
    }

    static String descreverFalhaInesperada(Exception falha) {
        return "Falha inesperada: ${falha.class.name}"
    }

    private static SQLException encontrarFalhaSql(Throwable falha) {
        Throwable atual = falha

        while (atual != null && !(atual instanceof SQLException)) {
            atual = atual.cause
        }
        return atual as SQLException
    }
}
