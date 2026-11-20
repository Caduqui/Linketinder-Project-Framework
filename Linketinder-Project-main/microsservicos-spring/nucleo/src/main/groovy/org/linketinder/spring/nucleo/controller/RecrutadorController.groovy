package org.linketinder.spring.nucleo.controller

import org.linketinder.spring.nucleo.model.Recrutador
import org.linketinder.spring.nucleo.service.RecrutadorService
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
@RequestMapping("/recrutadores")
class RecrutadorController {

    private final RecrutadorService recrutadorService

    RecrutadorController(RecrutadorService recrutadorService) {
        this.recrutadorService = recrutadorService
    }

    @PostMapping
    ResponseEntity<Map<String, Object>> cadastrar(@RequestBody Map<String, Object> corpo) {
        return ResponseEntity.status(HttpStatus.CREATED).body(Representacao.deRecrutador(recrutadorService.cadastrar(corpo)))
    }

    @GetMapping
    List<Map<String, Object>> listarPorEmpresa(@RequestParam(name = "idEmpresa", required = false) Integer idEmpresa) {
        return recrutadorService.listarPorEmpresa(idEmpresa).collect { Recrutador recrutador -> Representacao.deRecrutador(recrutador) }
    }

    @DeleteMapping("/{id}")
    ResponseEntity<Void> excluir(@PathVariable("id") Integer id) {
        recrutadorService.excluir(id)
        return ResponseEntity.noContent().build()
    }
}
