package org.linketinder.emails.service

import io.micronaut.json.JsonMapper
import org.linketinder.emails.DadosFicticios
import org.linketinder.emails.model.VagaCriada
import spock.lang.Specification

import java.time.OffsetDateTime

/**
 *
 * @author Guilherme Lima Conte
 */

class LeitorDeMensagemSpec extends Specification {

    LeitorDeMensagem leitor = new LeitorDeMensagem(JsonMapper.createDefault())

    def "mensagem completa vira a vaga criada com todos os campos"() {
        when:
        VagaCriada vaga = leitor.ler(DadosFicticios.mensagem())

        then:
        vaga.id == 10
        vaga.nome == "Vaga Exemplo"
        vaga.descricao == "Vaga inventada para os testes"
        vaga.competencias == ["Java", "SQL"]
        vaga.estado == "MS"
        vaga.cidade == "Campo Grande"
        vaga.dataHoraCriacao == OffsetDateTime.parse(DadosFicticios.DATA_HORA_CRIACAO)
    }

    def "campos opcionais ausentes ou nulos não impedem a leitura"() {
        when:
        VagaCriada vaga = leitor.ler('{"id": 10, "nome": "Vaga Exemplo", "descricao": null}')

        then:
        vaga.id == 10
        vaga.descricao == null
        vaga.competencias == []
        vaga.dataHoraCriacao == null
    }

    def "data e hora fora do formato ficam sem valor, sem descartar a mensagem"() {
        expect:
        leitor.ler(DadosFicticios.mensagem(dataHoraCriacao: "ontem")).dataHoraCriacao == null
    }

    def "mensagem inválida é recusada sem repetir o conteúdo: #motivo"() {
        when:
        leitor.ler(mensagem)

        then:
        MensagemInvalidaException erro = thrown()
        !erro.message.contains("Vaga Exemplo")
        !erro.message.contains("{")

        where:
        motivo             | mensagem
        "JSON quebrado"    | '{"nome": "Vaga Exemplo"'
        "não é objeto"     | '["Vaga Exemplo"]'
        "sem id"           | DadosFicticios.mensagem(id: null)
        "id não numérico"  | DadosFicticios.mensagem(id: "dez")
        "sem nome"         | DadosFicticios.mensagem(nome: null)
        "nome vazio"       | DadosFicticios.mensagem(nome: "  ")
        "vazia"            | ""
        "nula"             | null
    }
}
