package org.linketinder.cadastro

/**
 *
 * @author Guilherme Lima Conte
 */

class Configuracao {

    static final String VARIAVEL_PORTA = "LINKETINDER_CADASTRO_PORTA"
    static final String VARIAVEL_BANCO_URL = "LINKETINDER_CADASTRO_BANCO_URL"
    static final String VARIAVEL_BANCO_USUARIO = "LINKETINDER_CADASTRO_BANCO_USUARIO"
    static final String VARIAVEL_BANCO_SENHA = "LINKETINDER_CADASTRO_BANCO_SENHA"

    static final String PORTA_PADRAO = "8081"
    static final String BANCO_URL_PADRAO = "jdbc:postgresql://localhost:5432/linketinder"
    static final String BANCO_USUARIO_PADRAO = "postgres"

    final int porta
    final String bancoUrl
    final String bancoUsuario
    final String bancoSenha

    private Configuracao(int porta, String bancoUrl, String bancoUsuario, String bancoSenha) {
        this.porta = porta
        this.bancoUrl = bancoUrl
        this.bancoUsuario = bancoUsuario
        this.bancoSenha = bancoSenha
    }

    static Configuracao doAmbiente() {
        return aPartirDe(System.getenv())
    }

    static Configuracao aPartirDe(Map<String, String> ambiente) {
        return new Configuracao(
                lerPorta(ambiente),
                valorOuPadrao(ambiente, VARIAVEL_BANCO_URL, BANCO_URL_PADRAO),
                valorOuPadrao(ambiente, VARIAVEL_BANCO_USUARIO, BANCO_USUARIO_PADRAO),
                exigirSenhaDoBanco(ambiente)
        )
    }

    @Override
    String toString() {
        return "Configuracao(porta: ${porta}, bancoUrl: ${bancoUrl}, bancoUsuario: ${bancoUsuario})"
    }

    private static int lerPorta(Map<String, String> ambiente) {
        try {
            return Integer.parseInt(valorOuPadrao(ambiente, VARIAVEL_PORTA, PORTA_PADRAO))
        } catch (NumberFormatException e) {
            throw new ConfiguracaoInvalidaException("A variável de ambiente ${VARIAVEL_PORTA} deve ser um número de porta")
        }
    }

    private static String exigirSenhaDoBanco(Map<String, String> ambiente) {
        String senha = ambiente[VARIAVEL_BANCO_SENHA]

        if (senha == null || senha.trim().isEmpty()) {
            throw new ConfiguracaoInvalidaException("Defina a variável de ambiente ${VARIAVEL_BANCO_SENHA} com a senha do banco de dados")
        }
        return senha
    }

    private static String valorOuPadrao(Map<String, String> ambiente, String variavel, String padrao) {
        String valor = ambiente[variavel]
        return valor == null || valor.trim().isEmpty() ? padrao : valor.trim()
    }
}
