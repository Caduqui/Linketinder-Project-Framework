package org.example.repositorio

import org.example.model.Recrutador
import org.example.model.Vaga

/**
 *
 * @author Guilherme Lima Conte
 */
interface RecrutadorRepositorio {
    void inserir(Recrutador recrutador)
    void atualizar(Recrutador recrutador)
    void deletar(Integer id)
    Recrutador buscarPorId(Integer id)
    Recrutador buscarPorEmail(String email)
    Recrutador buscarPorCpf(String cpf)
    List<Recrutador> listarPorEmpresa(Integer idEmpresa)
    void atribuirVaga(Integer idRecrutador, Integer idVaga)
    void removerVaga(Integer idRecrutador, Integer idVaga)
    List<Vaga> listarVagasDoRecrutador(Integer idRecrutador)
}
