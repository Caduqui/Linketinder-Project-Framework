package org.linketinder.vagas.model

/**
 *
 * @author Guilherme Lima Conte
 */

class Vaga {

    Integer id
    String nome
    String descricao
    String estado
    String cidade
    Integer idEmpresa
    String nomeEmpresa
    List<String> competencias = []
}
