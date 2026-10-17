package org.linketinder.vagas.repositorio

import org.linketinder.vagas.model.Competencia

/**
 *
 * @author Guilherme Lima Conte
 */

interface CompetenciaRepositorio {

    Competencia buscarPorNomeIgnorandoCaixa(String nome)

    Competencia inserir(String nome)
}
