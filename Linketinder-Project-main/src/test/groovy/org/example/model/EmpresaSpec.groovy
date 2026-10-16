package org.example.model

import spock.lang.Specification

/**
 *
 * @author Guilherme Lima Conte
 */

class EmpresaSpec extends Specification {

    Empresa criarEmpresa() {
        Empresa empresa = new Empresa(
                "DataWise",
                "jobs@datawise.com",
                "44.444.444/0001-44",
                "Brasil",
                "30130-000",
                "Consultoria de dados",
                "123456")
        empresa.id = 3
        return empresa
    }

    def "o construtor preenche todos os campos"() {
        when:
        Empresa empresa = criarEmpresa()

        then:
        empresa.nome == "DataWise"
        empresa.email == "jobs@datawise.com"
        empresa.cnpj == "44.444.444/0001-44"
        empresa.pais == "Brasil"
        empresa.cep == "30130-000"
        empresa.descricao == "Consultoria de dados"
        empresa.senha == "123456"
    }

    def "Empresa é uma pessoa e estende TipoPessoa"() {
        expect:
        criarEmpresa() instanceof Pessoa
        criarEmpresa() instanceof TipoPessoa
    }

    def "descrever mostra ID, nome e CNPJ"() {
        when:
        String saida = criarEmpresa().descrever()

        then:
        saida.contains("ID: 3")
        saida.contains("DataWise")
        saida.contains("44.444.444/0001-44")
    }
}
