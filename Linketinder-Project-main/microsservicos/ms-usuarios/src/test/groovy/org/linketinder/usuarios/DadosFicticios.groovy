package org.linketinder.usuarios

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
                nome          : "Recrutadora Exemplo",
                area          : "TI",
                descricao     : "Pessoa inventada para os testes",
                email         : "recrutadora@example.com",
                cpf           : "987.654.321-11",
                dataNascimento: "1995-05-20",
                senha         : "senha-ficticia",
                idEmpresa     : 3
        ] + alteracoes
    }

    static Map<String, Object> sem(Map<String, Object> dados, String campo) {
        return dados.findAll { String chave, Object valor -> chave != campo }
    }
}
