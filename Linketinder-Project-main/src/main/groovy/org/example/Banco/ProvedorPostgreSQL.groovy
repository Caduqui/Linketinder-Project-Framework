package org.example.Banco

import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException

/**
 *
 * @author Guilherme Lima Conte
 */

class ProvedorPostgreSQL implements ProvedorConexao{
    static final String URL = "jdbc:postgresql://localhost:5432/linketinder"
    static final String USUARIO = "postgres"
    static final String SENHA = "postgres"
    static final String DRIVER = "org.postgresql.Driver"

    @Override
    Connection abrirConexao() {
        try {
            Class.forName(DRIVER)
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver ${DRIVER} não encontrado no classpath", e)
        }

        return DriverManager.getConnection(URL, USUARIO, SENHA)
    }
}
