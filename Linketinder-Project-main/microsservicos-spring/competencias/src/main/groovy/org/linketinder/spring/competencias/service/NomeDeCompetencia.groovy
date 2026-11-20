package org.linketinder.spring.competencias.service

/**
 *
 * @author Guilherme Lima Conte
 */

class NomeDeCompetencia {

    static final int TAMANHO_MAXIMO = 50

    static String validar(Map<String, Object> dados, String campo) {
        return limitar(CamposDoCorpo.exigirTexto(dados, campo), "O campo '${campo}' deve ter no máximo ${TAMANHO_MAXIMO} caracteres")
    }

    static String limitar(String nome, String mensagem) {
        if (nome.length() > TAMANHO_MAXIMO) {
            throw new DadosInvalidosException(mensagem)
        }
        return nome
    }
}
