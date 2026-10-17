package org.linketinder.usuarios.service

import org.linketinder.usuarios.DadosFicticios
import org.linketinder.usuarios.model.Empresa
import org.linketinder.usuarios.repositorio.EmpresaRepositorio
import spock.lang.Specification

import java.time.Clock

/**
 *
 * @author Guilherme Lima Conte
 */

class EmpresaServiceSpec extends Specification {

    EmpresaRepositorio repositorio = Mock()
    EmpresaService service = new EmpresaService(repositorio, new ValidadorDeCadastro(Clock.systemDefaultZone()))

    def "cadastra empresa válida"() {
        when:
        Empresa criada = service.cadastrar(DadosFicticios.empresa())

        then:
        1 * repositorio.inserir({ Empresa e -> e.cnpj == "12.345.678/0001-00" && e.senha == "senha-ficticia" }) >> { Empresa e -> e.id = 3; e }
        criada.id == 3
    }

    def "cadastro com CNPJ inválido não grava e não repete o valor"() {
        when:
        service.cadastrar(DadosFicticios.empresa(cnpj: "1234567890123"))

        then:
        RegraDeNegocioException erro = thrown()
        erro.message.contains("'cnpj'")
        !erro.message.contains("1234567890123")
        0 * repositorio.inserir(_)
    }

    def "atualiza empresa existente e recusa a inexistente"() {
        given:
        repositorio.buscarPorId(3) >> new Empresa(id: 3)
        repositorio.buscarPorId(99) >> null

        when:
        service.atualizar(3, DadosFicticios.empresa(nome: "Empresa Renomeada"))

        then:
        1 * repositorio.atualizar({ Empresa e -> e.id == 3 && e.nome == "Empresa Renomeada" })

        when:
        service.atualizar(99, DadosFicticios.empresa())

        then:
        RecursoNaoEncontradoException erro = thrown()
        erro.message == "Empresa 99 não encontrada"
        0 * repositorio.atualizar(_)
    }

    def "exclui empresa sem vagas e sem recrutadores"() {
        given:
        repositorio.buscarPorId(3) >> new Empresa(id: 3)
        repositorio.possuiVagas(3) >> false
        repositorio.possuiRecrutadores(3) >> false

        when:
        service.excluir(3)

        then:
        1 * repositorio.excluir(3)
    }

    def "recusa excluir empresa com #motivo"() {
        given:
        repositorio.buscarPorId(3) >> new Empresa(id: 3)
        repositorio.possuiVagas(3) >> comVagas
        repositorio.possuiRecrutadores(3) >> comRecrutadores

        when:
        service.excluir(3)

        then:
        RegraDeNegocioException erro = thrown()
        erro.message == mensagem
        0 * repositorio.excluir(_)

        where:
        motivo         | comVagas | comRecrutadores | mensagem
        "vagas"        | true     | false           | EmpresaService.EMPRESA_COM_VAGAS
        "recrutadores" | false    | true            | EmpresaService.EMPRESA_COM_RECRUTADORES
    }

    def "excluir empresa inexistente é recurso não encontrado"() {
        given:
        repositorio.buscarPorId(99) >> null

        when:
        service.excluir(99)

        then:
        thrown(RecursoNaoEncontradoException)
        0 * repositorio.excluir(_)
    }
}
