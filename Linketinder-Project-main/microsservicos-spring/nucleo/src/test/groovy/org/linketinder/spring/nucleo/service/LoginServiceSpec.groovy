package org.linketinder.spring.nucleo.service

import org.linketinder.spring.nucleo.model.Candidato
import org.linketinder.spring.nucleo.model.Empresa
import org.linketinder.spring.nucleo.model.Recrutador
import org.linketinder.spring.nucleo.model.UsuarioLogado
import org.linketinder.spring.nucleo.repositorio.CandidatoRepositorio
import org.linketinder.spring.nucleo.repositorio.EmpresaRepositorio
import org.linketinder.spring.nucleo.repositorio.RecrutadorRepositorio
import spock.lang.Specification

/**
 *
 * @author Guilherme Lima Conte
 */

class LoginServiceSpec extends Specification {

    CandidatoRepositorio candidatoRepositorio = Stub()
    EmpresaRepositorio empresaRepositorio = Stub()
    RecrutadorRepositorio recrutadorRepositorio = Stub()
    LoginService service = new LoginService(candidatoRepositorio, empresaRepositorio, recrutadorRepositorio)

    def "candidato com credenciais certas é autenticado"() {
        given:
        candidatoRepositorio.buscarPorEmail("candidata@example.com") >> new Candidato(id: 1, nome: "Candidata", senha: "senha-ficticia")

        when:
        UsuarioLogado usuario = service.autenticar(credenciais("candidato"))

        then:
        usuario.tipo == "candidato"
        usuario.id == 1
        usuario.nome == "Candidata"
        usuario.idEmpresa == null
    }

    def "empresa com credenciais certas é autenticada"() {
        given:
        empresaRepositorio.buscarPorEmail("candidata@example.com") >> new Empresa(id: 2, nome: "Empresa Exemplo", senha: "senha-ficticia")

        expect:
        service.autenticar(credenciais("empresa")).nome == "Empresa Exemplo"
    }

    def "recrutador autenticado leva o id da empresa dele"() {
        given:
        recrutadorRepositorio.buscarPorEmail("candidata@example.com") >> new Recrutador(id: 3, nome: "Recrutadora", senha: "senha-ficticia", idEmpresa: 2)

        when:
        UsuarioLogado usuario = service.autenticar(credenciais("recrutador"))

        then:
        usuario.tipo == "recrutador"
        usuario.idEmpresa == 2
    }

    def "senha errada e e-mail inexistente dão a mesma falha genérica"() {
        given:
        candidatoRepositorio.buscarPorEmail("candidata@example.com") >> new Candidato(id: 1, nome: "Candidata", senha: "senha-ficticia")
        candidatoRepositorio.buscarPorEmail("outra@example.com") >> null

        when:
        service.autenticar(credenciais("candidato", senha: "senha-errada"))

        then:
        CredenciaisInvalidasException senhaErrada = thrown()
        senhaErrada.message == "E-mail ou senha inválidos"

        when:
        service.autenticar(credenciais("candidato", email: "outra@example.com"))

        then:
        CredenciaisInvalidasException emailInexistente = thrown()
        emailInexistente.message == senhaErrada.message
    }

    def "a senha é comparada exatamente como enviada"() {
        given:
        candidatoRepositorio.buscarPorEmail("candidata@example.com") >> new Candidato(id: 1, nome: "Candidata", senha: "senha-ficticia")

        when:
        service.autenticar(credenciais("candidato", senha: " senha-ficticia "))

        then:
        thrown(CredenciaisInvalidasException)
    }

    def "tipo fora dos três valores é dado inválido"() {
        when:
        service.autenticar(credenciais("administrador"))

        then:
        DadosInvalidosException erro = thrown()
        erro.message == "O campo 'tipo' deve ser 'candidato', 'empresa' ou 'recrutador'"
    }

    def "login sem #campo é dado inválido"() {
        when:
        service.autenticar(credenciais("candidato").findAll { String chave, Object valor -> chave != campo })

        then:
        DadosInvalidosException erro = thrown()
        erro.message == "O campo '${campo}' é obrigatório"

        where:
        campo << ["email", "senha"]
    }

    private static Map<String, Object> credenciais(Map<String, Object> alteracoes = [:], String tipo) {
        return [tipo: tipo, email: "candidata@example.com", senha: "senha-ficticia"] + alteracoes
    }
}
