package org.linketinder.usuarios.banco

import jakarta.inject.Singleton
import org.linketinder.usuarios.repositorio.Transacao

import java.sql.Connection

/**
 *
 * @author Guilherme Lima Conte
 */

@Singleton
class TransacaoJdbc implements Transacao {

    private final ConexaoBanco conexaoBanco

    TransacaoJdbc(ConexaoBanco conexaoBanco) {
        this.conexaoBanco = conexaoBanco
    }

    @Override
    def <T> T executar(Closure<T> bloco) {
        Connection conexao = conexaoBanco.abrir()

        try {
            conexao.autoCommit = false
            conexaoBanco.vincularATransacao(conexao)

            T resultado = bloco()
            conexao.commit()
            return resultado
        } catch (Exception e) {
            desfazer(conexao, e)
            throw e
        } finally {
            conexaoBanco.desvincularDaTransacao()
            conexao.close()
        }
    }

    private static void desfazer(Connection conexao, Exception causa) {
        try {
            conexao.rollback()
        } catch (Exception falhaNoRollback) {
            causa.addSuppressed(falhaNoRollback)
        }
    }
}
