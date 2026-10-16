package org.example.dao

import org.example.model.Competencia
import org.example.Banco.ConexaoBanco
import org.example.model.Vaga
import org.example.repositorio.CompetenciaRepositorio
import org.example.repositorio.EmpresaRepositorio
import org.example.repositorio.VagaRepositorio

import java.sql.PreparedStatement
import java.sql.ResultSet

/**
 *
 * @author Guilherme Lima Conte
 */

class VagaDAO implements VagaRepositorio{

    final ConexaoBanco conexaoBanco
    final CompetenciaRepositorio competenciaRepositorio
    final EmpresaRepositorio empresaRepositorio

    VagaDAO(ConexaoBanco conexaoBanco, CompetenciaRepositorio competenciaRepositorio, EmpresaRepositorio empresaRepositorio) {
        this.conexaoBanco = conexaoBanco
        this.competenciaRepositorio = competenciaRepositorio
        this.empresaRepositorio = empresaRepositorio
    }

    @Override
    void inserir(Vaga vaga) {

        String sql = """
            INSERT INTO vagas (nome, descricao, estado, cidade, id_empresa) VALUES (?, ?, ?, ?, ?) RETURNING id
        """

        conexaoBanco.executar(sql) { PreparedStatement statement ->
            preencherDados(statement, vaga)
            statement.executeQuery().withCloseable { ResultSet resultado ->
                if (resultado.next()) {
                    vaga.id = resultado.getInt("id")
                }
            }
        }

        salvarCompetencias(vaga)
    }

    @Override
    void atualizar(Vaga vaga) {

        String sql = """
            UPDATE vagas
            SET nome = ?, descricao = ?, estado = ?, cidade = ?, id_empresa = ?
            WHERE id = ?
        """

        conexaoBanco.executar(sql) { PreparedStatement statement ->
            preencherDados(statement, vaga)
            statement.setInt(6, vaga.id)
            statement.executeUpdate()
        }

        removerCompetenciasDaVaga(vaga.id)
        salvarCompetencias(vaga)
    }

    @Override
    void deletar(Integer id) {
        [
                "vaga_competencia",
                "curtida_candidato_vaga",
                "curtida_empresa_candidato",
                "curtida_recrutador_candidato",
                "matches",
                "recrutador_vaga"
        ].each { String tabela ->
            removerRegistrosDaVaga(tabela, id)
        }

        conexaoBanco.executar("DELETE FROM vagas WHERE id = ?") { PreparedStatement statement ->
            statement.setInt(1, id)
            statement.executeUpdate()
        }
    }

    @Override
    List<Vaga> listar() {
        return buscarVagas("SELECT * FROM vagas ORDER BY id")
    }

    @Override
    List<Vaga> listarPorEmpresa(Integer idEmpresa) {
        return buscarVagas("SELECT * FROM vagas WHERE id_empresa = ? ORDER BY id", idEmpresa)
    }

    @Override
    Vaga buscarPorId(Integer id) {
        List<Vaga> vagas = buscarVagas("SELECT * FROM vagas WHERE id = ?", id)
        return vagas ? vagas.first() : null
    }

    List<Vaga> buscarVagas( String sql, Integer parametro = null ) {
        conexaoBanco.executar(sql) { PreparedStatement statement ->
            if (parametro != null) {
                statement.setInt(1, parametro)
            }
            statement.executeQuery().withCloseable { ResultSet resultado ->
                List<Vaga> vagas = []
                while (resultado.next()) {
                    vagas << mapearVaga(resultado)
                }
                return vagas
            }
        }
    }

    List<String> buscarCompetenciasDaVaga(Integer idVaga) {
        String sql = """
            SELECT comp.nome FROM competencias AS comp, vaga_competencia AS vc
            WHERE comp.id = vc.id_competencia
            AND vc.id_vaga = ?
        """

        conexaoBanco.executar(sql) { PreparedStatement statement ->
            statement.setInt(1, idVaga)
            statement.executeQuery().withCloseable { ResultSet resultado ->
                List<String> competencias = []
                while (resultado.next()) {
                    competencias << resultado.getString("nome")
                }
                return competencias
            }
        }
    }

    void removerCompetenciasDaVaga(Integer idVaga) {
        removerRegistrosDaVaga("vaga_competencia", idVaga)
    }

    void inserirRelacaoVagaCompetencia( Integer idVaga, Integer idCompetencia) {
        String sql = """
            INSERT INTO vaga_competencia (id_vaga, id_competencia) VALUES (?, ?)
        """

        conexaoBanco.executar(sql) { PreparedStatement statement ->
            statement.setInt(1, idVaga)
            statement.setInt(2, idCompetencia)
            statement.executeUpdate()
        }
    }

    void salvarCompetencias(Vaga vaga) {
        vaga.competencias.each { String nomeCompetencia ->
            Competencia competencia = competenciaRepositorio.buscarOuInserir(nomeCompetencia)
            inserirRelacaoVagaCompetencia(vaga.id, competencia.id)
        }
    }

    void removerRegistrosDaVaga(String tabela, Integer idVaga) {
        conexaoBanco.executar("DELETE FROM ${tabela} WHERE id_vaga = ?") { PreparedStatement statement ->
            statement.setInt(1, idVaga)
            statement.executeUpdate()
        }
    }

    static void preencherDados(PreparedStatement statement, Vaga vaga) {
        statement.setString(1, vaga.nome)
        statement.setString(2, vaga.descricao)
        statement.setString(3, vaga.estado)
        statement.setString(4, vaga.cidade)
        statement.setInt(5, vaga.empresa.id)
    }

    Vaga mapearVaga(ResultSet resultado) {
        Integer id = resultado.getInt("id")

        Vaga vaga = new Vaga(
                resultado.getString("nome"),
                resultado.getString("descricao"),
                resultado.getString("estado"),
                resultado.getString("cidade"),
                empresaRepositorio.buscarPorId(resultado.getInt("id_empresa")),
                buscarCompetenciasDaVaga(id)
        )
        vaga.id = id
        return vaga
    }
}
