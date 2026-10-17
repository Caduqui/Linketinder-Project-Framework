package org.linketinder.competencias

/**
 *
 * @author Guilherme Lima Conte
 */

class VerificadorDeAmbiente {

    static final String VARIAVEL_SENHA_DO_BANCO = "LINKETINDER_BANCO_SENHA"

    static void exigirSenhaDoBanco(Map<String, String> ambiente) {
        String senha = ambiente[VARIAVEL_SENHA_DO_BANCO]

        if (senha == null || senha.trim().isEmpty()) {
            throw new ConfiguracaoInvalidaException(
                    "O serviço não pode subir: defina a variável de ambiente ${VARIAVEL_SENHA_DO_BANCO} com a senha do banco")
        }
    }
}
