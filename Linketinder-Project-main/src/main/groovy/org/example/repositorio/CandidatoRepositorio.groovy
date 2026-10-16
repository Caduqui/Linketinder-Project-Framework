package org.example.repositorio

import org.example.model.Candidato

/**
 *
 * @author Guilherme Lima Conte
 */

interface CandidatoRepositorio {
    void inserir(Candidato candidato)
    void atualizar(Candidato candidato)
    void deletar(Integer id)
    Candidato buscarPorId(Integer id)
    Candidato buscarPorEmail(String email)
    List<Candidato> listar()
}