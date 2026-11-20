package org.linketinder.spring.competencias.service

import org.linketinder.spring.competencias.model.Competencia
import org.linketinder.spring.competencias.repositorio.CompetenciaRepositorio
import org.springframework.dao.DataIntegrityViolationException
import spock.lang.Specification

/**
 *
 * @author Guilherme Lima Conte
 */

class CompetenciaServiceSpec extends Specification {

    CompetenciaRepositorio repositorio = Mock()
    CompetenciaService service = new CompetenciaService(repositorio)

    def "criar grava a competência com o nome sem os espaços das pontas"() {
        when:
        Competencia competencia = service.criar([nome: "  Kotlin  "])

        then:
        1 * repositorio.buscarPorNomeIgnorandoCaixa("Kotlin") >> null
        1 * repositorio.saveAndFlush({ Competencia gravada -> gravada.nome == "Kotlin" }) >> { Competencia gravada ->
            gravada.id = 11
            return gravada
        }
        competencia.id == 11
    }

    def "criar competência que já existe em outra caixa é recusado"() {
        given:
        repositorio.buscarPorNomeIgnorandoCaixa("java") >> new Competencia(id: 1, nome: "Java")

        when:
        service.criar([nome: "java"])

        then:
        RegraDeNegocioException erro = thrown()
        erro.message == "Essa competência já está cadastrada"
        0 * repositorio.saveAndFlush(_)
    }

    def "a unicidade do banco também vira competência repetida"() {
        given:
        repositorio.saveAndFlush(_) >> { throw new DataIntegrityViolationException("competencias_nome_sem_caixa") }

        when:
        service.criar([nome: "Kotlin"])

        then:
        RegraDeNegocioException erro = thrown()
        erro.message == "Essa competência já está cadastrada"
    }

    def "criar sem nome ou com nome longo demais é dado inválido"() {
        when:
        service.criar(corpo)

        then:
        DadosInvalidosException erro = thrown()
        erro.message == mensagem

        where:
        corpo               | mensagem
        [:]                 | "O campo 'nome' é obrigatório"
        [nome: "x" * 51]    | "O campo 'nome' deve ter no máximo 50 caracteres"
    }

    def "renomear pode trocar só a caixa do próprio nome"() {
        given:
        Competencia competencia = new Competencia(id: 1, nome: "java")
        repositorio.findById(1) >> Optional.of(competencia)
        repositorio.buscarPorNomeIgnorandoCaixa("Java") >> competencia

        when:
        Competencia renomeada = service.renomear(1, [nome: "Java"])

        then:
        1 * repositorio.saveAndFlush(competencia) >> competencia
        renomeada.nome == "Java"
    }

    def "buscar competência inexistente é recurso não encontrado"() {
        given:
        repositorio.findById(9) >> Optional.empty()

        when:
        service.buscar(9)

        then:
        RecursoNaoEncontradoException erro = thrown()
        erro.message == "Competência 9 não encontrada"
    }

    def "excluir competência ligada a candidato ou vaga é recusado"() {
        given:
        repositorio.findById(1) >> Optional.of(new Competencia(id: 1, nome: "Java"))
        repositorio.estaVinculada(1) >> true

        when:
        service.excluir(1)

        then:
        RegraDeNegocioException erro = thrown()
        erro.message == "Não é possível excluir a competência porque ela está relacionada a algum candidato ou vaga"
        0 * repositorio.deleteById(_)
    }

    def "excluir competência sem ligações apaga a competência"() {
        given:
        repositorio.findById(1) >> Optional.of(new Competencia(id: 1, nome: "Java"))
        repositorio.estaVinculada(1) >> false

        when:
        service.excluir(1)

        then:
        1 * repositorio.deleteById(1)
    }

    def "buscarOuCriar reaproveita a competência existente em qualquer caixa"() {
        given:
        Competencia java = new Competencia(id: 1, nome: "Java")
        repositorio.buscarPorNomeIgnorandoCaixa("JAVA") >> java

        when:
        Competencia encontrada = service.buscarOuCriar("JAVA")

        then:
        encontrada.is(java)
        0 * repositorio.save(_)
    }

    def "buscarOuCriar cria a competência inédita"() {
        given:
        repositorio.buscarPorNomeIgnorandoCaixa("Kotlin") >> null

        when:
        service.buscarOuCriar("Kotlin")

        then:
        1 * repositorio.save({ Competencia nova -> nova.nome == "Kotlin" }) >> { Competencia nova -> nova }
    }
}
