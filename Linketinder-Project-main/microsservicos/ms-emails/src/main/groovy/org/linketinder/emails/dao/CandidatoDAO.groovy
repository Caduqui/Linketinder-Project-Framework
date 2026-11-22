package org.linketinder.emails.dao

import jakarta.inject.Singleton
import org.linketinder.emails.banco.ConexaoBanco
import org.linketinder.emails.repositorio.CandidatoRepositorio

import java.sql.PreparedStatement
import java.sql.ResultSet

/**
 *
 * @author Guilherme Lima Conte
 */

@Singleton
class CandidatoDAO implements CandidatoRepositorio {

    private final ConexaoBanco conexaoBanco

    CandidatoDAO(ConexaoBanco conexaoBanco) {
        this.conexaoBanco = conexaoBanco
    }

    @Override
    List<String> listarEmails() {
        return conexaoBanco.executar("SELECT email FROM candidatos ORDER BY id") { PreparedStatement statement ->
            statement.executeQuery().withCloseable { ResultSet resultado ->
                List<String> emails = []
                while (resultado.next()) {
                    emails << resultado.getString("email")
                }
                emails
            }
        }
    }
}
