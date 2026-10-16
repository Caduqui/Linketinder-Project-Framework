package org.example.model

/**
 *
 * @author Guilherme Lima Conte
 */

class Vaga {
    Integer id
    String nome
    String descricao
    String estado
    String cidade
    Empresa empresa
    List<String> competencias = []

    Vaga(String nome, String descricao, String estado, String cidade, Empresa empresa, List<String> competencias) {
        this.nome = nome
        this.descricao = descricao
        this.estado = estado
        this.cidade = cidade
        this.empresa = empresa
        this.competencias = competencias
    }

    String formatarCompetencias() {
        competencias ? competencias.join(", ") : "Nenhuma competência cadastrada"
    }

    String descrever() {
        return [
                "ID: ${id} | Vaga: ${nome}",
                "Empresa: ${empresa.nome}",
                "Descrição: ${descricao}",
                "Local: ${cidade} - ${estado}",
                "Competências: ${formatarCompetencias()}"
        ].join("\n")
    }

}

