package org.example.service

import org.example.model.Empresa
import org.example.model.Recrutador
import org.example.model.Vaga
import org.example.repositorio.RecrutadorRepositorio
import org.example.repositorio.VagaRepositorio
import spock.lang.Specification

import java.time.LocalDate

/**
 *
 * @author Guilherme Lima Conte
 */
class RecrutadorServiceSpec extends Specification {

    RecrutadorRepositorio recrutadorRepositorio = Mock(RecrutadorRepositorio)
    VagaRepositorio vagaRepositorio = Mock(VagaRepositorio)
    RecrutadorService recrutadorService = new RecrutadorService(recrutadorRepositorio, vagaRepositorio)

    Recrutador criarRecrutador() {
        return new Recrutador(
                "Ana Paula",
                "TI",
                "Líder de tecnologia",
                "ana.paula@example.com",
                "120.120.120-12",
                LocalDate.of(1990, 7, 12),
                "123456",
                1
        )
    }

    Vaga criarVaga(Empresa empresa = new Empresa("Alfa", "alfa@example.com", "12.345.678/0001-99", "Brasil", "01000-000", "Empresa", "123456")) {
        if (empresa.id == null) {
            empresa.id = 1
        }
        Vaga vaga = new Vaga("Dev Groovy", "Vaga de backend", "SP", "São Paulo", empresa, ["Groovy", "Java"])
        vaga.id = 10
        return vaga
    }

    def "cadastra recrutador válido"() {
        given:
        Recrutador recrutador = criarRecrutador()
        recrutadorRepositorio.buscarPorEmail(_ as String) >> null
        recrutadorRepositorio.buscarPorCpf(_ as String) >> null

        when:
        recrutadorService.cadastrar(recrutador)

        then:
        1 * recrutadorRepositorio.inserir(recrutador)
    }

    def "recusa cadastro com e-mail ou CPF repetido"() {
        given:
        Recrutador recrutador = criarRecrutador()
        recrutadorRepositorio.buscarPorEmail(recrutador.email) >> new Recrutador("Ana", "RH", "", "ana.paula@example.com", "555.555.555-55", LocalDate.of(1988, 1, 1), "123456", 1)

        when:
        recrutadorService.cadastrar(recrutador)

        then:
        RegraDeNegocioException erro = thrown()
        erro.message == "Já existe um recrutador com esses dados"
        0 * recrutadorRepositorio.inserir(_)
    }

    def "recusa cadastro com CPF repetido"() {
        given:
        Recrutador recrutador = criarRecrutador()
        Recrutador existente = new Recrutador("Bruno", "RH", "", "bruno@example.com", recrutador.cpf, LocalDate.of(1988, 1, 1), "123456", 1)
        existente.id = 2
        recrutadorRepositorio.buscarPorEmail(recrutador.email) >> null
        recrutadorRepositorio.buscarPorCpf(recrutador.cpf) >> existente

        when:
        recrutadorService.cadastrar(recrutador)

        then:
        RegraDeNegocioException erro = thrown()
        erro.message == "Já existe um recrutador com esses dados"
        0 * recrutadorRepositorio.inserir(_)
    }

    def "atualiza dados mantendo o próprio e-mail e CPF"() {
        given:
        Recrutador existente = criarRecrutador()
        existente.id = 1
        Recrutador dados = criarRecrutador()
        recrutadorRepositorio.buscarPorEmail(dados.email) >> existente
        recrutadorRepositorio.buscarPorCpf(dados.cpf) >> existente

        when:
        recrutadorService.atualizar(1, dados)

        then:
        1 * recrutadorRepositorio.atualizar(dados)
    }

    def "recusa atualização com e-mail de outro recrutador"() {
        given:
        Recrutador dados = criarRecrutador()
        Recrutador existente = new Recrutador("Bruno", "RH", "", dados.email, "555.555.555-55", LocalDate.of(1988, 1, 1), "123456", 1)
        existente.id = 2
        recrutadorRepositorio.buscarPorEmail(dados.email) >> existente
        recrutadorRepositorio.buscarPorCpf(dados.cpf) >> null

        when:
        recrutadorService.atualizar(1, dados)

        then:
        RegraDeNegocioException erro = thrown()
        erro.message == "Já existe um recrutador com esses dados"
        0 * recrutadorRepositorio.atualizar(_)
    }

