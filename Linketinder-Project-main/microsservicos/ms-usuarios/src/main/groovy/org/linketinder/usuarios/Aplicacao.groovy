package org.linketinder.usuarios

import io.micronaut.runtime.Micronaut
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 *
 * @author Guilherme Lima Conte
 */

class Aplicacao {

    private static final Logger LOG = LoggerFactory.getLogger(Aplicacao)

    static void main(String[] args) {
        try {
            VerificadorDeAmbiente.exigirSenhaDoBanco(System.getenv())
        } catch (ConfiguracaoInvalidaException e) {
            LOG.error(e.message)
            System.exit(1)
        }

        Micronaut.run(Aplicacao, args)
    }
}
