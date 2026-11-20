package org.linketinder.spring.nucleo.controller

import org.linketinder.spring.nucleo.ClienteDeTeste
import org.linketinder.spring.nucleo.DadosFicticios
import org.linketinder.spring.nucleo.RespostaDeTeste
import org.linketinder.spring.nucleo.integracao.ServicoDeCompetencias
import org.linketinder.spring.nucleo.model.Vaga
import org.linketinder.spring.nucleo.repositorio.AtribuicaoDeVagaRepositorio
import org.linketinder.spring.nucleo.repositorio.EmpresaRepositorio
import org.linketinder.spring.nucleo.repositorio.VagaRepositorio
import org.linketinder.spring.nucleo.service.AtribuicaoDeVagaService
import org.linketinder.spring.nucleo.service.ServicoIndisponivelException
import org.linketinder.spring.nucleo.service.ValidadorDeVaga
import org.linketinder.spring.nucleo.service.VagaService
import spock.lang.Specification

/**
 *
 * @author Guilherme Lima Conte
 */

class VagaControllerSpec extends Specification {

    static final List<String> CAMPOS_DA_VAGA = ["id", "nome", "descricao", "estado", "cidade", "competencias"]

    VagaRepositorio vagaRepositorio = Mock()
    EmpresaRepositorio empresaRepositorio = Stub()
    AtribuicaoDeVagaRepositorio atribuicaoRepositorio = Mock()
    ServicoDeCompetencias servicoDeCompetencias = Mock()
    VagaService vagaService = new VagaService(new ValidadorDeVaga(), vagaRepositorio, empresaRepositorio, servicoDeCompetencias)
    ClienteDeTeste cliente = new ClienteDeTeste(
            new VagaController(vagaService),
            new VagaDoRecrutadorController(new AtribuicaoDeVagaService(atribuicaoRepositorio, vagaService)))

    def setup() {
        empresaRepositorio.existsById(2) >> true
    }

    def "POST /vagas cadastra a vaga, grava as competências no MS Competências e não devolve a empresa"() {
        when:
        RespostaDeTeste resposta = cliente.post("/vagas", DadosFicticios.vaga())

        then:
        1 * vagaRepositorio.saveAndFlush(_) >> { Vaga vaga ->
            vaga.id = 10
            return vaga
        }
        1 * servicoDeCompetencias.substituirDaVaga(10, ["Java", "SQL"]) >> ["Java", "SQL"]
        resposta.status == 201
        resposta.corpo.keySet() as List == CAMPOS_DA_VAGA
        resposta.corpo.competencias == ["Java", "SQL"]
    }

    def "POST /vagas responde 503 quando o MS Competências está fora do ar"() {
        given:
        vagaRepositorio.saveAndFlush(_) >> { Vaga vaga ->
            vaga.id = 10
            return vaga
        }
        servicoDeCompetencias.substituirDaVaga(*_) >> { throw new ServicoIndisponivelException() }

        when:
        RespostaDeTeste resposta = cliente.post("/vagas", DadosFicticios.vaga())

        then:
        resposta.status == 503
        resposta.corpo == [erro: "O serviço de competências está indisponível"]
    }

    def "POST /vagas com estado inválido responde 400"() {
        when:
        RespostaDeTeste resposta = cliente.post("/vagas", DadosFicticios.vaga(estado: "Mato Grosso do Sul"))

        then:
        resposta.status == 400
        resposta.corpo == [erro: "O campo 'estado' deve ser a sigla da UF, com 2 letras"]
        0 * vagaRepositorio.saveAndFlush(_)
    }

    def "GET /vagas lista as vagas com as competências e sem a empresa"() {
        given:
        vagaRepositorio.listarTodas() >> [new Vaga(id: 1, nome: "Vaga", descricao: "", estado: "MS", cidade: "Cidade", idEmpresa: 2)]
        servicoDeCompetencias.buscarDasVagas([1]) >> [1: ["Java"]]

        when:
        RespostaDeTeste resposta = cliente.get("/vagas")

        then:
        resposta.status == 200
        resposta.corpo[0].keySet() as List == CAMPOS_DA_VAGA
        resposta.corpo[0].competencias == ["Java"]
    }

    def "GET /vagas?idEmpresa lista as vagas da empresa"() {
        given:
        vagaRepositorio.listarDaEmpresa(2) >> [new Vaga(id: 1, idEmpresa: 2)]
        servicoDeCompetencias.buscarDasVagas([1]) >> [:]

        when:
        RespostaDeTeste resposta = cliente.get("/vagas?idEmpresa=2")

        then:
        resposta.status == 200
        resposta.corpo*.id == [1]
    }

    def "PUT /vagas/{id} de outra empresa responde 400"() {
        given:
        vagaRepositorio.findById(10) >> Optional.of(new Vaga(id: 10, idEmpresa: 7))

        when:
        RespostaDeTeste resposta = cliente.put("/vagas/10", DadosFicticios.vaga())

        then:
        resposta.status == 400
        resposta.corpo == [erro: "A vaga 10 não pertence à empresa 2"]
    }

    def "DELETE /vagas/{id}?idEmpresa da dona responde 204"() {
        given:
        vagaRepositorio.findById(10) >> Optional.of(new Vaga(id: 10, idEmpresa: 2))

        when:
        RespostaDeTeste resposta = cliente.delete("/vagas/10?idEmpresa=2")

        then:
        1 * vagaRepositorio.deleteById(10)
        1 * servicoDeCompetencias.apagarDaVaga(10)
        resposta.status == 204
    }

    def "GET /recrutadores/{id}/vagas lista as vagas atribuídas ao recrutador"() {
        given:
        atribuicaoRepositorio.buscarEmpresaDoRecrutador(8) >> 2
        vagaRepositorio.listarDoRecrutador(8) >> [new Vaga(id: 10, idEmpresa: 2)]
        servicoDeCompetencias.buscarDasVagas([10]) >> [10: ["Java"]]

        when:
        RespostaDeTeste resposta = cliente.get("/recrutadores/8/vagas")

        then:
        resposta.status == 200
        resposta.corpo*.id == [10]
        resposta.corpo[0].keySet() as List == CAMPOS_DA_VAGA
    }

    def "POST /recrutadores/{id}/vagas atribui a vaga e responde 201 com as vagas do recrutador"() {
        given:
        atribuicaoRepositorio.buscarEmpresaDoRecrutador(8) >> 2
        vagaRepositorio.findById(10) >> Optional.of(new Vaga(id: 10, idEmpresa: 2))
        vagaRepositorio.listarDoRecrutador(8) >> [new Vaga(id: 10, idEmpresa: 2)]
        servicoDeCompetencias.buscarDasVagas([10]) >> [:]

        when:
        RespostaDeTeste resposta = cliente.post("/recrutadores/8/vagas", [idVaga: 10])

        then:
        1 * atribuicaoRepositorio.atribuir(8, 10)
        resposta.status == 201
        resposta.corpo*.id == [10]
    }

    def "DELETE /recrutadores/{id}/vagas/{idVaga} remove a atribuição e responde 204"() {
        given:
        atribuicaoRepositorio.buscarEmpresaDoRecrutador(8) >> 2
        vagaRepositorio.findById(10) >> Optional.of(new Vaga(id: 10, idEmpresa: 2))
        atribuicaoRepositorio.estaAtribuida(8, 10) >> true

        when:
        RespostaDeTeste resposta = cliente.delete("/recrutadores/8/vagas/10")

        then:
        1 * atribuicaoRepositorio.remover(8, 10)
        resposta.status == 204
    }
}
