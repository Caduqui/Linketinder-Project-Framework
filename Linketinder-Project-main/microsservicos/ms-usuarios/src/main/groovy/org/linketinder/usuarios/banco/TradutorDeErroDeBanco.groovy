package org.linketinder.usuarios.banco

import org.linketinder.usuarios.service.CadastroRepetidoException

import java.sql.SQLException

/**
 *
 * @author Guilherme Lima Conte
 */

class TradutorDeErroDeBanco {

    static final String VIOLACAO_DE_UNICIDADE = "23505"

    static Exception traduzir(SQLException falha, String mensagemDeRepetido) {
        return falha.SQLState == VIOLACAO_DE_UNICIDADE ? new CadastroRepetidoException(mensagemDeRepetido) : falha
    }
}
