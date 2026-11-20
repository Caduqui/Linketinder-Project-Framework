package org.linketinder.spring.nucleo.service

import org.linketinder.spring.nucleo.model.Empresa
import org.linketinder.spring.nucleo.repositorio.EmpresaRepositorio
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 *
 * @author Guilherme Lima Conte
 */

@Service
class EmpresaService {

    private final ValidadorDeCadastro validador
    private final EmpresaRepositorio empresaRepositorio

    EmpresaService(ValidadorDeCadastro validador, EmpresaRepositorio empresaRepositorio) {
        this.validador = validador
        this.empresaRepositorio = empresaRepositorio
    }

    Empresa cadastrar(Map<String, Object> dados) {
        Empresa empresa = validador.validarEmpresa(dados)
        return gravarSemRepetir(empresa)
    }

    Empresa atualizar(Integer id, Map<String, Object> dados) {
        exigirExistente(id)

        Empresa empresa = validador.validarEmpresa(dados)
        empresa.id = id
        return gravarSemRepetir(empresa)
    }

    @Transactional
    void excluir(Integer id) {
        exigirExistente(id)
        exigirSemDependentes(id)
        empresaRepositorio.deleteById(id)
    }

    private Empresa gravarSemRepetir(Empresa empresa) {
        try {
            return empresaRepositorio.saveAndFlush(empresa)
        } catch (DataIntegrityViolationException ignorada) {
            throw new CadastroRepetidoException()
        }
    }

    private void exigirExistente(Integer id) {
        if (!empresaRepositorio.existsById(id)) {
            throw new RecursoNaoEncontradoException("Empresa ${id} não encontrada")
        }
    }

    private void exigirSemDependentes(Integer id) {
        if (empresaRepositorio.possuiVagas(id)) {
            throw new RegraDeNegocioException("Não é possível excluir a empresa porque ela possui vagas cadastradas")
        }
        if (empresaRepositorio.possuiRecrutadores(id)) {
            throw new RegraDeNegocioException("Não é possível excluir a empresa porque ela possui recrutadores cadastrados")
        }
    }
}
