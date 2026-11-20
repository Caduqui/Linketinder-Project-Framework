package org.linketinder.spring.nucleo.controller

import org.linketinder.spring.nucleo.model.Candidato
import org.linketinder.spring.nucleo.model.CandidatoAnonimo
import org.linketinder.spring.nucleo.model.CompetenciaDoCandidato
import org.linketinder.spring.nucleo.model.Empresa
import org.linketinder.spring.nucleo.model.Recrutador
import org.linketinder.spring.nucleo.model.UsuarioLogado
import org.linketinder.spring.nucleo.model.Vaga

/**
 *
 * @author Guilherme Lima Conte
 */

class Representacao {

    static Map<String, Object> deCandidato(Candidato candidato) {
        return [
                id            : candidato.id,
                nome          : candidato.nome,
                sobrenome     : candidato.sobrenome,
                dataNascimento: candidato.dataNascimento.toString(),
                email         : candidato.email,
                cpf           : candidato.cpf,
                pais          : candidato.pais,
                cep           : candidato.cep,
                descricao     : candidato.descricao,
                formacao      : candidato.formacao
        ] as Map<String, Object>
    }

    static Map<String, Object> deCandidatoAnonimo(CandidatoAnonimo candidato) {
        return [
                id          : candidato.id,
                formacao    : candidato.formacao,
                descricao   : candidato.descricao,
                competencias: candidato.competencias.collect { CompetenciaDoCandidato competencia ->
                    [nome: competencia.nome, nivel: competencia.nivel]
                }
        ] as Map<String, Object>
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
        ] as Map<String, Object>
    }

    static Map<String, Object> deRecrutador(Recrutador recrutador) {
        return [
                id       : recrutador.id,
                nome     : recrutador.nome,
                area     : recrutador.area,
                descricao: recrutador.descricao,
                email    : recrutador.email,
                idEmpresa: recrutador.idEmpresa
        ] as Map<String, Object>
    }

    static Map<String, Object> deUsuarioLogado(UsuarioLogado usuario) {
        Map<String, Object> dados = [tipo: usuario.tipo, id: usuario.id, nome: usuario.nome] as Map<String, Object>

        if (usuario.idEmpresa != null) {
            dados.idEmpresa = usuario.idEmpresa
        }
        return dados
    }

    static Map<String, Object> deVaga(Vaga vaga) {
        return [
                id          : vaga.id,
                nome        : vaga.nome,
                descricao   : vaga.descricao,
                estado      : vaga.estado,
                cidade      : vaga.cidade,
                competencias: vaga.competencias
        ] as Map<String, Object>
    }

    static List<Map<String, Object>> deVagas(List<Vaga> vagas) {
        return vagas.collect { Vaga vaga -> deVaga(vaga) }
    }
}
