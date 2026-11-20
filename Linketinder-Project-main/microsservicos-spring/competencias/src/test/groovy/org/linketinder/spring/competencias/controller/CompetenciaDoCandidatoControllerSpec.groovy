package org.linketinder.spring.competencias.controller

import org.linketinder.spring.competencias.ClienteDeTeste
import org.linketinder.spring.competencias.RespostaDeTeste
import org.linketinder.spring.competencias.integracao.ServicoNucleo
import org.linketinder.spring.competencias.model.Competencia
import org.linketinder.spring.competencias.model.CompetenciaDaVaga
import org.linketinder.spring.competencias.model.CompetenciaDoCandidato
import org.linketinder.spring.competencias.repositorio.CompetenciaDaVagaRepositorio
import org.linketinder.spring.competencias.repositorio.CompetenciaDoCandidatoRepositorio
import org.linketinder.spring.competencias.repositorio.CompetenciaRepositorio
import org.linketinder.spring.competencias.service.CompetenciaDaVagaService
import org.linketinder.spring.competencias.service.CompetenciaDoCandidatoService
import org.linketinder.spring.competencias.service.CompetenciaService
import org.linketinder.spring.competencias.service.ServicoIndisponivelException
import spock.lang.Specification

/**
 *
 * @author Guilherme Lima Conte
 */

class CompetenciaDoCandidatoControllerSpec extends Specification {

    CompetenciaRepositorio competenciaRepositorio = Stub()
    CompetenciaDoCandidatoRepositorio doCandidatoRepositorio = Mock()
    CompetenciaDaVagaRepositorio daVagaRepositorio = Mock()
    ServicoNucleo servicoNucleo = Stub()
    CompetenciaService competenciaService = new CompetenciaService(competenciaRepositorio)
    ClienteDeTeste cliente = new ClienteDeTeste(
            new CompetenciaDoCandidatoController(new CompetenciaDoCandidatoService(competenciaService, doCandidatoRepositorio, servicoNucleo)),
            new CompetenciaDaVagaController(new CompetenciaDaVagaService(competenciaService, daVagaRepositorio)))

    Competencia java = new Competencia(id: 1, nome: "Java")

    def setup() {
        servicoNucleo.candidatoExiste(4) >> true
        competenciaRepositorio.buscarPorNomeIgnorandoCaixa(_) >> java
    }

    def "POST /candidatos/{id}/competencias responde 201 só com o id e as competências"() {
        given:
        doCandidatoRepositorio.listarDoCandidato(4) >> [new CompetenciaDoCandidato(idCandidato: 4, competencia: java, nivel: "AVANCADO")]

        when:
        RespostaDeTeste resposta = cliente.post("/candidatos/4/competencias", [competencias: [[nome: "java", nivel: "AVANCADO"]]])

        then:
        resposta.status == 201
        resposta.corpo == [id: 4, competencias: [[nome: "Java", nivel: "AVANCADO"]]]
    }

    def "POST /candidatos/{id}/competencias de candidato inexistente responde 404"() {
        given:
        servicoNucleo.candidatoExiste(9) >> false

        when:
        RespostaDeTeste resposta = cliente.post("/candidatos/9/competencias", [competencias: [[nome: "Java", nivel: "BASICO"]]])

        then:
        resposta.status == 404
        resposta.corpo == [erro: "Candidato 9 não encontrado"]
    }

    def "POST /candidatos/{id}/competencias responde 503 quando o MS Núcleo está fora do ar"() {
        given:
        servicoNucleo.candidatoExiste(7) >> { throw new ServicoIndisponivelException() }

        when:
        RespostaDeTeste resposta = cliente.post("/candidatos/7/competencias", [competencias: [[nome: "Java", nivel: "BASICO"]]])

        then:
        resposta.status == 503
        resposta.corpo == [erro: "O serviço núcleo está indisponível"]
    }

    def "GET /candidatos/competencias?ids devolve a lista de cada candidato pedido, mesmo vazia"() {
        given:
        doCandidatoRepositorio.listarDosCandidatos([4, 6]) >> [new CompetenciaDoCandidato(idCandidato: 4, competencia: java, nivel: "MEDIO")]

        when:
        RespostaDeTeste resposta = cliente.get("/candidatos/competencias?ids=4,6")

        then:
        resposta.status == 200
        resposta.corpo == [[id: 4, competencias: [[nome: "Java", nivel: "MEDIO"]]], [id: 6, competencias: []]]
    }

    def "GET /candidatos/competencias com id que não é número responde 400"() {
        when:
        RespostaDeTeste resposta = cliente.get("/candidatos/competencias?ids=4,abc")

        then:
        resposta.status == 400
        resposta.corpo == [erro: "O parâmetro 'ids' deve ser um número"]
    }

    def "DELETE /candidatos/{id}/competencias apaga e responde 204"() {
        when:
        RespostaDeTeste resposta = cliente.delete("/candidatos/4/competencias")

        then:
        1 * doCandidatoRepositorio.apagarDoCandidato(4)
        resposta.status == 204
    }

    def "PUT /vagas/{id}/competencias substitui e responde só o id e os nomes"() {
        when:
        RespostaDeTeste resposta = cliente.put("/vagas/10/competencias", [competencias: ["java"]])

        then:
        1 * daVagaRepositorio.apagarDaVaga(10)
        1 * daVagaRepositorio.save(_)
        resposta.status == 200
        resposta.corpo == [id: 10, competencias: ["Java"]]
    }

    def "GET /vagas/competencias?ids devolve as competências de cada vaga pedida"() {
        given:
        daVagaRepositorio.listarDasVagas([10, 20]) >> [new CompetenciaDaVaga(idVaga: 10, competencia: java)]

        when:
        RespostaDeTeste resposta = cliente.get("/vagas/competencias?ids=10,20")

        then:
        resposta.status == 200
        resposta.corpo == [[id: 10, competencias: ["Java"]], [id: 20, competencias: []]]
    }

    def "DELETE /vagas/{id}/competencias apaga e responde 204"() {
        when:
        RespostaDeTeste resposta = cliente.delete("/vagas/10/competencias")

        then:
        1 * daVagaRepositorio.apagarDaVaga(10)
        resposta.status == 204
    }
}
