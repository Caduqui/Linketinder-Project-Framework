package org.linketinder.vagas.mensageria

import io.micronaut.configuration.kafka.annotation.KafkaClient
import io.micronaut.configuration.kafka.annotation.KafkaKey
import io.micronaut.configuration.kafka.annotation.Topic

/**
 *
 * @author Guilherme Lima Conte
 */

@KafkaClient
interface ClienteKafkaDeVagas {

    @Topic("vagas-criadas")
    void publicar(@KafkaKey String id, Map<String, Object> mensagem)
}
