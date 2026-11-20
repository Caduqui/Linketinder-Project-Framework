package org.linketinder.spring.nucleo.controller

import org.linketinder.spring.nucleo.ClienteDeTeste
import org.linketinder.spring.nucleo.DadosFicticios
import org.linketinder.spring.nucleo.RespostaDeTeste
import org.linketinder.spring.nucleo.integracao.ServicoDeCompetencias
import org.linketinder.spring.nucleo.model.Candidato
import org.linketinder.spring.nucleo.model.CandidatoAnonimo
import org.linketinder.spring.nucleo.model.CompetenciaDoCandidato
import org.linketinder.spring.nucleo.repositorio.CandidatoRepositorio
import org.linketinder.spring.nucleo.service.CandidatoService
import org.linketinder.spring.nucleo.service.ServicoIndisponivelException
import org.linketinder.spring.nucleo.service.ValidadorDeCadastro
import org.springframework.dao.DataIntegrityViolationException
import spock.lang.Specification

import java.time.Clock

/**
 *
 * @author Guilherme Lima Conte
 */

class CandidatoControllerSpec extends Specification {

    static final List<String> CAMPOS_ANONIMOS = ["id", "formacao", "descricao", "competencias"]

    CandidatoRepositorio repositorio = Mock()
    ServicoDeCompetencias servicoDeCompetencias = Mock()
    ClienteDeTeste cliente = new ClienteDeTeste(new CandidatoController(
            new CandidatoService(new ValidadorDeCadastro(Clock.systemUTC()), repositorio, servicoDeCompetencias)))

    def "POST /candidatos cadastra e devolve o candidato sem a senha"() {
        when:
        RespostaDeTeste resposta = cliente.post("/candidatos", DadosFicticios.candidato())

        then:
        1 * repositorio.saveAndFlush(_) >> { Candidato candidato ->
            candidato.id = 7
            return candidato
        }
        resposta.status == 201
        resposta.corpo.id == 7
        resposta.corpo.nome == "Candidata"
        resposta.corpo.dataNascimento == "2000-01-15"
        !resposta.corpo.containsKey("senha")
        !resposta.texto.contains("senha-ficticia")
    }

    def "POST /candidatos repetido responde 409 sem dizer qual campo coincidiu"() {
        given:
        repositorio.saveAndFlush(_) >> { throw new DataIntegrityViolationException("candidatos_email_key") }

        when:
        RespostaDeTeste resposta = cliente.post("/candidatos", DadosFicticios.candidato())

        then:
        resposta.status == 409
        resposta.corpo == [erro: "Já existe um cadastro com esses dados"]
    }

    def "POST /candidatos com CPF inválido responde 400 citando o campo e sem repetir o valor"() {
        when:
        RespostaDeTeste resposta = cliente.post("/candidatos", DadosFicticios.candidato(cpf: "1234567890"))

        then:
        resposta.status == 400
        resposta.corpo.erro.contains("'cpf'")
        !resposta.corpo.erro.contains("1234567890")
        0 * repositorio.saveAndFlush(_)
    }

    def "GET /candidatos devolve só os campos anônimos, com as competências e o nível"() {
        given:
        repositorio.listarAnonimos() >> [new CandidatoAnonimo(1, "Curso de exemplo", "Pessoa inventada")]
        servicoDeCompetencias.buscarDosCandidatos([1]) >> [1: [new CompetenciaDoCandidato(nome: "Java", nivel: "MEDIO")]]

        when:
        RespostaDeTeste resposta = cliente.get("/candidatos")

        then:
        resposta.status == 200
        resposta.corpo.size() == 1
        resposta.corpo[0].keySet() as List == CAMPOS_ANONIMOS
        resposta.corpo[0].competencias == [[nome: "Java", nivel: "MEDIO"]]
    }

    def "GET /candidatos/{id} devolve o candidato anônimo"() {
        given:
        repositorio.buscarAnonimo(1) >> new CandidatoAnonimo(1, "Curso de exemplo", "Pessoa inventada")
        servicoDeCompetencias.buscarDosCandidatos([1]) >> [:]

        when:
        RespostaDeTeste resposta = cliente.get("/candidatos/1")

        then:
        resposta.status == 200
        resposta.corpo == [id: 1, formacao: "Curso de exemplo", descricao: "Pessoa inventada", competencias: []]
    }

    def "GET /candidatos responde 503 quando o MS Competências está fora do ar"() {
        given:
        repositorio.listarAnonimos() >> [new CandidatoAnonimo(1, "Curso", "Descrição")]
        servicoDeCompetencias.buscarDosCandidatos(_) >> { throw new ServicoIndisponivelException() }

        when:
        RespostaDeTeste resposta = cliente.get("/candidatos")

        then:
        resposta.status == 503
        resposta.corpo == [erro: "O serviço de competências está indisponível"]
    }

    def "GET /candidatos/{id} inexistente responde 404"() {
        given:
        repositorio.buscarAnonimo(9) >> null

        when:
        RespostaDeTeste resposta = cliente.get("/candidatos/9")

        then:
        resposta.status == 404
        resposta.corpo == [erro: "Candidato 9 não encontrado"]
    }

    def "HEAD /candidatos/{id} responde 200 ou 404 sem corpo, conforme o candidato existe"() {
        given:
        repositorio.existsById(1) >> true
        repositorio.existsById(9) >> false

        expect:
        cliente.head("/candidatos/1").status == 200
        cliente.head("/candidatos/9").status == 404
        cliente.head("/candidatos/9").texto == ""
    }

    def "PUT /candidatos/{id} atualiza e devolve sem a senha"() {
        given:
        repositorio.existsById(4) >> true

        when:
        RespostaDeTeste resposta = cliente.put("/candidatos/4", DadosFicticios.candidato(nome: "Outra"))

        then:
        1 * repositorio.saveAndFlush(_) >> { Candidato candidato -> candidato }
        resposta.status == 200
        resposta.corpo.id == 4
        resposta.corpo.nome == "Outra"
        !resposta.corpo.containsKey("senha")
    }

    def "DELETE /candidatos/{id} responde 204"() {
        given:
        repositorio.existsById(3) >> true

        when:
        RespostaDeTeste resposta = cliente.delete("/candidatos/3")

        then:
        1 * repositorio.deleteById(3)
        1 * servicoDeCompetencias.apagarDoCandidato(3)
        resposta.status == 204
        resposta.texto == ""
    }
}
