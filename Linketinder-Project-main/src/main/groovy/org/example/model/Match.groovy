package org.example.model

/**
 *
 * @author Guilherme Lima Conte
 */

class Match {
    Candidato candidato
    Empresa empresa
    Vaga vaga
    Integer idRecrutador

    Match(Candidato candidato, Empresa empresa, Vaga vaga) {
        this(candidato, empresa, vaga, null)
    }

    Match(Candidato candidato, Empresa empresa, Vaga vaga, Integer idRecrutador) {
        this.candidato = candidato
        this.empresa = empresa
        this.vaga = vaga
        this.idRecrutador = idRecrutador
    }

    String descrever() {
        return [
                "Candidato: ${candidato.nome} (${candidato.email})",
                "Vaga: ${vaga.nome}",
                "Empresa: ${empresa.nome} (${empresa.email})"
        ].join("\n")
    }
}
