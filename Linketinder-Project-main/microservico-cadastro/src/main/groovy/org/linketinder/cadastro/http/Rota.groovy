package org.linketinder.cadastro.http

import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * Rota conhecida do serviço. O modelo pode ter trechos variáveis entre chaves,
 * como /candidatos/{id}/competencias.
 *
 * @author Guilherme Lima Conte
 */

class Rota {

    private static final Pattern TRECHO_VARIAVEL = ~/\{(\w+)\}/

    final String modelo
    final Map<String, Manipulador> manipuladoresPorMetodo = [:]
    private final Pattern formato
    private final List<String> nomesDosParametros

    Rota(String modelo) {
        this.modelo = modelo
        this.nomesDosParametros = (modelo =~ TRECHO_VARIAVEL).collect { List<String> trecho -> trecho[1] }
        this.formato = Pattern.compile(modelo.split("/", -1).collect { String trecho -> formatoDoTrecho(trecho) }.join("/"))
    }

    Map<String, String> extrairParametros(String caminho) {
        Matcher correspondencia = formato.matcher(caminho)
        if (!correspondencia.matches()) {
            return null
        }

        Map<String, String> parametros = [:]
        nomesDosParametros.eachWithIndex { String nome, int indice ->
            parametros[nome] = correspondencia.group(indice + 1)
        }
        return parametros
    }

    Set<String> getMetodosAceitos() {
        return manipuladoresPorMetodo.keySet()
    }

    private static String formatoDoTrecho(String trecho) {
        return trecho ==~ TRECHO_VARIAVEL ? "([^/]+)" : Pattern.quote(trecho)
    }
}
