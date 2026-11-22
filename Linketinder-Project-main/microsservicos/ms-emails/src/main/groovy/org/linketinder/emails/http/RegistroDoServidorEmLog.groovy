package org.linketinder.emails.http

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
    void registrarNotificacao(Integer idVaga, int enviados, int falhas) {
        LOG.info("Vaga {} notificada: {} e-mails enviados, {} falhas", idVaga, enviados, falhas)
    }

    @Override
    void registrarFalhaDeBanco(Integer idVaga, String codigoDoErro) {
        LOG.error("Falha ao acessar o banco de dados ao notificar a vaga {} (SQLState {})", idVaga, codigoDoErro)
    }

    @Override
    void registrarMensagemDescartada(String motivo) {
        LOG.warn("Mensagem de vaga criada descartada: {}", motivo)
    }

    @Override
    void registrarFalhaAoProcessar(String tipoDaFalha) {
        LOG.error("Falha ao processar a mensagem de vaga criada ({})", tipoDaFalha)
    }
}
