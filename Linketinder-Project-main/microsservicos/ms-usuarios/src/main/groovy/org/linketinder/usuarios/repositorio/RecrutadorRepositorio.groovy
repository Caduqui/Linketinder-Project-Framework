package org.linketinder.usuarios.repositorio

import org.linketinder.usuarios.model.Recrutador

/**
 *
 * @author Guilherme Lima Conte
 */

interface RecrutadorRepositorio {

    List<Recrutador> listarPorEmpresa(Integer idEmpresa)

    Recrutador buscarPorId(Integer id)

    Recrutador buscarParaLogin(String email)

    Integer buscarIdPorEmail(String email)

    Integer buscarIdPorCpf(String cpf)

    Recrutador inserir(Recrutador recrutador)

    void atualizar(Recrutador recrutador)

    void excluir(Integer id)

    boolean vagaAtribuida(Integer idRecrutador, Integer idVaga)

    void atribuirVaga(Integer idRecrutador, Integer idVaga)

    void removerVaga(Integer idRecrutador, Integer idVaga)
}
