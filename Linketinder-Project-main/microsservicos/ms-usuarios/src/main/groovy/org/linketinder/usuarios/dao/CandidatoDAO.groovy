package org.linketinder.usuarios.dao

import jakarta.inject.Singleton
import org.linketinder.usuarios.banco.ConexaoBanco
import org.linketinder.usuarios.banco.TradutorDeErroDeBanco
import org.linketinder.usuarios.model.Candidato
import org.linketinder.usuarios.repositorio.CandidatoRepositorio
import org.linketinder.usuarios.service.CadastroRepetidoException

import java.sql.Date
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException

/**
 *
 * @author Guilherme Lima Conte
 */

@Singleton
class CandidatoDAO implements CandidatoRepositorio {

    static final String CONSULTA_SEM_SENHA = """
        SELECT c.id, c.nome, c.sobrenome, c.data_nascimento, c.email, c.cpf, c.pais, c.cep, c.descricao, c.formacao,
               COALESCE(ARRAY_AGG(comp.nome ORDER BY cc.id) FILTER (WHERE comp.nome IS NOT NULL), '{}') AS competencias
        FROM candidatos AS c
        LEFT JOIN candidato_competencia AS cc ON cc.id_candidato = c.id
        LEFT JOIN competencias AS comp ON comp.id = cc.id_competencia
    """

    static final List<String> TABELAS_DO_CANDIDATO = [
            "candidato_competencia",
            "curtida_candidato_vaga",
            "curtida_empresa_candidato",
            "curtida_recrutador_candidato",
            "matches"
    ]

    private final ConexaoBanco conexaoBanco

    CandidatoDAO(ConexaoBanco conexaoBanco) {
        this.conexaoBanco = conexaoBanco
    }

    @Override
    List<Candidato> listar() {
        return conexaoBanco.executar("${CONSULTA_SEM_SENHA} GROUP BY c.id ORDER BY c.id".toString()) { PreparedStatement statement ->
            statement.executeQuery().withCloseable { ResultSet resultado ->
                List<Candidato> candidatos = []
                while (resultado.next()) {
                    candidatos << mapear(resultado)
                }
                return candidatos
            }
        }
    }

    @Override
    Candidato buscarPorId(Integer id) {
        return conexaoBanco.executar("${CONSULTA_SEM_SENHA} WHERE c.id = ? GROUP BY c.id".toString()) { PreparedStatement statement ->
            statement.setInt(1, id)
            statement.executeQuery().withCloseable { ResultSet resultado ->
                resultado.next() ? mapear(resultado) : null
            }
        }
    }

    @Override
    Candidato buscarParaLogin(String email) {
        return conexaoBanco.executar("SELECT id, nome, senha FROM candidatos WHERE email = ?") { PreparedStatement statement ->
            statement.setString(1, email)
            statement.executeQuery().withCloseable { ResultSet resultado ->
                resultado.next()
                        ? new Candidato(id: resultado.getInt("id"), nome: resultado.getString("nome"), senha: resultado.getString("senha"))
                        : null
            }
        }
    }

    @Override
    Candidato inserir(Candidato candidato) {
        String sql = """
            INSERT INTO candidatos (nome, sobrenome, data_nascimento, email, cpf, pais, cep, descricao, senha, formacao)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            RETURNING id
        """

        try {
            candidato.id = conexaoBanco.executar(sql) { PreparedStatement statement ->
                preencher(statement, candidato)
                statement.executeQuery().withCloseable { ResultSet resultado ->
                    resultado.next()
                    resultado.getInt("id")
                }
            }
            return candidato
        } catch (SQLException falha) {
            throw TradutorDeErroDeBanco.traduzir(falha, CadastroRepetidoException.MENSAGEM_GENERICA)
        }
    }

    @Override
    void atualizar(Candidato candidato) {
        String sql = """
            UPDATE candidatos
            SET nome = ?, sobrenome = ?, data_nascimento = ?, email = ?, cpf = ?, pais = ?, cep = ?, descricao = ?,
                senha = ?, formacao = ?
            WHERE id = ?
        """

        try {
            conexaoBanco.executar(sql) { PreparedStatement statement ->
                preencher(statement, candidato)
                statement.setInt(11, candidato.id)
                statement.executeUpdate()
            }
        } catch (SQLException falha) {
            throw TradutorDeErroDeBanco.traduzir(falha, CadastroRepetidoException.MENSAGEM_GENERICA)
        }
    }

    @Override
    void excluir(Integer id) {
        TABELAS_DO_CANDIDATO.each { String tabela ->
            conexaoBanco.executar("DELETE FROM ${tabela} WHERE id_candidato = ?".toString()) { PreparedStatement statement ->
                statement.setInt(1, id)
                statement.executeUpdate()
            }
        }

        conexaoBanco.executar("DELETE FROM candidatos WHERE id = ?") { PreparedStatement statement ->
            statement.setInt(1, id)
            statement.executeUpdate()
        }
    }

    private static void preencher(PreparedStatement statement, Candidato candidato) {
        statement.setString(1, candidato.nome)
        statement.setString(2, candidato.sobrenome)
        statement.setDate(3, Date.valueOf(candidato.dataNascimento))
        statement.setString(4, candidato.email)
        statement.setString(5, candidato.cpf)
        statement.setString(6, candidato.pais)
        statement.setString(7, candidato.cep)
        statement.setString(8, candidato.descricao)
        statement.setString(9, candidato.senha)
        statement.setString(10, candidato.formacao)
    }

    private static Candidato mapear(ResultSet resultado) {
        return new Candidato(
                id: resultado.getInt("id"),
                nome: resultado.getString("nome"),
                sobrenome: resultado.getString("sobrenome"),
                dataNascimento: resultado.getDate("data_nascimento").toLocalDate(),
                email: resultado.getString("email"),
                cpf: resultado.getString("cpf"),
                pais: resultado.getString("pais"),
                cep: resultado.getString("cep"),
                descricao: resultado.getString("descricao"),
                formacao: resultado.getString("formacao"),
                competencias: (resultado.getArray("competencias").array as String[]).toList()
        )
    }
}
