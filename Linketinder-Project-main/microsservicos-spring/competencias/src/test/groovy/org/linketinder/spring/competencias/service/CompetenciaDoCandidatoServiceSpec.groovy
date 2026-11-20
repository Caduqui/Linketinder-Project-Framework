package org.linketinder.spring.competencias.service

import org.linketinder.spring.competencias.integracao.ServicoNucleo
import org.linketinder.spring.competencias.model.Competencia
import org.linketinder.spring.competencias.model.CompetenciaDoCandidato
import org.linketinder.spring.competencias.repositorio.CompetenciaDoCandidatoRepositorio
import org.linketinder.spring.competencias.repositorio.CompetenciaRepositorio
import spock.lang.Specification

/**
 *
 * @author Guilherme Lima Conte
 */

class CompetenciaDoCandidatoServiceSpec extends Specification {

    CompetenciaRepositorio competenciaRepositorio = Mock()
    CompetenciaDoCandidatoRepositorio doCandidatoRepositorio = Mock()
    ServicoNucleo servicoNucleo = Stub()
    CompetenciaDoCandidatoService service = new CompetenciaDoCandidatoService(new CompetenciaService(competenciaRepositorio), doCandidatoRepositorio, servicoNucleo)

    Competencia java = new Competencia(id: 1, nome: "Java")

    def setup() {
        servicoNucleo.candidatoExiste(4) >> true
        servicoNucleo.candidatoExiste(9) >> false
    }

    def "adicionar cria a competência inédita e liga ao candidato com o nível"() {
        given:
        competenciaRepositorio.buscarPorNomeIgnorandoCaixa("Kotlin") >> null

        when:
        service.adicionar(4, [competencias: [[nome: "Kotlin", nivel: "MEDIO"]]])

        then:
        1 * competenciaRepositorio.save({ Competencia nova -> nova.nome == "Kotlin" }) >> { Competencia nova ->
            nova.id = 11
            return nova
        }
        1 * doCandidatoRepositorio.save({ CompetenciaDoCandidato ligacao ->
            ligacao.idCandidato == 4 && ligacao.competencia.id == 11 && ligacao.nivel == "MEDIO"
        })
        1 * doCandidatoRepositorio.listarDoCandidato(4) >> []
    }

    def "adicionar reaproveita a competência em qualquer caixa e só atualiza o nível da que o candidato já tem"() {
        given:
        competenciaRepositorio.buscarPorNomeIgnorandoCaixa("java") >> java
        CompetenciaDoCandidato existente = new CompetenciaDoCandidato(id: 5, idCandidato: 4, competencia: java, nivel: "BASICO")
        doCandidatoRepositorio.buscarLigacao(4, 1) >> existente

        when:
        service.adicionar(4, [competencias: [[nome: "java", nivel: "AVANCADO"]]])

        then:
        0 * competenciaRepositorio.save(_)
        1 * doCandidatoRepositorio.save({ CompetenciaDoCandidato ligacao -> ligacao.is(existente) && ligacao.nivel == "AVANCADO" })
    }

    def "adicionar ao candidato inexistente é recurso não encontrado e não grava nada"() {
        when:
        service.adicionar(9, [competencias: [[nome: "Java", nivel: "BASICO"]]])

        then:
        RecursoNaoEncontradoException erro = thrown()
        erro.message == "Candidato 9 não encontrado"
        0 * doCandidatoRepositorio.save(_)
    }

    def "adicionar recusa o corpo inválido antes de consultar o MS Núcleo"() {
        given:
        ServicoNucleo nucleo = Mock()
        CompetenciaDoCandidatoService semConsulta = new CompetenciaDoCandidatoService(new CompetenciaService(competenciaRepositorio), doCandidatoRepositorio, nucleo)

        when:
        semConsulta.adicionar(4, corpo)

        then:
        DadosInvalidosException erro = thrown()
        erro.message == mensagem
        0 * nucleo._

        where:
        corpo                                              | mensagem
        [:]                                                | "O campo 'competencias' deve ser uma lista com ao menos uma competência"
        [competencias: []]                                 | "O campo 'competencias' deve ter ao menos uma competência"
        [competencias: [[nome: "  ", nivel: "BASICO"]]]    | "O campo 'competencias' deve ter ao menos uma competência"
        [competencias: ["Java"]]                           | "Cada item do campo 'competencias' deve ser um objeto com 'nome' e 'nivel'"
        [competencias: [[nome: "Java", nivel: "EXPERT"]]]  | "O campo 'nivel' de cada competência deve ser BASICO, MEDIO, AVANCADO"
        [competencias: [[nome: "x" * 51, nivel: "MEDIO"]]] | "O 'nome' de cada competência deve ter no máximo 50 caracteres"
    }

    def "listar de candidato inexistente é recurso não encontrado"() {
        when:
        service.listar(9)

        then:
        thrown(RecursoNaoEncontradoException)
    }

    def "listarDosCandidatos agrupa as competências por candidato"() {
        given:
        doCandidatoRepositorio.listarDosCandidatos([4, 6]) >> [
                new CompetenciaDoCandidato(idCandidato: 4, competencia: java, nivel: "MEDIO"),
                new CompetenciaDoCandidato(idCandidato: 6, competencia: java, nivel: "BASICO")
        ]

        when:
        Map<Integer, List<CompetenciaDoCandidato>> porCandidato = service.listarDosCandidatos([4, 6])

        then:
        porCandidato.keySet() == [4, 6] as Set
        porCandidato[4]*.nivel == ["MEDIO"]
    }

    def "listarDosCandidatos sem ids não consulta o banco"() {
        when:
        Map<Integer, List<CompetenciaDoCandidato>> porCandidato = service.listarDosCandidatos([])

        then:
        porCandidato == [:]
        0 * doCandidatoRepositorio._
    }
}
