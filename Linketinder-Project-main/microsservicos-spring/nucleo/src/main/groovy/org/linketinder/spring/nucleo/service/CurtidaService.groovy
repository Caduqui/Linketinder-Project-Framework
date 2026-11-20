package org.linketinder.spring.nucleo.service

import org.linketinder.spring.nucleo.model.PedidoDeCurtida
import org.linketinder.spring.nucleo.repositorio.CurtidaRepositorio
import org.linketinder.spring.nucleo.repositorio.MatchRepositorio
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 *
 * @author Guilherme Lima Conte
 */

@Service
class CurtidaService {

    static final String CANDIDATO = "candidato"
    static final String EMPRESA = "empresa"
    static final String RECRUTADOR = "recrutador"

    private final CurtidaRepositorio curtidaRepositorio
    private final MatchRepositorio matchRepositorio

    CurtidaService(CurtidaRepositorio curtidaRepositorio, MatchRepositorio matchRepositorio) {
        this.curtidaRepositorio = curtidaRepositorio
        this.matchRepositorio = matchRepositorio
    }

    @Transactional
    boolean curtir(Map<String, Object> dados) {
        PedidoDeCurtida pedido = lerPedido(dados)

        exigirCandidato(pedido.idCandidato)
        Integer idEmpresaDaVaga = exigirEmpresaDaVaga(pedido.idVaga)

        registrarCurtida(pedido, idEmpresaDaVaga)
        return matchRepositorio.criarMatchSePossivel(pedido.idCandidato, idEmpresaDaVaga, pedido.idVaga) > 0
    }

    private void registrarCurtida(PedidoDeCurtida pedido, Integer idEmpresaDaVaga) {
        if (pedido.tipo == CANDIDATO) {
            curtidaRepositorio.candidatoCurtirVaga(pedido.idCandidato, pedido.idVaga)
        } else if (pedido.tipo == EMPRESA) {
            exigirEmpresaDona(pedido, idEmpresaDaVaga)
            curtidaRepositorio.empresaCurtirCandidato(pedido.idEmpresa, pedido.idCandidato, pedido.idVaga)
        } else {
            exigirVagaAtribuida(pedido)
            curtidaRepositorio.recrutadorCurtirCandidato(pedido.idRecrutador, pedido.idCandidato, pedido.idVaga)
        }
    }

    private void exigirCandidato(Integer idCandidato) {
        if (!curtidaRepositorio.candidatoExiste(idCandidato)) {
            throw new RecursoNaoEncontradoException("Candidato ${idCandidato} não encontrado")
        }
    }

    private Integer exigirEmpresaDaVaga(Integer idVaga) {
        Integer idEmpresa = curtidaRepositorio.buscarEmpresaDaVaga(idVaga)

        if (idEmpresa == null) {
            throw new RecursoNaoEncontradoException("Vaga ${idVaga} não encontrada")
        }
        return idEmpresa
    }

    private static void exigirEmpresaDona(PedidoDeCurtida pedido, Integer idEmpresaDaVaga) {
        if (pedido.idEmpresa != idEmpresaDaVaga) {
            throw new RegraDeNegocioException("A vaga ${pedido.idVaga} não pertence à empresa ${pedido.idEmpresa}")
        }
    }

    private void exigirVagaAtribuida(PedidoDeCurtida pedido) {
        if (!curtidaRepositorio.recrutadorExiste(pedido.idRecrutador)) {
            throw new RecursoNaoEncontradoException("Recrutador ${pedido.idRecrutador} não encontrado")
        }
        if (!curtidaRepositorio.vagaAtribuidaAoRecrutador(pedido.idRecrutador, pedido.idVaga)) {
            throw new RegraDeNegocioException("A vaga não está atribuída ao recrutador")
        }
    }

    private static PedidoDeCurtida lerPedido(Map<String, Object> dados) {
        String tipo = CamposDoCorpo.texto(dados, "tipo").trim()

        if (!(tipo in [CANDIDATO, EMPRESA, RECRUTADOR])) {
            throw new DadosInvalidosException("O campo 'tipo' deve ser '${CANDIDATO}', '${EMPRESA}' ou '${RECRUTADOR}'")
        }

        return new PedidoDeCurtida(
                tipo: tipo,
                idCandidato: CamposDoCorpo.exigirInteiro(dados, "idCandidato"),
                idVaga: CamposDoCorpo.exigirInteiro(dados, "idVaga"),
                idEmpresa: tipo == EMPRESA ? CamposDoCorpo.exigirInteiro(dados, "idEmpresa") : null,
                idRecrutador: tipo == RECRUTADOR ? CamposDoCorpo.exigirInteiro(dados, "idRecrutador") : null
        )
    }
}
