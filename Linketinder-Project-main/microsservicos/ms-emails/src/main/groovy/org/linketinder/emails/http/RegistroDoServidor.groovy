package org.linketinder.emails.http

/**
 *
 * @author Guilherme Lima Conte
 */

interface RegistroDoServidor {

    void registrarNotificacao(Integer idVaga, int enviados, int falhas)

    void registrarFalhaDeBanco(Integer idVaga, String codigoDoErro)

    void registrarMensagemDescartada(String motivo)

    void registrarFalhaAoProcessar(String tipoDaFalha)
}
