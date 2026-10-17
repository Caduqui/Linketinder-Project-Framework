package org.linketinder.competencias.http

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
import org.linketinder.competencias.model.Candidato
import org.linketinder.competencias.model.Competencia
import org.linketinder.competencias.service.CatalogoDeCompetenciasService
import org.linketinder.competencias.service.CompetenciaDoCandidatoService

/**
 *
 * @author Guilherme Lima Conte
 */

@Controller("/competencias")
@ExecuteOn(TaskExecutors.BLOCKING)
class CompetenciaController {

    private final CatalogoDeCompetenciasService catalogo
    private final CompetenciaDoCandidatoService competenciaDoCandidato

    CompetenciaController(CatalogoDeCompetenciasService catalogo, CompetenciaDoCandidatoService competenciaDoCandidato) {
        this.catalogo = catalogo
        this.competenciaDoCandidato = competenciaDoCandidato
    }

    @Get
    HttpResponse<List<Map<String, Object>>> listar() {
        return HttpResponse.ok(catalogo.listar().collect { Competencia competencia -> Json.deCompetencia(competencia) })
    }

    @Post
    HttpResponse<Map<String, Object>> adicionarAoCandidato(@Body @Nullable Map<String, Object> corpo) {
        Map<String, Object> dados = CorpoJson.exigir(corpo)
        Integer idCandidato = CorpoJson.exigirInteiro(dados, "idCandidato")
        Candidato candidato = competenciaDoCandidato.adicionar(idCandidato, dados.competencias)

        return HttpResponse.created(Json.deCandidato(candidato))
    }

    @Post("/catalogo")
    HttpResponse<Map<String, Object>> cadastrar(@Body @Nullable Map<String, Object> corpo) {
        Competencia competencia = catalogo.cadastrar(CorpoJson.exigir(corpo).nome)

        return HttpResponse.created(Json.deCompetencia(competencia))
                .header("Location", "/competencias/${competencia.id}".toString())
    }

    @Put("/{id}")
    HttpResponse<Map<String, Object>> renomear(@PathVariable String id, @Body @Nullable Map<String, Object> corpo) {
        Integer idCompetencia = CorpoJson.lerId(id)
        Competencia competencia = catalogo.renomear(idCompetencia, CorpoJson.exigir(corpo).nome)

        return HttpResponse.ok(Json.deCompetencia(competencia))
    }

    @Delete("/{id}")
    HttpResponse excluir(@PathVariable String id) {
        catalogo.excluir(CorpoJson.lerId(id))
        return HttpResponse.noContent()
    }
}
