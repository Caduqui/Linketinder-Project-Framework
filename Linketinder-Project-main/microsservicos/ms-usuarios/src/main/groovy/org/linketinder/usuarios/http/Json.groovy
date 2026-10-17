package org.linketinder.usuarios.http

import org.linketinder.usuarios.model.Candidato
import org.linketinder.usuarios.model.Empresa
import org.linketinder.usuarios.model.Recrutador
import org.linketinder.usuarios.model.Vaga

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

    static Map<String, Object> deRecrutador(Recrutador recrutador) {
        return [
                id       : recrutador.id,
                nome     : recrutador.nome,
                area     : recrutador.area,
                descricao: recrutador.descricao,
                email    : recrutador.email,
                idEmpresa: recrutador.idEmpresa
        ]
    }

    static Map<String, Object> deVaga(Vaga vaga) {
        return [
                id          : vaga.id,
                nome        : vaga.nome,
                descricao   : vaga.descricao,
                estado      : vaga.estado,
                cidade      : vaga.cidade,
                empresa     : [id: vaga.idEmpresa, nome: vaga.nomeEmpresa],
                competencias: vaga.competencias
        ]
    }

    static Map<String, Object> deEmpresa(Empresa empresa) {
        return [
                id       : empresa.id,
                nome     : empresa.nome,
                email    : empresa.email,
                cnpj     : empresa.cnpj,
                pais     : empresa.pais,
                cep      : empresa.cep,
                descricao: empresa.descricao
        ]
    }
}
