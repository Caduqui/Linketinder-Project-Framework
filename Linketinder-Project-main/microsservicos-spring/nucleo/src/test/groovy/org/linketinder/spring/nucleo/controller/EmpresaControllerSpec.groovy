package org.linketinder.spring.nucleo.controller

import org.linketinder.spring.nucleo.ClienteDeTeste
import org.linketinder.spring.nucleo.DadosFicticios
import org.linketinder.spring.nucleo.RespostaDeTeste
import org.linketinder.spring.nucleo.model.Empresa
import org.linketinder.spring.nucleo.repositorio.EmpresaRepositorio
import org.linketinder.spring.nucleo.service.EmpresaService
import org.linketinder.spring.nucleo.service.ValidadorDeCadastro
import spock.lang.Specification

import java.time.Clock

/**
 *
 * @author Guilherme Lima Conte
 */

class EmpresaControllerSpec extends Specification {

    EmpresaRepositorio repositorio = Mock()
    ClienteDeTeste cliente = new ClienteDeTeste(new EmpresaController(new EmpresaService(new ValidadorDeCadastro(Clock.systemUTC()), repositorio)))

    def "POST /empresas cadastra e devolve a empresa sem a senha"() {
        when:
        RespostaDeTeste resposta = cliente.post("/empresas", DadosFicticios.empresa())

        then:
        1 * repositorio.saveAndFlush(_) >> { Empresa empresa ->
            empresa.id = 3
            return empresa
        }
        resposta.status == 201
        resposta.corpo.id == 3
        resposta.corpo.cnpj == "12.345.678/0001-00"
        !resposta.corpo.containsKey("senha")
        !resposta.texto.contains("senha-ficticia")
    }

    def "GET /empresas não existe e responde 405"() {
        when:
        RespostaDeTeste resposta = cliente.get("/empresas")

        then:
        resposta.status == 405
        resposta.cabecalho("Allow") == "POST"
    }

    def "PUT /empresas/{id} de empresa inexistente responde 404"() {
        given:
        repositorio.existsById(9) >> false

        when:
        RespostaDeTeste resposta = cliente.put("/empresas/9", DadosFicticios.empresa())

        then:
        resposta.status == 404
        resposta.corpo == [erro: "Empresa 9 não encontrada"]
    }

    def "DELETE /empresas/{id} de empresa com vagas responde 400"() {
        given:
        repositorio.existsById(3) >> true
        repositorio.possuiVagas(3) >> true

        when:
        RespostaDeTeste resposta = cliente.delete("/empresas/3")

        then:
        resposta.status == 400
        resposta.corpo == [erro: "Não é possível excluir a empresa porque ela possui vagas cadastradas"]
        0 * repositorio.deleteById(_)
    }

    def "DELETE /empresas/{id} sem dependentes responde 204"() {
        given:
        repositorio.existsById(3) >> true

        when:
        RespostaDeTeste resposta = cliente.delete("/empresas/3")

        then:
        1 * repositorio.deleteById(3)
        resposta.status == 204
    }
}
