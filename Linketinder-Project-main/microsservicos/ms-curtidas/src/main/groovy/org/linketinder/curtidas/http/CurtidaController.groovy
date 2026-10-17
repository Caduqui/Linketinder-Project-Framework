package org.linketinder.curtidas.http

import io.micronaut.core.annotation.Nullable
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Post
import io.micronaut.scheduling.TaskExecutors
import io.micronaut.scheduling.annotation.ExecuteOn
import org.linketinder.curtidas.model.Curtida
import org.linketinder.curtidas.service.CurtidaService
import org.linketinder.curtidas.service.RegraDeNegocioException

/**
 *
 * @author Guilherme Lima Conte
 */

@Controller("/curtidas")
@ExecuteOn(TaskExecutors.BLOCKING)
class CurtidaController {

    private final CurtidaService curtidaService

    CurtidaController(CurtidaService curtidaService) {
        this.curtidaService = curtidaService
    }

    @Post
    HttpResponse<Map<String, Object>> curtir(@Body @Nullable Map<String, Object> corpo) {
        Map<String, Object> dados = CorpoJson.exigir(corpo)
        Curtida curtida = new Curtida(
                tipo: exigirTexto(dados, "tipo"),
                idCandidato: CorpoJson.exigirInteiro(dados, "idCandidato"),
                idVaga: CorpoJson.exigirInteiro(dados, "idVaga"),
                idEmpresa: inteiroOpcional(dados, "idEmpresa"),
                idRecrutador: inteiroOpcional(dados, "idRecrutador")
        )

        return HttpResponse.created([match: curtidaService.curtir(curtida)] as Map<String, Object>)
    }

    private static String exigirTexto(Map<String, Object> dados, String campo) {
        Object valor = dados[campo]

        if (!(valor instanceof CharSequence) || valor.toString().trim().isEmpty()) {
            throw new RegraDeNegocioException("O campo '${campo}' é obrigatório")
        }
        return valor.toString().trim()
    }

    private static Integer inteiroOpcional(Map<String, Object> dados, String campo) {
        Object valor = dados[campo]
        return valor instanceof Number ? ((Number) valor).intValue() : null
    }
}
