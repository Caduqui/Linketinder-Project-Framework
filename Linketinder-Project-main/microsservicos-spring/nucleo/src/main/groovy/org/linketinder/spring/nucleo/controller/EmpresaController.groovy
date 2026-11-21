package org.linketinder.spring.nucleo.controller

import org.linketinder.spring.nucleo.service.EmpresaService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
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
@RequestMapping("/empresas")
class EmpresaController {

    private final EmpresaService empresaService

    EmpresaController(EmpresaService empresaService) {
        this.empresaService = empresaService
    }

    @PostMapping
    ResponseEntity<Map<String, Object>> cadastrar(@RequestBody Map<String, Object> corpo) {
        return ResponseEntity.status(HttpStatus.CREATED).body(Representacao.deEmpresa(empresaService.cadastrar(corpo)))
    }

    @PutMapping("/{id}")
    Map<String, Object> atualizar(@PathVariable("id") Integer id, @RequestBody Map<String, Object> corpo) {
        return Representacao.deEmpresa(empresaService.atualizar(id, corpo))
    }

    @DeleteMapping("/{id}")
    ResponseEntity<Void> excluir(@PathVariable("id") Integer id) {
        empresaService.excluir(id)
        return ResponseEntity.noContent().build()
    }
}
