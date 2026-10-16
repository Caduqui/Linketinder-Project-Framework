package org.example.repositorio

/**
 *
 * @author Guilherme Lima Conte
 */

interface CurtidaRepositorio {
    boolean candidatoCurtirVaga(Integer idCandidato, Integer idVaga)
    boolean empresaCurtirCandidato(Integer idEmpresa, Integer idCandidato, Integer idVaga)
    boolean recrutadorCurtirCandidato(Integer idRecrutador, Integer idCandidato, Integer idVaga)
}