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
@Table(name = "empresas")
class Empresa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id

    @Column(nullable = false, length = 100)
    String nome

    @Column(nullable = false, length = 200, unique = true)
    String email

    @Column(nullable = false, length = 30, unique = true)
    String cnpj

    @Column(nullable = false, length = 100)
    String pais

    @Column(nullable = false, length = 15)
    String cep

    @Column(nullable = false, length = 500)
    String descricao

    @Column(nullable = false, length = 50)
    String senha
}
