package org.linketinder.usuarios.http

import io.micronaut.core.annotation.Nullable
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Delete
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Put
import io.micronaut.scheduling.TaskExecutors
import io.micronaut.scheduling.annotation.ExecuteOn
import org.linketinder.usuarios.model.Candidato
import org.linketinder.usuarios.service.CandidatoService

/**
 *
 * @author Guilherme Lima Conte
 */

@Controller("/candidatos")
@ExecuteOn(TaskExecutors.BLOCKING)
class CandidatoController {

    private final CandidatoService candidatoService

    CandidatoController(CandidatoService candidatoService) {
        this.candidatoService = candidatoService
    }

    @Get
    HttpResponse<List<Map<String, Object>>> listar() {
        return HttpResponse.ok(candidatoService.listar().collect { Candidato candidato -> Json.deCandidato(candidato) })
    }

    @Get("/{id}")
    HttpResponse<Map<String, Object>> buscar(@PathVariable String id) {
        return HttpResponse.ok(Json.deCandidato(candidatoService.buscar(CorpoJson.lerId(id))))
    }

    @Post
    HttpResponse<Map<String, Object>> cadastrar(@Body @Nullable Map<String, Object> corpo) {
        Candidato candidato = candidatoService.cadastrar(CorpoJson.exigir(corpo))

        return HttpResponse.created(Json.deCandidato(candidato))
                .header("Location", "/candidatos/${candidato.id}".toString())
    }

    @Put("/{id}")
    HttpResponse<Map<String, Object>> atualizar(@PathVariable String id, @Body @Nullable Map<String, Object> corpo) {
        Integer idCandidato = CorpoJson.lerId(id)
        return HttpResponse.ok(Json.deCandidato(candidatoService.atualizar(idCandidato, CorpoJson.exigir(corpo))))
    }

    @Delete("/{id}")
    HttpResponse excluir(@PathVariable String id) {
        candidatoService.excluir(CorpoJson.lerId(id))
        return HttpResponse.noContent()
    }
}
