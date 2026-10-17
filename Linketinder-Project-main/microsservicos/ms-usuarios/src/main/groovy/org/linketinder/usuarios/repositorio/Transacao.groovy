package org.linketinder.usuarios.repositorio

/**
 *
 * @author Guilherme Lima Conte
 */

interface Transacao {

    def <T> T executar(Closure<T> bloco)
}
