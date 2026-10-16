package org.example.api

import org.example.model.Candidato
import org.example.model.Competencia
import org.example.model.Empresa
import org.example.model.Recrutador
import org.example.model.Vaga
import org.example.service.RegraDeNegocioException

import java.time.LocalDate
import java.time.format.DateTimeParseException

/**
 *
 * @author Guilherme Lima Conte
 */

class Json {

    static Candidato paraCandidato(Map dados) {
        return new Candidato(
                exigirTexto(dados, "nome"),
                exigirTexto(dados, "sobrenome"),
                exigirData(dados, "dataNascimento"),
                exigirTexto(dados, "email"),
                exigirTexto(dados, "cpf"),
                exigirTexto(dados, "pais"),
                exigirTexto(dados, "cep"),
                texto(dados, "descricao"),
                exigirTexto(dados, "senha"),
                texto(dados, "formacao"),
                competencias(dados)
        )
    }

    static Map deCandidato(Candidato candidato) {
        return [
                id : candidato.id,
                nome : candidato.nome,
                sobrenome : candidato.sobrenome,
                dataNascimento: candidato.dataNascimento.toString(),
                email : candidato.email,
                cpf : candidato.cpf,
                pais : candidato.pais,
                cep : candidato.cep,
                descricao: candidato.descricao,
                formacao: candidato.formacao,
                competencias : candidato.competencias
        ]
    }

    static Empresa paraEmpresa(Map dados) {
        return new Empresa(
                exigirTexto(dados, "nome"),
                exigirTexto(dados, "email"),
                exigirTexto(dados, "cnpj"),
                exigirTexto(dados, "pais"),
                exigirTexto(dados, "cep"),
                texto(dados, "descricao"),
                exigirTexto(dados, "senha")
        )
    }

    static Map deEmpresa(Empresa empresa) {
        return [
                id : empresa.id,
                nome : empresa.nome,
                email : empresa.email,
                cnpj: empresa.cnpj,
                pais : empresa.pais,
                cep : empresa.cep,
                descricao: empresa.descricao
        ]
    }

    static Recrutador paraRecrutador(Map dados) {
        return new Recrutador(
                exigirTexto(dados, "nome"),
                exigirTexto(dados, "area"),
                texto(dados, "descricao"),
                exigirTexto(dados, "email"),
                exigirTexto(dados, "cpf"),
                exigirData(dados, "dataNascimento"),
                exigirTexto(dados, "senha"),
                exigirInteiro(dados, "idEmpresa")
        )
    }

    static Map deRecrutador(Recrutador recrutador) {
        return [
                id: recrutador.id,
                nome: recrutador.nome,
                area: recrutador.area,
                descricao: recrutador.descricao,
                email: recrutador.email,
                idEmpresa: recrutador.idEmpresa
        ]
    }

    static Vaga paraVaga(Map dados, Empresa empresa) {
        return new Vaga(
                exigirTexto(dados, "nome"),
                texto(dados, "descricao"),
                exigirTexto(dados, "estado"),
                exigirTexto(dados, "cidade"),
                empresa,
                competencias(dados)
        )
    }

    static Map deVaga(Vaga vaga) {
        return [
                id : vaga.id,
                nome : vaga.nome,
                descricao: vaga.descricao,
                estado : vaga.estado,
                cidade : vaga.cidade,
                empresa : [id: vaga.empresa.id, nome: vaga.empresa.nome],
                competencias : vaga.competencias
        ]
    }

    static Map deCompetencia(Competencia competencia) {
        return [
                id: competencia.id,
                nome : competencia.nome
        ]
    }

    static Integer exigirInteiro(Map dados, String campo) {
        Object valor = dados[campo]

        if (valor instanceof Number) {
            return ((Number) valor).intValue()
        }

        throw new RegraDeNegocioException("O campo '${campo}' é obrigatório e deve ser um número")
    }

    static String exigirTexto(Map dados, String campo) {
        String valor = texto(dados, campo)

        if (valor.isEmpty()) {
            throw new RegraDeNegocioException("O campo '${campo}' é obrigatório")
        }

        return valor
    }

    private static String texto(Map dados, String campo) {
        Object valor = dados[campo]
        return valor == null ? "" : valor.toString().trim()
    }

    private static LocalDate exigirData(Map dados, String campo) {
        try {
            return LocalDate.parse(exigirTexto(dados, campo))
        } catch (DateTimeParseException e) {
            throw new RegraDeNegocioException("O campo '${campo}' deve estar no formato AAAA-MM-DD")
        }
    }

    static List<String> competencias(Map dados) {
        List lista = dados["competencias"] instanceof List ? (List) dados["competencias"] : []

        return lista*.toString()*.trim().findAll { String competencia ->
            !competencia.isEmpty()
        }.unique { String competencia ->
            competencia.toLowerCase()
        }
    }
}
