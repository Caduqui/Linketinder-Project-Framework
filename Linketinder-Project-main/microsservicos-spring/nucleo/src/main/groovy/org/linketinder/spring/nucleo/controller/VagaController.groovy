package org.linketinder.spring.nucleo.controller

import org.linketinder.spring.nucleo.service.VagaService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
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
class VagaController {

    private final VagaService vagaService

    VagaController(VagaService vagaService) {
        this.vagaService = vagaService
    }

    @PostMapping
    ResponseEntity<Map<String, Object>> cadastrar(@RequestBody Map<String, Object> corpo) {
        return ResponseEntity.status(HttpStatus.CREATED).body(Representacao.deVaga(vagaService.cadastrar(corpo)))
    }

    @GetMapping
    List<Map<String, Object>> listar(@RequestParam(name = "idEmpresa", required = false) Integer idEmpresa) {
        return Representacao.deVagas(vagaService.listar(idEmpresa))
    }

    @GetMapping("/{id}")
    Map<String, Object> buscar(@PathVariable("id") Integer id) {
        return Representacao.deVaga(vagaService.buscar(id))
    }

    @PutMapping("/{id}")
    Map<String, Object> atualizar(@PathVariable("id") Integer id, @RequestBody Map<String, Object> corpo) {
        return Representacao.deVaga(vagaService.atualizar(id, corpo))
    }

    @DeleteMapping("/{id}")
    ResponseEntity<Void> excluir(@PathVariable("id") Integer id, @RequestParam(name = "idEmpresa", required = false) Integer idEmpresa) {
        vagaService.excluir(id, idEmpresa)
        return ResponseEntity.noContent().build()
    }
}
