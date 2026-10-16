package org.linketinder.cadastro.service

import org.linketinder.cadastro.model.Empresa
import org.linketinder.cadastro.repositorio.EmpresaRepositorio

/**
 *
 * @author Guilherme Lima Conte
 */

class CadastroEmpresaService {

    private final ValidadorDeCadastro validador
    private final EmpresaRepositorio empresaRepositorio

    CadastroEmpresaService(ValidadorDeCadastro validador, EmpresaRepositorio empresaRepositorio) {
        this.validador = validador
        this.empresaRepositorio = empresaRepositorio
    }

    Empresa cadastrar(Map<String, Object> dados) {
        Empresa empresa = validador.validarEmpresa(dados)
        empresaRepositorio.inserir(empresa)
        return empresa
    }
}
