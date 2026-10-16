package org.linketinder.cadastro.http

import org.linketinder.cadastro.model.Empresa
import org.linketinder.cadastro.service.CadastroEmpresaService

/**
 *
 * @author Guilherme Lima Conte
 */

class EmpresaHandler implements Manipulador {

    static final int CRIADO = 201

    private final CadastroEmpresaService cadastroEmpresaService

    EmpresaHandler(CadastroEmpresaService cadastroEmpresaService) {
        this.cadastroEmpresaService = cadastroEmpresaService
    }

    @Override
    RespostaJson atender(Requisicao requisicao) {
        Empresa empresa = cadastroEmpresaService.cadastrar(CorpoJson.ler(requisicao.corpo))
        return RespostaJson.de(CRIADO, semSenha(empresa))
    }

    private static Map<String, Object> semSenha(Empresa empresa) {
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
