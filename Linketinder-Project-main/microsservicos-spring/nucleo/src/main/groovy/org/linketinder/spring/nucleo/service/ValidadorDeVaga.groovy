package org.linketinder.spring.nucleo.service

import org.springframework.stereotype.Component
import org.linketinder.spring.nucleo.model.Vaga

import java.util.regex.Pattern

/**
 *
 * @author Guilherme Lima Conte
 */

@Component
class ValidadorDeVaga {

    static final int TAMANHO_MAXIMO_NOME = 100
    static final int TAMANHO_MAXIMO_DESCRICAO = 500
    static final int TAMANHO_MAXIMO_CIDADE = 100
    static final int TAMANHO_MAXIMO_COMPETENCIA = 50

    static final Pattern FORMATO_ESTADO = ~/[A-Za-z]{2}/

    Vaga validar(Map<String, Object> dados) {
        return new Vaga(
                nome: limitar(CamposDoCorpo.exigirTexto(dados, "nome"), "nome", TAMANHO_MAXIMO_NOME),
                descricao: limitar(CamposDoCorpo.texto(dados, "descricao").trim(), "descricao", TAMANHO_MAXIMO_DESCRICAO),
                estado: exigirEstado(dados),
                cidade: limitar(CamposDoCorpo.exigirTexto(dados, "cidade"), "cidade", TAMANHO_MAXIMO_CIDADE),
                idEmpresa: CamposDoCorpo.exigirInteiro(dados, "idEmpresa"),
                competencias: lerCompetencias(dados)
        )
    }

    private static String exigirEstado(Map<String, Object> dados) {
        String estado = CamposDoCorpo.exigirTexto(dados, "estado")

        if (!(estado ==~ FORMATO_ESTADO)) {
            throw new DadosInvalidosException("O campo 'estado' deve ser a sigla da UF, com 2 letras")
        }
        return estado
    }

    private static List<String> lerCompetencias(Map<String, Object> dados) {
        Object lista = dados["competencias"]

        if (lista == null) {
            return []
        }
        if (!(lista instanceof List)) {
            throw new DadosInvalidosException("O campo 'competencias' deve ser uma lista de nomes")
        }

        return (lista as List)
                .collect { Object item -> lerNomeDeCompetencia(item) }
                .findAll { String nome -> !nome.isEmpty() }
                .unique { String nome -> nome.toLowerCase() }
    }

    private static String lerNomeDeCompetencia(Object item) {
        if (!(item instanceof CharSequence)) {
            throw new DadosInvalidosException("Cada item do campo 'competencias' deve ser um texto")
        }
        return limitar(item.toString().trim(), "competencias", TAMANHO_MAXIMO_COMPETENCIA)
    }

    private static String limitar(String valor, String campo, int tamanhoMaximo) {
        if (valor.length() > tamanhoMaximo) {
            throw new DadosInvalidosException("O campo '${campo}' deve ter no máximo ${tamanhoMaximo} caracteres")
        }
        return valor
    }
}
