package org.example.Banco

import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException

/**
 *
 * @author Guilherme Lima Conte
 */

class ProvedorMySQL implements ProvedorConexao {
    static final String URL = "jdbc:mysql://localhost:3306/linketinder"
    static final String USUARIO = "root"
    static final String SENHA = "root"
    static final String DRIVER = "com.mysql.cj.jdbc.Driver"

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
