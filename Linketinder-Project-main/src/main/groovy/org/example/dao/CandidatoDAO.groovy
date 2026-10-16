package org.example.dao

import org.example.model.Candidato
import org.example.model.Competencia
import org.example.Banco.ConexaoBanco
import org.example.repositorio.CandidatoRepositorio
import org.example.repositorio.CompetenciaRepositorio

import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Date

/**
 *
 * @author Guilherme Lima Conte
 */

class CandidatoDAO implements CandidatoRepositorio {

    final ConexaoBanco conexaoBanco
    final CompetenciaRepositorio competenciaRepositorio

    CandidatoDAO(ConexaoBanco conexaoBanco, CompetenciaRepositorio competenciaRepositorio) {
        this.conexaoBanco = conexaoBanco
        this.competenciaRepositorio = competenciaRepositorio
    }

    @Override
    void inserir(Candidato candidato) {

        String sql = """
            INSERT INTO candidatos (nome, sobrenome, data_nascimento, email, cpf, pais, cep, descricao, senha, formacao)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id
        """

        conexaoBanco.executar(sql) { PreparedStatement statement ->
            preencherDados(statement, candidato)
            statement.executeQuery().withCloseable { ResultSet resultado ->
                if (resultado.next()) {
                    candidato.id = resultado.getInt("id")
                }
            }
        }

        salvarCompetencias(candidato)
    }

    @Override
    Candidato buscarPorId(Integer id) {
        String sql = """
            SELECT * FROM candidatos WHERE id = ?
        """

        conexaoBanco.executar(sql) { PreparedStatement statement ->
            statement.setInt(1, id)
            statement.executeQuery().withCloseable { ResultSet resultado ->
                resultado.next() ? mapearCandidato(resultado) : null
            }
        }
    }

    @Override
    Candidato buscarPorEmail(String email) {
        String sql = """
            SELECT * FROM candidatos WHERE email = ?
        """

        conexaoBanco.executar(sql) { PreparedStatement statement ->
            statement.setString(1, email)
            statement.executeQuery().withCloseable { ResultSet resultado ->
                resultado.next() ? mapearCandidato(resultado) : null
            }
        }
    }

    @Override
    List<Candidato> listar() {
        String sql = """
            SELECT * FROM candidatos ORDER BY id
        """

        conexaoBanco.executar(sql) { PreparedStatement statement ->
            statement.executeQuery().withCloseable { ResultSet resultado ->
                List<Candidato> candidatos = []
                while(resultado.next()) {
                    candidatos << mapearCandidato(resultado)
                }
                return candidatos
            }
        }
    }

    @Override
    void atualizar(Candidato candidato) {
        String sql = """
            UPDATE candidatos
            SET nome = ?, sobrenome = ?, data_nascimento = ?, email = ?, cpf = ?, pais = ?, cep = ?, descricao = ?, senha = ?, formacao = ?
            WHERE id = ?
        """

        conexaoBanco.executar(sql) { PreparedStatement statement ->
            preencherDados(statement, candidato)
            statement.setInt(11, candidato.id)
            statement.executeUpdate()
        }

        removerCompetenciasDoCandidato(candidato.id)
        salvarCompetencias(candidato)
    }

    @Override
    void deletar(Integer id) {
        [
                "candidato_competencia",
                "curtida_candidato_vaga",
                "curtida_empresa_candidato",
                "curtida_recrutador_candidato",
                "matches"
        ].each { String tabela ->
            removerRegistrosDoCandidato(tabela, id)
        }

        conexaoBanco.executar("DELETE FROM candidatos WHERE id = ?") { PreparedStatement statement ->
            statement.setInt(1, id)
            statement.executeUpdate()
        }
    }

    List<String> buscarCompetenciasDoCandidato(Integer idCandidato) {
        String sql = """
            SELECT comp.nome FROM competencias AS comp, candidato_competencia AS cc
            WHERE comp.id = cc.id_competencia
            AND cc.id_candidato = ?
        """

        conexaoBanco.executar(sql) { PreparedStatement statement ->
            statement.setInt(1, idCandidato)

            statement.executeQuery().withCloseable { ResultSet resultado ->
                List<String> competencias = []

                while (resultado.next()) {
                    competencias << resultado.getString("nome")
                }
                return competencias
            }
        }
    }

    void inserirRelacaoCandidatoCompetencia(Integer idCandidato, Integer idCompetencia) {
        String sql = """
            INSERT INTO candidato_competencia (id_candidato, id_competencia) VALUES (?, ?)
        """

        conexaoBanco.executar(sql) { PreparedStatement statement ->
            statement.setInt(1, idCandidato)
            statement.setInt(2, idCompetencia)
            statement.executeUpdate()
        }
    }

    void removerCompetenciasDoCandidato(Integer idCandidato) {
            removerRegistrosDoCandidato("candidato_competencia", idCandidato)
    }

    void salvarCompetencias(Candidato candidato) {
        candidato.competencias.each { String nomeCompetencia ->
            Competencia competencia = competenciaRepositorio.buscarOuInserir(nomeCompetencia)
            inserirRelacaoCandidatoCompetencia(candidato.id, competencia.id)
        }
    }

    void removerRegistrosDoCandidato(String tabela, Integer idCandidato) {
        conexaoBanco.executar("DELETE FROM ${tabela} WHERE id_candidato = ?") { PreparedStatement statement ->
            statement.setInt(1, idCandidato)
            statement.executeUpdate()
        }
    }

    static void preencherDados(PreparedStatement statement, Candidato candidato) {
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

    Candidato mapearCandidato(ResultSet resultado) {
        Integer id = resultado.getInt("id")

        Candidato candidato = new Candidato(
                resultado.getString("nome"),
                resultado.getString("sobrenome"),
                resultado.getDate("data_nascimento").toLocalDate(),
                resultado.getString("email"),
                resultado.getString("cpf"),
                resultado.getString("pais"),
                resultado.getString("cep"),
                resultado.getString("descricao"),
                resultado.getString("senha"),
                resultado.getString("formacao"),
                buscarCompetenciasDoCandidato(id)
        )
        candidato.id = id

        return candidato
    }

}
