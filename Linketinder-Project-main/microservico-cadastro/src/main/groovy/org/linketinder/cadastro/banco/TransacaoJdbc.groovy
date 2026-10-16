package org.linketinder.cadastro.banco

import org.linketinder.cadastro.repositorio.Transacao

import java.sql.Connection

/**
 *
 * @author Guilherme Lima Conte
 */

class TransacaoJdbc implements Transacao {

    private final ConexaoBanco conexaoBanco

    TransacaoJdbc(ConexaoBanco conexaoBanco) {
        this.conexaoBanco = conexaoBanco
    }

    @Override
    Object executar(Closure bloco) {
        Connection conexao = conexaoBanco.abrir()

        try {
            conexao.autoCommit = false
            conexaoBanco.vincularATransacao(conexao)

            Object resultado = bloco()
            conexao.commit()
            return resultado
        } catch (Exception e) {
            try {
                conexao.rollback()
            } catch (Exception rollbackEx) {
                e.addSuppressed(rollbackEx)
            }
            throw e
        } finally {
            conexaoBanco.desvincularDaTransacao()
            conexao.close()
        }
    }
}
