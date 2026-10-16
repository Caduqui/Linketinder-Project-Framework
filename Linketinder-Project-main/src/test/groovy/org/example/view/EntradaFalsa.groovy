package org.example.view
/**
 *
 * @author Guilherme Lima Conte
 */

class EntradaFalsa {

    static EntradaDados comRespostas(Map<String, String> respostasPorPergunta) {
        LeitorEntrada leitor = [lerLinha: { String pergunta -> respostasPorPergunta[pergunta] }] as LeitorEntrada
        return new EntradaDados(leitor)
    }

}
