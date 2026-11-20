package org.linketinder.spring.nucleo.service

import org.linketinder.spring.nucleo.model.Recrutador
import org.linketinder.spring.nucleo.repositorio.EmpresaRepositorio
import org.linketinder.spring.nucleo.repositorio.RecrutadorRepositorio
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 *
 * @author Guilherme Lima Conte
 */

@Service
class RecrutadorService {

    static final String MENSAGEM_REPETIDO = "Já existe um recrutador com esses dados"

    private final ValidadorDeRecrutador validador
    private final RecrutadorRepositorio recrutadorRepositorio
    private final EmpresaRepositorio empresaRepositorio

    RecrutadorService(ValidadorDeRecrutador validador, RecrutadorRepositorio recrutadorRepositorio, EmpresaRepositorio empresaRepositorio) {
        this.validador = validador
        this.recrutadorRepositorio = recrutadorRepositorio
        this.empresaRepositorio = empresaRepositorio
    }

    Recrutador cadastrar(Map<String, Object> dados) {
        Recrutador recrutador = validador.validar(dados)
        exigirEmpresa(recrutador.idEmpresa)

        if (recrutadorRepositorio.existeComEmailOuCpf(recrutador.email, recrutador.cpf)) {
            throw new RegraDeNegocioException(MENSAGEM_REPETIDO)
        }
        return recrutadorRepositorio.save(recrutador)
    }

    List<Recrutador> listarPorEmpresa(Integer idEmpresa) {
        if (idEmpresa == null) {
            throw new DadosInvalidosException("O parâmetro 'idEmpresa' é obrigatório e deve ser um número")
        }

        exigirEmpresa(idEmpresa)
        return recrutadorRepositorio.listarDaEmpresa(idEmpresa)
    }

    @Transactional
    void excluir(Integer id) {
        if (!recrutadorRepositorio.existsById(id)) {
            throw new RecursoNaoEncontradoException("Recrutador ${id} não encontrado")
        }

        recrutadorRepositorio.desvincularDosMatches(id)
        recrutadorRepositorio.apagarCurtidas(id)
        recrutadorRepositorio.apagarAtribuicoes(id)
        recrutadorRepositorio.deleteById(id)
    }

    private void exigirEmpresa(Integer idEmpresa) {
        if (!empresaRepositorio.existsById(idEmpresa)) {
            throw new RecursoNaoEncontradoException("Empresa ${idEmpresa} não encontrada")
        }
    }
}
