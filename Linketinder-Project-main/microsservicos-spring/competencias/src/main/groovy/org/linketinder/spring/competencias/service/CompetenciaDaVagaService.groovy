package org.linketinder.spring.competencias.service

import org.linketinder.spring.competencias.model.Competencia
import org.linketinder.spring.competencias.model.CompetenciaDaVaga
import org.linketinder.spring.competencias.repositorio.CompetenciaDaVagaRepositorio
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 *
 * @author Guilherme Lima Conte
 */

@Service
class CompetenciaDaVagaService {

    static final String CAMPO = "competencias"

    private final CompetenciaService competenciaService
    private final CompetenciaDaVagaRepositorio daVagaRepositorio

    CompetenciaDaVagaService(CompetenciaService competenciaService, CompetenciaDaVagaRepositorio daVagaRepositorio) {
        this.competenciaService = competenciaService
        this.daVagaRepositorio = daVagaRepositorio
    }

    @Transactional
    List<String> substituir(Integer idVaga, Map<String, Object> dados) {
        List<String> nomes = lerNomes(dados)
        daVagaRepositorio.apagarDaVaga(idVaga)

        List<Competencia> competencias = nomes.collect { String nome -> competenciaService.buscarOuCriar(nome) }
        competencias.each { Competencia competencia -> daVagaRepositorio.save(new CompetenciaDaVaga(idVaga: idVaga, competencia: competencia)) }
        return competencias*.nome
    }

    Map<Integer, List<String>> listarDasVagas(List<Integer> idsVagas) {
        if (idsVagas.isEmpty()) {
            return [:]
        }
        return daVagaRepositorio.listarDasVagas(idsVagas)
                .groupBy { CompetenciaDaVaga ligacao -> ligacao.idVaga }
                .collectEntries { Integer idVaga, List<CompetenciaDaVaga> ligacoes -> [(idVaga): ligacoes*.competencia*.nome] } as Map<Integer, List<String>>
    }

    @Transactional
    void apagarDaVaga(Integer idVaga) {
        daVagaRepositorio.apagarDaVaga(idVaga)
    }

    private static List<String> lerNomes(Map<String, Object> dados) {
        Object lista = dados[CAMPO]

        if (!(lista instanceof List)) {
            throw new DadosInvalidosException("O campo '${CAMPO}' deve ser uma lista de nomes")
        }

        return (lista as List)
                .collect { Object item -> lerNome(item) }
                .findAll { String nome -> !nome.isEmpty() }
                .unique { String nome -> nome.toLowerCase() }
    }

    private static String lerNome(Object item) {
        if (!(item instanceof CharSequence)) {
            throw new DadosInvalidosException("Cada item do campo '${CAMPO}' deve ser um texto")
        }
        return NomeDeCompetencia.limitar(item.toString().trim(), "O campo '${CAMPO}' deve ter no máximo ${NomeDeCompetencia.TAMANHO_MAXIMO} caracteres")
    }
}
