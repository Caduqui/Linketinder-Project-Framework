package org.linketinder.spring.competencias.controller

import org.linketinder.spring.competencias.service.CompetenciaDaVagaService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 *
 * @author Guilherme Lima Conte
 */

@RestController
@RequestMapping("/vagas")
class CompetenciaDaVagaController {

    private final CompetenciaDaVagaService competenciaDaVagaService

    CompetenciaDaVagaController(CompetenciaDaVagaService competenciaDaVagaService) {
        this.competenciaDaVagaService = competenciaDaVagaService
    }

    @PutMapping("/{idVaga}/competencias")
    Map<String, Object> substituir(@PathVariable("idVaga") Integer idVaga, @RequestBody Map<String, Object> corpo) {
        return Representacao.deCompetenciasDaVaga(idVaga, competenciaDaVagaService.substituir(idVaga, corpo))
    }

    @DeleteMapping("/{idVaga}/competencias")
    ResponseEntity<Void> apagar(@PathVariable("idVaga") Integer idVaga) {
        competenciaDaVagaService.apagarDaVaga(idVaga)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/competencias")
    List<Map<String, Object>> listarEmLote(@RequestParam(name = "ids", required = false) List<Integer> ids) {
        List<Integer> idsVagas = ids ?: []
        Map<Integer, List<String>> porVaga = competenciaDaVagaService.listarDasVagas(idsVagas)

        return idsVagas.collect { Integer idVaga -> Representacao.deCompetenciasDaVaga(idVaga, porVaga[idVaga] ?: []) }
    }
}
