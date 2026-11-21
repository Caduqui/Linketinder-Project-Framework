package org.linketinder.spring.nucleo.controller

import org.linketinder.spring.nucleo.model.CandidatoAnonimo
import org.linketinder.spring.nucleo.service.CandidatoService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

/**
 *
 * @author Guilherme Lima Conte
 */

@RestController
@RequestMapping("/candidatos")
class CandidatoController {

    private final CandidatoService candidatoService

    CandidatoController(CandidatoService candidatoService) {
        this.candidatoService = candidatoService
    }

    @PostMapping
    ResponseEntity<Map<String, Object>> cadastrar(@RequestBody Map<String, Object> corpo) {
        return ResponseEntity.status(HttpStatus.CREATED).body(Representacao.deCandidato(candidatoService.cadastrar(corpo)))
    }

    @GetMapping
    List<Map<String, Object>> listar() {
        return candidatoService.listar().collect { CandidatoAnonimo candidato -> Representacao.deCandidatoAnonimo(candidato) }
    }

    @GetMapping("/{id}")
    Map<String, Object> buscar(@PathVariable("id") Integer id) {
        return Representacao.deCandidatoAnonimo(candidatoService.buscar(id))
    }

    @RequestMapping(path = "/{id}", method = RequestMethod.HEAD)
    ResponseEntity<Void> conferirExistencia(@PathVariable("id") Integer id) {
        return candidatoService.existe(id) ? ResponseEntity.ok().build() : ResponseEntity.notFound().build()
    }

    @PutMapping("/{id}")
    Map<String, Object> atualizar(@PathVariable("id") Integer id, @RequestBody Map<String, Object> corpo) {
        return Representacao.deCandidato(candidatoService.atualizar(id, corpo))
    }

    @DeleteMapping("/{id}")
    ResponseEntity<Void> excluir(@PathVariable("id") Integer id) {
        candidatoService.excluir(id)
        return ResponseEntity.noContent().build()
    }
}
