package org.linketinder.emails.mensageria

import io.micronaut.configuration.kafka.annotation.KafkaListener
import io.micronaut.configuration.kafka.annotation.OffsetReset
import io.micronaut.configuration.kafka.annotation.Topic
import org.linketinder.emails.http.RegistroDoServidor
import org.linketinder.emails.service.LeitorDeMensagem
import org.linketinder.emails.service.MensagemInvalidaException
import org.linketinder.emails.service.NotificacaoService

/**
 *
 * @author Guilherme Lima Conte
 */

@KafkaListener(groupId = "ms-emails", offsetReset = OffsetReset.EARLIEST)
class ConsumidorDeVagasCriadas {

    private final LeitorDeMensagem leitor
    private final NotificacaoService notificacaoService
    private final RegistroDoServidor registro

    ConsumidorDeVagasCriadas(LeitorDeMensagem leitor, NotificacaoService notificacaoService, RegistroDoServidor registro) {
        this.leitor = leitor
        this.notificacaoService = notificacaoService
        this.registro = registro
    }

    @Topic("vagas-criadas")
    void receber(String mensagem) {
        try {
            notificacaoService.notificar(leitor.ler(mensagem))
        } catch (MensagemInvalidaException e) {
            registro.registrarMensagemDescartada(e.message)
        } catch (Exception e) {
            registro.registrarFalhaAoProcessar(e.class.simpleName)
        }
    }
}
