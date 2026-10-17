package org.linketinder.usuarios.http

import io.micronaut.core.annotation.Nullable
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Post
import io.micronaut.scheduling.TaskExecutors
import io.micronaut.scheduling.annotation.ExecuteOn
import org.linketinder.usuarios.service.LoginService
import org.linketinder.usuarios.service.RegraDeNegocioException

/**
 *
 * @author Guilherme Lima Conte
 */

@Controller("/login")
@ExecuteOn(TaskExecutors.BLOCKING)
class LoginController {

    private final LoginService loginService

    LoginController(LoginService loginService) {
        this.loginService = loginService
    }

    @Post
    HttpResponse<Map<String, Object>> entrar(@Body @Nullable Map<String, Object> corpo) {
        Map<String, Object> dados = CorpoJson.exigir(corpo)
        String tipo = exigirTexto(dados, "tipo").trim()
        String email = exigirTexto(dados, "email").trim()
        String senha = exigirTexto(dados, "senha")

        return HttpResponse.ok(loginService.autenticar(tipo, email, senha))
    }

    private static String exigirTexto(Map<String, Object> dados, String campo) {
        Object valor = dados[campo]

        if (!(valor instanceof CharSequence) || valor.toString().trim().isEmpty()) {
            throw new RegraDeNegocioException("O campo '${campo}' é obrigatório")
        }
        return valor.toString()
    }
}
