package org.linketinder.emails.service

import org.linketinder.emails.model.EmailDeVaga
import org.linketinder.emails.model.VagaCriada

import java.time.format.DateTimeFormatter

/**
 *
 * @author Guilherme Lima Conte
 */

class ModeloDeEmailDeVaga {

    static final String NAO_INFORMADO = "Não informado"

    private static final DateTimeFormatter FORMATO_DA_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")

    static EmailDeVaga montar(VagaCriada vaga) {
        return new EmailDeVaga(assunto: "Nova vaga no Linketinder: ${vaga.nome}".toString(), texto: textoDe(vaga))
    }

    private static String textoDe(VagaCriada vaga) {
        return """\
Olá!

Uma nova vaga acabou de ser publicada no Linketinder e pode combinar com você.

Vaga: ${vaga.nome}
Descrição: ${ouNaoInformado(vaga.descricao)}
Competências: ${ouNaoInformado(vaga.competencias.join(", "))}
Local: ${localDe(vaga)}
Publicada em: ${vaga.dataHoraCriacao ? FORMATO_DA_DATA.format(vaga.dataHoraCriacao) : NAO_INFORMADO}

Acesse o Linketinder para ver a vaga e curtir se tiver interesse.

Este é um e-mail automático. Não é preciso respondê-lo.
""".toString()
    }

    private static String localDe(VagaCriada vaga) {
        List<String> partes = [vaga.cidade, vaga.estado].findAll { String parte -> parte?.trim() }
        return partes ? partes.join("/") : NAO_INFORMADO
    }

    private static String ouNaoInformado(String valor) {
        return valor?.trim() ? valor.trim() : NAO_INFORMADO
    }
}
