package org.linketinder.spring.nucleo.controller

import org.linketinder.spring.nucleo.service.AtribuicaoDeVagaService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 *
 * @author Guilherme Lima Conte
 */

@RestController
@RequestMapping("/recrutadores/{idRecrutador}/vagas")
class VagaDoRecrutadorController {

    private final AtribuicaoDeVagaService atribuicaoDeVagaService

    VagaDoRecrutadorController(AtribuicaoDeVagaService atribuicaoDeVagaService) {
        this.atribuicaoDeVagaService = atribuicaoDeVagaService
    }

    @GetMapping
    List<Map<String, Object>> listar(@PathVariable("idRecrutador") Integer idRecrutador) {
        return Representacao.deVagas(atribuicaoDeVagaService.listar(idRecrutador))
    }

    @PostMapping
    ResponseEntity<List<Map<String, Object>>> atribuir(@PathVariable("idRecrutador") Integer idRecrutador, @RequestBody Map<String, Object> corpo) {
        return ResponseEntity.status(HttpStatus.CREATED).body(Representacao.deVagas(atribuicaoDeVagaService.atribuir(idRecrutador, corpo)))
    }

    @DeleteMapping("/{idVaga}")
    ResponseEntity<Void> remover(@PathVariable("idRecrutador") Integer idRecrutador, @PathVariable("idVaga") Integer idVaga) {
        atribuicaoDeVagaService.remover(idRecrutador, idVaga)
        return ResponseEntity.noContent().build()
    }
}
