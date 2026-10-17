package org.linketinder.competencias.http

import org.linketinder.competencias.model.Candidato
import org.linketinder.competencias.model.Competencia

/**
 *
 * @author Guilherme Lima Conte
 */

class Json {

    static Map<String, Object> deCandidato(Candidato candidato) {
        return [
                id            : candidato.id,
                nome          : candidato.nome,
                sobrenome     : candidato.sobrenome,
                dataNascimento: candidato.dataNascimento?.toString(),
                email         : candidato.email,
                cpf           : candidato.cpf,
                pais          : candidato.pais,
                cep           : candidato.cep,
                descricao     : candidato.descricao,
                formacao      : candidato.formacao,
                competencias  : candidato.competencias
        ]
    }

    static Map<String, Object> deCompetencia(Competencia competencia) {
        return [
                id  : competencia.id,
                nome: competencia.nome
        ]
    }
}
