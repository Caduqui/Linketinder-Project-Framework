package org.linketinder.cadastro.repositorio

import org.linketinder.cadastro.model.Empresa

import java.sql.SQLException

/**
 *
 * @author Guilherme Lima Conte
 */

interface EmpresaRepositorio {
    void inserir(Empresa empresa) throws SQLException
}
