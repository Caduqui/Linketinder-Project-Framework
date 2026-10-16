package org.linketinder.cadastro.http

import org.linketinder.cadastro.service.CadastroCandidatoService
import org.linketinder.cadastro.service.CadastroEmpresaService
import org.linketinder.cadastro.service.CompetenciaDoCandidatoService

/**
 *
 * @author Guilherme Lima Conte
 */

class RotasDeCadastro {

    static void registrar(Roteador roteador, CadastroCandidatoService cadastroCandidatoService, CadastroEmpresaService cadastroEmpresaService) {
        roteador.registrar("POST", "/candidatos", new CandidatoHandler(cadastroCandidatoService))
        roteador.registrar("POST", "/empresas", new EmpresaHandler(cadastroEmpresaService))
    }

    static void registrarCompetencias(Roteador roteador, CompetenciaDoCandidatoService competenciaDoCandidatoService) {
        roteador.registrar("POST", "/candidatos/{id}/competencias", new CompetenciaHandler(competenciaDoCandidatoService))
    }
}
