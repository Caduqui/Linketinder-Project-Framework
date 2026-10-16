package org.linketinder.cadastro.repositorio

/**
 *
 * @author Guilherme Lima Conte
 */

interface Transacao {
    Object executar(Closure bloco)
}
