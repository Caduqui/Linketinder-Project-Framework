package org.linketinder.spring.nucleo

/**
 *
 * @author Guilherme Lima Conte
 */

class DadosFicticios {

    static Map<String, Object> candidato(Map<String, Object> alteracoes = [:]) {
        return [
                nome          : "Candidata",
                sobrenome     : "Exemplo",
                dataNascimento: "2000-01-15",
                email         : "candidata@example.com",
                cpf           : "123.456.789-00",
                pais          : "Brasil",
                cep           : "79000-000",
                descricao     : "Pessoa inventada para os testes",
                senha         : "senha-ficticia",
                formacao      : "Curso de exemplo"
        ] + alteracoes
    }

    static Map<String, Object> empresa(Map<String, Object> alteracoes = [:]) {
        return [
                nome     : "Empresa Exemplo",
                email    : "contato@example.com",
                cnpj     : "12.345.678/0001-00",
                pais     : "Brasil",
                cep      : "79000-000",
                descricao: "Empresa inventada para os testes",
                senha    : "senha-ficticia"
        ] + alteracoes
    }

    static Map<String, Object> recrutador(Map<String, Object> alteracoes = [:]) {
        return [
                nome          : "Recrutadora",
                area          : "TI",
                descricao     : "Recrutadora inventada para os testes",
                email         : "recrutadora@example.com",
                cpf           : "987.654.321-00",
                dataNascimento: "1990-05-20",
                senha         : "senha-ficticia",
                idEmpresa     : 2
        ] + alteracoes
    }

    static Map<String, Object> vaga(Map<String, Object> alteracoes = [:]) {
        return [
                nome        : "Vaga de exemplo",
                descricao   : "Vaga inventada para os testes",
                estado      : "MS",
                cidade      : "Cidade Exemplo",
                idEmpresa   : 2,
                competencias: ["Java", "SQL"]
        ] + alteracoes
    }

    static Map<String, Object> sem(Map<String, Object> dados, String campo) {
        return dados.findAll { String chave, Object valor -> chave != campo }
    }
}
