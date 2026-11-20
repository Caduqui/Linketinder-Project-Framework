package org.linketinder.spring.nucleo.service

import org.linketinder.spring.nucleo.DadosFicticios
import org.linketinder.spring.nucleo.model.Empresa
import org.linketinder.spring.nucleo.repositorio.EmpresaRepositorio
import org.springframework.dao.DataIntegrityViolationException
import spock.lang.Specification

import java.time.Clock

/**
 *
 * @author Guilherme Lima Conte
 */

class EmpresaServiceSpec extends Specification {

    EmpresaRepositorio repositorio = Mock()
    EmpresaService service = new EmpresaService(new ValidadorDeCadastro(Clock.systemUTC()), repositorio)

    def "cadastrar valida os dados e grava a empresa"() {
        when:
        Empresa empresa = service.cadastrar(DadosFicticios.empresa())

        then:
        1 * repositorio.saveAndFlush({ Empresa gravada -> gravada.cnpj == "12.345.678/0001-00" }) >> { Empresa gravada ->
            gravada.id = 3
            return gravada
        }
        empresa.id == 3
    }

    def "cadastrar com CNPJ inválido não grava nada"() {
        when:
        service.cadastrar(DadosFicticios.empresa(cnpj: "123"))

        then:
        DadosInvalidosException erro = thrown()
        erro.message.contains("'cnpj'")
        0 * repositorio.saveAndFlush(_)
    }

    def "cadastro que fere a unicidade do banco é cadastro repetido"() {
        given:
        repositorio.saveAndFlush(_) >> { throw new DataIntegrityViolationException("empresas_cnpj_key") }

        when:
        service.cadastrar(DadosFicticios.empresa())

        then:
        thrown(CadastroRepetidoException)
    }

    def "atualizar grava os dados validados na empresa informada"() {
        given:
        repositorio.existsById(3) >> true

        when:
        Empresa empresa = service.atualizar(3, DadosFicticios.empresa(nome: "Outro nome"))

        then:
        1 * repositorio.saveAndFlush({ Empresa gravada -> gravada.id == 3 && gravada.nome == "Outro nome" }) >> { Empresa gravada -> gravada }
        empresa.id == 3
    }

    def "atualizar empresa inexistente não grava nada"() {
        given:
        repositorio.existsById(3) >> false

        when:
        service.atualizar(3, DadosFicticios.empresa())

        then:
        RecursoNaoEncontradoException erro = thrown()
        erro.message == "Empresa 3 não encontrada"
        0 * repositorio.saveAndFlush(_)
    }

    def "excluir apaga a empresa sem vagas nem recrutadores"() {
        given:
        repositorio.existsById(3) >> true
        repositorio.possuiVagas(3) >> false
        repositorio.possuiRecrutadores(3) >> false

        when:
        service.excluir(3)

        then:
        1 * repositorio.deleteById(3)
    }

    def "excluir empresa com #motivo é recusado"() {
        given:
        repositorio.existsById(3) >> true
        repositorio.possuiVagas(3) >> comVagas
        repositorio.possuiRecrutadores(3) >> comRecrutadores

        when:
        service.excluir(3)

        then:
        RegraDeNegocioException erro = thrown()
        erro.message == mensagem
        0 * repositorio.deleteById(_)

        where:
        motivo         | comVagas | comRecrutadores | mensagem
        "vagas"        | true     | false           | "Não é possível excluir a empresa porque ela possui vagas cadastradas"
        "recrutadores" | false    | true            | "Não é possível excluir a empresa porque ela possui recrutadores cadastrados"
    }

    def "excluir empresa inexistente é recurso não encontrado"() {
        given:
        repositorio.existsById(3) >> false

        when:
        service.excluir(3)

        then:
        thrown(RecursoNaoEncontradoException)
        0 * repositorio.deleteById(_)
    }
}
