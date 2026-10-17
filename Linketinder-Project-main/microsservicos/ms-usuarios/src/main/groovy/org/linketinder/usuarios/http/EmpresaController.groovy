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
import org.linketinder.usuarios.model.Empresa
import org.linketinder.usuarios.service.EmpresaService

/**
 *
 * @author Guilherme Lima Conte
 */

@Controller("/empresas")
@ExecuteOn(TaskExecutors.BLOCKING)
class EmpresaController {

    private final EmpresaService empresaService

    EmpresaController(EmpresaService empresaService) {
        this.empresaService = empresaService
    }

    @Get
    HttpResponse<List<Map<String, Object>>> listar() {
        return HttpResponse.ok(empresaService.listar().collect { Empresa empresa -> Json.deEmpresa(empresa) })
    }

    @Get("/{id}")
    HttpResponse<Map<String, Object>> buscar(@PathVariable String id) {
        return HttpResponse.ok(Json.deEmpresa(empresaService.buscar(CorpoJson.lerId(id))))
    }

    @Post
    HttpResponse<Map<String, Object>> cadastrar(@Body @Nullable Map<String, Object> corpo) {
        Empresa empresa = empresaService.cadastrar(CorpoJson.exigir(corpo))

        return HttpResponse.created(Json.deEmpresa(empresa))
                .header("Location", "/empresas/${empresa.id}".toString())
    }

    @Put("/{id}")
    HttpResponse<Map<String, Object>> atualizar(@PathVariable String id, @Body @Nullable Map<String, Object> corpo) {
        Integer idEmpresa = CorpoJson.lerId(id)
        return HttpResponse.ok(Json.deEmpresa(empresaService.atualizar(idEmpresa, CorpoJson.exigir(corpo))))
    }

    @Delete("/{id}")
    HttpResponse excluir(@PathVariable String id) {
        empresaService.excluir(CorpoJson.lerId(id))
        return HttpResponse.noContent()
    }
}
