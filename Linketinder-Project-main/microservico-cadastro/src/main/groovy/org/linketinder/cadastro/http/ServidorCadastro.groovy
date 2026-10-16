package org.linketinder.cadastro.http

import com.sun.net.httpserver.HttpServer

/**
 *
 * @author Guilherme Lima Conte
 */

class ServidorCadastro {

    static final int FILA_PADRAO_DO_SISTEMA = 0
    static final int PARAR_IMEDIATAMENTE = 0

    private final HttpServer servidor

    ServidorCadastro(int porta, Roteador roteador) {
        servidor = HttpServer.create(new InetSocketAddress(porta), FILA_PADRAO_DO_SISTEMA)
        servidor.createContext("/", roteador)
    }

    void iniciar() {
        servidor.start()
    }

    void parar() {
        servidor.stop(PARAR_IMEDIATAMENTE)
    }

    int getPorta() {
        return servidor.address.port
    }
}
