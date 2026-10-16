package org.example.model

import java.time.LocalDate

/**
 *
 * @author Guilherme Lima Conte
 */

class Candidato extends TipoPessoa {
    Integer id
    String sobrenome
    LocalDate dataNascimento
    String cpf
    String formacao
    List<String> competencias = []

    Candidato(String nome, String sobrenome, LocalDate dataNascimento, String email, String cpf, String pais, String cep, String descricao, String senha, String formacao, List<String> competencias) {
        super(nome, email, pais, cep, descricao, senha)
        this.sobrenome = sobrenome
        this.cpf = cpf
        this.formacao = formacao
        this.dataNascimento = dataNascimento
        this.competencias = competencias
    }

    String formatarCompetencias() {
        competencias ? competencias.join(", ") : "Nenhuma competência cadastrada"
    }

    @Override
    String descrever() {
        return [
                "ID: ${id} | Candidato: ${nome} ${sobrenome}",
                "Data de nascimento: ${dataNascimento}",
                "email: ${email}",
                "CPF: ${cpf}",
                "País: ${pais}",
                "CEP: ${cep}",
                "Descrição: ${descricao}",
                "Formação: ${formacao}",
                "Competências: ${formatarCompetencias()}"
        ].join("\n")

    }

    String descreverAnonimo() {
        return [
                "ID: ${id}",
                "Formação: ${formacao}",
                "Descrição: ${descricao}",
                "Competências: ${formatarCompetencias()}"
        ].join("\n")
    }

    String descreverNome() {
        return "ID: ${id} - ${nome} ${sobrenome}"
    }
}
