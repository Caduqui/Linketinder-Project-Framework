package org.linketinder.emails.service

import org.linketinder.emails.DadosFicticios
import org.linketinder.emails.email.EnviadorDeEmails
import org.linketinder.emails.http.RegistroDoServidor
import org.linketinder.emails.model.EmailDeVaga
import org.linketinder.emails.repositorio.CandidatoRepositorio
import spock.lang.Specification

import java.sql.SQLException

/**
 *
 * @author Guilherme Lima Conte
 */

class NotificacaoServiceSpec extends Specification {

    CandidatoRepositorio candidatoRepositorio = Mock()
    EnviadorDeEmails enviador = Mock()
    RegistroDoServidor registro = Mock()
    NotificacaoService service = new NotificacaoService(candidatoRepositorio, enviador, registro)

    def "envia um e-mail por candidato, cada um com um único destinatário, e registra as contagens"() {
        given:
        candidatoRepositorio.listarEmails() >> DadosFicticios.emailsDeCandidatos()

        when:
        service.notificar(DadosFicticios.vagaCriada())

        then:
        1 * enviador.enviar("candidata.um@example.com", { EmailDeVaga e -> e.assunto == "Nova vaga no Linketinder: Vaga Exemplo" })
        1 * enviador.enviar("candidato.dois@example.com", _ as EmailDeVaga)
        1 * enviador.enviar("candidata.tres@example.com", _ as EmailDeVaga)
        0 * enviador.enviar({ String destinatario -> destinatario.contains(",") || destinatario.contains(";") }, _)
        1 * registro.registrarNotificacao(10, 3, 0)
    }

    def "falha no envio para um candidato não impede os seguintes"() {
        given:
        candidatoRepositorio.listarEmails() >> DadosFicticios.emailsDeCandidatos()
        enviador.enviar("candidato.dois@example.com", _) >> { throw new IllegalStateException("SMTP recusou") }

        when:
        service.notificar(DadosFicticios.vagaCriada())

        then:
        1 * enviador.enviar("candidata.tres@example.com", _)
        1 * registro.registrarNotificacao(10, 2, 1)
    }

    def "sem candidatos nada é enviado e o registro mostra zero"() {
        given:
        candidatoRepositorio.listarEmails() >> []

        when:
        service.notificar(DadosFicticios.vagaCriada())

        then:
        0 * enviador._
        1 * registro.registrarNotificacao(10, 0, 0)
    }

    def "falha de banco não envia nada e registra só o id da vaga e o SQLState"() {
        given:
        candidatoRepositorio.listarEmails() >> { throw new SQLException("detalhe técnico com candidata.um@example.com", "08006") }

        when:
        service.notificar(DadosFicticios.vagaCriada())

        then:
        0 * enviador._
        1 * registro.registrarFalhaDeBanco(10, "08006")
        0 * registro.registrarNotificacao(*_)
    }
}
