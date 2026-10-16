package org.example.dao

import org.example.Banco.ConexaoBanco
import org.example.model.Recrutador
import org.example.model.Vaga
import org.example.repositorio.RecrutadorRepositorio
import org.example.repositorio.VagaRepositorio

import java.sql.Date
import java.sql.PreparedStatement
import java.sql.ResultSet

/**
 *
 * @author Guilherme Lima Conte
 */
class RecrutadorDAO implements RecrutadorRepositorio {

    private final ConexaoBanco conexaoBanco
    private final VagaRepositorio vagaRepositorio

    RecrutadorDAO(ConexaoBanco conexaoBanco, VagaRepositorio vagaRepositorio) {
        this.conexaoBanco = conexaoBanco
        this.vagaRepositorio = vagaRepositorio
    }

    @Override
    void inserir(Recrutador recrutador) {
        String sql = """
            INSERT INTO recrutadores (nome, area, descricao, email, cpf, data_nascimento, senha, id_empresa)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            RETURNING id
        """

        conexaoBanco.executar(sql) { PreparedStatement statement ->
            preencherDados(statement, recrutador)
            statement.executeQuery().withCloseable { ResultSet resultado ->
                if (!resultado.next()) {
                    throw new IllegalStateException("O cadastro do recrutador não retornou um identificador")
                }
                recrutador.id = resultado.getInt("id")
            }
        }
    }

    @Override
    void atualizar(Recrutador recrutador) {
        String sql = """
            UPDATE recrutadores
            SET nome = ?, area = ?, descricao = ?, email = ?, cpf = ?, data_nascimento = ?, senha = ?, id_empresa = ?
            WHERE id = ?
        """

        conexaoBanco.executar(sql) { PreparedStatement statement ->
            preencherDados(statement, recrutador)
            statement.setInt(9, recrutador.id)
            statement.executeUpdate()
        }
    }

    @Override
    void deletar(Integer id) {
        conexaoBanco.executar("UPDATE matches SET id_recrutador = NULL WHERE id_recrutador = ?") { PreparedStatement statement ->
            statement.setInt(1, id)
            statement.executeUpdate()
        }

        conexaoBanco.executar("DELETE FROM curtida_recrutador_candidato WHERE id_recrutador = ?") { PreparedStatement statement ->
            statement.setInt(1, id)
            statement.executeUpdate()
        }

        conexaoBanco.executar("DELETE FROM recrutador_vaga WHERE id_recrutador = ?") { PreparedStatement statement ->
            statement.setInt(1, id)
            statement.executeUpdate()
        }

        conexaoBanco.executar("DELETE FROM recrutadores WHERE id = ?") { PreparedStatement statement ->
            statement.setInt(1, id)
            statement.executeUpdate()
        }
    }

    @Override
    Recrutador buscarPorId(Integer id) {
        return buscarUm("SELECT * FROM recrutadores WHERE id = ?", id)
    }

    @Override
    Recrutador buscarPorEmail(String email) {
        return buscarUm("SELECT * FROM recrutadores WHERE email = ?", email)
    }

    @Override
    Recrutador buscarPorCpf(String cpf) {
        return buscarUm("SELECT * FROM recrutadores WHERE cpf = ?", cpf)
    }

