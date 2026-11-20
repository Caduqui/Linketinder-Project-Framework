package org.linketinder.spring.nucleo.controller

import org.linketinder.spring.nucleo.service.CurtidaService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 *
 * @author Guilherme Lima Conte
 */

@RestController
@RequestMapping("/curtidas")
class CurtidaController {

    private final CurtidaService curtidaService

    CurtidaController(CurtidaService curtidaService) {
        this.curtidaService = curtidaService
    }

    @PostMapping
    ResponseEntity<Map<String, Object>> curtir(@RequestBody Map<String, Object> corpo) {
        return ResponseEntity.status(HttpStatus.CREATED).body([match: curtidaService.curtir(corpo)] as Map<String, Object>)
    }
}
