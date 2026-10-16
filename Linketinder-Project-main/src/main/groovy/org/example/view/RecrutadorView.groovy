package org.example.view

import org.example.model.Recrutador
import org.example.model.Vaga

import java.time.LocalDate

/**
 *
 * @author Guilherme Lima Conte
 */
class RecrutadorView {

    private final EntradaDados entrada

    RecrutadorView(EntradaDados entrada) {
        this.entrada = entrada
    }

    Integer lerId(String mensagem) {
        return entrada.lerId(mensagem)
    }

    Recrutador lerDados(Integer idEmpresa) {
        String nome = entrada.lerTexto("Nome: ")
        String area = entrada.lerTexto("Área de recrutamento: ")
        String descricao = entrada.lerTexto("Descrição: ")
        String email = entrada.lerTexto("Email: ")
        String cpf = entrada.lerTexto("CPF: ")
        LocalDate dataNascimento = entrada.lerDataNascimento()
        String senha = entrada.lerSenha()

        return new Recrutador(nome, area, descricao, email, cpf, dataNascimento, senha, idEmpresa)
    }

    void exibir(List<Recrutador> recrutadores) {
        recrutadores.each { Recrutador recrutador ->
            println "\n${recrutador.descreverParaEmpresa()}"
        }
    }

    void exibirVagas(List<Vaga> vagas) {
        vagas.each { Vaga vaga ->
            println "\n${vaga.descrever()}"
        }
    }

    void avisarNaoEncontrado() {
        println "Recrutador não encontrado"
    }

    void avisarSemVagasAtribuidas() {
        println "Esse recrutador não possui vagas atribuídas"
    }

    void avisarCadastrado() {
        println "Recrutador cadastrado com sucesso!"
    }

    void avisarAtualizado() {
        println "Recrutador atualizado com sucesso!"
    }

    void avisarExcluido() {
        println "Recrutador excluído com sucesso!"
    }

    void avisarVagaAtribuida() {
        println "Vaga atribuída ao recrutador com sucesso!"
    }

    void avisarVagaJaAtribuida() {
        println "Essa vaga já está atribuída ao recrutador"
    }

    void avisarVagaRemovida() {
        println "Vaga removida do recrutador com sucesso!"
    }
}