    @Override
    List<Recrutador> listarPorEmpresa(Integer idEmpresa) {
        String sql = """
            SELECT id, nome, area, descricao, email, id_empresa
            FROM recrutadores
            WHERE id_empresa = ?
            ORDER BY id
        """

        return conexaoBanco.executar(sql) { PreparedStatement statement ->
            statement.setInt(1, idEmpresa)
            statement.executeQuery().withCloseable { ResultSet resultado ->
                List<Recrutador> recrutadores = []
                while (resultado.next()) {
                    recrutadores << mapearResumoRecrutador(resultado)
                }
                return recrutadores
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

        conexaoBanco.executar(sql) { PreparedStatement statement ->
            statement.setInt(1, idRecrutador)
            statement.setInt(2, idVaga)
            statement.executeUpdate()
        }
    }

    @Override
    void removerVaga(Integer idRecrutador, Integer idVaga) {
        conexaoBanco.executar(
                "UPDATE matches SET id_recrutador = NULL WHERE id_recrutador = ? AND id_vaga = ?"
        ) { PreparedStatement statement ->
            statement.setInt(1, idRecrutador)
            statement.setInt(2, idVaga)
            statement.executeUpdate()
        }

        conexaoBanco.executar(
                "DELETE FROM curtida_recrutador_candidato WHERE id_recrutador = ? AND id_vaga = ?"
        ) { PreparedStatement statement ->
            statement.setInt(1, idRecrutador)
            statement.setInt(2, idVaga)
            statement.executeUpdate()
        }

        conexaoBanco.executar(
                "DELETE FROM recrutador_vaga WHERE id_recrutador = ? AND id_vaga = ?"
        ) { PreparedStatement statement ->
            statement.setInt(1, idRecrutador)
            statement.setInt(2, idVaga)
            statement.executeUpdate()
        }
    }

    @Override
    List<Vaga> listarVagasDoRecrutador(Integer idRecrutador) {
        String sql = """
            SELECT id_vaga FROM recrutador_vaga
            WHERE id_recrutador = ?
            ORDER BY id_vaga
        """

        List<Integer> idsVagas = conexaoBanco.executar(sql) { PreparedStatement statement ->
            statement.setInt(1, idRecrutador)
            statement.executeQuery().withCloseable { ResultSet resultado ->
                List<Integer> ids = []
                while (resultado.next()) {
                    ids << resultado.getInt("id_vaga")
                }
                return ids
            }
        }

        return idsVagas.collect { Integer idVaga ->
            Vaga vaga = vagaRepositorio.buscarPorId(idVaga)
            if (vaga == null) {
                throw new IllegalStateException("Uma vaga atribuída ao recrutador não foi encontrada")
            }
            return vaga
        }
    }

    private Recrutador buscarUm(String sql, String parametro) {
        return conexaoBanco.executar(sql) { PreparedStatement statement ->
            statement.setString(1, parametro)
            statement.executeQuery().withCloseable { ResultSet resultado ->
                resultado.next() ? mapearRecrutador(resultado) : null
            }
        }
    }

    private Recrutador buscarUm(String sql, Integer parametro) {
        return conexaoBanco.executar(sql) { PreparedStatement statement ->
            statement.setInt(1, parametro)
            statement.executeQuery().withCloseable { ResultSet resultado ->
                resultado.next() ? mapearRecrutador(resultado) : null
            }
        }
    }

    private static void preencherDados(PreparedStatement statement, Recrutador recrutador) {
        statement.setString(1, recrutador.nome)
        statement.setString(2, recrutador.area)
        statement.setString(3, recrutador.descricao)
        statement.setString(4, recrutador.email)
        statement.setString(5, recrutador.cpf)
        statement.setDate(6, Date.valueOf(recrutador.dataNascimento))
        statement.setString(7, recrutador.senha)
        statement.setInt(8, recrutador.idEmpresa)
    }

    private static Recrutador mapearRecrutador(ResultSet resultado) {
        Recrutador recrutador = new Recrutador(
                resultado.getString("nome"),
                resultado.getString("area"),
                resultado.getString("descricao"),
                resultado.getString("email"),
                resultado.getString("cpf"),
                resultado.getDate("data_nascimento").toLocalDate(),
                resultado.getString("senha"),
                resultado.getInt("id_empresa")
        )
        recrutador.id = resultado.getInt("id")
        return recrutador
    }

    private static Recrutador mapearResumoRecrutador(ResultSet resultado) {
        Recrutador recrutador = new Recrutador(
                resultado.getString("nome"),
                resultado.getString("area"),
                resultado.getString("descricao"),
                resultado.getString("email"),
                null,
                null,
                null,
                resultado.getInt("id_empresa")
        )
        recrutador.id = resultado.getInt("id")
        return recrutador
    }
}
