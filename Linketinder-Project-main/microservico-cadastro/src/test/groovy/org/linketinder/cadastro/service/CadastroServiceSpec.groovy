package org.linketinder.cadastro.service

import org.linketinder.cadastro.DadosFicticios
import org.linketinder.cadastro.model.Candidato
import org.linketinder.cadastro.model.Empresa
import org.linketinder.cadastro.repositorio.CandidatoRepositorio
import org.linketinder.cadastro.repositorio.EmpresaRepositorio
import spock.lang.Specification

import java.time.Clock

/**
 *
 * @author Guilherme Lima Conte
 */

class CadastroServiceSpec extends Specification {

    ValidadorDeCadastro validador = new ValidadorDeCadastro(Clock.systemDefaultZone())
    CandidatoRepositorio candidatoRepositorio = Mock(CandidatoRepositorio)
    EmpresaRepositorio empresaRepositorio = Mock(EmpresaRepositorio)

    CadastroCandidatoService candidatoService = new CadastroCandidatoService(validador, candidatoRepositorio)
    CadastroEmpresaService empresaService = new CadastroEmpresaService(validador, empresaRepositorio)

    def "candidato válido é gravado uma vez e volta com o id"() {
        when:
        Candidato candidato = candidatoService.cadastrar(DadosFicticios.candidato())

        then:
        1 * candidatoRepositorio.inserir({ Candidato recebido -> recebido.email == "candidata@example.com" }) >> { Candidato recebido ->
            recebido.id = 7
        }
        candidato.id == 7
        candidato.nome == "Candidata"
    }

    def "candidato inválido não chega ao repositório"() {
        when:
        candidatoService.cadastrar(DadosFicticios.sem(DadosFicticios.candidato(), "email"))

        then:
        thrown(DadosInvalidosException)
        0 * candidatoRepositorio.inserir(_)
    }

    def "candidato repetido é informado a quem chamou"() {
        given:
        candidatoRepositorio.inserir(_) >> { throw new CadastroRepetidoException() }

        when:
        candidatoService.cadastrar(DadosFicticios.candidato())

        then:
        CadastroRepetidoException erro = thrown()
        erro.message == "Já existe um cadastro com esses dados"
    }

    def "empresa válida é gravada uma vez e volta com o id"() {
        when:
        Empresa empresa = empresaService.cadastrar(DadosFicticios.empresa())

        then:
        1 * empresaRepositorio.inserir({ Empresa recebida -> recebida.cnpj == "12.345.678/0001-00" }) >> { Empresa recebida ->
            recebida.id = 3
        }
        empresa.id == 3
        empresa.nome == "Empresa Exemplo"
    }

    def "empresa inválida não chega ao repositório"() {
        when:
        empresaService.cadastrar(DadosFicticios.sem(DadosFicticios.empresa(), "cnpj"))

        then:
        thrown(DadosInvalidosException)
        0 * empresaRepositorio.inserir(_)
    }

    def "empresa repetida é informada a quem chamou"() {
        given:
        empresaRepositorio.inserir(_) >> { throw new CadastroRepetidoException() }

        when:
        empresaService.cadastrar(DadosFicticios.empresa())

        then:
        thrown(CadastroRepetidoException)
    }
}
