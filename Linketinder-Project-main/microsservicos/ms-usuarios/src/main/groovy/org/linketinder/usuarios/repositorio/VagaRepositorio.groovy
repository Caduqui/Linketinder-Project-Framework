package org.linketinder.usuarios.repositorio

import org.linketinder.usuarios.model.Vaga

/**
 *
 * @author Guilherme Lima Conte
 */

interface VagaRepositorio {

    Vaga buscarPorId(Integer id)

    List<Vaga> listarAtribuidasAoRecrutador(Integer idRecrutador)
}
