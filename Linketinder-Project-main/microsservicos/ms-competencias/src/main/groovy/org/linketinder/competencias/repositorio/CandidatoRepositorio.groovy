package org.linketinder.competencias.repositorio

import org.linketinder.competencias.model.Candidato

/**
 *
 * @author Guilherme Lima Conte
 */

interface CandidatoRepositorio {

    boolean existe(Integer id)

    Candidato buscarPorId(Integer id)
}
