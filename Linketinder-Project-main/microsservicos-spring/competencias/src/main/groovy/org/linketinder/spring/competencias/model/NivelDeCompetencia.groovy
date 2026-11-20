package org.linketinder.spring.competencias.model

/**
 *
 * @author Guilherme Lima Conte
 */

enum NivelDeCompetencia {
    BASICO,
    MEDIO,
    AVANCADO

    static List<String> valores() {
        values()*.name()
    }

    static boolean ehValido(Object valor) {
        valor instanceof String && valor in valores()
    }
}
