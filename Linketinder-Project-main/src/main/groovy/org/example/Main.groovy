package org.example

import org.example.Banco.ConexaoBanco

import org.example.controller.CandidatoController
import org.example.controller.CompetenciaController
import org.example.controller.CurtidaController
import org.example.controller.EmpresaController
import org.example.controller.MenuController
import org.example.controller.RecrutadorController
import org.example.controller.VagaController
import org.example.service.CandidatoService
import org.example.service.CompetenciaService
import org.example.service.CurtidaService
import org.example.service.EmpresaService
import org.example.service.RecrutadorService
import org.example.service.VagaService
import org.example.view.CandidatoView
import org.example.view.CompetenciaView
import org.example.view.CurtidaView
import org.example.view.EmpresaView
import org.example.view.EntradaDados
import org.example.view.MenuView
import org.example.view.RecrutadorView
import org.example.view.ScannerLeitorEntrada
import org.example.dao.CandidatoDAO
import org.example.dao.CompetenciaDAO
import org.example.dao.CurtidaDAO
import org.example.dao.EmpresaDAO
import org.example.dao.MatchDAO
import org.example.dao.RecrutadorDAO
import org.example.dao.VagaDAO
import org.example.repositorio.CandidatoRepositorio
import org.example.repositorio.CompetenciaRepositorio
import org.example.repositorio.CurtidaRepositorio
import org.example.repositorio.EmpresaRepositorio
import org.example.repositorio.MatchRepositorio
import org.example.repositorio.RecrutadorRepositorio
import org.example.repositorio.VagaRepositorio
import org.example.view.VagaView

/**
 *
 * @author Guilherme Lima Conte
 */

class Main {

    static void main(String[] args) {

        EntradaDados entrada = new EntradaDados(new ScannerLeitorEntrada(new Scanner(System.in)))
        ConexaoBanco conexaoBanco = ConexaoBanco.obterInstancia()

        CompetenciaRepositorio competenciaRepositorio = new CompetenciaDAO(conexaoBanco)
        CandidatoRepositorio candidatoRepositorio = new CandidatoDAO(conexaoBanco, competenciaRepositorio)
        EmpresaRepositorio empresaRepositorio = new EmpresaDAO(conexaoBanco)
        VagaRepositorio vagaRepositorio = new VagaDAO(conexaoBanco, competenciaRepositorio, empresaRepositorio)
        RecrutadorRepositorio recrutadorRepositorio = new RecrutadorDAO(conexaoBanco, vagaRepositorio)
        CurtidaRepositorio curtidaRepositorio = new CurtidaDAO(conexaoBanco)
        MatchRepositorio matchRepositorio = new MatchDAO(conexaoBanco, candidatoRepositorio, empresaRepositorio, vagaRepositorio)

        CandidatoController candidatoController = new CandidatoController(new CandidatoService(candidatoRepositorio), new CandidatoView(entrada))
        EmpresaController empresaController = new EmpresaController(new EmpresaService(empresaRepositorio), new EmpresaView(entrada))
        CompetenciaController competenciaController = new CompetenciaController(new CompetenciaService(competenciaRepositorio), new CompetenciaView(entrada))
        VagaController vagaController = new VagaController(new VagaService(vagaRepositorio), new VagaView(entrada), empresaController)
        RecrutadorService recrutadorService = new RecrutadorService(recrutadorRepositorio, vagaRepositorio)
        RecrutadorController recrutadorController = new RecrutadorController(recrutadorService, new RecrutadorView(entrada), empresaController, vagaController)
        CurtidaController curtidaController = new CurtidaController(new CurtidaService(curtidaRepositorio, matchRepositorio, recrutadorService), new CurtidaView(), candidatoController, empresaController, vagaController, recrutadorController)

        new MenuController(new MenuView(entrada), candidatoController, empresaController, competenciaController, vagaController, curtidaController, recrutadorController).executar()

        conexaoBanco.fechar()
    }

}
