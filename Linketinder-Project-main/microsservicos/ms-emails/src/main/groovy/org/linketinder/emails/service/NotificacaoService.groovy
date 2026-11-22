package org.linketinder.emails.service

import jakarta.inject.Singleton
import org.linketinder.emails.email.EnviadorDeEmails
import org.linketinder.emails.http.RegistroDoServidor
import org.linketinder.emails.model.EmailDeVaga
import org.linketinder.emails.model.VagaCriada
import org.linketinder.emails.repositorio.CandidatoRepositorio

import java.sql.SQLException

/**
 *
 * @author Guilherme Lima Conte
 */

@Singleton
class NotificacaoService {

    private final CandidatoRepositorio candidatoRepositorio
    private final EnviadorDeEmails enviador
    private final RegistroDoServidor registro

    NotificacaoService(CandidatoRepositorio candidatoRepositorio, EnviadorDeEmails enviador, RegistroDoServidor registro) {
        this.candidatoRepositorio = candidatoRepositorio
        this.enviador = enviador
        this.registro = registro
    }

    void notificar(VagaCriada vaga) {
        List<String> destinatarios
        try {
            destinatarios = candidatoRepositorio.listarEmails()
        } catch (SQLException e) {
            registro.registrarFalhaDeBanco(vaga.id, e.SQLState)
            return
        }

        EmailDeVaga email = ModeloDeEmailDeVaga.montar(vaga)
        int falhas = destinatarios.count { String destinatario -> !enviou(destinatario, email) } as int
        registro.registrarNotificacao(vaga.id, destinatarios.size() - falhas, falhas)
    }

    private boolean enviou(String destinatario, EmailDeVaga email) {
        try {
            enviador.enviar(destinatario, email)
            return true
        } catch (Exception ignorada) {
            return false
        }
    }
}
