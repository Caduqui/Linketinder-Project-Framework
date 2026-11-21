package org.linketinder.spring.nucleo.controller

import org.linketinder.spring.nucleo.service.LoginService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 *
 * @author Guilherme Lima Conte
 */

@RestController
@RequestMapping("/login")
class LoginController {

    private final LoginService loginService

    LoginController(LoginService loginService) {
        this.loginService = loginService
    }

    @PostMapping
    Map<String, Object> entrar(@RequestBody Map<String, Object> corpo) {
        return Representacao.deUsuarioLogado(loginService.autenticar(corpo))
    }
}
