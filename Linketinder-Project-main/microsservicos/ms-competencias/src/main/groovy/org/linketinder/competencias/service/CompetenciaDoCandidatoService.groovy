package org.linketinder.competencias.service

import jakarta.inject.Singleton
import org.linketinder.competencias.model.Candidato
import org.linketinder.competencias.model.Competencia
import org.linketinder.competencias.repositorio.CandidatoRepositorio
import org.linketinder.competencias.repositorio.CompetenciaRepositorio
import org.linketinder.competencias.repositorio.Transacao

/**
 *
 * @author Guilherme Lima Conte
 */

@Singleton
class CompetenciaDoCandidatoService {

    static final String CAMPO = "competencias"

    private final CandidatoRepositorio candidatoRepositorio
    private final CompetenciaRepositorio competenciaRepositorio
    private final Transacao transacao

    CompetenciaDoCandidatoService(CandidatoRepositorio candidatoRepositorio, CompetenciaRepositorio competenciaRepositorio, Transacao transacao) {
        this.candidatoRepositorio = candidatoRepositorio
        this.competenciaRepositorio = competenciaRepositorio
        this.transacao = transacao
    }

    Candidato adicionar(Integer idCandidato, Object competenciasInformadas) {
        List<String> nomes = validarNomes(competenciasInformadas)

        return transacao.executar {
            somarAoCandidato(idCandidato, nomes)
        }
    }

    private Candidato somarAoCandidato(Integer idCandidato, List<String> nomes) {
        if (!candidatoRepositorio.existe(idCandidato)) {
            throw new RecursoNaoEncontradoException("Candidato ${idCandidato} não encontrado")
        }

        Set<Integer> idsJaVinculados = competenciaRepositorio.buscarDoCandidato(idCandidato)*.id as Set<Integer>

        nomes.each { String nome ->
            Competencia competencia = buscarOuCriar(nome)
            if (idsJaVinculados.add(competencia.id)) {
                competenciaRepositorio.vincularAoCandidato(idCandidato, competencia.id)
            }
        }

        Candidato candidato = candidatoRepositorio.buscarPorId(idCandidato)
        candidato.competencias = competenciaRepositorio.buscarDoCandidato(idCandidato)*.nome
        return candidato
    }

    private Competencia buscarOuCriar(String nome) {
        Competencia existente = competenciaRepositorio.buscarPorNomeIgnorandoCaixa(nome)
        return existente ?: competenciaRepositorio.inserir(nome)
    }

    private static List<String> validarNomes(Object lista) {
        if (!(lista instanceof List)) {
            throw new RegraDeNegocioException("O campo '${CAMPO}' deve ser uma lista com ao menos uma competência")
        }

        List<String> nomes = semVaziosNemRepetidos((List) lista)
        if (nomes.isEmpty()) {
            throw new RegraDeNegocioException("O campo '${CAMPO}' deve ter ao menos uma competência")
        }
        return nomes
    }

    private static List<String> semVaziosNemRepetidos(List lista) {
        return lista
                .collect { Object item -> exigirNome(item) }
                .findAll { String nome -> !nome.isEmpty() }
                .unique { String nome -> nome.toLowerCase() }
    }

    private static String exigirNome(Object item) {
        if (!(item instanceof CharSequence)) {
            throw new RegraDeNegocioException("Cada item do campo '${CAMPO}' deve ser um texto")
        }

        String nome = item.toString().trim()
        if (nome.length() > ValidadorDeCompetencia.TAMANHO_MAXIMO_NOME) {
            throw new RegraDeNegocioException(
                    "Cada item do campo '${CAMPO}' deve ter no máximo ${ValidadorDeCompetencia.TAMANHO_MAXIMO_NOME} caracteres")
        }
        return nome
    }
}
