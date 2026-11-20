package org.linketinder.spring.competencias.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

/**
 *
 * @author Guilherme Lima Conte
 */

@Entity
@Table(name = "candidato_competencia")
class CompetenciaDoCandidato {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id

    @Column(name = "id_candidato", nullable = false)
    Integer idCandidato

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_competencia", nullable = false)
    Competencia competencia

    @Column(nullable = false, length = 8)
    String nivel
}
