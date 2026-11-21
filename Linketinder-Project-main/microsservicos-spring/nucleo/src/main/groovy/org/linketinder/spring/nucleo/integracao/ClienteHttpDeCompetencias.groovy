package org.linketinder.spring.nucleo.integracao

import org.linketinder.spring.nucleo.model.CompetenciaDoCandidato
import org.linketinder.spring.nucleo.service.DadosInvalidosException
import org.linketinder.spring.nucleo.service.ServicoIndisponivelException
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestClientResponseException

/**
 *
 * @author Guilherme Lima Conte
 */

@Component
class ClienteHttpDeCompetencias implements ServicoDeCompetencias {

    static final String MENSAGEM_RECUSA = "O serviço de competências recusou as competências enviadas"

    private final RestClient clienteDeCompetencias

    ClienteHttpDeCompetencias(RestClient clienteDeCompetencias) {
        this.clienteDeCompetencias = clienteDeCompetencias
    }

    @Override
    List<String> substituirDaVaga(Integer idVaga, List<String> competencias) {
        Map<String, Object> resposta = chamar {
            clienteDeCompetencias.put()
                    .uri("/vagas/{idVaga}/competencias", idVaga)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body([competencias: competencias])
                    .retrieve()
                    .body(Map)
        } as Map<String, Object>
        return resposta.competencias as List<String>
    }

    @Override
    Map<Integer, List<String>> buscarDasVagas(List<Integer> idsVagas) {
        return buscarEmLote("/vagas/competencias?ids={ids}", idsVagas).collectEntries { Map<String, Object> item ->
            [(item.id as Integer): item.competencias as List<String>]
        } as Map<Integer, List<String>>
    }

    @Override
    void apagarDaVaga(Integer idVaga) {
        apagar("/vagas/{id}/competencias", idVaga)
    }

    @Override
    Map<Integer, List<CompetenciaDoCandidato>> buscarDosCandidatos(List<Integer> idsCandidatos) {
        return buscarEmLote("/candidatos/competencias?ids={ids}", idsCandidatos).collectEntries { Map<String, Object> item ->
            [(item.id as Integer): lerCompetenciasDoCandidato(item.competencias as List<Map<String, Object>>)]
        } as Map<Integer, List<CompetenciaDoCandidato>>
    }

    @Override
    void apagarDoCandidato(Integer idCandidato) {
        apagar("/candidatos/{id}/competencias", idCandidato)
    }

    private List<Map<String, Object>> buscarEmLote(String caminho, List<Integer> ids) {
        if (ids.isEmpty()) {
            return []
        }
        return chamar {
            clienteDeCompetencias.get().uri(caminho, ids.join(",")).retrieve().body(List)
        } as List<Map<String, Object>>
    }

    private void apagar(String caminho, Integer id) {
        chamar {
            clienteDeCompetencias.delete().uri(caminho, id).retrieve().toBodilessEntity()
        }
    }

    private static List<CompetenciaDoCandidato> lerCompetenciasDoCandidato(List<Map<String, Object>> competencias) {
        return competencias.collect { Map<String, Object> competencia ->
            new CompetenciaDoCandidato(nome: competencia.nome as String, nivel: competencia.nivel as String)
        }
    }

    private static Object chamar(Closure requisicao) {
        try {
            return requisicao()
        } catch (RestClientResponseException falha) {
            if (falha.statusCode.value() == HttpStatus.BAD_REQUEST.value()) {
                throw new DadosInvalidosException(lerMensagemDeRecusa(falha))
            }
            throw new ServicoIndisponivelException()
        } catch (RestClientException ignorada) {
            throw new ServicoIndisponivelException()
        }
    }

    private static String lerMensagemDeRecusa(RestClientResponseException falha) {
        try {
            Map<String, Object> corpo = falha.getResponseBodyAs(Map) as Map<String, Object>
            return corpo?.erro instanceof String ? corpo.erro : MENSAGEM_RECUSA
        } catch (Exception ignorada) {
            return MENSAGEM_RECUSA
        }
    }
}
