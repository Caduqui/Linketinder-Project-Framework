package org.linketinder.spring.nucleo.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

import java.time.LocalDate

/**
 *
 * @author Guilherme Lima Conte
 */

@Entity
@Table(name = "candidatos")
class Candidato {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id

    @Column(nullable = false, length = 50)
    String nome

    @Column(nullable = false, length = 50)
    String sobrenome

    @Column(name = "data_nascimento", nullable = false)
    LocalDate dataNascimento

    @Column(nullable = false, length = 200, unique = true)
    String email

    @Column(nullable = false, length = 15, unique = true)
    String cpf

    @Column(nullable = false, length = 100)
    String pais

    @Column(nullable = false, length = 15)
    String cep

    @Column(nullable = false, length = 500)
    String descricao

    @Column(nullable = false, length = 50)
    String senha

    @Column(nullable = false, length = 100)
    String formacao
}
