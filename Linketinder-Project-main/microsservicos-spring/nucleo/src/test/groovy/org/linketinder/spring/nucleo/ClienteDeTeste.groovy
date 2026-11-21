package org.linketinder.spring.nucleo

import groovy.json.JsonOutput
import org.linketinder.spring.nucleo.http.RegistroDoServidor
import org.linketinder.spring.nucleo.http.TratadorDeErros
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.setup.MockMvcBuilders

/**
 *
 * @author Guilherme Lima Conte
 */

class ClienteDeTeste {

    private final MockMvc mockMvc

    ClienteDeTeste(Object... controllers) {
        mockMvc = MockMvcBuilders.standaloneSetup(controllers)
                .setControllerAdvice(new TratadorDeErros(new RegistroDoServidor()))
                .build()
    }

    RespostaDeTeste get(String caminho) {
        return enviar(MockMvcRequestBuilders.get(caminho))
    }

    RespostaDeTeste head(String caminho) {
        return enviar(MockMvcRequestBuilders.head(caminho))
    }

    RespostaDeTeste post(String caminho, Object corpo) {
        return enviar(MockMvcRequestBuilders.post(caminho), corpo)
    }

    RespostaDeTeste put(String caminho, Object corpo) {
        return enviar(MockMvcRequestBuilders.put(caminho), corpo)
    }

    RespostaDeTeste delete(String caminho) {
        return enviar(MockMvcRequestBuilders.delete(caminho))
    }

    private RespostaDeTeste enviar(MockHttpServletRequestBuilder requisicao, Object corpo = null) {
        if (corpo != null) {
            requisicao.contentType(MediaType.APPLICATION_JSON).content(corpo instanceof String ? corpo : JsonOutput.toJson(corpo))
        }
        return new RespostaDeTeste(mockMvc.perform(requisicao).andReturn().response)
    }
}
