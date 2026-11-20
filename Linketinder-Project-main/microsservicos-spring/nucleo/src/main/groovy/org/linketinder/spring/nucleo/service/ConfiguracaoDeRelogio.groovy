package org.linketinder.spring.nucleo.service

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

import java.time.Clock

/**
 *
 * @author Guilherme Lima Conte
 */

@Configuration
class ConfiguracaoDeRelogio {

    @Bean
    Clock relogio() {
        return Clock.systemDefaultZone()
    }
}
