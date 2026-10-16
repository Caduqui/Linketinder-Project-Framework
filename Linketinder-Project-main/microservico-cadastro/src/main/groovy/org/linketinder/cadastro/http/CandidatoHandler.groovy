package org.linketinder.cadastro.http

import org.linketinder.cadastro.model.Candidato
import org.linketinder.cadastro.service.CadastroCandidatoService

/**
 *
 * @author Guilherme Lima Conte
 */

class CandidatoHandler implements Manipulador {

    static final int CRIADO = 201

    private final CadastroCandidatoService cadastroCandidatoService

    CandidatoHandler(CadastroCandidatoService cadastroCandidatoService) {
        this.cadastroCandidatoService = cadastroCandidatoService
    }

    @Override
    RespostaJson atender(Requisicao requisicao) {
        Candidato candidato = cadastroCandidatoService.cadastrar(CorpoJson.ler(requisicao.corpo))
        return RespostaJson.de(CRIADO, semSenha(candidato))
    }

    private static Map<String, Object> semSenha(Candidato candidato) {
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
        ]
    }
}
