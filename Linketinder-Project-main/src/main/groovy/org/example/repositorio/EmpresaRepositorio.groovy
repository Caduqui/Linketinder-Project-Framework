package org.example.repositorio

import org.example.model.Empresa

/**
 *
 * @author Guilherme Lima Conte
 */

interface EmpresaRepositorio {
    void inserir(Empresa empresa)
    void atualizar(Empresa empresa)
    void deletar(Integer id)
    Empresa buscarPorId(Integer id)
    Empresa buscarPorEmail(String email)
    List<Empresa> listar()
    boolean possuiVagas(Integer idEmpresa)
    boolean possuiRecrutadores(Integer idEmpresa)
}