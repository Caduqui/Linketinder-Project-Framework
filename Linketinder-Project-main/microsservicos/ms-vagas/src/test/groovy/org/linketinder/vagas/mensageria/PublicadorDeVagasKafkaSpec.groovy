package org.linketinder.vagas.mensageria

import org.linketinder.vagas.DadosFicticios
import org.linketinder.vagas.http.RegistroDoServidor
import spock.lang.Specification

import java.time.OffsetDateTime

/**
 *
 * @author Guilherme Lima Conte
 */

class PublicadorDeVagasKafkaSpec extends Specification {

    static final Set<String> CAMPOS_DA_MENSAGEM =
            ["id", "nome", "descricao", "competencias", "estado", "cidade", "dataHoraCriacao"] as Set

    ClienteKafkaDeVagas cliente = Mock()
    RegistroDoServidor registro = Mock()
    PublicadorDeVagasKafka publicador = new PublicadorDeVagasKafka(cliente, registro)

    def "publica com a chave igual ao id e só os dados da vaga, sem a empresa"() {
        when:
        publicador.publicarVagaCriada(DadosFicticios.vagaGravada(10, 3))

        then:
        1 * cliente.publicar("10", { Map<String, Object> mensagem ->
            mensagem.keySet() == CAMPOS_DA_MENSAGEM &&
                    mensagem.id == 10 &&
                    mensagem.nome == "Vaga Exemplo" &&
                    mensagem.descricao == "" &&
                    mensagem.competencias == ["Java"] &&
                    mensagem.estado == "MS" &&
                    mensagem.cidade == "Campo Grande" &&
                    !mensagem.values().any { it.toString().contains("Empresa Exemplo") }
        })
    }

    def "data e hora de criação vão em ISO-8601 com fuso"() {
        given:
        Map<String, Object> publicada = null
        cliente.publicar(_, _) >> { String id, Map<String, Object> mensagem -> publicada = mensagem }

        when:
        publicador.publicarVagaCriada(DadosFicticios.vagaGravada(10))

        then:
        OffsetDateTime.parse(publicada.dataHoraCriacao as String) != null
    }

    def "falha do Kafka não é relançada e vai para o log só com o id da vaga e o tipo da falha"() {
        given:
        cliente.publicar(_, _) >> { throw new IllegalStateException("broker localhost:9092 fora do ar") }

        when:
        publicador.publicarVagaCriada(DadosFicticios.vagaGravada(10))

        then:
        noExceptionThrown()
        1 * registro.registrarFalhaDePublicacao(10, "IllegalStateException")
    }
}
