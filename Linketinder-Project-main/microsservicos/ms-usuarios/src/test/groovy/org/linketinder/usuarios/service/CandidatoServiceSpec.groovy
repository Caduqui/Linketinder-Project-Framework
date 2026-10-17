package org.linketinder.usuarios.service

import org.linketinder.usuarios.DadosFicticios
import org.linketinder.usuarios.model.Candidato
import org.linketinder.usuarios.repositorio.CandidatoRepositorio
import org.linketinder.usuarios.repositorio.Transacao
import spock.lang.Specification

import java.time.Clock

/**
 *
 * @author Guilherme Lima Conte
 */

class CandidatoServiceSpec extends Specification {

    CandidatoRepositorio repositorio = Mock()
    Transacao transacao = Mock() {
        executar(_) >> { Closure bloco -> bloco() }
    }
    CandidatoService service = new CandidatoService(repositorio, new ValidadorDeCadastro(Clock.systemDefaultZone()), transacao)

    def "cadastra candidato válido com os valores sem espaços das pontas e sem competências"() {
        when:
        Candidato criado = service.cadastrar(DadosFicticios.candidato(nome: "  Candidata  "))

        then:
        1 * repositorio.inserir({ Candidato c -> c.nome == "Candidata" && c.senha == "senha-ficticia" }) >> { Candidato c -> c.id = 9; c }
        criado.id == 9
        criado.competencias == []
    }

    def "cadastro inválido não grava"() {
        when:
        service.cadastrar(DadosFicticios.candidato(cpf: "123"))

        then:
        RegraDeNegocioException erro = thrown()
        erro.message.contains("'cpf'")
        !erro.message.contains("123")
        0 * repositorio.inserir(_)
    }

    def "cadastro repetido sobe como cadastro repetido"() {
        given:
        repositorio.inserir(_) >> { throw new CadastroRepetidoException() }

        when:
        service.cadastrar(DadosFicticios.candidato())

        then:
        CadastroRepetidoException erro = thrown()
        erro.message == "Já existe um cadastro com esses dados"
    }

    def "buscar candidato inexistente é recurso não encontrado"() {
        given:
        repositorio.buscarPorId(99) >> null

        when:
        service.buscar(99)

        then:
        RecursoNaoEncontradoException erro = thrown()
        erro.message == "Candidato 99 não encontrado"
    }

    def "atualiza todos os dados do candidato existente"() {
        given:
        repositorio.buscarPorId(7) >> new Candidato(id: 7, nome: "Candidata", competencias: ["Java"])

        when:
        service.atualizar(7, DadosFicticios.candidato(nome: "Nova"))

        then:
        1 * repositorio.atualizar({ Candidato c -> c.id == 7 && c.nome == "Nova" && c.senha == "senha-ficticia" })
    }

    def "atualizar candidato inexistente não valida nem grava"() {
        given:
        repositorio.buscarPorId(99) >> null

        when:
        service.atualizar(99, DadosFicticios.candidato())

        then:
        thrown(RecursoNaoEncontradoException)
        0 * repositorio.atualizar(_)
    }

    def "atualização exige a senha, como o cadastro"() {
        given:
        repositorio.buscarPorId(7) >> new Candidato(id: 7)

        when:
        service.atualizar(7, DadosFicticios.sem(DadosFicticios.candidato(), "senha"))

        then:
        RegraDeNegocioException erro = thrown()
        erro.message == "O campo 'senha' é obrigatório"
        0 * repositorio.atualizar(_)
    }

    def "exclui candidato existente dentro de uma transação"() {
        given:
        repositorio.buscarPorId(7) >> new Candidato(id: 7)

        when:
        service.excluir(7)

        then:
        1 * transacao.executar(_) >> { Closure bloco -> bloco() }
        1 * repositorio.excluir(7)
    }

    def "excluir candidato inexistente é recurso não encontrado"() {
        given:
        repositorio.buscarPorId(99) >> null

        when:
        service.excluir(99)

        then:
        thrown(RecursoNaoEncontradoException)
        0 * repositorio.excluir(_)
    }
}
