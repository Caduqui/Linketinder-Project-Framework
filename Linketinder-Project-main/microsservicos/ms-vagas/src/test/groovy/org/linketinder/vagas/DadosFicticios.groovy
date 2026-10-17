package org.linketinder.vagas

import org.linketinder.vagas.model.Vaga

/**
 *
 * @author Guilherme Lima Conte
 */

class DadosFicticios {

    static Map<String, Object> vaga(Map<String, Object> alteracoes = [:]) {
        return [
                idEmpresa   : 3,
                nome        : "Vaga Exemplo",
                descricao   : "Vaga inventada para os testes",
                estado      : "MS",
                cidade      : "Campo Grande",
                competencias: ["Java", "SQL"]
        ] + alteracoes
    }

    static Vaga vagaGravada(Integer id = 10, Integer idEmpresa = 3) {
        return new Vaga(id: id, nome: "Vaga Exemplo", descricao: "", estado: "MS", cidade: "Campo Grande",
                idEmpresa: idEmpresa, nomeEmpresa: "Empresa Exemplo", competencias: ["Java"])
    }
}
