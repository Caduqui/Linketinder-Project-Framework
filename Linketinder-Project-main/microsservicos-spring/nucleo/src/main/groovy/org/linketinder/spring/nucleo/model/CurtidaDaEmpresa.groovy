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
@Table(name = "curtida_empresa_candidato")
class CurtidaDaEmpresa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id

    @Column(name = "id_empresa", nullable = false)
    Integer idEmpresa

    @Column(name = "id_candidato", nullable = false)
    Integer idCandidato

    @Column(name = "id_vaga", nullable = false)
    Integer idVaga
}
