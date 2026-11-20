package org.linketinder.spring.nucleo.service

import org.springframework.stereotype.Component
import org.linketinder.spring.nucleo.model.AreaDeRecrutamento
import org.linketinder.spring.nucleo.model.Recrutador

import java.time.Clock
import java.time.LocalDate
import java.time.format.DateTimeParseException
import java.util.regex.Pattern

/**
 *
 * @author Guilherme Lima Conte
 */

@Component
class ValidadorDeRecrutador {

    static final int TAMANHO_MAXIMO_NOME = 50
    static final int TAMANHO_MAXIMO_DESCRICAO = 500
    static final int TAMANHO_MAXIMO_EMAIL = 200
    static final int TAMANHO_MINIMO_SENHA = 6
    static final int TAMANHO_MAXIMO_SENHA = 50

    static final Pattern FORMATO_EMAIL = ~/[^\s@]+@[^\s@]+\.[^\s@]+/
    static final Pattern FORMATO_CPF = ~/\d{11}|\d{3}\.\d{3}\.\d{3}-\d{2}/

    private final Clock relogio

    ValidadorDeRecrutador(Clock relogio) {
        this.relogio = relogio
    }

    Recrutador validar(Map<String, Object> dados) {
        return new Recrutador(
                nome: limitarTamanho(CamposDoCorpo.exigirTexto(dados, "nome"), "nome", TAMANHO_MAXIMO_NOME),
                area: exigirArea(dados),
                descricao: limitarTamanho(CamposDoCorpo.texto(dados, "descricao").trim(), "descricao", TAMANHO_MAXIMO_DESCRICAO),
                email: exigirEmail(dados),
                cpf: exigirCpf(dados),
                dataNascimento: exigirDataNaoFutura(dados),
                senha: exigirSenha(dados),
                idEmpresa: CamposDoCorpo.exigirInteiro(dados, "idEmpresa")
        )
    }

    private static String exigirArea(Map<String, Object> dados) {
        String area = CamposDoCorpo.exigirTexto(dados, "area")

        if (!AreaDeRecrutamento.ehValida(area)) {
            throw new DadosInvalidosException("O campo 'area' deve ser um dos valores válidos: ${AreaDeRecrutamento.valores().join(', ')}.")
        }
        return area
    }

    private static String exigirEmail(Map<String, Object> dados) {
        String email = limitarTamanho(CamposDoCorpo.exigirTexto(dados, "email"), "email", TAMANHO_MAXIMO_EMAIL)

        if (!(email ==~ FORMATO_EMAIL)) {
            throw new DadosInvalidosException("O campo 'email' é inválido.")
        }
        return email
    }

    private static String exigirCpf(Map<String, Object> dados) {
        String cpf = CamposDoCorpo.exigirTexto(dados, "cpf")

        if (!(cpf ==~ FORMATO_CPF)) {
            throw new DadosInvalidosException("O campo 'cpf' deve ter 11 dígitos, só números ou no formato 000.000.000-00.")
        }
        return cpf
    }

    private LocalDate exigirDataNaoFutura(Map<String, Object> dados) {
        LocalDate data = lerData(CamposDoCorpo.exigirTexto(dados, "dataNascimento"))

        if (data.isAfter(LocalDate.now(relogio))) {
            throw new DadosInvalidosException("O campo 'dataNascimento' não pode ser uma data futura.")
        }
        return data
    }

    private static LocalDate lerData(String texto) {
        try {
            return LocalDate.parse(texto)
        } catch (DateTimeParseException ignored) {
            throw new DadosInvalidosException("O campo 'dataNascimento' deve estar no formato AAAA-MM-DD.")
        }
    }

    private static String exigirSenha(Map<String, Object> dados) {
        String senha = CamposDoCorpo.texto(dados, "senha")

        if (senha.trim().isEmpty()) {
            throw new DadosInvalidosException("O campo 'senha' é obrigatório.")
        }
        if (senha.length() < TAMANHO_MINIMO_SENHA || senha.length() > TAMANHO_MAXIMO_SENHA) {
            throw new DadosInvalidosException("O campo 'senha' deve ter entre ${TAMANHO_MINIMO_SENHA} e ${TAMANHO_MAXIMO_SENHA} caracteres.")
        }
        return senha
    }

    private static String limitarTamanho(String valor, String campo, int tamanhoMaximo) {
        if (valor.length() > tamanhoMaximo) {
            throw new DadosInvalidosException("O campo '${campo}' deve ter no máximo ${tamanhoMaximo} caracteres.")
        }
        return valor
    }
}
