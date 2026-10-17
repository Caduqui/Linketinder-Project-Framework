package org.linketinder.usuarios.repositorio

import org.linketinder.usuarios.model.Empresa

/**
 *
 * @author Guilherme Lima Conte
 */

interface EmpresaRepositorio {

    List<Empresa> listar()

    Empresa buscarPorId(Integer id)

    Empresa buscarParaLogin(String email)

    Empresa inserir(Empresa empresa)

    void atualizar(Empresa empresa)

    void excluir(Integer id)

    boolean possuiVagas(Integer id)

    boolean possuiRecrutadores(Integer id)
}
