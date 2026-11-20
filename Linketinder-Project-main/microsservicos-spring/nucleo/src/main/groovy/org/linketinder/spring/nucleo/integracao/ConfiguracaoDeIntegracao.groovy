package org.linketinder.spring.nucleo.integracao

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.JdkClientHttpRequestFactory
import org.springframework.web.client.RestClient

import java.net.http.HttpClient
import java.time.Duration

/**
 *
 * @author Guilherme Lima Conte
 */

@Configuration
class ConfiguracaoDeIntegracao {

    static final Duration TEMPO_PARA_CONECTAR = Duration.ofSeconds(2)
    static final Duration TEMPO_PARA_RESPONDER = Duration.ofSeconds(5)

    @Bean
    RestClient clienteDeCompetencias(@Value('${linketinder.competencias.url}') String url) {
        HttpClient clienteHttp = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(TEMPO_PARA_CONECTAR)
                .build()
        JdkClientHttpRequestFactory fabrica = new JdkClientHttpRequestFactory(clienteHttp)
        fabrica.readTimeout = TEMPO_PARA_RESPONDER

        return RestClient.builder().baseUrl(url).requestFactory(fabrica).build()
    }
}
