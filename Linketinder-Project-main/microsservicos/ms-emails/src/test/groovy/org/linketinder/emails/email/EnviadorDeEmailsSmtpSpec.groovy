package org.linketinder.emails.email

import io.micronaut.email.BodyType
import io.micronaut.email.Email
import io.micronaut.email.EmailSender
import org.linketinder.emails.model.EmailDeVaga
import spock.lang.Specification

/**
 *
 * @author Guilherme Lima Conte
 */

class EnviadorDeEmailsSmtpSpec extends Specification {

    EmailSender<Object, Object> emailSender = Mock()
    EnviadorDeEmailsSmtp enviador = new EnviadorDeEmailsSmtp(emailSender, "remetente@example.com")

    def "envia em texto puro do remetente configurado para um único destinatário"() {
        given:
        Email enviado = null

        when:
        enviador.enviar("candidata.um@example.com", new EmailDeVaga(assunto: "Assunto Exemplo", texto: "Texto exemplo"))

        then:
        1 * emailSender.send(_ as Email.Builder) >> { Email.Builder builder -> enviado = builder.build(); null }
        enviado.from.email == "remetente@example.com"
        enviado.to*.email == ["candidata.um@example.com"]
        enviado.cc == null || enviado.cc.isEmpty()
        enviado.bcc == null || enviado.bcc.isEmpty()
        enviado.subject == "Assunto Exemplo"
        enviado.body.get(BodyType.TEXT).get() == "Texto exemplo"
        !enviado.body.get(BodyType.HTML).isPresent()
    }
}
