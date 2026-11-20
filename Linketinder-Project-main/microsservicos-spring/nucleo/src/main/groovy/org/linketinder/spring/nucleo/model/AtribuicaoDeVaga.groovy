package org.linketinder.spring.nucleo.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

/**
 *
 * @author Guilherme Lima Conte
 */

@Entity
@Table(name = "recrutador_vaga")
class AtribuicaoDeVaga {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id

    @Column(name = "id_recrutador", nullable = false)
    Integer idRecrutador

    @Column(name = "id_vaga", nullable = false)
    Integer idVaga
}
