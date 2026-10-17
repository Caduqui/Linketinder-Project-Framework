package org.linketinder.vagas.service

import org.linketinder.vagas.DadosFicticios
import org.linketinder.vagas.model.Competencia
import org.linketinder.vagas.model.Vaga
import org.linketinder.vagas.repositorio.CompetenciaRepositorio
import org.linketinder.vagas.repositorio.EmpresaRepositorio
import org.linketinder.vagas.repositorio.Transacao
import org.linketinder.vagas.repositorio.VagaRepositorio
import spock.lang.Specification

/**
 *
 * @author Guilherme Lima Conte
 */

class VagaServiceSpec extends Specification {

    VagaRepositorio vagaRepositorio = Mock()
    CompetenciaRepositorio competenciaRepositorio = Mock()
    EmpresaRepositorio empresaRepositorio = Mock()
    Transacao transacao = Mock() {
        executar(_) >> { Closure bloco -> bloco() }
    }
    VagaService service = new VagaService(vagaRepositorio, competenciaRepositorio, empresaRepositorio, transacao)

    def setup() {
        empresaRepositorio.existe(3) >> true
        vagaRepositorio.buscarPorId(10) >> DadosFicticios.vagaGravada(10)
    }

    def "cadastra vaga e liga as competências reaproveitando a já cadastrada em qualquer caixa"() {
        given:
        competenciaRepositorio.buscarPorNomeIgnorandoCaixa("java") >> new Competencia(id: 1, nome: "Java")
        competenciaRepositorio.buscarPorNomeIgnorandoCaixa("Kotlin") >> null

        when:
        service.cadastrar(DadosFicticios.vaga(nome: "  Vaga Exemplo  ", competencias: ["java", "Kotlin", " KOTLIN ", ""]), 3)

        then:
        1 * vagaRepositorio.inserir({ Vaga v -> v.nome == "Vaga Exemplo" && v.idEmpresa == 3 }) >> { Vaga v -> v.id = 10; v }
        1 * competenciaRepositorio.inserir("Kotlin") >> new Competencia(id: 7, nome: "Kotlin")
        1 * vagaRepositorio.vincularCompetencia(10, 1)
        1 * vagaRepositorio.vincularCompetencia(10, 7)
    }

    def "empresa inexistente é regra de negócio e nada é gravado"() {
        given:
        empresaRepositorio.existe(99) >> false

        when:
        service.cadastrar(DadosFicticios.vaga(), 99)

        then:
        RegraDeNegocioException erro = thrown()
        erro.message == "Empresa 99 não encontrada"
        0 * vagaRepositorio.inserir(_)
    }

    def "campo obrigatório ausente é regra de negócio: #campo"() {
        when:
        service.cadastrar(DadosFicticios.vaga((campo): "  "), 3)

        then:
        RegraDeNegocioException erro = thrown()
        erro.message == "O campo '${campo}' é obrigatório"
        0 * vagaRepositorio.inserir(_)

        where:
        campo << ["nome", "estado", "cidade"]
    }

    def "atualiza a vaga, troca as competências e não muda a empresa"() {
        given:
        competenciaRepositorio.buscarPorNomeIgnorandoCaixa("Python") >> new Competencia(id: 5, nome: "Python")

        when:
        service.atualizar(10, DadosFicticios.vaga(nome: "Vaga Nova", idEmpresa: 4, competencias: ["Python"]))

        then:
        1 * vagaRepositorio.atualizar({ Vaga v -> v.id == 10 && v.nome == "Vaga Nova" && v.idEmpresa == null })
        1 * vagaRepositorio.desvincularCompetencias(10)
        1 * vagaRepositorio.vincularCompetencia(10, 5)
    }

    def "atualizar ou excluir vaga inexistente é recurso não encontrado"() {
        given:
        vagaRepositorio.buscarPorId(99) >> null

        when:
        service.atualizar(99, DadosFicticios.vaga())

        then:
        RecursoNaoEncontradoException erro = thrown()
        erro.message == "Vaga 99 não encontrada"

        when:
        service.excluir(99)

        then:
        thrown(RecursoNaoEncontradoException)
        0 * vagaRepositorio.excluir(_)
    }

    def "exclui vaga existente numa transação"() {
        when:
        service.excluir(10)

        then:
        1 * vagaRepositorio.excluir(10)
    }
}
