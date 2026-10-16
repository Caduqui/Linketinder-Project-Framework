package org.example.view

import org.example.model.Empresa

/**
 *
 * @author Guilherme Lima Conte
 */

class EmpresaView {

    private final EntradaDados entrada

    EmpresaView(EntradaDados entrada) {
        this.entrada = entrada
    }

    Integer lerId(String mensagem) {
        return entrada.lerId(mensagem)
    }

    Empresa lerDados() {
        String nome = entrada.lerTexto("Nome: ")
        String email = entrada.lerTexto("Email: ")
        String pais = entrada.lerTexto("País: ")
        String cep =entrada.lerTexto("CEP: ")
        String descricao = entrada.lerTexto("Descrição: ")
        String cnpj = entrada.lerTexto("CNPJ: ")
        String senha = entrada.lerSenha()

        return new Empresa(nome, email, cnpj, pais, cep, descricao, senha)
    }

    void exibir(List<Empresa> empresas) {
        empresas.each { Empresa empresa ->
            println "\n${empresa.descrever()}"
        }
    }

    void exibirNomes(List<Empresa> empresas) {
        empresas.each { Empresa empresa ->
            println empresa.descreverNome()
        }
    }

    void avisarNaoEncontrado() {
        println "Empresa não encontrada"
    }

    void avisarCadastrado() {
        println "Empresa cadastrada com sucesso!"
    }

    void avisarAtualizado() {
        println "Empresa atualizada com sucesso!"
    }

    void avisarExcluido() {
        println "Empresa excluída com sucesso!"
    }
}
