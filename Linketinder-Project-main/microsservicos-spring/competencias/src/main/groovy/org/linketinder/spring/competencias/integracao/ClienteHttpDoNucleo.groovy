package org.linketinder.spring.competencias.integracao

import org.linketinder.spring.competencias.service.ServicoIndisponivelException
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientException

/**
 *
 * @author Guilherme Lima Conte
 */

@Component
class ClienteHttpDoNucleo implements ServicoNucleo {

    private final RestClient clienteDoNucleo

    ClienteHttpDoNucleo(RestClient clienteDoNucleo) {
        this.clienteDoNucleo = clienteDoNucleo
    }

    @Override
    boolean candidatoExiste(Integer idCandidato) {
        int status = consultarStatus(idCandidato)

        if (status == HttpStatus.OK.value()) {
            return true
        }
        if (status == HttpStatus.NOT_FOUND.value()) {
            return false
        }
        throw new ServicoIndisponivelException()
    }

    private int consultarStatus(Integer idCandidato) {
        try {
            return clienteDoNucleo.head()
                    .uri("/candidatos/{id}", idCandidato)
                    .exchange { requisicao, resposta -> resposta.statusCode.value() }
        } catch (RestClientException ignorada) {
            throw new ServicoIndisponivelException()
        }
    }
}
