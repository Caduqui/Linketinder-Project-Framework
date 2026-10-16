package org.example.Banco

/**
 *
 * @author Guilherme Lima Conte
 */

enum TipoBanco {
    POSTGRESQL,
    MYSQL

    static final String VARIAVEL_AMBIENTE = "LINKETINDER_BANCO"

    static TipoBanco configurado() {
        return aPartirDoNome(System.getenv(VARIAVEL_AMBIENTE))
    }

    static TipoBanco aPartirDoNome(String nomeBanco) {
        if (nomeBanco == null || nomeBanco.trim().isEmpty()) {
            return POSTGRESQL
        }

        TipoBanco tipo = values().find {
            it.name() == nomeBanco.trim().toUpperCase()
        }

        if (tipo == null) {
            throw new IllegalArgumentException("Banco ${nomeBanco} não suportado. Use um destes: ${values().join(', ')}")
        }
        return tipo
    }


}