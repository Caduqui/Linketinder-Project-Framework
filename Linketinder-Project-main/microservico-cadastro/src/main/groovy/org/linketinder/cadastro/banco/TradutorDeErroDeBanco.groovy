package org.linketinder.cadastro.banco

import org.linketinder.cadastro.service.CadastroRepetidoException

import java.sql.SQLException

/**
 *
 * @author Guilherme Lima Conte
 */

class TradutorDeErroDeBanco {

    static final String VIOLACAO_DE_UNICIDADE = "23505"

    static Exception traduzir(SQLException falha) {
        return falha.SQLState == VIOLACAO_DE_UNICIDADE ? new CadastroRepetidoException() : falha
    }
}
