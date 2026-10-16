package org.example.view

import org.example.model.Candidato

import java.time.LocalDate

/**
 *
 * @author Guilherme Lima Conte
 */

class CandidatoView {

    private final EntradaDados entrada

    CandidatoView(EntradaDados entrada) {
        this.entrada = entrada
    }

    Integer lerId(String mensagem) {
        return entrada.lerId(mensagem)
    }

    Candidato lerDados() {
        String nome = entrada.lerTexto("Nome: ")
        String sobrenome = entrada.lerTexto("Sobrenome: ")
        LocalDate dataNascimento = entrada.lerDataNascimento()
        String email = entrada.lerTexto("Email: ")
        String cpf = entrada.lerTexto("CPF: ")
        String pais = entrada.lerTexto("País: ")
        String cep = entrada.lerTexto("CEP: ")
        String descricao = entrada.lerTexto("Descrição: ")
        String senha = entrada.lerSenha()
        String formacao = entrada.lerTexto("Formação: ")
        List<String> competencias = entrada.lerCompetencias("Competências: ")

        return new Candidato(nome, sobrenome, dataNascimento, email, cpf, pais, cep, descricao, senha, formacao, competencias)
    }

    void exibir(List<Candidato> candidatos) {
        candidatos.each {
            Candidato candidato -> println "\n${candidato.descrever()}"
        }
    }

    void exibirNomes(List<Candidato> candidatos) {
        candidatos.each {
            Candidato candidato -> println candidato.descreverNome()
        }
    }

    void exibirAnonimos(List<Candidato> candidatos) {
        println "\nCandidatos disponíveis: "
        candidatos.each {
            Candidato candidato -> println "\n${candidato.descreverAnonimo()}"
        }
    }

    void avisarNaoEncontrado() {
        println "Candidato não encontrado"
    }

    void avisarCadastrado() {
        println "Candidato cadastrado com sucesso!"
    }

    void avisarAtualizado() {
        println "Candidato atualizado com sucesso"
    }

    void avisarExcluido() {
        println "Candidato excluído com sucesso"
    }
}
