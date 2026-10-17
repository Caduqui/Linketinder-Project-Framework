package org.linketinder.vagas.http

import jakarta.inject.Singleton
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 *
 * @author Guilherme Lima Conte
 */

@Singleton
class RegistroDoServidorEmLog implements RegistroDoServidor {

    private static final Logger LOG = LoggerFactory.getLogger(RegistroDoServidor)

    @Override
    void registrarRequisicao(String metodo, String rota, int status) {
        LOG.info("{} {} -> {}", metodo, rota, status)
    }

    @Override
    void registrarFalhaDeBanco(String codigoDoErro) {
        LOG.error("Falha ao acessar o banco de dados (SQLState {})", codigoDoErro)
    }
}
