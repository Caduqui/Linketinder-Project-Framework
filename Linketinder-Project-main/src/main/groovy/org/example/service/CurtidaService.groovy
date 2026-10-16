package org.example.service

import org.example.model.Match
import org.example.repositorio.CurtidaRepositorio
import org.example.repositorio.MatchRepositorio

/**
 *
 * @author Guilherme Lima Conte
 */

class CurtidaService {

    private final CurtidaRepositorio curtidaRepositorio
    private final MatchRepositorio matchRepositorio
    private final RecrutadorService recrutadorService

    CurtidaService(CurtidaRepositorio curtidaRepositorio, MatchRepositorio matchRepositorio) {
        this(curtidaRepositorio, matchRepositorio, null)
    }

    CurtidaService(CurtidaRepositorio curtidaRepositorio, MatchRepositorio matchRepositorio, RecrutadorService recrutadorService) {
        this.curtidaRepositorio = curtidaRepositorio
        this.matchRepositorio = matchRepositorio
        this.recrutadorService = recrutadorService
    }

    boolean candidatoCurtirVaga(Integer idCandidato, Integer idVaga) {
        return curtidaRepositorio.candidatoCurtirVaga(idCandidato, idVaga)
    }

    boolean empresaCurtirCandidato(Integer idEmpresa, Integer idCandidato, Integer idVaga) {
        return curtidaRepositorio.empresaCurtirCandidato(idEmpresa, idCandidato, idVaga)
    }

    boolean recrutadorCurtirCandidato(Integer idRecrutador, Integer idCandidato, Integer idVaga) {
        if (recrutadorService == null) {
            throw new IllegalStateException("Curtida de recrutador exige validação de escopo")
        }

        recrutadorService.validarEscopo(idRecrutador, idVaga)
        return curtidaRepositorio.recrutadorCurtirCandidato(idRecrutador, idCandidato, idVaga)
    }

    boolean houveMatch(Integer idCandidato, Integer idEmpresa, Integer idVaga) {
        return matchRepositorio.criarMatchSePossivel(idCandidato, idEmpresa, idVaga)
    }

    List<Match> listarMatches() {
        return matchRepositorio.listar()
    }
}
