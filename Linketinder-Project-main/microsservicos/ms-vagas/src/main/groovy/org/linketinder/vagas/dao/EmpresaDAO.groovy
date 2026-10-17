package org.linketinder.vagas.dao

import jakarta.inject.Singleton
import org.linketinder.vagas.banco.ConexaoBanco
import org.linketinder.vagas.repositorio.EmpresaRepositorio

import java.sql.PreparedStatement
import java.sql.ResultSet

/**
 *
 * @author Guilherme Lima Conte
 */

@Singleton
class EmpresaDAO implements EmpresaRepositorio {

    private final ConexaoBanco conexaoBanco

    EmpresaDAO(ConexaoBanco conexaoBanco) {
        this.conexaoBanco = conexaoBanco
    }

    @Override
    boolean existe(Integer id) {
        return conexaoBanco.executar("SELECT EXISTS (SELECT 1 FROM empresas WHERE id = ?)") { PreparedStatement statement ->
            statement.setInt(1, id)
            statement.executeQuery().withCloseable { ResultSet resultado ->
                resultado.next() && resultado.getBoolean(1)
            }
        }
    }
}
