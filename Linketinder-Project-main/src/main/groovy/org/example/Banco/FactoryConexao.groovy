package org.example.Banco

/**
 *
 * @author Guilherme Lima Conte
 */

class FactoryConexao {

    static ProvedorConexao criar(TipoBanco tipo) {
        switch (tipo) {
            case TipoBanco.POSTGRESQL:
                return new ProvedorPostgreSQL()
            case TipoBanco.MYSQL:
                return new ProvedorMySQL()
            default:
                throw new IllegalArgumentException("Nenhum provedor de conexão para o banco ${tipo}")
        }
    }
}
