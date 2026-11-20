package org.linketinder.spring.nucleo.service

import org.linketinder.spring.nucleo.integracao.ServicoDeCompetencias
import org.linketinder.spring.nucleo.model.Candidato
import org.linketinder.spring.nucleo.model.CandidatoAnonimo
import org.linketinder.spring.nucleo.model.CompetenciaDoCandidato
import org.linketinder.spring.nucleo.repositorio.CandidatoRepositorio
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 *
 * @author Guilherme Lima Conte
 */

@Service
class CandidatoService {

    private final ValidadorDeCadastro validador
    private final CandidatoRepositorio candidatoRepositorio
    private final ServicoDeCompetencias servicoDeCompetencias

    CandidatoService(ValidadorDeCadastro validador, CandidatoRepositorio candidatoRepositorio, ServicoDeCompetencias servicoDeCompetencias) {
        this.validador = validador
        this.candidatoRepositorio = candidatoRepositorio
        this.servicoDeCompetencias = servicoDeCompetencias
    }

    Candidato cadastrar(Map<String, Object> dados) {
        Candidato candidato = validador.validarCandidato(dados)
        return gravarSemRepetir(candidato)
    }

    List<CandidatoAnonimo> listar() {
        return completarCompetencias(candidatoRepositorio.listarAnonimos())
    }

    CandidatoAnonimo buscar(Integer id) {
        CandidatoAnonimo candidato = candidatoRepositorio.buscarAnonimo(id)

        if (candidato == null) {
            throw naoEncontrado(id)
        }
        return completarCompetencias([candidato]).first()
    }

    boolean existe(Integer id) {
        return candidatoRepositorio.existsById(id)
    }

    Candidato atualizar(Integer id, Map<String, Object> dados) {
        exigirExistente(id)

        Candidato candidato = validador.validarCandidato(dados)
        candidato.id = id
        return gravarSemRepetir(candidato)
    }

    @Transactional
    void excluir(Integer id) {
        exigirExistente(id)

        candidatoRepositorio.apagarCompetenciasDoBancoPrincipal(id)
        candidatoRepositorio.apagarCurtidasDadas(id)
        candidatoRepositorio.apagarCurtidasDeEmpresas(id)
        candidatoRepositorio.apagarCurtidasDeRecrutadores(id)
        candidatoRepositorio.apagarMatches(id)
        candidatoRepositorio.deleteById(id)
        servicoDeCompetencias.apagarDoCandidato(id)
    }

    private Candidato gravarSemRepetir(Candidato candidato) {
        try {
            return candidatoRepositorio.saveAndFlush(candidato)
        } catch (DataIntegrityViolationException ignorada) {
            throw new CadastroRepetidoException()
        }
    }

    private List<CandidatoAnonimo> completarCompetencias(List<CandidatoAnonimo> candidatos) {
        Map<Integer, List<CompetenciaDoCandidato>> competencias = servicoDeCompetencias.buscarDosCandidatos(candidatos*.id)
        candidatos.each { CandidatoAnonimo candidato -> candidato.competencias = competencias[candidato.id] ?: [] }
        return candidatos
    }

    private void exigirExistente(Integer id) {
        if (!candidatoRepositorio.existsById(id)) {
            throw naoEncontrado(id)
        }
    }

    private static RecursoNaoEncontradoException naoEncontrado(Integer id) {
        return new RecursoNaoEncontradoException("Candidato ${id} não encontrado")
    }
}
