package org.linketinder.spring.competencias.controller

import org.linketinder.spring.competencias.model.CompetenciaDoCandidato
import org.linketinder.spring.competencias.service.CompetenciaDoCandidatoService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 *
 * @author Guilherme Lima Conte
 */

@RestController
@RequestMapping("/candidatos")
class CompetenciaDoCandidatoController {

    private final CompetenciaDoCandidatoService competenciaDoCandidatoService

    CompetenciaDoCandidatoController(CompetenciaDoCandidatoService competenciaDoCandidatoService) {
        this.competenciaDoCandidatoService = competenciaDoCandidatoService
    }

    @PostMapping("/{idCandidato}/competencias")
    ResponseEntity<Map<String, Object>> adicionar(@PathVariable("idCandidato") Integer idCandidato, @RequestBody Map<String, Object> corpo) {
        List<CompetenciaDoCandidato> competencias = competenciaDoCandidatoService.adicionar(idCandidato, corpo)
        return ResponseEntity.status(HttpStatus.CREATED).body(Representacao.deCompetenciasDoCandidato(idCandidato, competencias))
    }

    @GetMapping("/{idCandidato}/competencias")
    Map<String, Object> listar(@PathVariable("idCandidato") Integer idCandidato) {
        return Representacao.deCompetenciasDoCandidato(idCandidato, competenciaDoCandidatoService.listar(idCandidato))
    }

    @DeleteMapping("/{idCandidato}/competencias")
    ResponseEntity<Void> apagar(@PathVariable("idCandidato") Integer idCandidato) {
        competenciaDoCandidatoService.apagarDoCandidato(idCandidato)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/competencias")
    List<Map<String, Object>> listarEmLote(@RequestParam(name = "ids", required = false) List<Integer> ids) {
        List<Integer> idsCandidatos = ids ?: []
        Map<Integer, List<CompetenciaDoCandidato>> porCandidato = competenciaDoCandidatoService.listarDosCandidatos(idsCandidatos)

        return idsCandidatos.collect { Integer idCandidato ->
            Representacao.deCompetenciasDoCandidato(idCandidato, porCandidato[idCandidato] ?: [])
        }
    }
}
