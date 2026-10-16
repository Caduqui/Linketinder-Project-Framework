package org.example.model

/**
 *
 * @author Guilherme Lima Conte
 */

abstract class TipoPessoa implements Pessoa {
    String nome
    String email
    String pais
    String cep
    String descricao
    String senha

    TipoPessoa(String nome, String email, String pais, String cep, String descricao, String senha) {
        this.nome = nome
        this.email = email
        this.cep = cep
        this.pais = pais
        this.descricao = descricao
        this.senha = senha
    }
}
