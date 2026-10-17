package org.linketinder.vagas.http

import org.linketinder.vagas.model.Vaga

/**
 *
 * @author Guilherme Lima Conte
 */

class Json {

    static Map<String, Object> deVaga(Vaga vaga) {
        return [
                id          : vaga.id,
                nome        : vaga.nome,
                descricao   : vaga.descricao,
                estado      : vaga.estado,
                cidade      : vaga.cidade,
                empresa     : [id: vaga.idEmpresa, nome: vaga.nomeEmpresa],
                competencias: vaga.competencias
        ]
    }
}
