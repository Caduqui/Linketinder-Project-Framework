package org.linketinder.cadastro.service

import org.linketinder.cadastro.model.Competencia
import org.linketinder.cadastro.repositorio.CandidatoRepositorio
import org.linketinder.cadastro.repositorio.CompetenciaRepositorio
import org.linketinder.cadastro.repositorio.Transacao

/**
 *
 * @author Guilherme Lima Conte
 */

class CompetenciaDoCandidatoService {

    static final String CAMPO = "competencias"
    static final int TAMANHO_MAXIMO_NOME = 50

    private final CandidatoRepositorio candidatoRepositorio
    private final CompetenciaRepositorio competenciaRepositorio
    private final Transacao transacao

    CompetenciaDoCandidatoService(CandidatoRepositorio candidatoRepositorio, CompetenciaRepositorio competenciaRepositorio, Transacao transacao) {
        this.candidatoRepositorio = candidatoRepositorio
        this.competenciaRepositorio = competenciaRepositorio
        this.transacao = transacao
    }

    List<String> adicionar(Integer idCandidato, Map<String, Object> dados) {
        List<String> nomes = validarNomes(dados)

        return transacao.executar {
            somarAoCandidato(idCandidato, nomes)
        } as List<String>
    }

    private List<String> somarAoCandidato(Integer idCandidato, List<String> nomes) {
        if (!candidatoRepositorio.existe(idCandidato)) {
            throw new CandidatoNaoEncontradoException()
        }

        Set<Integer> idsJaVinculados = competenciaRepositorio.buscarDoCandidato(idCandidato)*.id as Set<Integer>

        nomes.each { String nome ->
            Competencia competencia = buscarOuCriar(nome)
            if (idsJaVinculados.add(competencia.id)) {
                competenciaRepositorio.vincularAoCandidato(idCandidato, competencia.id)
            }
        }

        return competenciaRepositorio.buscarDoCandidato(idCandidato)*.nome
    }

    private Competencia buscarOuCriar(String nome) {
        Competencia existente = competenciaRepositorio.buscarPorNomeIgnorandoCaixa(nome)
        return existente ?: competenciaRepositorio.inserir(nome)
    }

    private static List<String> validarNomes(Map<String, Object> dados) {
        Object lista = dados[CAMPO]

        if (!(lista instanceof List)) {
            throw new DadosInvalidosException("O campo '${CAMPO}' deve ser uma lista com ao menos uma competência")
        }

        List<String> nomes = semVaziosNemRepetidos(lista as List)
        if (nomes.isEmpty()) {
            throw new DadosInvalidosException("O campo '${CAMPO}' deve ter ao menos uma competência")
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
            throw new DadosInvalidosException("Cada item do campo '${CAMPO}' deve ser um texto")
        }

        String nome = item.toString().trim()
        if (nome.length() > TAMANHO_MAXIMO_NOME) {
            throw new DadosInvalidosException("Cada item do campo '${CAMPO}' deve ter no máximo ${TAMANHO_MAXIMO_NOME} caracteres")
        }
        return nome
    }
}
