package org.linketinder.vagas.http

import io.micronaut.core.annotation.Nullable
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Delete
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Put
import io.micronaut.http.annotation.QueryValue
import io.micronaut.scheduling.TaskExecutors
import io.micronaut.scheduling.annotation.ExecuteOn
import org.linketinder.vagas.model.Vaga
import org.linketinder.vagas.service.RegraDeNegocioException
import org.linketinder.vagas.service.VagaService

/**
 *
 * @author Guilherme Lima Conte
 */

@Controller("/vagas")
@ExecuteOn(TaskExecutors.BLOCKING)
class VagaController {

    static final String ID_EMPRESA_INVALIDO = "O parâmetro 'idEmpresa' deve ser um número"

    private final VagaService vagaService

    VagaController(VagaService vagaService) {
        this.vagaService = vagaService
    }

    @Get
    HttpResponse<List<Map<String, Object>>> listar(@QueryValue @Nullable String idEmpresa) {
        List<Vaga> vagas = idEmpresa == null ? vagaService.listar() : vagaService.listarPorEmpresa(lerIdEmpresa(idEmpresa))
        return HttpResponse.ok(vagas.collect { Vaga vaga -> Json.deVaga(vaga) })
    }

    @Get("/{id}")
    HttpResponse<Map<String, Object>> buscar(@PathVariable String id) {
        return HttpResponse.ok(Json.deVaga(vagaService.buscar(CorpoJson.lerId(id))))
    }

    @Post
    HttpResponse<Map<String, Object>> cadastrar(@Body @Nullable Map<String, Object> corpo) {
        Map<String, Object> dados = CorpoJson.exigir(corpo)
        Vaga vaga = vagaService.cadastrar(dados, CorpoJson.exigirInteiro(dados, "idEmpresa"))

        return HttpResponse.created(Json.deVaga(vaga))
                .header("Location", "/vagas/${vaga.id}".toString())
    }

    @Put("/{id}")
    HttpResponse<Map<String, Object>> atualizar(@PathVariable String id, @Body @Nullable Map<String, Object> corpo) {
        Integer idVaga = CorpoJson.lerId(id)
        return HttpResponse.ok(Json.deVaga(vagaService.atualizar(idVaga, CorpoJson.exigir(corpo))))
    }

    @Delete("/{id}")
    HttpResponse excluir(@PathVariable String id) {
        vagaService.excluir(CorpoJson.lerId(id))
        return HttpResponse.noContent()
    }

    private static Integer lerIdEmpresa(String valor) {
        try {
            return Integer.parseInt(valor)
        } catch (NumberFormatException e) {
            throw new RegraDeNegocioException(ID_EMPRESA_INVALIDO)
        }
    }
}
