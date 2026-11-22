package org.linketinder.emails.mensageria

import io.micronaut.json.JsonMapper
import org.linketinder.emails.DadosFicticios
import org.linketinder.emails.email.EnviadorDeEmails
import org.linketinder.emails.http.RegistroDoServidor
import org.linketinder.emails.model.EmailDeVaga
import org.linketinder.emails.repositorio.CandidatoRepositorio
import org.linketinder.emails.service.LeitorDeMensagem
import org.linketinder.emails.service.NotificacaoService
import spock.lang.Specification

/**
 *
 * @author Guilherme Lima Conte
 */

class ConsumidorDeVagasCriadasSpec extends Specification {

    CandidatoRepositorio candidatoRepositorio = Mock()
    EnviadorDeEmails enviador = Mock()
    RegistroDoServidor registro = Mock()
    ConsumidorDeVagasCriadas consumidor = new ConsumidorDeVagasCriadas(
            new LeitorDeMensagem(JsonMapper.createDefault()),
            new NotificacaoService(candidatoRepositorio, enviador, registro),
            registro)

    def setup() {
        candidatoRepositorio.listarEmails() >> DadosFicticios.emailsDeCandidatos()
    }

    def "mensagem válida notifica os candidatos com os dados da vaga"() {
        when:
        consumidor.receber(DadosFicticios.mensagem())

        then:
        3 * enviador.enviar(_, { EmailDeVaga email -> email.texto.contains("Vaga: Vaga Exemplo") })
        1 * registro.registrarNotificacao(10, 3, 0)
    }

    def "mensagem inválida é descartada sem enviar nada e sem repetir o conteúdo no registro"() {
        when:
        consumidor.receber('{"nome": "Vaga Exemplo"}')

        then:
        0 * enviador._
        1 * registro.registrarMensagemDescartada({ String motivo -> !motivo.contains("Vaga Exemplo") })
    }

    def "falha inesperada não escapa do consumidor e vai para o registro só com o tipo"() {
        given:
        LeitorDeMensagem leitorQuebrado = new LeitorDeMensagem(null)
        ConsumidorDeVagasCriadas consumidorComFalha = new ConsumidorDeVagasCriadas(
                leitorQuebrado, new NotificacaoService(candidatoRepositorio, enviador, registro), registro)

        when:
        consumidorComFalha.receber(DadosFicticios.mensagem())

        then:
        noExceptionThrown()
        0 * enviador._
        1 * registro.registrarFalhaAoProcessar("NullPointerException")
    }
}
