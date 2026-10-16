package org.linketinder.cadastro.service

import org.linketinder.cadastro.model.Candidato
import org.linketinder.cadastro.model.Empresa

import java.time.Clock
import java.time.LocalDate
import java.time.format.DateTimeParseException
import java.util.regex.Pattern

/**
 *
 * @author Guilherme Lima Conte
 */

class ValidadorDeCadastro {

    static final int TAMANHO_MINIMO_SENHA = 6
    static final int TAMANHO_MAXIMO_SENHA = 50
    static final int TAMANHO_MAXIMO_NOME_CANDIDATO = 50
    static final int TAMANHO_MAXIMO_NOME_EMPRESA = 100
    static final int TAMANHO_MAXIMO_SOBRENOME = 50
    static final int TAMANHO_MAXIMO_EMAIL = 200
    static final int TAMANHO_MAXIMO_PAIS = 100
    static final int TAMANHO_MAXIMO_DESCRICAO = 500
    static final int TAMANHO_MAXIMO_FORMACAO = 100

    static final Pattern FORMATO_EMAIL = ~/[^@\s]+@[^@\s]+\.[^@\s]+/
    static final Pattern FORMATO_CPF = ~/\d{11}|\d{3}\.\d{3}\.\d{3}-\d{2}/
    static final Pattern FORMATO_CNPJ = ~/\d{14}|\d{2}\.\d{3}\.\d{3}\/\d{4}-\d{2}/
    static final Pattern FORMATO_CEP = ~/\d{8}|\d{5}-\d{3}/

    private final Clock relogio

    ValidadorDeCadastro(Clock relogio) {
        this.relogio = relogio
    }

    Candidato validarCandidato(Map<String, Object> dados) {
        return new Candidato(
                nome: exigirTexto(dados, "nome", TAMANHO_MAXIMO_NOME_CANDIDATO),
                sobrenome: exigirTexto(dados, "sobrenome", TAMANHO_MAXIMO_SOBRENOME),
                dataNascimento: exigirDataNaoFutura(dados, "dataNascimento"),
                email: exigirEmail(dados),
                cpf: exigirFormato(dados, "cpf", FORMATO_CPF, "ter 11 dígitos, só os dígitos ou no formato 000.000.000-00"),
                pais: exigirTexto(dados, "pais", TAMANHO_MAXIMO_PAIS),
                cep: exigirCep(dados),
                descricao: textoOpcional(dados, "descricao", TAMANHO_MAXIMO_DESCRICAO),
                senha: exigirSenha(dados),
                formacao: textoOpcional(dados, "formacao", TAMANHO_MAXIMO_FORMACAO)
        )
    }

    Empresa validarEmpresa(Map<String, Object> dados) {
        return new Empresa(
                nome: exigirTexto(dados, "nome", TAMANHO_MAXIMO_NOME_EMPRESA),
                email: exigirEmail(dados),
                cnpj: exigirFormato(dados, "cnpj", FORMATO_CNPJ, "ter 14 dígitos, só os dígitos ou no formato 00.000.000/0000-00"),
                pais: exigirTexto(dados, "pais", TAMANHO_MAXIMO_PAIS),
                cep: exigirCep(dados),
                descricao: textoOpcional(dados, "descricao", TAMANHO_MAXIMO_DESCRICAO),
                senha: exigirSenha(dados)
        )
    }

    private static String exigirEmail(Map<String, Object> dados) {
        String email = exigirTexto(dados, "email", TAMANHO_MAXIMO_EMAIL)

        if (!(email ==~ FORMATO_EMAIL)) {
            throw new DadosInvalidosException("O campo 'email' deve ser um e-mail válido, como nome@example.com")
        }
        return email
    }

    private static String exigirCep(Map<String, Object> dados) {
        return exigirFormato(dados, "cep", FORMATO_CEP, "ter 8 dígitos, só os dígitos ou no formato 00000-000")
    }

    private static String exigirFormato(Map<String, Object> dados, String campo, Pattern formato, String exigencia) {
        String valor = exigirTexto(dados, campo)

        if (!(valor ==~ formato)) {
            throw new DadosInvalidosException("O campo '${campo}' deve ${exigencia}")
        }
        return valor
    }

    private static String exigirSenha(Map<String, Object> dados) {
        String senha = textoBruto(dados, "senha")

        if (senha.isEmpty()) {
            throw new DadosInvalidosException("O campo 'senha' é obrigatório")
        }
        if (senha.length() < TAMANHO_MINIMO_SENHA || senha.length() > TAMANHO_MAXIMO_SENHA) {
            throw new DadosInvalidosException("O campo 'senha' deve ter de ${TAMANHO_MINIMO_SENHA} a ${TAMANHO_MAXIMO_SENHA} caracteres")
        }
        return senha
    }

    private LocalDate exigirDataNaoFutura(Map<String, Object> dados, String campo) {
        LocalDate data = lerData(exigirTexto(dados, campo), campo)

        if (data.isAfter(LocalDate.now(relogio))) {
            throw new DadosInvalidosException("O campo '${campo}' não pode ser uma data futura")
        }
        return data
    }

    private static LocalDate lerData(String texto, String campo) {
        try {
            return LocalDate.parse(texto)
        } catch (DateTimeParseException e) {
            throw new DadosInvalidosException("O campo '${campo}' deve ser uma data existente no formato AAAA-MM-DD")
        }
    }

    private static String exigirTexto(Map<String, Object> dados, String campo, int tamanhoMaximo) {
        String valor = exigirTexto(dados, campo)
        return limitarTamanho(valor, campo, tamanhoMaximo)
    }

    private static String exigirTexto(Map<String, Object> dados, String campo) {
        String valor = textoBruto(dados, campo).trim()

        if (valor.isEmpty()) {
            throw new DadosInvalidosException("O campo '${campo}' é obrigatório")
        }
        return valor
    }

    private static String textoOpcional(Map<String, Object> dados, String campo, int tamanhoMaximo) {
        return limitarTamanho(textoBruto(dados, campo).trim(), campo, tamanhoMaximo)
    }

    private static String limitarTamanho(String valor, String campo, int tamanhoMaximo) {
        if (valor.length() > tamanhoMaximo) {
            throw new DadosInvalidosException("O campo '${campo}' deve ter no máximo ${tamanhoMaximo} caracteres")
        }
        return valor
    }

    private static String textoBruto(Map<String, Object> dados, String campo) {
        Object valor = dados[campo]

        if (valor == null) {
            return ""
        }
        if (!(valor instanceof CharSequence)) {
            throw new DadosInvalidosException("O campo '${campo}' deve ser um texto")
        }
        return valor.toString()
    }
}
