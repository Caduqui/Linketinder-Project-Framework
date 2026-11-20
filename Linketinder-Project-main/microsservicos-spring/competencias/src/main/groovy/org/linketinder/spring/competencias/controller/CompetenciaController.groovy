package org.linketinder.spring.competencias.controller

import org.linketinder.spring.competencias.model.Competencia
import org.linketinder.spring.competencias.service.CompetenciaService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 *
 * @author Guilherme Lima Conte
 */

@RestController
@RequestMapping("/competencias")
class CompetenciaController {

    private final CompetenciaService competenciaService

    CompetenciaController(CompetenciaService competenciaService) {
        this.competenciaService = competenciaService
    }

    @GetMapping
    List<Map<String, Object>> listar() {
        return competenciaService.listar().collect { Competencia competencia -> Representacao.deCompetencia(competencia) }
    }

    @GetMapping("/{id}")
    Map<String, Object> buscar(@PathVariable("id") Integer id) {
        return Representacao.deCompetencia(competenciaService.buscar(id))
    }

    @PostMapping
    ResponseEntity<Map<String, Object>> criar(@RequestBody Map<String, Object> corpo) {
        return ResponseEntity.status(HttpStatus.CREATED).body(Representacao.deCompetencia(competenciaService.criar(corpo)))
    }

    @PutMapping("/{id}")
    Map<String, Object> renomear(@PathVariable("id") Integer id, @RequestBody Map<String, Object> corpo) {
        return Representacao.deCompetencia(competenciaService.renomear(id, corpo))
    }

    @DeleteMapping("/{id}")
    ResponseEntity<Void> excluir(@PathVariable("id") Integer id) {
        competenciaService.excluir(id)
        return ResponseEntity.noContent().build()
    }
}
