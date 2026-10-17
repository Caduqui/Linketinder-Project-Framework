package org.linketinder.competencias

import org.linketinder.competencias.model.Candidato

import java.time.LocalDate

/**
 *
 * @author Guilherme Lima Conte
 */

class DadosFicticios {

    static Candidato candidata(Integer id = 7) {
        return new Candidato(
                id: id,
                nome: "Candidata",
                sobrenome: "Exemplo",
                dataNascimento: LocalDate.of(2000, 1, 15),
                email: "candidata@example.com",
                cpf: "111.111.111-11",
                pais: "Brasil",
                cep: "79000-000",
                descricao: "Descrição fictícia",
                formacao: "Curso fictício"
        )
    }
}
