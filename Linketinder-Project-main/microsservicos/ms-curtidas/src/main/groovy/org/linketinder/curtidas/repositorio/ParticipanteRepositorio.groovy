package org.linketinder.curtidas.repositorio

/**
 *
 * @author Guilherme Lima Conte
 */

interface ParticipanteRepositorio {

    boolean candidatoExiste(Integer idCandidato)

    Integer buscarEmpresaDaVaga(Integer idVaga)

    Integer buscarEmpresaDoRecrutador(Integer idRecrutador)

    boolean vagaAtribuidaAoRecrutador(Integer idRecrutador, Integer idVaga)
}
