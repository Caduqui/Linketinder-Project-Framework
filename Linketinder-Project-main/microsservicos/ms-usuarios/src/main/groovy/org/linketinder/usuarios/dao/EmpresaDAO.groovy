package org.linketinder.usuarios.dao

import jakarta.inject.Singleton
import org.linketinder.usuarios.banco.ConexaoBanco
import org.linketinder.usuarios.banco.TradutorDeErroDeBanco
import org.linketinder.usuarios.model.Empresa
import org.linketinder.usuarios.repositorio.EmpresaRepositorio
import org.linketinder.usuarios.service.CadastroRepetidoException

import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException

/**
 *
 * @author Guilherme Lima Conte
 */

@Singleton
class EmpresaDAO implements EmpresaRepositorio {

    static final String COLUNAS_SEM_SENHA = "id, nome, email, cnpj, pais, cep, descricao"

    private final ConexaoBanco conexaoBanco

    EmpresaDAO(ConexaoBanco conexaoBanco) {
        this.conexaoBanco = conexaoBanco
    }

    @Override
    List<Empresa> listar() {
        return conexaoBanco.executar("SELECT ${COLUNAS_SEM_SENHA} FROM empresas ORDER BY id".toString()) { PreparedStatement statement ->
            statement.executeQuery().withCloseable { ResultSet resultado ->
                List<Empresa> empresas = []
                while (resultado.next()) {
                    empresas << mapear(resultado)
                }
                return empresas
            }
        }
    }

    @Override
    Empresa buscarPorId(Integer id) {
        return conexaoBanco.executar("SELECT ${COLUNAS_SEM_SENHA} FROM empresas WHERE id = ?".toString()) { PreparedStatement statement ->
            statement.setInt(1, id)
            statement.executeQuery().withCloseable { ResultSet resultado ->
                resultado.next() ? mapear(resultado) : null
            }
        }
    }

    @Override
    Empresa buscarParaLogin(String email) {
        return conexaoBanco.executar("SELECT id, nome, senha FROM empresas WHERE email = ?") { PreparedStatement statement ->
            statement.setString(1, email)
            statement.executeQuery().withCloseable { ResultSet resultado ->
                resultado.next()
                        ? new Empresa(id: resultado.getInt("id"), nome: resultado.getString("nome"), senha: resultado.getString("senha"))
                        : null
            }
        }
    }

    @Override
    Empresa inserir(Empresa empresa) {
        String sql = """
            INSERT INTO empresas (nome, email, cnpj, pais, cep, descricao, senha)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            RETURNING id
        """

        try {
            empresa.id = conexaoBanco.executar(sql) { PreparedStatement statement ->
                preencher(statement, empresa)
                statement.executeQuery().withCloseable { ResultSet resultado ->
                    resultado.next()
                    resultado.getInt("id")
                }
            }
            return empresa
        } catch (SQLException falha) {
            throw TradutorDeErroDeBanco.traduzir(falha, CadastroRepetidoException.MENSAGEM_GENERICA)
        }
    }

    @Override
    void atualizar(Empresa empresa) {
        String sql = """
            UPDATE empresas
            SET nome = ?, email = ?, cnpj = ?, pais = ?, cep = ?, descricao = ?, senha = ?
            WHERE id = ?
        """

        try {
            conexaoBanco.executar(sql) { PreparedStatement statement ->
                preencher(statement, empresa)
                statement.setInt(8, empresa.id)
                statement.executeUpdate()
            }
        } catch (SQLException falha) {
            throw TradutorDeErroDeBanco.traduzir(falha, CadastroRepetidoException.MENSAGEM_GENERICA)
        }
    }

    @Override
    void excluir(Integer id) {
        conexaoBanco.executar("DELETE FROM empresas WHERE id = ?") { PreparedStatement statement ->
            statement.setInt(1, id)
            statement.executeUpdate()
        }
    }

    @Override
    boolean possuiVagas(Integer id) {
        return existe("SELECT EXISTS (SELECT 1 FROM vagas WHERE id_empresa = ?)", id)
    }

    @Override
    boolean possuiRecrutadores(Integer id) {
        return existe("SELECT EXISTS (SELECT 1 FROM recrutadores WHERE id_empresa = ?)", id)
    }

    private boolean existe(String sql, Integer id) {
        return conexaoBanco.executar(sql) { PreparedStatement statement ->
            statement.setInt(1, id)
            statement.executeQuery().withCloseable { ResultSet resultado ->
                resultado.next() && resultado.getBoolean(1)
            }
        }
    }

    private static void preencher(PreparedStatement statement, Empresa empresa) {
        statement.setString(1, empresa.nome)
        statement.setString(2, empresa.email)
        statement.setString(3, empresa.cnpj)
        statement.setString(4, empresa.pais)
        statement.setString(5, empresa.cep)
        statement.setString(6, empresa.descricao)
        statement.setString(7, empresa.senha)
    }

    private static Empresa mapear(ResultSet resultado) {
        return new Empresa(
                id: resultado.getInt("id"),
                nome: resultado.getString("nome"),
                email: resultado.getString("email"),
                cnpj: resultado.getString("cnpj"),
                pais: resultado.getString("pais"),
                cep: resultado.getString("cep"),
                descricao: resultado.getString("descricao")
        )
    }
}
