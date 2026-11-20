package org.linketinder.spring.competencias.service

import org.linketinder.spring.competencias.model.Competencia
import org.linketinder.spring.competencias.repositorio.CompetenciaRepositorio
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 *
 * @author Guilherme Lima Conte
 */

@Service
class CompetenciaService {

    static final String MENSAGEM_REPETIDA = "Essa competência já está cadastrada"

    private final CompetenciaRepositorio competenciaRepositorio

    CompetenciaService(CompetenciaRepositorio competenciaRepositorio) {
        this.competenciaRepositorio = competenciaRepositorio
    }

    List<Competencia> listar() {
        return competenciaRepositorio.listarTodas()
    }

    Competencia buscar(Integer id) {
        Competencia competencia = competenciaRepositorio.findById(id).orElse(null)

        if (competencia == null) {
            throw new RecursoNaoEncontradoException("Competência ${id} não encontrada")
        }
        return competencia
    }

    Competencia criar(Map<String, Object> dados) {
        String nome = NomeDeCompetencia.validar(dados, "nome")
        exigirNomeLivre(nome, null)
        return gravar(new Competencia(nome: nome))
    }

    @Transactional
    Competencia renomear(Integer id, Map<String, Object> dados) {
        Competencia competencia = buscar(id)
        String nome = NomeDeCompetencia.validar(dados, "nome")
        exigirNomeLivre(nome, id)

        competencia.nome = nome
        return gravar(competencia)
    }

    @Transactional
    void excluir(Integer id) {
        buscar(id)
        if (competenciaRepositorio.estaVinculada(id)) {
            throw new RegraDeNegocioException("Não é possível excluir a competência porque ela está relacionada a algum candidato ou vaga")
        }
        competenciaRepositorio.deleteById(id)
    }

    Competencia buscarOuCriar(String nome) {
        return competenciaRepositorio.buscarPorNomeIgnorandoCaixa(nome) ?: competenciaRepositorio.save(new Competencia(nome: nome))
    }

    private void exigirNomeLivre(String nome, Integer idDaPropria) {
        Competencia existente = competenciaRepositorio.buscarPorNomeIgnorandoCaixa(nome)

        if (existente != null && existente.id != idDaPropria) {
            throw new RegraDeNegocioException(MENSAGEM_REPETIDA)
        }
    }

    private Competencia gravar(Competencia competencia) {
        try {
            return competenciaRepositorio.saveAndFlush(competencia)
        } catch (DataIntegrityViolationException ignorada) {
            throw new RegraDeNegocioException(MENSAGEM_REPETIDA)
        }
    }
}
