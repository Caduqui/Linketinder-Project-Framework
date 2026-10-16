package org.linketinder.cadastro.model

import java.time.LocalDate

/**
 *
 * @author Guilherme Lima Conte
 */

class Candidato {
    Integer id
    String nome
    String sobrenome
    LocalDate dataNascimento
    String email
    String cpf
    String pais
    String cep
    String descricao
    String senha
    String formacao
}
