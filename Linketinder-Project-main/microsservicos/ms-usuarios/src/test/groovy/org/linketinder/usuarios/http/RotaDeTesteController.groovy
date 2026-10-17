package org.linketinder.usuarios.http

import io.micronaut.context.annotation.Requires
import io.micronaut.context.env.Environment
import io.micronaut.core.annotation.Nullable
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.Post
import org.linketinder.usuarios.service.CadastroRepetidoException
import org.linketinder.usuarios.service.RecursoNaoEncontradoException
import org.linketinder.usuarios.service.RegraDeNegocioException

import java.sql.SQLException

/**
 *
 * @author Guilherme Lima Conte
 */

@Requires(env = Environment.TEST)
@Controller("/teste")
class RotaDeTesteController {

    @Get("/{id}")
    HttpResponse<Map<String, Object>> buscar(@PathVariable String id) {
        return HttpResponse.ok([id: CorpoJson.lerId(id)])
    }

    @Post
    HttpResponse<Map<String, Object>> receber(@Body @Nullable Map<String, Object> corpo) {
        Map<String, Object> dados = CorpoJson.exigir(corpo)
        return HttpResponse.created([numero: CorpoJson.exigirInteiro(dados, "numero")])
    }

    @Get("/falhas/regra")
    HttpResponse<Map> regra() {
        throw new RegraDeNegocioException("Regra violada")
    }

    @Get("/falhas/inexistente")
    HttpResponse<Map> inexistente() {
        throw new RecursoNaoEncontradoException("Recurso não encontrado")
    }

    @Get("/falhas/repetido")
    HttpResponse<Map> repetido() {
        throw new CadastroRepetidoException()
    }

    @Get("/falhas/banco")
    HttpResponse<Map> banco() {
        throw new SQLException("detalhe técnico com candidata@example.com", "08006")
    }
}
