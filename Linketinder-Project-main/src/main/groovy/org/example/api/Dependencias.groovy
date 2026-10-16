package org.example.api

import org.example.Banco.ConexaoBanco
import org.example.dao.CandidatoDAO
import org.example.dao.CompetenciaDAO
import org.example.dao.CurtidaDAO
import org.example.dao.EmpresaDAO
import org.example.dao.MatchDAO
import org.example.dao.RecrutadorDAO
import org.example.dao.VagaDAO
import org.example.repositorio.CandidatoRepositorio
import org.example.repositorio.CompetenciaRepositorio
import org.example.repositorio.EmpresaRepositorio
import org.example.repositorio.RecrutadorRepositorio
import org.example.repositorio.VagaRepositorio
import org.example.service.CandidatoService
import org.example.service.CompetenciaService
import org.example.service.CurtidaService
import org.example.service.EmpresaService
import org.example.service.RecrutadorService
import org.example.service.VagaService

/**
 *
 * @author Guilherme Lima Conte
 */

class Dependencias {

    private static Dependencias instancia

    final CandidatoService candidatoService
    final EmpresaService empresaService
    final VagaService vagaService
    final CompetenciaService competenciaService
    final CurtidaService curtidaService
    final RecrutadorService recrutadorService

    Dependencias(CandidatoService candidatoService, EmpresaService empresaService, VagaService vagaService, CompetenciaService competenciaService, CurtidaService curtidaService, RecrutadorService recrutadorService) {
        this.candidatoService = candidatoService
        this.empresaService = empresaService
        this.vagaService = vagaService
        this.competenciaService = competenciaService
        this.curtidaService = curtidaService
        this.recrutadorService = recrutadorService
    }

    private Dependencias() {
        ConexaoBanco conexaoBanco = ConexaoBanco.obterInstancia()
        CompetenciaRepositorio competenciaRepositorio = new CompetenciaDAO(conexaoBanco)
        EmpresaRepositorio empresaRepositorio = new EmpresaDAO(conexaoBanco)
        CandidatoRepositorio candidatoRepositorio = new CandidatoDAO(conexaoBanco, competenciaRepositorio)
        VagaRepositorio vagaRepositorio = new VagaDAO(conexaoBanco, competenciaRepositorio, empresaRepositorio)
        RecrutadorRepositorio recrutadorRepositorio = new RecrutadorDAO(conexaoBanco, vagaRepositorio)
        this.recrutadorService = new RecrutadorService(recrutadorRepositorio, vagaRepositorio)

        this.candidatoService = new CandidatoService(candidatoRepositorio)
        this.empresaService = new EmpresaService(empresaRepositorio)
        this.vagaService = new VagaService(vagaRepositorio)
        this.competenciaService = new CompetenciaService(competenciaRepositorio)
        this.curtidaService = new CurtidaService(new CurtidaDAO(conexaoBanco), new MatchDAO(conexaoBanco, candidatoRepositorio, empresaRepositorio, vagaRepositorio), recrutadorService)
    }

    static synchronized Dependencias obterInstancia() {
        if (instancia == null) {
            instancia = new Dependencias()
        }
        return instancia
    }
}
