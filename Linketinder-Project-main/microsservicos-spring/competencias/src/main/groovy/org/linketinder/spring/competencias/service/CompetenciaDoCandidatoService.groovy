package org.linketinder.spring.competencias.service

import org.linketinder.spring.competencias.integracao.ServicoNucleo
import org.linketinder.spring.competencias.model.Competencia
import org.linketinder.spring.competencias.model.CompetenciaDoCandidato
import org.linketinder.spring.competencias.model.CompetenciaPedida
import org.linketinder.spring.competencias.model.NivelDeCompetencia
import org.linketinder.spring.competencias.repositorio.CompetenciaDoCandidatoRepositorio
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 *
 * @author Guilherme Lima Conte
 */

@Service
class CompetenciaDoCandidatoService {

    static final String CAMPO = "competencias"

    private final CompetenciaService competenciaService
    private final CompetenciaDoCandidatoRepositorio doCandidatoRepositorio
    private final ServicoNucleo servicoNucleo

    CompetenciaDoCandidatoService(CompetenciaService competenciaService, CompetenciaDoCandidatoRepositorio doCandidatoRepositorio, ServicoNucleo servicoNucleo) {
        this.competenciaService = competenciaService
        this.doCandidatoRepositorio = doCandidatoRepositorio
        this.servicoNucleo = servicoNucleo
    }

    @Transactional
    List<CompetenciaDoCandidato> adicionar(Integer idCandidato, Map<String, Object> dados) {
        List<CompetenciaPedida> pedidas = validar(dados)
        exigirCandidato(idCandidato)

        pedidas.each { CompetenciaPedida pedida -> somarAoCandidato(idCandidato, pedida) }
        return doCandidatoRepositorio.listarDoCandidato(idCandidato)
    }

    List<CompetenciaDoCandidato> listar(Integer idCandidato) {
        exigirCandidato(idCandidato)
        return doCandidatoRepositorio.listarDoCandidato(idCandidato)
    }

    Map<Integer, List<CompetenciaDoCandidato>> listarDosCandidatos(List<Integer> idsCandidatos) {
        if (idsCandidatos.isEmpty()) {
            return [:]
        }
        return doCandidatoRepositorio.listarDosCandidatos(idsCandidatos).groupBy { CompetenciaDoCandidato ligacao -> ligacao.idCandidato }
    }

    @Transactional
    void apagarDoCandidato(Integer idCandidato) {
        doCandidatoRepositorio.apagarDoCandidato(idCandidato)
    }

    private void somarAoCandidato(Integer idCandidato, CompetenciaPedida pedida) {
        Competencia competencia = competenciaService.buscarOuCriar(pedida.nome)
        CompetenciaDoCandidato ligacao = doCandidatoRepositorio.buscarLigacao(idCandidato, competencia.id) ?:
                new CompetenciaDoCandidato(idCandidato: idCandidato, competencia: competencia)

        ligacao.nivel = pedida.nivel
        doCandidatoRepositorio.save(ligacao)
    }

    private void exigirCandidato(Integer idCandidato) {
        if (!servicoNucleo.candidatoExiste(idCandidato)) {
            throw new RecursoNaoEncontradoException("Candidato ${idCandidato} não encontrado")
        }
    }

    private static List<CompetenciaPedida> validar(Map<String, Object> dados) {
        Object lista = dados[CAMPO]

        if (!(lista instanceof List)) {
            throw new DadosInvalidosException("O campo '${CAMPO}' deve ser uma lista com ao menos uma competência")
        }

        List<CompetenciaPedida> competencias = (lista as List)
                .collect { Object item -> lerItem(item) }
                .findAll { CompetenciaPedida competencia -> competencia != null }
                .unique { CompetenciaPedida competencia -> competencia.nome.toLowerCase() }

        if (competencias.isEmpty()) {
            throw new DadosInvalidosException("O campo '${CAMPO}' deve ter ao menos uma competência")
        }
        return competencias
    }

    private static CompetenciaPedida lerItem(Object item) {
        if (!(item instanceof Map)) {
            throw new DadosInvalidosException("Cada item do campo '${CAMPO}' deve ser um objeto com 'nome' e 'nivel'")
        }

        Map<String, Object> dados = item as Map<String, Object>
        String nome = CamposDoCorpo.texto(dados, "nome").trim()
        if (nome.isEmpty()) {
            return null
        }

        NomeDeCompetencia.limitar(nome, "O 'nome' de cada competência deve ter no máximo ${NomeDeCompetencia.TAMANHO_MAXIMO} caracteres")
        return new CompetenciaPedida(nome: nome, nivel: exigirNivel(dados))
    }

    private static String exigirNivel(Map<String, Object> dados) {
        Object nivel = dados["nivel"]

        if (!NivelDeCompetencia.ehValido(nivel)) {
            throw new DadosInvalidosException("O campo 'nivel' de cada competência deve ser ${NivelDeCompetencia.valores().join(', ')}")
        }
        return nivel as String
    }
}
