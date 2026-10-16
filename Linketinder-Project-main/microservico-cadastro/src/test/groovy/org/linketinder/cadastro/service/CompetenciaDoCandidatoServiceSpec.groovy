package org.linketinder.cadastro.service

import org.linketinder.cadastro.model.Competencia
import org.linketinder.cadastro.repositorio.CandidatoRepositorio
import org.linketinder.cadastro.repositorio.CompetenciaRepositorio
import org.linketinder.cadastro.repositorio.Transacao
import spock.lang.Specification

/**
 *
 * @author Guilherme Lima Conte
 */

class CompetenciaDoCandidatoServiceSpec extends Specification {

    static final Integer ID_CANDIDATO = 7

    CandidatoRepositorio candidatoRepositorio = Mock(CandidatoRepositorio)
    CompetenciaRepositorio competenciaRepositorio = Mock(CompetenciaRepositorio)
    Transacao transacao = Mock(Transacao)

    CompetenciaDoCandidatoService service = new CompetenciaDoCandidatoService(candidatoRepositorio, competenciaRepositorio, transacao)

    def setup() {
        transacao.executar(_) >> { Closure bloco -> bloco() }
    }

    static Competencia competencia(Integer id, String nome) {
        return new Competencia(id: id, nome: nome)
    }

    def "competências novas são criadas e vinculadas, sem vazios nem repetidos"() {
        given:
        candidatoRepositorio.existe(ID_CANDIDATO) >> true
        competenciaRepositorio.buscarDoCandidato(ID_CANDIDATO) >>> [[], [competencia(20, "Kotlin"), competencia(21, "Rust")]]
        competenciaRepositorio.buscarPorNomeIgnorandoCaixa(_) >> null

        when:
        List<String> resultado = service.adicionar(ID_CANDIDATO, [competencias: ["Kotlin", "kotlin", " Rust ", ""]])

        then:
        1 * competenciaRepositorio.inserir("Kotlin") >> competencia(20, "Kotlin")
        1 * competenciaRepositorio.inserir("Rust") >> competencia(21, "Rust")
        1 * competenciaRepositorio.vincularAoCandidato(ID_CANDIDATO, 20)
        1 * competenciaRepositorio.vincularAoCandidato(ID_CANDIDATO, 21)
        resultado == ["Kotlin", "Rust"]
    }

    def "competência que já existe em outra caixa é reaproveitada com a grafia do banco"() {
        given:
        candidatoRepositorio.existe(ID_CANDIDATO) >> true
        competenciaRepositorio.buscarDoCandidato(ID_CANDIDATO) >>> [[], [competencia(1, "Java")]]
        competenciaRepositorio.buscarPorNomeIgnorandoCaixa("java") >> competencia(1, "Java")

        when:
        List<String> resultado = service.adicionar(ID_CANDIDATO, [competencias: ["java"]])

        then:
        0 * competenciaRepositorio.inserir(_)
        1 * competenciaRepositorio.vincularAoCandidato(ID_CANDIDATO, 1)
        resultado == ["Java"]
    }

    def "competências são somadas às que o candidato já tem, sem vínculo repetido"() {
        given:
        candidatoRepositorio.existe(ID_CANDIDATO) >> true
        competenciaRepositorio.buscarDoCandidato(ID_CANDIDATO) >>> [[competencia(1, "Java")], [competencia(1, "Java"), competencia(3, "SQL")]]
        competenciaRepositorio.buscarPorNomeIgnorandoCaixa("java") >> competencia(1, "Java")
        competenciaRepositorio.buscarPorNomeIgnorandoCaixa("SQL") >> competencia(3, "SQL")

        when:
        List<String> resultado = service.adicionar(ID_CANDIDATO, [competencias: ["java", "SQL"]])

        then:
        0 * competenciaRepositorio.vincularAoCandidato(ID_CANDIDATO, 1)
        1 * competenciaRepositorio.vincularAoCandidato(ID_CANDIDATO, 3)
        resultado == ["Java", "SQL"]
    }

