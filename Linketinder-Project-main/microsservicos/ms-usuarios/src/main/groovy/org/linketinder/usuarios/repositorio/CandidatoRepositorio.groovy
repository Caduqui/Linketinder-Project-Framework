package org.linketinder.usuarios.repositorio

import org.linketinder.usuarios.model.Candidato

/**
 *
 * @author Guilherme Lima Conte
 */

interface CandidatoRepositorio {

    List<Candidato> listar()

    Candidato buscarPorId(Integer id)

    Candidato buscarParaLogin(String email)

    Candidato inserir(Candidato candidato)

    void atualizar(Candidato candidato)

    void excluir(Integer id)
}
