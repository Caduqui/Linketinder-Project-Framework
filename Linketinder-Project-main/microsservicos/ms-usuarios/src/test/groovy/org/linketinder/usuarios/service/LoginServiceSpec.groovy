package org.linketinder.usuarios.service

import org.linketinder.usuarios.model.Candidato
import org.linketinder.usuarios.model.Empresa
import org.linketinder.usuarios.model.Recrutador
import org.linketinder.usuarios.repositorio.CandidatoRepositorio
import org.linketinder.usuarios.repositorio.EmpresaRepositorio
import org.linketinder.usuarios.repositorio.RecrutadorRepositorio
import spock.lang.Specification

/**
 *
 * @author Guilherme Lima Conte
 */

class LoginServiceSpec extends Specification {

    CandidatoRepositorio candidatoRepositorio = Mock()
    EmpresaRepositorio empresaRepositorio = Mock()
    RecrutadorRepositorio recrutadorRepositorio = Mock()
    LoginService service = new LoginService(candidatoRepositorio, empresaRepositorio, recrutadorRepositorio)

    def setup() {
        candidatoRepositorio.buscarParaLogin("candidata@example.com") >> new Candidato(id: 1, nome: "Candidata", senha: "senha-ficticia")
        empresaRepositorio.buscarParaLogin("contato@example.com") >> new Empresa(id: 2, nome: "Empresa Exemplo", senha: "senha-ficticia")
        recrutadorRepositorio.buscarParaLogin("recrutadora@example.com") >> new Recrutador(id: 3, nome: "Recrutadora", senha: "senha-ficticia", idEmpresa: 2)
    }

    def "autentica cada tipo e devolve só tipo, id e nome (mais idEmpresa do recrutador)"() {
        expect:
        service.autenticar(tipo, email, "senha-ficticia") == esperado

        where:
        tipo         | email                     | esperado
        "candidato"  | "candidata@example.com"   | [tipo: "candidato", id: 1, nome: "Candidata"]
        "empresa"    | "contato@example.com"     | [tipo: "empresa", id: 2, nome: "Empresa Exemplo"]
        "recrutador" | "recrutadora@example.com" | [tipo: "recrutador", id: 3, nome: "Recrutadora", idEmpresa: 2]
    }

    def "senha errada ou e-mail desconhecido dão a mesma resposta genérica"() {
        when:
        service.autenticar("candidato", email, senha)

        then:
        CredenciaisInvalidasException erro = thrown()
        erro.message == "E-mail ou senha inválidos"

        where:
        email                    | senha
        "candidata@example.com"  | "senha-errada"
        "ninguem@example.com"    | "senha-ficticia"
        "candidata@example.com"  | " senha-ficticia "
    }

    def "o e-mail é procurado só na tabela do tipo informado"() {
        when:
        service.autenticar("empresa", "candidata@example.com", "senha-ficticia")

        then:
        thrown(CredenciaisInvalidasException)
        0 * candidatoRepositorio.buscarParaLogin(_)
    }

    def "tipo fora dos três valores é regra de negócio"() {
        when:
        service.autenticar("administrador", "candidata@example.com", "senha-ficticia")

        then:
        RegraDeNegocioException erro = thrown()
        erro.message == "O campo 'tipo' deve ser 'candidato', 'empresa' ou 'recrutador'"
    }
}
