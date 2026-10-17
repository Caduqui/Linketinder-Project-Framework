package org.linketinder.usuarios.dao

import jakarta.inject.Singleton
import org.linketinder.usuarios.banco.ConexaoBanco
import org.linketinder.usuarios.banco.TradutorDeErroDeBanco
import org.linketinder.usuarios.model.Recrutador
import org.linketinder.usuarios.repositorio.RecrutadorRepositorio
import org.linketinder.usuarios.service.RecrutadorService
import org.linketinder.usuarios.service.RegraDeNegocioException

import java.sql.Date
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException

/**
 *
 * @author Guilherme Lima Conte
 */

@Singleton
class RecrutadorDAO implements RecrutadorRepositorio {

    static final String COLUNAS_PUBLICAS = "id, nome, area, descricao, email, id_empresa"

    private final ConexaoBanco conexaoBanco

    RecrutadorDAO(ConexaoBanco conexaoBanco) {
        this.conexaoBanco = conexaoBanco
    }

    @Override
    List<Recrutador> listarPorEmpresa(Integer idEmpresa) {
        String sql = "SELECT ${COLUNAS_PUBLICAS} FROM recrutadores WHERE id_empresa = ? ORDER BY id"

        return conexaoBanco.executar(sql.toString()) { PreparedStatement statement ->
            statement.setInt(1, idEmpresa)
            statement.executeQuery().withCloseable { ResultSet resultado ->
                List<Recrutador> recrutadores = []
                while (resultado.next()) {
                    recrutadores << mapear(resultado)
                }
                return recrutadores
            }
        }
    }

    @Override
    Recrutador buscarPorId(Integer id) {
        return conexaoBanco.executar("SELECT ${COLUNAS_PUBLICAS} FROM recrutadores WHERE id = ?".toString()) { PreparedStatement statement ->
            statement.setInt(1, id)
            statement.executeQuery().withCloseable { ResultSet resultado ->
                resultado.next() ? mapear(resultado) : null
            }
        }
    }

    @Override
    Recrutador buscarParaLogin(String email) {
        return conexaoBanco.executar("SELECT id, nome, senha, id_empresa FROM recrutadores WHERE email = ?") { PreparedStatement statement ->
            statement.setString(1, email)
            statement.executeQuery().withCloseable { ResultSet resultado ->
                resultado.next() ? new Recrutador(
                        id: resultado.getInt("id"),
                        nome: resultado.getString("nome"),
                        senha: resultado.getString("senha"),
                        idEmpresa: resultado.getInt("id_empresa")
                ) : null
            }
        }
    }

    @Override
    Integer buscarIdPorEmail(String email) {
        return buscarId("SELECT id FROM recrutadores WHERE email = ?", email)
    }

    @Override
    Integer buscarIdPorCpf(String cpf) {
        return buscarId("SELECT id FROM recrutadores WHERE cpf = ?", cpf)
    }

    @Override
    Recrutador inserir(Recrutador recrutador) {
        String sql = """
            INSERT INTO recrutadores (nome, area, descricao, email, cpf, data_nascimento, senha, id_empresa)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            RETURNING id
        """

        try {
            recrutador.id = conexaoBanco.executar(sql) { PreparedStatement statement ->
                preencher(statement, recrutador)
                statement.setInt(8, recrutador.idEmpresa)
                statement.executeQuery().withCloseable { ResultSet resultado ->
                    resultado.next()
                    resultado.getInt("id")
                }
            }
            return recrutador
        } catch (SQLException falha) {
            throw traduzir(falha)
        }
    }

    @Override
    void atualizar(Recrutador recrutador) {
        String sql = """
            UPDATE recrutadores
            SET nome = ?, area = ?, descricao = ?, email = ?, cpf = ?, data_nascimento = ?, senha = ?
            WHERE id = ?
        """

        try {
            conexaoBanco.executar(sql) { PreparedStatement statement ->
                preencher(statement, recrutador)
                statement.setInt(8, recrutador.id)
                statement.executeUpdate()
            }
        } catch (SQLException falha) {
            throw traduzir(falha)
        }
    }

