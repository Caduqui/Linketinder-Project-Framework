package org.linketinder.spring.nucleo.controller

import org.linketinder.spring.nucleo.ClienteDeTeste
import org.linketinder.spring.nucleo.RespostaDeTeste
import org.linketinder.spring.nucleo.repositorio.CurtidaRepositorio
import org.linketinder.spring.nucleo.repositorio.MatchRepositorio
import org.linketinder.spring.nucleo.service.CurtidaService
import spock.lang.Specification

/**
 *
 * @author Guilherme Lima Conte
 */

class CurtidaControllerSpec extends Specification {

    CurtidaRepositorio curtidaRepositorio = Stub()
    MatchRepositorio matchRepositorio = Stub()
    ClienteDeTeste cliente = new ClienteDeTeste(new CurtidaController(new CurtidaService(curtidaRepositorio, matchRepositorio)))

    def setup() {
        curtidaRepositorio.candidatoExiste(1) >> true
        curtidaRepositorio.buscarEmpresaDaVaga(10) >> 2
    }

    def "POST /curtidas responde 201 só com o resultado do match"() {
        given:
        matchRepositorio.criarMatchSePossivel(1, 2, 10) >> 1

        when:
        RespostaDeTeste resposta = cliente.post("/curtidas", [tipo: "candidato", idCandidato: 1, idVaga: 10])

        then:
        resposta.status == 201
        resposta.corpo == [match: true]
    }

    def "POST /curtidas sem match responde match falso"() {
        when:
        RespostaDeTeste resposta = cliente.post("/curtidas", [tipo: "empresa", idEmpresa: 2, idCandidato: 1, idVaga: 10])

        then:
        resposta.status == 201
        resposta.corpo == [match: false]
    }

    def "POST /curtidas com vaga inexistente responde 404"() {
        given:
        curtidaRepositorio.buscarEmpresaDaVaga(99) >> null

        when:
        RespostaDeTeste resposta = cliente.post("/curtidas", [tipo: "candidato", idCandidato: 1, idVaga: 99])

        then:
        resposta.status == 404
        resposta.corpo == [erro: "Vaga 99 não encontrada"]
    }

    def "POST /curtidas com tipo desconhecido responde 400"() {
        when:
        RespostaDeTeste resposta = cliente.post("/curtidas", [tipo: "administrador", idCandidato: 1, idVaga: 10])

        then:
        resposta.status == 400
    }
}
