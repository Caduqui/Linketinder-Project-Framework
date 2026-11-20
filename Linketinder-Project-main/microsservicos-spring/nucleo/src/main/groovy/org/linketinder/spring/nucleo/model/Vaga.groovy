package org.linketinder.spring.nucleo.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.Transient

/**
 *
 * @author Guilherme Lima Conte
 */

@Entity
@Table(name = "vagas")
class Vaga {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id

    @Column(nullable = false, length = 100)
    String nome

    @Column(nullable = false, length = 500)
    String descricao

    @Column(nullable = false, length = 2)
    String estado

    @Column(nullable = false, length = 100)
    String cidade

    @Column(name = "id_empresa", nullable = false)
    Integer idEmpresa

    @Transient
    List<String> competencias = []
}
