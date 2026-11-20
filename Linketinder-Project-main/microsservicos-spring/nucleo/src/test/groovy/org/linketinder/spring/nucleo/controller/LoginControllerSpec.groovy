package org.linketinder.spring.nucleo.controller

import org.linketinder.spring.nucleo.ClienteDeTeste
import org.linketinder.spring.nucleo.RespostaDeTeste
import org.linketinder.spring.nucleo.model.Candidato
import org.linketinder.spring.nucleo.model.Recrutador
import org.linketinder.spring.nucleo.repositorio.CandidatoRepositorio
import org.linketinder.spring.nucleo.repositorio.EmpresaRepositorio
import org.linketinder.spring.nucleo.repositorio.RecrutadorRepositorio
import org.linketinder.spring.nucleo.service.LoginService
import spock.lang.Specification

import java.time.LocalDate

/**
 *
 * @author Guilherme Lima Conte
 */

class LoginControllerSpec extends Specification {

    CandidatoRepositorio candidatoRepositorio = Stub()
    RecrutadorRepositorio recrutadorRepositorio = Stub()
    ClienteDeTeste cliente = new ClienteDeTeste(new LoginController(new LoginService(candidatoRepositorio, Stub(EmpresaRepositorio), recrutadorRepositorio)))

    def "POST /login com credenciais certas responde só tipo, id e nome"() {
        given:
        candidatoRepositorio.buscarPorEmail("candidata@example.com") >> new Candidato(id: 1, nome: "Candidata", senha: "senha-ficticia", cpf: "123.456.789-00")

        when:
        RespostaDeTeste resposta = cliente.post("/login", [tipo: "candidato", email: "candidata@example.com", senha: "senha-ficticia"])

        then:
        resposta.status == 200
        resposta.corpo == [tipo: "candidato", id: 1, nome: "Candidata"]
    }

    def "POST /login de recrutador leva o idEmpresa e nunca o CPF ou a data de nascimento"() {
        given:
        recrutadorRepositorio.buscarPorEmail("recrutadora@example.com") >> new Recrutador(
                id: 8, nome: "Recrutadora", senha: "senha-ficticia", idEmpresa: 2, cpf: "987.654.321-00", dataNascimento: LocalDate.of(1990, 5, 20))

        when:
        RespostaDeTeste resposta = cliente.post("/login", [tipo: "recrutador", email: "recrutadora@example.com", senha: "senha-ficticia"])

        then:
        resposta.status == 200
        resposta.corpo == [tipo: "recrutador", id: 8, nome: "Recrutadora", idEmpresa: 2]
    }

    def "POST /login com senha errada responde 401 genérico"() {
        given:
        candidatoRepositorio.buscarPorEmail("candidata@example.com") >> new Candidato(id: 1, nome: "Candidata", senha: "senha-ficticia")

        when:
        RespostaDeTeste resposta = cliente.post("/login", [tipo: "candidato", email: "candidata@example.com", senha: "senha-errada"])

        then:
        resposta.status == 401
        resposta.corpo == [erro: "E-mail ou senha inválidos"]
    }

    def "POST /login com tipo desconhecido responde 400"() {
        when:
        RespostaDeTeste resposta = cliente.post("/login", [tipo: "administrador", email: "x@example.com", senha: "senha-ficticia"])

        then:
        resposta.status == 400
        resposta.corpo == [erro: "O campo 'tipo' deve ser 'candidato', 'empresa' ou 'recrutador'"]
    }

    def "GET /login responde 405 com Allow: POST"() {
        when:
        RespostaDeTeste resposta = cliente.get("/login")

        then:
        resposta.status == 405
        resposta.cabecalho("Allow") == "POST"
        resposta.corpo == [erro: "O método GET não é suportado. Use POST"]
    }
}
