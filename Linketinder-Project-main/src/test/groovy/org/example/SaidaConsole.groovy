package org.example

/**
 *
 * @author Guilherme Lima Conte
 */

class SaidaConsole {

    static String capturar(Closure acao) {
        PrintStream saidaOriginal = System.out
        ByteArrayOutputStream saidaCapturada = new ByteArrayOutputStream()
        System.setOut(new PrintStream(saidaCapturada, true, "UTF-8"))
        try {
            acao()
        } finally {
            System.setOut(saidaOriginal)
        }
        return  saidaCapturada.toString("UTF-8")
    }
}
