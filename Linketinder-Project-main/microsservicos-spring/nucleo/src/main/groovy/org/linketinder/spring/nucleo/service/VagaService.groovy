package org.linketinder.spring.nucleo.service

import org.linketinder.spring.nucleo.integracao.ServicoDeCompetencias
import org.linketinder.spring.nucleo.model.Vaga
import org.linketinder.spring.nucleo.repositorio.EmpresaRepositorio
import org.linketinder.spring.nucleo.repositorio.VagaRepositorio
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 *
 * @author Guilherme Lima Conte
 */

@Service
class VagaService {

    private final ValidadorDeVaga validador
    private final VagaRepositorio vagaRepositorio
    private final EmpresaRepositorio empresaRepositorio
    private final ServicoDeCompetencias servicoDeCompetencias

    VagaService(ValidadorDeVaga validador, VagaRepositorio vagaRepositorio, EmpresaRepositorio empresaRepositorio, ServicoDeCompetencias servicoDeCompetencias) {
        this.validador = validador
        this.vagaRepositorio = vagaRepositorio
        this.empresaRepositorio = empresaRepositorio
        this.servicoDeCompetencias = servicoDeCompetencias
    }

    @Transactional
    Vaga cadastrar(Map<String, Object> dados) {
        Vaga vaga = validador.validar(dados)
        exigirEmpresa(vaga.idEmpresa)

        return gravarComCompetencias(vaga)
    }

    List<Vaga> listar(Integer idEmpresa) {
        if (idEmpresa == null) {
            return completarCompetencias(vagaRepositorio.listarTodas())
        }

        exigirEmpresa(idEmpresa)
        return completarCompetencias(vagaRepositorio.listarDaEmpresa(idEmpresa))
    }

    List<Vaga> listarDoRecrutador(Integer idRecrutador) {
        return completarCompetencias(vagaRepositorio.listarDoRecrutador(idRecrutador))
    }

    Vaga buscar(Integer id) {
        return completarCompetencias([exigirVaga(id)]).first()
    }

    Vaga exigirVaga(Integer id) {
        Vaga vaga = vagaRepositorio.findById(id).orElse(null)

        if (vaga == null) {
            throw new RecursoNaoEncontradoException("Vaga ${id} não encontrada")
        }
        return vaga
    }

    @Transactional
    Vaga atualizar(Integer id, Map<String, Object> dados) {
        Vaga vaga = validador.validar(dados)
        vaga.id = id
        exigirDona(exigirVaga(id), vaga.idEmpresa)

        return gravarComCompetencias(vaga)
    }

    @Transactional
    void excluir(Integer id, Integer idEmpresa) {
        if (idEmpresa == null) {
            throw new DadosInvalidosException("O parâmetro 'idEmpresa' é obrigatório e deve ser um número")
        }
        exigirDona(exigirVaga(id), idEmpresa)

        vagaRepositorio.apagarCompetenciasDoBancoPrincipal(id)
        vagaRepositorio.apagarCurtidasDeCandidatos(id)
        vagaRepositorio.apagarCurtidasDaEmpresa(id)
        vagaRepositorio.apagarCurtidasDeRecrutadores(id)
        vagaRepositorio.apagarMatches(id)
        vagaRepositorio.apagarAtribuicoes(id)
        vagaRepositorio.deleteById(id)
        servicoDeCompetencias.apagarDaVaga(id)
    }

    private Vaga gravarComCompetencias(Vaga vaga) {
        List<String> competencias = vaga.competencias
        Vaga gravada = vagaRepositorio.saveAndFlush(vaga)

        gravada.competencias = servicoDeCompetencias.substituirDaVaga(gravada.id, competencias)
        return gravada
    }

    private List<Vaga> completarCompetencias(List<Vaga> vagas) {
        Map<Integer, List<String>> competencias = servicoDeCompetencias.buscarDasVagas(vagas*.id)
        vagas.each { Vaga vaga -> vaga.competencias = competencias[vaga.id] ?: [] }
        return vagas
    }

    private void exigirEmpresa(Integer idEmpresa) {
        if (!empresaRepositorio.existsById(idEmpresa)) {
            throw new RecursoNaoEncontradoException("Empresa ${idEmpresa} não encontrada")
        }
    }

    private static void exigirDona(Vaga vaga, Integer idEmpresa) {
        if (vaga.idEmpresa != idEmpresa) {
            throw new RegraDeNegocioException("A vaga ${vaga.id} não pertence à empresa ${idEmpresa}")
        }
    }
}
