package org.linketinder.competencias.service

import org.linketinder.competencias.model.Competencia
import org.linketinder.competencias.repositorio.CompetenciaRepositorio
import spock.lang.Specification

/**
 *
 * @author Guilherme Lima Conte
 */

class CatalogoDeCompetenciasServiceSpec extends Specification {

    CompetenciaRepositorio repositorio = Mock()
    CatalogoDeCompetenciasService catalogo = new CatalogoDeCompetenciasService(repositorio)

    def "cadastra competência nova sem os espaços das pontas"() {
        when:
        Competencia criada = catalogo.cadastrar("  Kotlin  ")

        then:
        1 * repositorio.buscarPorNomeIgnorandoCaixa("Kotlin") >> null
        1 * repositorio.inserir("Kotlin") >> new Competencia(id: 9, nome: "Kotlin")
        criada.id == 9
    }

    def "recusa competência que já existe em qualquer caixa"() {
        given:
        repositorio.buscarPorNomeIgnorandoCaixa("JAVA") >> new Competencia(id: 1, nome: "Java")

        when:
        catalogo.cadastrar("JAVA")

        then:
        CadastroRepetidoException erro = thrown()
        erro.message == "Essa competência já está cadastrada"
        0 * repositorio.inserir(_)
    }

    def "recusa nome inválido sem gravar: #caso"() {
        when:
        catalogo.cadastrar(nome)

        then:
        RegraDeNegocioException erro = thrown()
        erro.message.contains("'nome'")
        0 * repositorio.inserir(_)

        where:
        caso             | nome
        "ausente"        | null
        "vazio"          | "   "
        "não é texto"    | 42
        "mais de 50"     | "x" * 51
    }

    def "aceita nome com exatamente 50 caracteres"() {
        given:
        String nome = "x" * 50

        when:
        catalogo.cadastrar(nome)

        then:
        1 * repositorio.inserir(nome) >> new Competencia(id: 3, nome: nome)
    }

    def "renomeia competência existente"() {
        given:
        repositorio.buscarPorId(4) >> new Competencia(id: 4, nome: "Cobol")

        when:
        Competencia renomeada = catalogo.renomear(4, "COBOL Moderno")

        then:
        1 * repositorio.renomear(4, "COBOL Moderno")
        renomeada.id == 4
        renomeada.nome == "COBOL Moderno"
    }

    def "renomear mudando só a caixa da própria competência é permitido"() {
        given:
        repositorio.buscarPorId(4) >> new Competencia(id: 4, nome: "cobol")
        repositorio.buscarPorNomeIgnorandoCaixa("Cobol") >> new Competencia(id: 4, nome: "cobol")

        when:
        catalogo.renomear(4, "Cobol")

        then:
        1 * repositorio.renomear(4, "Cobol")
    }

    def "renomear para o nome de outra competência é cadastro repetido"() {
        given:
        repositorio.buscarPorId(4) >> new Competencia(id: 4, nome: "Cobol")
        repositorio.buscarPorNomeIgnorandoCaixa("java") >> new Competencia(id: 1, nome: "Java")

        when:
        catalogo.renomear(4, "java")

        then:
        thrown(CadastroRepetidoException)
        0 * repositorio.renomear(_, _)
    }

    def "renomear competência inexistente é recurso não encontrado"() {
        given:
        repositorio.buscarPorId(99) >> null

        when:
        catalogo.renomear(99, "Qualquer")

        then:
        RecursoNaoEncontradoException erro = thrown()
        erro.message == "Competência 99 não encontrada"
    }

    def "exclui competência sem vínculo"() {
        given:
        repositorio.buscarPorId(4) >> new Competencia(id: 4, nome: "Cobol")
        repositorio.estaVinculada(4) >> false

        when:
        catalogo.excluir(4)

        then:
        1 * repositorio.excluir(4)
    }

    def "recusa excluir competência ligada a candidato ou vaga"() {
        given:
        repositorio.buscarPorId(1) >> new Competencia(id: 1, nome: "Java")
        repositorio.estaVinculada(1) >> true

        when:
        catalogo.excluir(1)

        then:
        RegraDeNegocioException erro = thrown()
        erro.message == CatalogoDeCompetenciasService.COMPETENCIA_VINCULADA
        0 * repositorio.excluir(_)
    }

    def "excluir competência inexistente é recurso não encontrado"() {
        given:
        repositorio.buscarPorId(99) >> null

        when:
        catalogo.excluir(99)

        then:
        thrown(RecursoNaoEncontradoException)
        0 * repositorio.excluir(_)
    }
}
