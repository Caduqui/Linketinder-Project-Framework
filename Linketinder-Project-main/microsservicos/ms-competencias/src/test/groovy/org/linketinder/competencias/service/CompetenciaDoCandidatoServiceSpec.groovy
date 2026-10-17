package org.linketinder.competencias.service

import org.linketinder.competencias.DadosFicticios
import org.linketinder.competencias.model.Candidato
import org.linketinder.competencias.model.Competencia
import org.linketinder.competencias.repositorio.CandidatoRepositorio
import org.linketinder.competencias.repositorio.CompetenciaRepositorio
import org.linketinder.competencias.repositorio.Transacao
import spock.lang.Specification

/**
 *
 * @author Guilherme Lima Conte
 */

class CompetenciaDoCandidatoServiceSpec extends Specification {

    CandidatoRepositorio candidatoRepositorio = Mock()
    CompetenciaRepositorio competenciaRepositorio = Mock()
    Transacao transacao = Mock() {
        executar(_) >> { Closure bloco -> bloco() }
    }
    CompetenciaDoCandidatoService service = new CompetenciaDoCandidatoService(candidatoRepositorio, competenciaRepositorio, transacao)

    def setup() {
        candidatoRepositorio.existe(7) >> true
        candidatoRepositorio.buscarPorId(7) >> DadosFicticios.candidata(7)
    }

    def "reaproveita a competência já cadastrada em outra caixa"() {
        given:
        competenciaRepositorio.buscarDoCandidato(7) >>> [[], [new Competencia(id: 1, nome: "Java")]]
        competenciaRepositorio.buscarPorNomeIgnorandoCaixa("java") >> new Competencia(id: 1, nome: "Java")

        when:
        Candidato candidato = service.adicionar(7, ["java"])

        then:
        0 * competenciaRepositorio.inserir(_)
        1 * competenciaRepositorio.vincularAoCandidato(7, 1)
        candidato.competencias == ["Java"]
    }

    def "cria a competência inexistente com a primeira grafia enviada e conta repetidas uma vez"() {
        given:
        competenciaRepositorio.buscarDoCandidato(7) >>> [[], [new Competencia(id: 5, nome: "java")]]
        competenciaRepositorio.buscarPorNomeIgnorandoCaixa("java") >> null

        when:
        service.adicionar(7, ["java", "Java", " JAVA "])

        then:
        1 * competenciaRepositorio.inserir("java") >> new Competencia(id: 5, nome: "java")
        1 * competenciaRepositorio.vincularAoCandidato(7, 5)
    }

    def "soma às competências que o candidato já tem sem vincular de novo"() {
        given:
        competenciaRepositorio.buscarDoCandidato(7) >>> [
                [new Competencia(id: 1, nome: "Java")],
                [new Competencia(id: 1, nome: "Java"), new Competencia(id: 2, nome: "SQL")]
        ]
        competenciaRepositorio.buscarPorNomeIgnorandoCaixa("java") >> new Competencia(id: 1, nome: "Java")
        competenciaRepositorio.buscarPorNomeIgnorandoCaixa("SQL") >> new Competencia(id: 2, nome: "SQL")

        when:
        Candidato candidato = service.adicionar(7, ["java", "SQL"])

        then:
        0 * competenciaRepositorio.vincularAoCandidato(7, 1)
        1 * competenciaRepositorio.vincularAoCandidato(7, 2)
        candidato.competencias == ["Java", "SQL"]
    }

    def "a resposta traz o cadastro do candidato sem senha"() {
        given:
        competenciaRepositorio.buscarDoCandidato(7) >> []
        competenciaRepositorio.buscarPorNomeIgnorandoCaixa(_) >> new Competencia(id: 1, nome: "Java")

        when:
        Candidato candidato = service.adicionar(7, ["Java"])

        then:
        candidato.email == "candidata@example.com"
        !candidato.hasProperty("senha")
    }

    def "lista inválida responde regra de negócio sem tocar no banco: #caso"() {
        when:
        service.adicionar(7, lista)

        then:
        RegraDeNegocioException erro = thrown()
        erro.message.contains("'competencias'")
        0 * transacao.executar(_)

        where:
        caso                 | lista
        "ausente"            | null
        "não é lista"        | "Java"
        "vazia"              | []
        "só brancos"         | ["  ", ""]
        "item não é texto"   | [42]
        "item com mais de 50" | ["x" * 51]
    }

    def "candidato inexistente é recurso não encontrado e nenhuma competência é criada"() {
        given:
        candidatoRepositorio.existe(99) >> false

        when:
        service.adicionar(99, ["Java"])

        then:
        RecursoNaoEncontradoException erro = thrown()
        erro.message == "Candidato 99 não encontrado"
        0 * competenciaRepositorio.inserir(_)
        0 * competenciaRepositorio.vincularAoCandidato(_, _)
    }

    def "falha no meio da gravação sai da transação para ser desfeita"() {
        given:
        competenciaRepositorio.buscarDoCandidato(7) >> []
        competenciaRepositorio.buscarPorNomeIgnorandoCaixa(_) >> null
        competenciaRepositorio.inserir("Java") >> new Competencia(id: 1, nome: "Java")
        competenciaRepositorio.inserir("SQL") >> { throw new IllegalStateException("falha no banco") }

        when:
        service.adicionar(7, ["Java", "SQL"])

        then:
        thrown(IllegalStateException)
    }
}
