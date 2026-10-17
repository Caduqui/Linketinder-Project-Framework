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
import io.micronaut.http.annotation.QueryValue
import io.micronaut.scheduling.TaskExecutors
import io.micronaut.scheduling.annotation.ExecuteOn
import org.linketinder.usuarios.model.Recrutador
import org.linketinder.usuarios.model.Vaga
import org.linketinder.usuarios.service.RecrutadorService
import org.linketinder.usuarios.service.RegraDeNegocioException

/**
 *
 * @author Guilherme Lima Conte
 */

@Controller("/recrutadores")
@ExecuteOn(TaskExecutors.BLOCKING)
class RecrutadorController {

    static final String ID_EMPRESA_INVALIDO = "O campo 'idEmpresa' é obrigatório e deve ser um número"

    private final RecrutadorService recrutadorService

    RecrutadorController(RecrutadorService recrutadorService) {
        this.recrutadorService = recrutadorService
    }

    @Get
    HttpResponse<List<Map<String, Object>>> listarPorEmpresa(@QueryValue @Nullable String idEmpresa) {
        List<Recrutador> recrutadores = recrutadorService.listarPorEmpresa(lerIdEmpresa(idEmpresa))
        return HttpResponse.ok(recrutadores.collect { Recrutador recrutador -> Json.deRecrutador(recrutador) })
    }

    @Post
    HttpResponse<Map<String, Object>> cadastrar(@Body @Nullable Map<String, Object> corpo) {
        Map<String, Object> dados = CorpoJson.exigir(corpo)
        Recrutador recrutador = recrutadorService.cadastrar(dados, CorpoJson.exigirInteiro(dados, "idEmpresa"))

        return HttpResponse.created(Json.deRecrutador(recrutador))
                .header("Location", "/recrutadores/${recrutador.id}".toString())
    }

    @Put("/{id}")
    HttpResponse<Map<String, Object>> atualizar(@PathVariable String id, @Body @Nullable Map<String, Object> corpo) {
        Integer idRecrutador = CorpoJson.lerId(id)
        return HttpResponse.ok(Json.deRecrutador(recrutadorService.atualizar(idRecrutador, CorpoJson.exigir(corpo))))
    }

    @Delete("/{id}")
    HttpResponse excluir(@PathVariable String id) {
        recrutadorService.excluir(CorpoJson.lerId(id))
        return HttpResponse.noContent()
    }

    @Get("/{id}/vagas")
    HttpResponse<List<Map<String, Object>>> listarVagas(@PathVariable String id) {
        return HttpResponse.ok(paraJson(recrutadorService.listarVagas(CorpoJson.lerId(id))))
    }

    @Post("/{id}/vagas")
    HttpResponse<List<Map<String, Object>>> atribuirVaga(@PathVariable String id, @Body @Nullable Map<String, Object> corpo) {
        Integer idRecrutador = CorpoJson.lerId(id)
        recrutadorService.buscar(idRecrutador)
        Integer idVaga = CorpoJson.exigirInteiro(CorpoJson.exigir(corpo), "idVaga")

        return HttpResponse.created(paraJson(recrutadorService.atribuirVaga(idRecrutador, idVaga)))
    }

    @Delete("/{id}/vagas/{idVaga}")
    HttpResponse removerVaga(@PathVariable String id, @PathVariable String idVaga) {
        recrutadorService.removerVaga(CorpoJson.lerId(id), CorpoJson.lerId(idVaga))
        return HttpResponse.noContent()
    }

    private static Integer lerIdEmpresa(String valor) {
        try {
            return Integer.parseInt(valor)
        } catch (NumberFormatException e) {
            throw new RegraDeNegocioException(ID_EMPRESA_INVALIDO)
        }
    }

    private static List<Map<String, Object>> paraJson(List<Vaga> vagas) {
        return vagas.collect { Vaga vaga -> Json.deVaga(vaga) }
    }
}
