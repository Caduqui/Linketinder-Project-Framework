package org.linketinder.usuarios.service

import org.linketinder.usuarios.DadosFicticios
import org.linketinder.usuarios.model.Empresa
import org.linketinder.usuarios.model.Recrutador
import org.linketinder.usuarios.model.Vaga
import org.linketinder.usuarios.repositorio.EmpresaRepositorio
import org.linketinder.usuarios.repositorio.RecrutadorRepositorio
import org.linketinder.usuarios.repositorio.Transacao
import org.linketinder.usuarios.repositorio.VagaRepositorio
import spock.lang.Specification

/**
 *
 * @author Guilherme Lima Conte
 */

class RecrutadorServiceSpec extends Specification {

    RecrutadorRepositorio recrutadorRepositorio = Mock()
    EmpresaRepositorio empresaRepositorio = Mock()
    VagaRepositorio vagaRepositorio = Mock()
    Transacao transacao = Mock() {
        executar(_) >> { Closure bloco -> bloco() }
    }
    RecrutadorService service = new RecrutadorService(recrutadorRepositorio, empresaRepositorio, vagaRepositorio, transacao)

    def setup() {
        empresaRepositorio.buscarPorId(3) >> new Empresa(id: 3)
        recrutadorRepositorio.buscarPorId(5) >> new Recrutador(id: 5, nome: "Recrutadora", idEmpresa: 3)
        vagaRepositorio.buscarPorId(10) >> new Vaga(id: 10, idEmpresa: 3)
        vagaRepositorio.buscarPorId(20) >> new Vaga(id: 20, idEmpresa: 4)
    }

    def "cadastra recrutador válido na empresa informada"() {
        when:
        Recrutador criado = service.cadastrar(DadosFicticios.recrutador(), 3)

        then:
        1 * recrutadorRepositorio.inserir({ Recrutador r -> r.idEmpresa == 3 && r.area == "TI" && r.senha == "senha-ficticia" }) >> { Recrutador r -> r.id = 8; r }
        criado.id == 8
    }

    def "recusa cadastro com e-mail ou CPF repetido com mensagem genérica"() {
        given:
        recrutadorRepositorio.buscarIdPorEmail(_) >> donoDoEmail
        recrutadorRepositorio.buscarIdPorCpf(_) >> donoDoCpf

        when:
        service.cadastrar(DadosFicticios.recrutador(), 3)

        then:
        RegraDeNegocioException erro = thrown()
        erro.message == "Já existe um recrutador com esses dados"
        0 * recrutadorRepositorio.inserir(_)

        where:
        donoDoEmail | donoDoCpf
        7           | null
        null        | 7
    }

    def "cadastro em empresa inexistente é recurso não encontrado"() {
        when:
        service.cadastrar(DadosFicticios.recrutador(), 99)

        then:
        RecursoNaoEncontradoException erro = thrown()
        erro.message == "Empresa 99 não encontrada"
    }

    def "cadastro inválido cita o campo sem repetir o valor"() {
        when:
        service.cadastrar(DadosFicticios.recrutador(area: "Astronomia"), 3)

        then:
        RegraDeNegocioException erro = thrown()
        erro.message.contains("'area'")
        !erro.message.contains("Astronomia")
    }

    def "atualiza mantendo o próprio e-mail e CPF e sem trocar a empresa"() {
        given:
        recrutadorRepositorio.buscarIdPorEmail(_) >> 5
        recrutadorRepositorio.buscarIdPorCpf(_) >> 5

        when:
        service.atualizar(5, DadosFicticios.recrutador(nome: "Novo Nome", idEmpresa: 4))

        then:
        1 * recrutadorRepositorio.atualizar({ Recrutador r -> r.id == 5 && r.nome == "Novo Nome" && r.idEmpresa == 3 })
    }

    def "recusa atualização com e-mail de outro recrutador"() {
        given:
        recrutadorRepositorio.buscarIdPorEmail(_) >> 6

        when:
        service.atualizar(5, DadosFicticios.recrutador())

        then:
        thrown(RegraDeNegocioException)
        0 * recrutadorRepositorio.atualizar(_)
    }

    def "atualizar recrutador inexistente é recurso não encontrado"() {
        when:
        service.atualizar(99, DadosFicticios.recrutador())

        then:
        RecursoNaoEncontradoException erro = thrown()
        erro.message == "Recrutador 99 não encontrado"
    }

    def "exclui recrutador existente numa transação"() {
        when:
        service.excluir(5)

        then:
        1 * recrutadorRepositorio.excluir(5)
    }

    def "atribui vaga da própria empresa e devolve a lista atualizada"() {
        given:
        vagaRepositorio.listarAtribuidasAoRecrutador(5) >> [new Vaga(id: 10, idEmpresa: 3)]

        when:
        List<Vaga> vagas = service.atribuirVaga(5, 10)

        then:
        1 * recrutadorRepositorio.atribuirVaga(5, 10)
        vagas*.id == [10]
    }

    def "recusa atribuir vaga de outra empresa"() {
        when:
        service.atribuirVaga(5, 20)

        then:
        RegraDeNegocioException erro = thrown()
        erro.message == RecrutadorService.VAGA_DE_OUTRA_EMPRESA
        0 * recrutadorRepositorio.atribuirVaga(_, _)
    }

    def "atribuir vaga inexistente é recurso não encontrado"() {
        when:
        service.atribuirVaga(5, 99)

        then:
        RecursoNaoEncontradoException erro = thrown()
        erro.message == "Vaga 99 não encontrada"
    }

    def "remove vaga atribuída numa transação"() {
        given:
        recrutadorRepositorio.vagaAtribuida(5, 10) >> true

        when:
        service.removerVaga(5, 10)

        then:
        1 * recrutadorRepositorio.removerVaga(5, 10)
    }

    def "recusa remover vaga não atribuída"() {
        given:
        recrutadorRepositorio.vagaAtribuida(5, 10) >> false

        when:
        service.removerVaga(5, 10)

        then:
        RegraDeNegocioException erro = thrown()
        erro.message == RecrutadorService.VAGA_NAO_ATRIBUIDA
        0 * recrutadorRepositorio.removerVaga(_, _)
    }

    def "listar recrutadores de empresa inexistente é recurso não encontrado"() {
        when:
        service.listarPorEmpresa(99)

        then:
        thrown(RecursoNaoEncontradoException)
    }
}
