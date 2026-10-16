package org.example.repositorio

import org.example.model.Competencia

/**
 *
 * @author Guilherme Lima Conte
 */

interface CompetenciaRepositorio {
    void inserir(Competencia competencia)
    void atualizar(Competencia competencia)
    void deletar(Integer id)
    Competencia buscarPorId(Integer id)
    Competencia buscarPorNome(String nome)
    Competencia buscarOuInserir(String nome)
    List<Competencia> listar()
    boolean estaVinculadoACandidatoOuVaga(Integer id)
}