    def "a mesma lista enviada de novo não cria vínculo nenhum"() {
        given:
        candidatoRepositorio.existe(ID_CANDIDATO) >> true
        competenciaRepositorio.buscarDoCandidato(ID_CANDIDATO) >> [competencia(1, "Java"), competencia(3, "SQL")]
        competenciaRepositorio.buscarPorNomeIgnorandoCaixa("Java") >> competencia(1, "Java")
        competenciaRepositorio.buscarPorNomeIgnorandoCaixa("SQL") >> competencia(3, "SQL")

        when:
        List<String> resultado = service.adicionar(ID_CANDIDATO, [competencias: ["Java", "SQL"]])

        then:
        0 * competenciaRepositorio.vincularAoCandidato(_, _)
        0 * competenciaRepositorio.inserir(_)
        resultado == ["Java", "SQL"]
    }

    def "duas grafias novas da mesma competência criam uma só, com a primeira grafia"() {
        given:
        candidatoRepositorio.existe(ID_CANDIDATO) >> true
        competenciaRepositorio.buscarDoCandidato(ID_CANDIDATO) >>> [[], [competencia(30, "java")]]
        competenciaRepositorio.buscarPorNomeIgnorandoCaixa(_) >> null

        when:
        service.adicionar(ID_CANDIDATO, [competencias: ["java", "Java", " JAVA "]])

        then:
        1 * competenciaRepositorio.inserir("java") >> competencia(30, "java")
        1 * competenciaRepositorio.vincularAoCandidato(ID_CANDIDATO, 30)
    }

    def "lista #situacao é recusada citando o campo e nada é gravado"() {
        when:
        service.adicionar(ID_CANDIDATO, dados)

        then:
        DadosInvalidosException erro = thrown()
        erro.message.contains("'competencias'")
        0 * competenciaRepositorio.inserir(_)
        0 * competenciaRepositorio.vincularAoCandidato(_, _)

        where:
        situacao                     | dados
        "ausente"                    | [:]
        "vazia"                      | [competencias: []]
        "só com vazios"              | [competencias: ["", "   "]]
        "que não é lista"            | [competencias: "Java"]
        "com item que não é texto"   | [competencias: ["Java", 42]]
        "com nome de 51 caracteres"  | [competencias: ["c" * 51]]
    }

    def "nome de competência com 50 caracteres é aceito"() {
        given:
        String nome = "c" * 50
        candidatoRepositorio.existe(ID_CANDIDATO) >> true
        competenciaRepositorio.buscarDoCandidato(ID_CANDIDATO) >>> [[], [competencia(40, nome)]]
        competenciaRepositorio.buscarPorNomeIgnorandoCaixa(nome) >> null
        competenciaRepositorio.inserir(nome) >> competencia(40, nome)

        expect:
        service.adicionar(ID_CANDIDATO, [competencias: [nome]]) == [nome]
    }

    def "mensagem de nome longo demais não repete o nome enviado"() {
        given:
        String nomeLongo = "c" * 51

        when:
        service.adicionar(ID_CANDIDATO, [competencias: [nomeLongo]])

        then:
        DadosInvalidosException erro = thrown()
        !erro.message.contains(nomeLongo)
    }

    def "candidato inexistente é informado e nenhuma competência é criada"() {
        given:
        candidatoRepositorio.existe(ID_CANDIDATO) >> false

        when:
        service.adicionar(ID_CANDIDATO, [competencias: ["Java"]])

        then:
        thrown(CandidatoNaoEncontradoException)
        0 * competenciaRepositorio.inserir(_)
        0 * competenciaRepositorio.vincularAoCandidato(_, _)
    }

    def "toda a gravação acontece dentro de uma transação"() {
        when:
        service.adicionar(ID_CANDIDATO, [competencias: ["Java"]])

        then: "sem a transação rodar o bloco, nenhum repositório é tocado"
        1 * transacao.executar(_) >> ["Java"]
        0 * candidatoRepositorio._
        0 * competenciaRepositorio._
    }

    def "lista inválida é recusada antes de abrir a transação"() {
        when:
        service.adicionar(ID_CANDIDATO, [competencias: []])

        then:
        thrown(DadosInvalidosException)
        0 * transacao.executar(_)
    }
}
