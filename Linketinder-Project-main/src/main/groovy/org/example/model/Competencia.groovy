package org.example.model

/**
 *
 * @author Guilherme Lima Conte
 */

class Competencia {

    Integer id
    String nome

    Competencia(String nome) {
        this.nome = nome
    }

    String descrever() {
        return "ID: ${id} | Competência: ${nome}"
    }
}