package org.linketinder.spring.nucleo.service

import org.linketinder.spring.nucleo.DadosFicticios
import org.linketinder.spring.nucleo.integracao.ServicoDeCompetencias
import org.linketinder.spring.nucleo.model.Candidato
import org.linketinder.spring.nucleo.model.CandidatoAnonimo
import org.linketinder.spring.nucleo.model.CompetenciaDoCandidato
import org.linketinder.spring.nucleo.repositorio.CandidatoRepositorio
import org.springframework.dao.DataIntegrityViolationException
import spock.lang.Specification

import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

/**
 *
 * @author Guilherme Lima Conte
 */

class CandidatoServiceSpec extends Specification {

    CandidatoRepositorio repositorio = Mock()
    ServicoDeCompetencias servicoDeCompetencias = Mock()
    ValidadorDeCadastro validador = new ValidadorDeCadastro(Clock.fixed(Instant.parse("2026-11-19T12:00:00Z"), ZoneOffset.UTC))
    CandidatoService service = new CandidatoService(validador, repositorio, servicoDeCompetencias)

    def "cadastrar valida os dados e grava o candidato"() {
        when:
        Candidato candidato = service.cadastrar(DadosFicticios.candidato())

        then:
        1 * repositorio.saveAndFlush({ Candidato gravado -> gravado.email == "candidata@example.com" }) >> { Candidato gravado ->
            gravado.id = 7
            return gravado
        }
        candidato.id == 7
    }

    def "cadastrar com dado inválido não grava nada"() {
        when:
        service.cadastrar(DadosFicticios.candidato(cpf: "123"))

        then:
        thrown(DadosInvalidosException)
        0 * repositorio.saveAndFlush(_)
    }

    def "cadastro que fere a unicidade do banco é cadastro repetido, sem dizer qual campo coincidiu"() {
        given:
        repositorio.saveAndFlush(_) >> { throw new DataIntegrityViolationException("candidatos_email_key") }

        when:
        service.cadastrar(DadosFicticios.candidato())

        then:
        CadastroRepetidoException erro = thrown()
        erro.message == "Já existe um cadastro com esses dados"
    }

    def "listar devolve os candidatos anônimos com as competências do MS Competências"() {
        given:
        repositorio.listarAnonimos() >> [new CandidatoAnonimo(1, "Curso A", "Descrição A"), new CandidatoAnonimo(2, "Curso B", "Descrição B")]
        servicoDeCompetencias.buscarDosCandidatos([1, 2]) >> [1: [new CompetenciaDoCandidato(nome: "Java", nivel: "AVANCADO")]]

        when:
        List<CandidatoAnonimo> candidatos = service.listar()

        then:
        candidatos*.id == [1, 2]
        candidatos[0].competencias*.nome == ["Java"]
        candidatos[1].competencias == []
    }

    def "buscar candidato inexistente é recurso não encontrado e não consulta competências"() {
        given:
        repositorio.buscarAnonimo(9) >> null

        when:
        service.buscar(9)

        then:
        RecursoNaoEncontradoException erro = thrown()
        erro.message == "Candidato 9 não encontrado"
        0 * servicoDeCompetencias._
    }

    def "atualizar grava os dados validados no candidato informado"() {
        given:
        repositorio.existsById(4) >> true

        when:
        Candidato candidato = service.atualizar(4, DadosFicticios.candidato(nome: "Outra"))

        then:
        1 * repositorio.saveAndFlush({ Candidato gravado -> gravado.id == 4 && gravado.nome == "Outra" }) >> { Candidato gravado -> gravado }
        candidato.id == 4
    }

    def "atualizar candidato inexistente não grava nada"() {
        given:
        repositorio.existsById(4) >> false

        when:
        service.atualizar(4, DadosFicticios.candidato())

        then:
        thrown(RecursoNaoEncontradoException)
        0 * repositorio.saveAndFlush(_)
    }

    def "excluir apaga as ligações, o candidato e as competências dele no MS Competências"() {
        given:
        repositorio.existsById(3) >> true

        when:
        service.excluir(3)

        then:
        1 * repositorio.apagarCompetenciasDoBancoPrincipal(3)
        1 * repositorio.apagarCurtidasDadas(3)
        1 * repositorio.apagarCurtidasDeEmpresas(3)
        1 * repositorio.apagarCurtidasDeRecrutadores(3)
        1 * repositorio.apagarMatches(3)

        then:
        1 * repositorio.deleteById(3)

        then:
        1 * servicoDeCompetencias.apagarDoCandidato(3)
    }

    def "excluir candidato inexistente não apaga nada"() {
        given:
        repositorio.existsById(3) >> false

        when:
        service.excluir(3)

        then:
        thrown(RecursoNaoEncontradoException)
        0 * repositorio.deleteById(_)
        0 * servicoDeCompetencias._
    }
}
