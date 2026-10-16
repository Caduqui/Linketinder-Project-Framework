package org.linketinder.cadastro.http

import org.linketinder.cadastro.service.DadosInvalidosException

import java.nio.ByteBuffer
import java.nio.charset.CharacterCodingException
import java.nio.charset.CodingErrorAction
import java.nio.charset.StandardCharsets

/**
 *
 * @author Guilherme Lima Conte
 */

class Requisicao {

    final String metodo
    final String caminho
    final Map<String, String> parametros
    private final byte[] corpoRecebido

    Requisicao(String metodo, String caminho, Map<String, String> parametros, byte[] corpoRecebido) {
        this.metodo = metodo
        this.caminho = caminho
        this.parametros = parametros
        this.corpoRecebido = corpoRecebido
    }

    String getCorpo() {
        try {
            return StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(corpoRecebido))
                    .toString()
        } catch (CharacterCodingException e) {
            throw new DadosInvalidosException("O corpo da requisição deve estar em UTF-8")
        }
    }
}
