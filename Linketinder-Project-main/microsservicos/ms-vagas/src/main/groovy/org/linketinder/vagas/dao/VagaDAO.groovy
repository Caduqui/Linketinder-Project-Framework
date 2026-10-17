package org.linketinder.vagas.dao

import jakarta.inject.Singleton
import org.linketinder.vagas.banco.ConexaoBanco
import org.linketinder.vagas.model.Vaga
import org.linketinder.vagas.repositorio.VagaRepositorio

import java.sql.PreparedStatement
import java.sql.ResultSet

/**
 *
 * @author Guilherme Lima Conte
 */

@Singleton
class VagaDAO implements VagaRepositorio {

    static final String CONSULTA = """
        SELECT v.id, v.nome, v.descricao, v.estado, v.cidade, v.id_empresa, e.nome AS nome_empresa,
               COALESCE(ARRAY_AGG(comp.nome ORDER BY vc.id) FILTER (WHERE comp.nome IS NOT NULL), '{}') AS competencias
        FROM vagas AS v
        JOIN empresas AS e ON e.id = v.id_empresa
        LEFT JOIN vaga_competencia AS vc ON vc.id_vaga = v.id
        LEFT JOIN competencias AS comp ON comp.id = vc.id_competencia
    """

    static final List<String> TABELAS_DA_VAGA = [
            "vaga_competencia",
            "curtida_candidato_vaga",
            "curtida_empresa_candidato",
            "curtida_recrutador_candidato",
            "matches",
            "recrutador_vaga"
    ]

    private final ConexaoBanco conexaoBanco

    VagaDAO(ConexaoBanco conexaoBanco) {
        this.conexaoBanco = conexaoBanco
    }

    @Override
    List<Vaga> listar() {
        return buscarLista("${CONSULTA} GROUP BY v.id, e.nome ORDER BY v.id", null)
    }

    @Override
    List<Vaga> listarPorEmpresa(Integer idEmpresa) {
        return buscarLista("${CONSULTA} WHERE v.id_empresa = ? GROUP BY v.id, e.nome ORDER BY v.id", idEmpresa)
    }

    @Override
    Vaga buscarPorId(Integer id) {
        List<Vaga> encontradas = buscarLista("${CONSULTA} WHERE v.id = ? GROUP BY v.id, e.nome", id)
        return encontradas ? encontradas.first() : null
    }

    @Override
    Vaga inserir(Vaga vaga) {
        String sql = """
            INSERT INTO vagas (nome, descricao, estado, cidade, id_empresa)
            VALUES (?, ?, ?, ?, ?)
            RETURNING id
        """

        vaga.id = conexaoBanco.executar(sql) { PreparedStatement statement ->
            preencher(statement, vaga)
            statement.setInt(5, vaga.idEmpresa)
            statement.executeQuery().withCloseable { ResultSet resultado ->
                resultado.next()
                resultado.getInt("id")
            }
        }
        return vaga
    }

    @Override
    void atualizar(Vaga vaga) {
        String sql = "UPDATE vagas SET nome = ?, descricao = ?, estado = ?, cidade = ? WHERE id = ?"

        conexaoBanco.executar(sql) { PreparedStatement statement ->
            preencher(statement, vaga)
            statement.setInt(5, vaga.id)
            statement.executeUpdate()
        }
    }

    @Override
    void excluir(Integer id) {
        TABELAS_DA_VAGA.each { String tabela ->
            executarComId("DELETE FROM ${tabela} WHERE id_vaga = ?".toString(), id)
        }
        executarComId("DELETE FROM vagas WHERE id = ?", id)
    }

    @Override
    void vincularCompetencia(Integer idVaga, Integer idCompetencia) {
        conexaoBanco.executar("INSERT INTO vaga_competencia (id_vaga, id_competencia) VALUES (?, ?)") { PreparedStatement statement ->
            statement.setInt(1, idVaga)
            statement.setInt(2, idCompetencia)
            statement.executeUpdate()
        }
    }

    @Override
    void desvincularCompetencias(Integer idVaga) {
        executarComId("DELETE FROM vaga_competencia WHERE id_vaga = ?", idVaga)
    }

    private List<Vaga> buscarLista(GString sql, Integer parametro) {
        return conexaoBanco.executar(sql.toString()) { PreparedStatement statement ->
            if (parametro != null) {
                statement.setInt(1, parametro)
            }
            statement.executeQuery().withCloseable { ResultSet resultado ->
                List<Vaga> vagas = []
                while (resultado.next()) {
                    vagas << mapear(resultado)
                }
                return vagas
            }
        }
    }

    private void executarComId(String sql, Integer id) {
        conexaoBanco.executar(sql) { PreparedStatement statement ->
            statement.setInt(1, id)
            statement.executeUpdate()
        }
    }

    private static void preencher(PreparedStatement statement, Vaga vaga) {
        statement.setString(1, vaga.nome)
        statement.setString(2, vaga.descricao)
        statement.setString(3, vaga.estado)
        statement.setString(4, vaga.cidade)
    }

    private static Vaga mapear(ResultSet resultado) {
        return new Vaga(
                id: resultado.getInt("id"),
                nome: resultado.getString("nome"),
                descricao: resultado.getString("descricao"),
                estado: resultado.getString("estado"),
                cidade: resultado.getString("cidade"),
                idEmpresa: resultado.getInt("id_empresa"),
                nomeEmpresa: resultado.getString("nome_empresa"),
                competencias: (resultado.getArray("competencias").array as String[]).toList()
        )
    }
}
