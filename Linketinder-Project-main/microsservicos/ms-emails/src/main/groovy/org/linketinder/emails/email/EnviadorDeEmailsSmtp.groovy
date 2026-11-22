package org.linketinder.emails.email

import io.micronaut.context.annotation.Value
import io.micronaut.email.BodyType
import io.micronaut.email.Email
import io.micronaut.email.EmailSender
import jakarta.inject.Singleton
import org.linketinder.emails.model.EmailDeVaga

/**
 *
 * @author Guilherme Lima Conte
 */

@Singleton
class EnviadorDeEmailsSmtp implements EnviadorDeEmails {

    private final EmailSender<?, ?> emailSender
    private final String remetente

    EnviadorDeEmailsSmtp(EmailSender<?, ?> emailSender, @Value('${linketinder.email.remetente}') String remetente) {
        this.emailSender = emailSender
        this.remetente = remetente
    }

    @Override
    void enviar(String destinatario, EmailDeVaga email) {
        emailSender.send(Email.builder()
                .from(remetente)
                .to(destinatario)
                .subject(email.assunto)
                .body(email.texto, BodyType.TEXT))
    }
}
