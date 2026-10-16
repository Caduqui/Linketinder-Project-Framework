package org.example.repositorio

import org.example.model.Match

/**
 *
 * @author Guilherme Lima Conte
 */

interface MatchRepositorio {
    boolean criarMatchSePossivel(Integer idCandidato, Integer idEmpresa, Integer idVaga)
    List<Match> listar()
}