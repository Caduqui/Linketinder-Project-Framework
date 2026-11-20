package org.linketinder.spring.nucleo.controller

import org.linketinder.spring.nucleo.ClienteDeTeste
import org.linketinder.spring.nucleo.DadosFicticios
import org.linketinder.spring.nucleo.RespostaDeTeste
import org.linketinder.spring.nucleo.model.Recrutador
import org.linketinder.spring.nucleo.repositorio.EmpresaRepositorio
import org.linketinder.spring.nucleo.repositorio.RecrutadorRepositorio
import org.linketinder.spring.nucleo.service.RecrutadorService
import org.linketinder.spring.nucleo.service.ValidadorDeRecrutador
import spock.lang.Specification

import java.time.Clock
import java.time.LocalDate

/**
 *
 * @author Guilherme Lima Conte
 */

class RecrutadorControllerSpec extends Specification {

    static final List<String> CAMPOS_PERMITIDOS = ["id", "nome", "area", "descricao", "email", "idEmpresa"]

    RecrutadorRepositorio recrutadorRepositorio = Mock()
    EmpresaRepositorio empresaRepositorio = Stub()
    ClienteDeTeste cliente = new ClienteDeTeste(new RecrutadorController(
            new RecrutadorService(new ValidadorDeRecrutador(Clock.systemUTC()), recrutadorRepositorio, empresaRepositorio)))

    def setup() {
        empresaRepositorio.existsById(2) >> true
    }

    def "POST /recrutadores devolve o recrutador sem CPF, data de nascimento ou senha"() {
        when:
        RespostaDeTeste resposta = cliente.post("/recrutadores", DadosFicticios.recrutador())

        then:
        1 * recrutadorRepositorio.save(_) >> { Recrutador recrutador ->
            recrutador.id = 8
            return recrutador
        }
        resposta.status == 201
        resposta.corpo.keySet() as List == CAMPOS_PERMITIDOS
        !resposta.texto.contains("987.654.321-00")
        !resposta.texto.contains("1990-05-20")
        !resposta.texto.contains("senha-ficticia")
    }

    def "POST /recrutadores repetido responde 400 genérico"() {
        given:
        recrutadorRepositorio.existeComEmailOuCpf(_, _) >> true

        when:
        RespostaDeTeste resposta = cliente.post("/recrutadores", DadosFicticios.recrutador())

        then:
        resposta.status == 400
        resposta.corpo == [erro: "Já existe um recrutador com esses dados"]
    }

    def "GET /recrutadores?idEmpresa lista os recrutadores da empresa só com os campos permitidos"() {
        given:
        recrutadorRepositorio.listarDaEmpresa(2) >> [new Recrutador(
                id: 8, nome: "Recrutadora", area: "TI", descricao: "Descrição", email: "recrutadora@example.com",
                cpf: "987.654.321-00", dataNascimento: LocalDate.of(1990, 5, 20), senha: "senha-ficticia", idEmpresa: 2)]

        when:
        RespostaDeTeste resposta = cliente.get("/recrutadores?idEmpresa=2")

        then:
        resposta.status == 200
        resposta.corpo[0].keySet() as List == CAMPOS_PERMITIDOS
    }

    def "GET /recrutadores sem idEmpresa responde 400"() {
        when:
        RespostaDeTeste resposta = cliente.get("/recrutadores")

        then:
        resposta.status == 400
        resposta.corpo == [erro: "O parâmetro 'idEmpresa' é obrigatório e deve ser um número"]
    }

    def "GET /recrutadores com idEmpresa que não é número responde 400 sem repetir o valor"() {
        when:
        RespostaDeTeste resposta = cliente.get("/recrutadores?idEmpresa=abc")

        then:
        resposta.status == 400
        resposta.corpo == [erro: "O parâmetro 'idEmpresa' deve ser um número"]
    }

    def "DELETE /recrutadores/{id} responde 204"() {
        given:
        recrutadorRepositorio.existsById(8) >> true

        when:
        RespostaDeTeste resposta = cliente.delete("/recrutadores/8")

        then:
        1 * recrutadorRepositorio.deleteById(8)
        resposta.status == 204
    }
}
