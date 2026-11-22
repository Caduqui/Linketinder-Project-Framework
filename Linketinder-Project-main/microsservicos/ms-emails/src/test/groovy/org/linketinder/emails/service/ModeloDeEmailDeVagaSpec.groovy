package org.linketinder.emails.service

import org.linketinder.emails.DadosFicticios
import org.linketinder.emails.model.EmailDeVaga
import spock.lang.Specification

/**
 *
 * @author Guilherme Lima Conte
 */

class ModeloDeEmailDeVagaSpec extends Specification {

    def "assunto cita o nome da vaga"() {
        expect:
        ModeloDeEmailDeVaga.montar(DadosFicticios.vagaCriada()).assunto == "Nova vaga no Linketinder: Vaga Exemplo"
    }

    def "corpo tem o texto padrão e os dados da vaga"() {
        when:
        String texto = ModeloDeEmailDeVaga.montar(DadosFicticios.vagaCriada()).texto

        then:
        texto.startsWith("Olá!")
        texto.contains("Uma nova vaga acabou de ser publicada no Linketinder")
        texto.contains("Vaga: Vaga Exemplo")
        texto.contains("Descrição: Vaga inventada para os testes")
        texto.contains("Competências: Java, SQL")
        texto.contains("Local: Campo Grande/MS")
        texto.contains("Publicada em: 21/11/2026 14:05")
    }

    def "campo vazio ou ausente aparece como Não informado"() {
        when:
        String texto = ModeloDeEmailDeVaga.montar(DadosFicticios.vagaCriada(
                descricao: "  ", competencias: [], cidade: null, estado: null, dataHoraCriacao: null)).texto

        then:
        texto.contains("Descrição: Não informado")
        texto.contains("Competências: Não informado")
        texto.contains("Local: Não informado")
        texto.contains("Publicada em: Não informado")
    }

    def "o e-mail não cita empresa nem recrutador e não chama o candidato pelo nome"() {
        when:
        EmailDeVaga email = ModeloDeEmailDeVaga.montar(DadosFicticios.vagaCriada())
        String tudo = (email.assunto + "\n" + email.texto).toLowerCase()

        then:
        !tudo.contains("empresa")
        !tudo.contains("recrutador")
        !tudo.contains("olá, ")
    }
}
