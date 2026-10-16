package org.example.view

import org.example.model.Recrutador
import org.example.model.Vaga
import spock.lang.Specification

import java.time.LocalDate

/**
 *
 * @author Guilherme Lima Conte
 */
class RecrutadorViewSpec extends Specification {

    def "lista recrutadores com a empresa e sem senha CPF ou data de nascimento"() {
        given:
        Recrutador recrutador = new Recrutador(
                "Ana Paula",
                "TI",
                "Líder de tecnologia",
                "ana@example.com",
                "120.120.120-12",
                LocalDate.of(1990, 7, 12),
                "senha-secreta",
                1
        )
        recrutador.id = 9
        RecrutadorView view = new RecrutadorView(EntradaFalsa.comRespostas([:]))

        when:
        String saida = org.example.SaidaConsole.capturar {
            view.exibir([recrutador])
        }

        then:
        saida.contains("Ana Paula")
        saida.contains("TI")
        saida.contains("ana@example.com")
        saida.contains("Líder de tecnologia")
        saida.contains("Empresa: 1")
        !saida.contains("senha-secreta")
        !saida.contains("120.120.120-12")
        !saida.contains("1990-07-12")
    }

    def "lê os campos do cadastro e associa a empresa"() {
        given:
        Map<String, String> respostas = [
                "Nome: "                                  : "Ana Paula",
                "Área de recrutamento: "                  : "TI",
                "Descrição: "                             : "Líder de tecnologia",
                "Email: "                                 : "ana@example.com",
                "CPF: "                                   : "120.120.120-12",
                "Data de nascimento (AAAA-MM-DD): "       : "1990-07-12",
                "Senha: "                                 : "senha-secreta"
        ]
        RecrutadorView view = new RecrutadorView(EntradaFalsa.comRespostas(respostas))

        when:
        Recrutador recrutador = view.lerDados(1)

        then:
        recrutador.nome == "Ana Paula"
        recrutador.area == "TI"
        recrutador.descricao == "Líder de tecnologia"
        recrutador.email == "ana@example.com"
        recrutador.cpf == "120.120.120-12"
        recrutador.dataNascimento == LocalDate.of(1990, 7, 12)
        recrutador.senha == "senha-secreta"
        recrutador.idEmpresa == 1
    }

    def "lista somente vagas fornecidas ao recrutador"() {
        given:
        Vaga vaga = Mock(Vaga)
        RecrutadorView view = new RecrutadorView(EntradaFalsa.comRespostas([:]))

        when:
        String saida = org.example.SaidaConsole.capturar {
            view.exibirVagas([vaga])
        }

        then:
        1 * vaga.descrever() >> "ID: 10 | Vaga: Dev Groovy"
        saida.contains("Dev Groovy")
    }
}
