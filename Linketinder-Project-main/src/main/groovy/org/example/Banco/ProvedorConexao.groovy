package org.example.Banco

import java.sql.Connection

/**
 *
 * @author Guilherme Lima Conte
 */

interface ProvedorConexao {
    Connection abrirConexao()
}