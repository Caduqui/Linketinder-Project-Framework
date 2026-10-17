package org.linketinder.usuarios

import io.micronaut.context.annotation.Factory
import jakarta.inject.Singleton

import java.time.Clock

/**
 *
 * @author Guilherme Lima Conte
 */

@Factory
class FabricaDeRelogio {

    @Singleton
    Clock relogio() {
        return Clock.systemDefaultZone()
    }
}
