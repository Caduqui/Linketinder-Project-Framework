package org.example.model

/**
 *
 * @author Guilherme Lima Conte
 */

class Empresa extends TipoPessoa {
    Integer id
    String cnpj

    Empresa(String nome, String email, String cnpj, String pais, String cep, String descricao, String senha) {
        super (nome, email, pais, cep, descricao, senha)
        this.cnpj = cnpj
    }

    @Override
    String descrever() {
        return [
                "ID: ${id} | Empresa: ${nome}",
                "email: ${email}",
                "CNPJ: ${cnpj}",
                "País: ${pais}",
                "CEP: ${cep}",
                "Descrição: ${descricao}"
        ].join("\n")
    }

    String descreverNome() {
        return "ID: ${id} - ${nome}"
    }
}
