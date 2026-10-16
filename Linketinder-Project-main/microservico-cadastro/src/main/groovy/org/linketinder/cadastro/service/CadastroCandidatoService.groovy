package org.linketinder.cadastro.service

import org.linketinder.cadastro.model.Candidato
import org.linketinder.cadastro.repositorio.CandidatoRepositorio

/**
 *
 * @author Guilherme Lima Conte
 */

class CadastroCandidatoService {

    private final ValidadorDeCadastro validador
    private final CandidatoRepositorio candidatoRepositorio

    CadastroCandidatoService(ValidadorDeCadastro validador, CandidatoRepositorio candidatoRepositorio) {
        this.validador = validador
        this.candidatoRepositorio = candidatoRepositorio
    }

    Candidato cadastrar(Map<String, Object> dados) {
        Candidato candidato = validador.validarCandidato(dados)
        candidatoRepositorio.inserir(candidato)
        return candidato
    }
}
