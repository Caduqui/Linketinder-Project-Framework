package org.example.view

import java.time.LocalDate
import java.time.format.DateTimeParseException

/**
 *
 * @author Guilherme Lima Conte
 */

class EntradaDados {
    static final int TAMANHO_MINIMO_SENHA = 6
    final LeitorEntrada leitor

    EntradaDados(LeitorEntrada leitor) {
        this.leitor = leitor
    }

    String lerTexto(String mensagem) {
        return leitor.lerLinha(mensagem).trim()
    }

    Integer lerId(String mensagem) {
        try {
            return Integer.parseInt(lerTexto(mensagem))
        } catch (NumberFormatException e) {
            println "ID inválido"
            return null
        }
    }

    String lerSenha() {
        String senha = leitor.lerLinha("Senha: ")
        while (senha.length() < TAMANHO_MINIMO_SENHA) {
            println "A senha deve possuir no mínimo ${TAMANHO_MINIMO_SENHA} caracteres"
            senha = leitor.lerLinha("Senha: ")
        }
        return senha
    }

    LocalDate lerDataNascimento() {
        while(true) {
            try {
                return LocalDate.parse(lerTexto("Data de nascimento (AAAA-MM-DD): "))
            } catch (DateTimeParseException e) {
                println "Data inválida. Use o formato AAAA-MM-DD."
            }
        }
    }

    String lerSiglaEstado() {
        String estado = lerTexto("Estado(sigla): ").toUpperCase()
        while (!(estado ==~ /[A-Z]{2}/)) {
            println "Informe a sigla do estado com somente 2 letras."
            estado = lerTexto("Estado(sigla): ").toUpperCase()
        }
        return estado
    }

    List<String> lerCompetencias(String mensagem) {
        return leitor.lerLinha(mensagem).split(",")*.trim().findAll {
            !it.isEmpty() }.unique {
                it.toLowerCase()
        }
    }


}
