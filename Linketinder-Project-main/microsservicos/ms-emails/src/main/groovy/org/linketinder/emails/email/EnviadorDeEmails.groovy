package org.linketinder.emails.email

import org.linketinder.emails.model.EmailDeVaga

/**
 *
 * @author Guilherme Lima Conte
 */

interface EnviadorDeEmails {

    void enviar(String destinatario, EmailDeVaga email)
}
