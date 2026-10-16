package org.example.view

/**
 *
 * @author Guilherme Lima Conte
 */

class MenuView {

    static final String OPCAO_VOLTAR = "0"
    private final EntradaDados entrada

    MenuView(EntradaDados entrada) {
        this.entrada = entrada
    }

    String lerOpcao(String titulo, List<String> opcoes, String textoVoltar) {
        println "\n${titulo}"
        opcoes.eachWithIndex{ String opcao, int indice ->
            println "${indice + 1} - ${opcao}"
        }
        println "${OPCAO_VOLTAR} - ${textoVoltar}"

        return entrada.lerTexto("Escolha sua opção: ")
    }

    void avisarOpcaoInvalida() {
        println "Opção inválida"
    }

    void exibirMensagem(String mensagem) {
        println mensagem
    }

    void exibirErroDeBanco(String detalhe) {
        println "Erro ao acessar o banco de dados: ${detalhe}"
    }
}
