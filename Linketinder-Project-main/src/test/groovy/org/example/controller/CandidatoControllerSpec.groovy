package org.example.controller


import org.example.model.Candidato
import org.example.service.CandidatoService
import org.example.view.CandidatoView
import spock.lang.Specification

import java.time.LocalDate

/**
 *
 * @author Guilherme Lima Conte
 */

class CandidatoControllerSpec extends Specification {

    CandidatoService candidatoService = Mock(CandidatoService)
    CandidatoView candidatoView = Mock(CandidatoView)
    CandidatoController candidatoController = new CandidatoController(candidatoService, candidatoView)

    Candidato candidato(Integer id) {
        Candidato candidato = new Candidato(
                "Ana",
                "Silva",
                LocalDate.of(2003, 4, 21),
                "ana@gmail.com",
                "111", "Brasil",
                "0",
                "d",
                "123456",
                "f", [])
        candidato.id = id
        return candidato
    }


    def "cadastrar pede os dados à view e entrega ao service"() {
        given:
        Candidato digitado = candidato(null)
        candidatoView.lerDados() >> digitado

        when:
        boolean cadastrou = candidatoController.cadastrar()

        then:
        1 * candidatoService.cadastrar(digitado)
        1 * candidatoView.avisarCadastrado()
        cadastrou
    }

    def "atualizar usa o ID do candidato escolhido"() {
        given:
        candidatoView.lerId(_) >> 1
        candidatoService.buscarPorId(1) >> candidato(1)
        candidatoView.lerDados() >> candidato(null)

        when:
        boolean atualizou = candidatoController.atualizar()

        then:
        1 * candidatoService.atualizar(1, _)
        atualizou
    }

    def "atualizar não faz nada quando o candidato não existe"() {
        given:
        candidatoView.lerId(_) >> 99
        candidatoService.buscarPorId(99) >> null

        when:
        boolean atualizou = candidatoController.atualizar()

        then:
        1 * candidatoView.avisarNaoEncontrado()
        0 * candidatoService.atualizar(_, _)
        !atualizou
    }

    def "excluir não faz nada quando o ID digitado é inválido"() {
        given:
        candidatoView.lerId(_) >> null

        when:
        boolean excluiu = candidatoController.excluir()

        then:
        0 * candidatoService.buscarPorId(_)
        0 * candidatoService.excluir(_)
        !excluiu
    }

    def "Listar pede a lista ao service e entrega à view"() {
        given:
        List<Candidato> candidatos = [candidato(1)]
        candidatoService.listar() >> candidatos

        when:
        candidatoController.listar()

        then:
        1 * candidatoView.exibir(candidatos)
    }
}
