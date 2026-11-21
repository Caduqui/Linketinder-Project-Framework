package org.linketinder.spring.nucleo.service

import org.linketinder.spring.nucleo.model.Vaga
import org.linketinder.spring.nucleo.repositorio.AtribuicaoDeVagaRepositorio
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 *
 * @author Guilherme Lima Conte
 */

@Service
class AtribuicaoDeVagaService {

    private final AtribuicaoDeVagaRepositorio atribuicaoRepositorio
    private final VagaService vagaService

    AtribuicaoDeVagaService(AtribuicaoDeVagaRepositorio atribuicaoRepositorio, VagaService vagaService) {
        this.atribuicaoRepositorio = atribuicaoRepositorio
        this.vagaService = vagaService
    }

    List<Vaga> listar(Integer idRecrutador) {
        exigirRecrutador(idRecrutador)
        return vagaService.listarDoRecrutador(idRecrutador)
    }

    @Transactional
    List<Vaga> atribuir(Integer idRecrutador, Map<String, Object> dados) {
        Integer idVaga = CamposDoCorpo.exigirInteiro(dados, "idVaga")

        exigirVagaDaEmpresaDoRecrutador(idRecrutador, idVaga)
        atribuicaoRepositorio.atribuir(idRecrutador, idVaga)
        return vagaService.listarDoRecrutador(idRecrutador)
    }

    @Transactional
    void remover(Integer idRecrutador, Integer idVaga) {
        exigirVagaDaEmpresaDoRecrutador(idRecrutador, idVaga)

        if (!atribuicaoRepositorio.estaAtribuida(idRecrutador, idVaga)) {
            throw new RegraDeNegocioException("A vaga não está atribuída ao recrutador")
        }
        atribuicaoRepositorio.desvincularDosMatches(idRecrutador, idVaga)
        atribuicaoRepositorio.apagarCurtidas(idRecrutador, idVaga)
        atribuicaoRepositorio.remover(idRecrutador, idVaga)
    }

    private void exigirVagaDaEmpresaDoRecrutador(Integer idRecrutador, Integer idVaga) {
        Integer idEmpresa = exigirRecrutador(idRecrutador)

        if (vagaService.exigirVaga(idVaga).idEmpresa != idEmpresa) {
            throw new RegraDeNegocioException("A vaga não pertence à empresa do recrutador")
        }
    }

    private Integer exigirRecrutador(Integer idRecrutador) {
        Integer idEmpresa = atribuicaoRepositorio.buscarEmpresaDoRecrutador(idRecrutador)

        if (idEmpresa == null) {
            throw new RecursoNaoEncontradoException("Recrutador ${idRecrutador} não encontrado")
        }
        return idEmpresa
    }
}
