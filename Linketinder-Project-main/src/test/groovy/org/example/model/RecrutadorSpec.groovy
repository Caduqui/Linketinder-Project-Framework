package org.example.model

import spock.lang.Specification

import java.time.LocalDate

/**
 *
 * @author Guilherme Lima Conte
 */
class RecrutadorSpec extends Specification {

    Recrutador criarRecrutador() {
        Recrutador recrutador = new Recrutador(
                "Ana Paula",
                "TI",
                "Líder de tecnologia",
                "ana.paula@example.com",
                "120.120.120-12",
                LocalDate.of(1990, 7, 12),
                "123456",
                1
        )
        recrutador.id = 9
        return recrutador
    }

    def "construtor preenche corretamente os campos do recrutador"() {
        when:
        Recrutador recrutador = criarRecrutador()

        then:
        recrutador.nome == "Ana Paula"
        recrutador.area == "TI"
        recrutador.descricao == "Líder de tecnologia"
        recrutador.email == "ana.paula@example.com"
        recrutador.cpf == "120.120.120-12"
        recrutador.dataNascimento == LocalDate.of(1990, 7, 12)
        recrutador.senha == "123456"
        recrutador.idEmpresa == 1
    }

    def "descrever mostra dados permitidos sem senha, CPF ou data de nascimento"() {
        when:
        String saida = criarRecrutador().descrever()

        then:
        saida.contains("ID: 9")
        saida.contains("Ana Paula")
        saida.contains("TI")
        saida.contains("ana.paula@example.com")
        !saida.contains("123456")
        !saida.contains("120.120.120-12")
        !saida.contains("1990-07-12")
    }

    def "descreverParaEmpresa não expõe dados sensíveis do recrutador"() {
        when:
        String saida = criarRecrutador().descreverParaEmpresa()

        then:
        saida.contains("Empresa: 1")
        !saida.contains("123456")
        !saida.contains("120.120.120-12")
        !saida.contains("1990-07-12")
    }

    def "enum de áreas expõe todas as opções válidas"() {
        expect:
        AreaDeRecrutamento.valores() == ["TI", "RH", "Marketing", "Segurança da Informação", "Vendas", "Financeiro", "Outros"]
        AreaDeRecrutamento.ehValida("TI")
        AreaDeRecrutamento.ehValida("Segurança da Informação")
        !AreaDeRecrutamento.ehValida("Jurídico")
    }
}
