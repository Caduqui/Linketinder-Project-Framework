package org.example.model

import java.time.LocalDate

/**
 *
 * @author Guilherme Lima Conte
 */
class Recrutador {
    Integer id
    String nome
    String area
    String descricao
    String email
    String cpf
    LocalDate dataNascimento
    String senha
    Integer idEmpresa

    Recrutador(String nome, String area, String descricao, String email, String cpf, LocalDate dataNascimento, String senha, Integer idEmpresa) {
        this.nome = nome
        this.area = area
        this.descricao = descricao
        this.email = email
        this.cpf = cpf
        this.dataNascimento = dataNascimento
        this.senha = senha
        this.idEmpresa = idEmpresa
    }

    String descrever() {
        return [
                "ID: ${id} | Recrutador: ${nome}",
                "Área: ${area}",
                "email: ${email}",
                "Descrição: ${descricao}"
        ].join("\n")
    }

    String descreverParaEmpresa() {
        return [
                "ID: ${id} | Recrutador: ${nome}",
                "Área: ${area}",
                "email: ${email}",
                "Descrição: ${descricao}",
                "Empresa: ${idEmpresa}"
        ].join("\n")
    }
}
