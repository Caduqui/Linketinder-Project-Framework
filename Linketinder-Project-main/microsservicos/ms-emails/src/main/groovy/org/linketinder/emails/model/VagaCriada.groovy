package org.linketinder.emails.model

import java.time.OffsetDateTime

/**
 *
 * @author Guilherme Lima Conte
 */

class VagaCriada {

    Integer id
    String nome
    String descricao
    List<String> competencias = []
    String estado
    String cidade
    OffsetDateTime dataHoraCriacao
}
