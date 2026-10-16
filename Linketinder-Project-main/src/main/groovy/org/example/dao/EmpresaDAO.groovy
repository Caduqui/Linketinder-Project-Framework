package org.example.dao

import org.example.Banco.ConexaoBanco
import org.example.model.Empresa
import org.example.repositorio.EmpresaRepositorio

import java.sql.PreparedStatement
import java.sql.ResultSet

/**
 *
 * @author Guilherme Lima Conte
 */

class EmpresaDAO implements EmpresaRepositorio {

    final ConexaoBanco conexaoBanco

    EmpresaDAO(ConexaoBanco conexaoBanco) {
        this.conexaoBanco = conexaoBanco
    }

    @Override
    void inserir(Empresa empresa) {
        String sql = """
            INSERT INTO empresas (nome, email, cnpj, pais, cep, descricao, senha)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            RETURNING id
        """

        conexaoBanco.executar(sql) { PreparedStatement statement ->
            preencherDados(statement, empresa)
            statement.executeQuery().withCloseable { ResultSet resultado ->
                if (resultado.next()) {
                    empresa.id = resultado.getInt("id")
                }
            }
        }
    }

    @Override
    List<Empresa> listar() {
        String sql = """
            SELECT * FROM empresas ORDER BY id
        """

        conexaoBanco.executar(sql) { PreparedStatement statement ->
            statement.executeQuery().withCloseable { ResultSet resultado ->
                List<Empresa> empresas = []
                while (resultado.next()) {
                    empresas << mapearEmpresa(resultado)
                }
                return empresas
            }
        }
    }

    @Override
    void atualizar(Empresa empresa) {
        String sql = """
            UPDATE empresas 
                SET nome = ?, email = ?, cnpj = ?, pais = ?, cep = ?, descricao = ?, senha = ?
                WHERE id = ?
            """

        conexaoBanco.executar(sql) { PreparedStatement statement ->
            preencherDados(statement, empresa)
            statement.setInt(8, empresa.id)
            statement.executeUpdate()
        }
    }

    @Override
    void deletar(Integer id) {
        conexaoBanco.executar("DELETE FROM empresas WHERE id = ?") { PreparedStatement statement ->
            statement.setInt(1, id)
            statement.executeUpdate()
        }
    }

    @Override
    Empresa buscarPorId(Integer id) {
        String sql = """
            SELECT * FROM empresas WHERE id = ?
        """

        conexaoBanco.executar(sql) { PreparedStatement statement ->
            statement.setInt(1, id)
            statement.executeQuery().withCloseable { ResultSet resultado ->
                resultado.next() ? mapearEmpresa(resultado) : null
            }
        }
    }

    @Override
    Empresa buscarPorEmail(String email) {
        String sql = """
            SELECT * FROM empresas WHERE email = ?
        """

        conexaoBanco.executar(sql) { PreparedStatement statement ->
            statement.setString(1, email)
            statement.executeQuery().withCloseable { ResultSet resultado ->
                resultado.next() ? mapearEmpresa(resultado) : null
            }
        }
    }

    @Override
    boolean possuiVagas(Integer idEmpresa) {

        String sql = """
            SELECT EXISTS 
                (SELECT 1 FROM vagas WHERE id_empresa = ?) AS possui
        """

        conexaoBanco.executar(sql) { PreparedStatement statement ->
            statement.setInt(1, idEmpresa)
            statement.executeQuery().withCloseable { ResultSet resultado ->
                resultado.next() && resultado.getBoolean("possui")
            }
        }
    }

    @Override
    boolean possuiRecrutadores(Integer idEmpresa) {
        String sql = """
            SELECT EXISTS
                (SELECT 1 FROM recrutadores WHERE id_empresa = ?) AS possui
        """

        conexaoBanco.executar(sql) { PreparedStatement statement ->
            statement.setInt(1, idEmpresa)
            statement.executeQuery().withCloseable { ResultSet resultado ->
                resultado.next() && resultado.getBoolean("possui")
            }
        }
    }

    static void preencherDados(PreparedStatement statement, Empresa empresa) {
        statement.setString(1, empresa.nome)
        statement.setString(2, empresa.email)
        statement.setString(3, empresa.cnpj)
        statement.setString(4, empresa.pais)
        statement.setString(5, empresa.cep)
        statement.setString(6, empresa.descricao)
        statement.setString(7, empresa.senha)
    }

    static Empresa mapearEmpresa(ResultSet resultado) {
        Empresa empresa = new Empresa(
                resultado.getString("nome"),
                resultado.getString("email"),
                resultado.getString("cnpj"),
                resultado.getString("pais"),
                resultado.getString("cep"),
                resultado.getString("descricao"),
                resultado.getString("senha")
        )
        empresa.id = resultado.getInt("id")
        return empresa
    }
}
