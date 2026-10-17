package org.linketinder.curtidas.http

/**
 *
 * @author Guilherme Lima Conte
 */

interface RegistroDoServidor {

    void registrarRequisicao(String metodo, String rota, int status)

    void registrarFalhaDeBanco(String codigoDoErro)
}
