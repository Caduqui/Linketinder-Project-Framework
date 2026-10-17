package org.linketinder.curtidas.service

import jakarta.inject.Singleton
import org.linketinder.curtidas.model.Curtida
import org.linketinder.curtidas.repositorio.CurtidaRepositorio
import org.linketinder.curtidas.repositorio.MatchRepositorio
import org.linketinder.curtidas.repositorio.ParticipanteRepositorio

/**
 *
 * @author Guilherme Lima Conte
 */

@Singleton
class CurtidaService {

    static final String TIPO_CANDIDATO = "candidato"
    static final String TIPO_EMPRESA = "empresa"
    static final String TIPO_RECRUTADOR = "recrutador"
    static final String VAGA_DE_OUTRA_EMPRESA_DO_RECRUTADOR = "A vaga não pertence à empresa do recrutador"
    static final String VAGA_NAO_ATRIBUIDA = "A vaga não está atribuída ao recrutador"

    private final ParticipanteRepositorio participanteRepositorio
    private final CurtidaRepositorio curtidaRepositorio
    private final MatchRepositorio matchRepositorio

    CurtidaService(ParticipanteRepositorio participanteRepositorio, CurtidaRepositorio curtidaRepositorio, MatchRepositorio matchRepositorio) {
        this.participanteRepositorio = participanteRepositorio
        this.curtidaRepositorio = curtidaRepositorio
        this.matchRepositorio = matchRepositorio
    }

    boolean curtir(Curtida curtida) {
        if (!participanteRepositorio.candidatoExiste(curtida.idCandidato)) {
            throw new RecursoNaoEncontradoException("Candidato ${curtida.idCandidato} não encontrado")
        }

        Integer idEmpresaDaVaga = participanteRepositorio.buscarEmpresaDaVaga(curtida.idVaga)
        if (idEmpresaDaVaga == null) {
            throw new RecursoNaoEncontradoException("Vaga ${curtida.idVaga} não encontrada")
        }

        registrar(curtida, idEmpresaDaVaga)
        return matchRepositorio.criarMatchSePossivel(curtida.idCandidato, idEmpresaDaVaga, curtida.idVaga)
    }

    private void registrar(Curtida curtida, Integer idEmpresaDaVaga) {
        switch (curtida.tipo) {
            case TIPO_CANDIDATO:
                curtidaRepositorio.candidatoCurtirVaga(curtida.idCandidato, curtida.idVaga)
                break
            case TIPO_EMPRESA:
                registrarDaEmpresa(curtida, idEmpresaDaVaga)
                break
            case TIPO_RECRUTADOR:
                registrarDoRecrutador(curtida, idEmpresaDaVaga)
                break
            default:
                throw new RegraDeNegocioException("O campo 'tipo' deve ser '${TIPO_CANDIDATO}', '${TIPO_EMPRESA}' ou '${TIPO_RECRUTADOR}'")
        }
    }

    private void registrarDaEmpresa(Curtida curtida, Integer idEmpresaDaVaga) {
        Integer idEmpresa = exigir(curtida.idEmpresa, "idEmpresa")

        if (idEmpresa != idEmpresaDaVaga) {
            throw new RegraDeNegocioException("A vaga ${curtida.idVaga} não pertence à empresa ${idEmpresa}")
        }
        curtidaRepositorio.empresaCurtirCandidato(idEmpresa, curtida.idCandidato, curtida.idVaga)
    }

    private void registrarDoRecrutador(Curtida curtida, Integer idEmpresaDaVaga) {
        Integer idRecrutador = exigir(curtida.idRecrutador, "idRecrutador")
        Integer idEmpresaDoRecrutador = participanteRepositorio.buscarEmpresaDoRecrutador(idRecrutador)

        if (idEmpresaDoRecrutador == null) {
            throw new RecursoNaoEncontradoException("Recrutador ${idRecrutador} não encontrado")
        }
        if (idEmpresaDoRecrutador != idEmpresaDaVaga) {
            throw new RegraDeNegocioException(VAGA_DE_OUTRA_EMPRESA_DO_RECRUTADOR)
        }
        if (!participanteRepositorio.vagaAtribuidaAoRecrutador(idRecrutador, curtida.idVaga)) {
            throw new RegraDeNegocioException(VAGA_NAO_ATRIBUIDA)
        }
        curtidaRepositorio.recrutadorCurtirCandidato(idRecrutador, curtida.idCandidato, curtida.idVaga)
    }

    private static Integer exigir(Integer valor, String campo) {
        if (valor == null) {
            throw new RegraDeNegocioException("O campo '${campo}' é obrigatório e deve ser um número")
        }
        return valor
    }
}