    def "autentica recrutador com e-mail e senha corretos"() {
        given:
        Recrutador recrutador = criarRecrutador()
        recrutadorRepositorio.buscarPorEmail("ana.paula@example.com") >> recrutador

        expect:
        recrutadorService.autenticar("ana.paula@example.com", "123456") == recrutador
        recrutadorService.autenticar("ana.paula@example.com", "senha-errada") == null
    }

    def "atribui vaga da própria empresa"() {
        given:
        Recrutador recrutador = criarRecrutador()
        Vaga vaga = criarVaga()
        recrutadorRepositorio.buscarPorId(1) >> recrutador
        vagaRepositorio.buscarPorId(10) >> vaga
        recrutadorRepositorio.listarVagasDoRecrutador(1) >> []

        when:
        boolean atribuida = recrutadorService.atribuirVaga(1, 10)

        then:
        atribuida
        1 * recrutadorRepositorio.atribuirVaga(1, 10)
    }

    def "recusa vaga de outra empresa"() {
        given:
        Recrutador recrutador = criarRecrutador()
        Empresa empresaOutra = new Empresa("Beta", "beta@example.com", "11.111.111/0001-11", "Brasil", "20000-000", "Outra", "123456")
        empresaOutra.id = 2
        Vaga vaga = criarVaga(empresaOutra)
        recrutadorRepositorio.buscarPorId(1) >> recrutador
        vagaRepositorio.buscarPorId(10) >> vaga

        when:
        recrutadorService.atribuirVaga(1, 10)

        then:
        RegraDeNegocioException erro = thrown()
        erro.message == "A vaga não pertence à empresa do recrutador"
        0 * recrutadorRepositorio.atribuirVaga(_, _)
    }

    def "não grava atribuição repetida"() {
        given:
        Recrutador recrutador = criarRecrutador()
        Vaga vaga = criarVaga()
        recrutadorRepositorio.buscarPorId(1) >> recrutador
        vagaRepositorio.buscarPorId(10) >> vaga
        recrutadorRepositorio.listarVagasDoRecrutador(1) >> [vaga]

        when:
        boolean atribuida = recrutadorService.atribuirVaga(1, 10)

        then:
        !atribuida
        0 * recrutadorRepositorio.atribuirVaga(_, _)
    }

    def "lista vagas atribuídas ao recrutador"() {
        given:
        Vaga vaga = criarVaga()
        recrutadorRepositorio.listarVagasDoRecrutador(1) >> [vaga]

        expect:
        recrutadorService.listarVagasDoRecrutador(1) == [vaga]
    }

    def "remove vaga atribuída ao recrutador"() {
        given:
        Recrutador recrutador = criarRecrutador()
        Vaga vaga = criarVaga()
        recrutadorRepositorio.buscarPorId(1) >> recrutador
        vagaRepositorio.buscarPorId(10) >> vaga
        recrutadorRepositorio.listarVagasDoRecrutador(1) >> [vaga]

        when:
        recrutadorService.removerVaga(1, 10)

        then:
        1 * recrutadorRepositorio.removerVaga(1, 10)
    }

    def "escopo recusa vaga não atribuída"() {
        given:
        Recrutador recrutador = criarRecrutador()
        Vaga vaga = criarVaga()
        recrutadorRepositorio.buscarPorId(1) >> recrutador
        vagaRepositorio.buscarPorId(10) >> vaga
        recrutadorRepositorio.listarVagasDoRecrutador(1) >> []

        when:
        recrutadorService.validarEscopo(1, 10)

        then:
        RegraDeNegocioException erro = thrown()
        erro.message == "A vaga não está atribuída ao recrutador"
    }
}
