package org.linketinder.usuarios.service

import jakarta.inject.Singleton
import org.linketinder.usuarios.model.Empresa
import org.linketinder.usuarios.repositorio.EmpresaRepositorio

/**
 *
 * @author Guilherme Lima Conte
 */

@Singleton
class EmpresaService {

    static final String EMPRESA_COM_VAGAS = "Não é possível excluir a empresa porque ela possui vagas cadastradas"
    static final String EMPRESA_COM_RECRUTADORES = "Não é possível excluir a empresa porque ela possui recrutadores cadastrados"

    private final EmpresaRepositorio empresaRepositorio
    private final ValidadorDeCadastro validador

    EmpresaService(EmpresaRepositorio empresaRepositorio, ValidadorDeCadastro validador) {
        this.empresaRepositorio = empresaRepositorio
        this.validador = validador
    }

    List<Empresa> listar() {
        return empresaRepositorio.listar()
    }

    Empresa buscar(Integer id) {
        Empresa empresa = empresaRepositorio.buscarPorId(id)

        if (empresa == null) {
            throw new RecursoNaoEncontradoException("Empresa ${id} não encontrada")
        }
        return empresa
    }

    Empresa cadastrar(Map<String, Object> dados) {
        return empresaRepositorio.inserir(validador.validarEmpresa(dados))
    }

    Empresa atualizar(Integer id, Map<String, Object> dados) {
        buscar(id)

        Empresa empresa = validador.validarEmpresa(dados)
        empresa.id = id
        empresaRepositorio.atualizar(empresa)

        return buscar(id)
    }

    void excluir(Integer id) {
        buscar(id)

        if (empresaRepositorio.possuiVagas(id)) {
            throw new RegraDeNegocioException(EMPRESA_COM_VAGAS)
        }
        if (empresaRepositorio.possuiRecrutadores(id)) {
            throw new RegraDeNegocioException(EMPRESA_COM_RECRUTADORES)
        }
        empresaRepositorio.excluir(id)
    }
}