    @Override
    void excluir(Integer id) {
        executarComId("UPDATE matches SET id_recrutador = NULL WHERE id_recrutador = ?", id)
        executarComId("DELETE FROM curtida_recrutador_candidato WHERE id_recrutador = ?", id)
        executarComId("DELETE FROM recrutador_vaga WHERE id_recrutador = ?", id)
        executarComId("DELETE FROM recrutadores WHERE id = ?", id)
    }

    @Override
    boolean vagaAtribuida(Integer idRecrutador, Integer idVaga) {
        String sql = "SELECT EXISTS (SELECT 1 FROM recrutador_vaga WHERE id_recrutador = ? AND id_vaga = ?)"

        return conexaoBanco.executar(sql) { PreparedStatement statement ->
            statement.setInt(1, idRecrutador)
            statement.setInt(2, idVaga)
            statement.executeQuery().withCloseable { ResultSet resultado ->
                resultado.next() && resultado.getBoolean(1)
            }
        }
    }

    @Override
    void atribuirVaga(Integer idRecrutador, Integer idVaga) {
        String sql = """
            INSERT INTO recrutador_vaga (id_recrutador, id_vaga)
            VALUES (?, ?)
            ON CONFLICT (id_recrutador, id_vaga) DO NOTHING
        """

        executarComDoisIds(sql, idRecrutador, idVaga)
    }

    @Override
    void removerVaga(Integer idRecrutador, Integer idVaga) {
        executarComDoisIds("UPDATE matches SET id_recrutador = NULL WHERE id_recrutador = ? AND id_vaga = ?", idRecrutador, idVaga)
        executarComDoisIds("DELETE FROM curtida_recrutador_candidato WHERE id_recrutador = ? AND id_vaga = ?", idRecrutador, idVaga)
        executarComDoisIds("DELETE FROM recrutador_vaga WHERE id_recrutador = ? AND id_vaga = ?", idRecrutador, idVaga)
    }

    private static Exception traduzir(SQLException falha) {
        if (falha.SQLState == TradutorDeErroDeBanco.VIOLACAO_DE_UNICIDADE) {
            return new RegraDeNegocioException(RecrutadorService.RECRUTADOR_REPETIDO)
        }
        return falha
    }

    private Integer buscarId(String sql, String valor) {
        return conexaoBanco.executar(sql) { PreparedStatement statement ->
            statement.setString(1, valor)
            statement.executeQuery().withCloseable { ResultSet resultado ->
                resultado.next() ? resultado.getInt("id") : null
            }
        }
    }

    private void executarComId(String sql, Integer id) {
        conexaoBanco.executar(sql) { PreparedStatement statement ->
            statement.setInt(1, id)
            statement.executeUpdate()
        }
    }

    private void executarComDoisIds(String sql, Integer primeiro, Integer segundo) {
        conexaoBanco.executar(sql) { PreparedStatement statement ->
            statement.setInt(1, primeiro)
            statement.setInt(2, segundo)
            statement.executeUpdate()
        }
    }

    private static void preencher(PreparedStatement statement, Recrutador recrutador) {
        statement.setString(1, recrutador.nome)
        statement.setString(2, recrutador.area)
        statement.setString(3, recrutador.descricao)
        statement.setString(4, recrutador.email)
        statement.setString(5, recrutador.cpf)
        statement.setDate(6, Date.valueOf(recrutador.dataNascimento))
        statement.setString(7, recrutador.senha)
    }

    private static Recrutador mapear(ResultSet resultado) {
        return new Recrutador(
                id: resultado.getInt("id"),
                nome: resultado.getString("nome"),
                area: resultado.getString("area"),
                descricao: resultado.getString("descricao"),
                email: resultado.getString("email"),
                idEmpresa: resultado.getInt("id_empresa")
        )
    }
}
