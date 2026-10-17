package org.linketinder.vagas.service

import jakarta.inject.Singleton
import org.linketinder.vagas.model.Competencia
import org.linketinder.vagas.model.Vaga
import org.linketinder.vagas.repositorio.CompetenciaRepositorio
import org.linketinder.vagas.repositorio.EmpresaRepositorio
import org.linketinder.vagas.repositorio.Transacao
import org.linketinder.vagas.repositorio.VagaRepositorio

/**
 *
 * @author Guilherme Lima Conte
 */

@Singleton
class VagaService {

    private final VagaRepositorio vagaRepositorio
    private final CompetenciaRepositorio competenciaRepositorio
    private final EmpresaRepositorio empresaRepositorio
    private final Transacao transacao

    VagaService(VagaRepositorio vagaRepositorio, CompetenciaRepositorio competenciaRepositorio,
                EmpresaRepositorio empresaRepositorio, Transacao transacao) {
        this.vagaRepositorio = vagaRepositorio
        this.competenciaRepositorio = competenciaRepositorio
        this.empresaRepositorio = empresaRepositorio
        this.transacao = transacao
    }

    List<Vaga> listar() {
        return vagaRepositorio.listar()
    }

    List<Vaga> listarPorEmpresa(Integer idEmpresa) {
        return vagaRepositorio.listarPorEmpresa(idEmpresa)
    }

    Vaga buscar(Integer id) {
        Vaga vaga = vagaRepositorio.buscarPorId(id)

        if (vaga == null) {
            throw new RecursoNaoEncontradoException("Vaga ${id} não encontrada")
        }
        return vaga
    }

    Vaga cadastrar(Map<String, Object> dados, Integer idEmpresa) {
        if (!empresaRepositorio.existe(idEmpresa)) {
            throw new RegraDeNegocioException("Empresa ${idEmpresa} não encontrada")
        }

        Vaga vaga = ValidadorDeVaga.validar(dados)
        vaga.idEmpresa = idEmpresa

        Integer id = transacao.executar {
            vagaRepositorio.inserir(vaga)
            vincularCompetencias(vaga.id, vaga.competencias)
            return vaga.id
        }
        return buscar(id)
    }

    Vaga atualizar(Integer id, Map<String, Object> dados) {
        buscar(id)

        Vaga vaga = ValidadorDeVaga.validar(dados)
        vaga.id = id

        transacao.executar {
            vagaRepositorio.atualizar(vaga)
            vagaRepositorio.desvincularCompetencias(id)
            vincularCompetencias(id, vaga.competencias)
        }
        return buscar(id)
    }

    void excluir(Integer id) {
        buscar(id)
        transacao.executar {
            vagaRepositorio.excluir(id)
        }
    }

    private void vincularCompetencias(Integer idVaga, List<String> nomes) {
        nomes.collect { String nome -> buscarOuCriar(nome).id }.unique().each { Integer idCompetencia ->
            vagaRepositorio.vincularCompetencia(idVaga, idCompetencia)
        }
    }

    private Competencia buscarOuCriar(String nome) {
        Competencia existente = competenciaRepositorio.buscarPorNomeIgnorandoCaixa(nome)
        return existente ?: competenciaRepositorio.inserir(nome)
    }
}
