package org.linketinder.competencias.service

import jakarta.inject.Singleton
import org.linketinder.competencias.model.Competencia
import org.linketinder.competencias.repositorio.CompetenciaRepositorio

/**
 *
 * @author Guilherme Lima Conte
 */

@Singleton
class CatalogoDeCompetenciasService {

    static final String COMPETENCIA_REPETIDA = "Essa competência já está cadastrada"
    static final String COMPETENCIA_VINCULADA = "Não é possível excluir a competência porque ela está relacionada a algum candidato ou vaga"

    private final CompetenciaRepositorio competenciaRepositorio

    CatalogoDeCompetenciasService(CompetenciaRepositorio competenciaRepositorio) {
        this.competenciaRepositorio = competenciaRepositorio
    }

    List<Competencia> listar() {
        return competenciaRepositorio.listar()
    }

    Competencia cadastrar(Object nomeInformado) {
        String nome = ValidadorDeCompetencia.exigirNome(nomeInformado)
        recusarRepetida(nome, null)

        return competenciaRepositorio.inserir(nome)
    }

    Competencia renomear(Integer id, Object nomeInformado) {
        exigirExistente(id)
        String nome = ValidadorDeCompetencia.exigirNome(nomeInformado)
        recusarRepetida(nome, id)

        competenciaRepositorio.renomear(id, nome)
        return new Competencia(id: id, nome: nome)
    }

    void excluir(Integer id) {
        exigirExistente(id)

        if (competenciaRepositorio.estaVinculada(id)) {
            throw new RegraDeNegocioException(COMPETENCIA_VINCULADA)
        }
        competenciaRepositorio.excluir(id)
    }

    private Competencia exigirExistente(Integer id) {
        Competencia competencia = competenciaRepositorio.buscarPorId(id)

        if (competencia == null) {
            throw new RecursoNaoEncontradoException("Competência ${id} não encontrada")
        }
        return competencia
    }

    private void recusarRepetida(String nome, Integer idIgnorado) {
        Competencia existente = competenciaRepositorio.buscarPorNomeIgnorandoCaixa(nome)

        if (existente != null && existente.id != idIgnorado) {
            throw new CadastroRepetidoException(COMPETENCIA_REPETIDA)
        }
    }
}
