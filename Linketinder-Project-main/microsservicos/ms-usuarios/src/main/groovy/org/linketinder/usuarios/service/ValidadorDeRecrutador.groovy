package org.linketinder.usuarios.service

import org.linketinder.usuarios.model.AreaDeRecrutamento

import java.time.LocalDate
import java.time.format.DateTimeParseException

/**
 *
 * @author Guilherme Lima Conte
 */
class ValidadorDeRecrutador {

    private static final String MENSAGEM_CPF = "O campo 'cpf' deve ter 11 dígitos, só números ou no formato 000.000.000-00."
    private static final String MENSAGEM_EMAIL = "O campo 'email' é inválido."
    private static final String MENSAGEM_AREA = "O campo 'area' deve ser um dos valores válidos: ${AreaDeRecrutamento.valores().join(', ')}."

    static String validarNome(String nome) {
        String nomeLimpo = limpar(nome)

        if (nomeLimpo.isEmpty()) {
            throw new RegraDeNegocioException("O campo 'nome' é obrigatório.")
        }

        if (nomeLimpo.length() > 50) {
            throw new RegraDeNegocioException("O campo 'nome' deve ter no máximo 50 caracteres.")
        }

        return nomeLimpo
    }

    static String validarArea(String area) {
        String areaLimpa = limpar(area)

        if (areaLimpa.isEmpty()) {
            throw new RegraDeNegocioException("O campo 'area' é obrigatório.")
        }

        if (!AreaDeRecrutamento.ehValida(areaLimpa)) {
            throw new RegraDeNegocioException(MENSAGEM_AREA)
        }

        return areaLimpa
    }

    static String validarDescricao(String descricao) {
        String descricaoLimpa = descricao == null ? "" : descricao.trim()

        if (descricaoLimpa.length() > 500) {
            throw new RegraDeNegocioException("O campo 'descricao' deve ter no máximo 500 caracteres.")
        }

        return descricaoLimpa
    }

    static String validarEmail(String email) {
        String emailLimpo = limpar(email)

        if (emailLimpo.isEmpty()) {
            throw new RegraDeNegocioException("O campo 'email' é obrigatório.")
        }

        if (emailLimpo.length() > 200) {
            throw new RegraDeNegocioException("O campo 'email' deve ter no máximo 200 caracteres.")
        }

        if (!(emailLimpo =~ /(?i)^[^\s@]+@[^\s@]+\.[^\s@]+$/)) {
            throw new RegraDeNegocioException(MENSAGEM_EMAIL)
        }

        return emailLimpo
    }

    static String validarCpf(String cpf) {
        String cpfLimpo = limpar(cpf)

        if (cpfLimpo.isEmpty()) {
            throw new RegraDeNegocioException("O campo 'cpf' é obrigatório.")
        }

        if (!(cpfLimpo ==~ /(?:\d{11}|\d{3}\.\d{3}\.\d{3}-\d{2})/)) {
            throw new RegraDeNegocioException(MENSAGEM_CPF)
        }

        return cpfLimpo
    }

    static LocalDate validarDataNascimento(String dataNascimento) {
        String dataLimpa = limpar(dataNascimento)

        if (dataLimpa.isEmpty()) {
            throw new RegraDeNegocioException("O campo 'dataNascimento' é obrigatório.")
        }

        LocalDate data
        try {
            data = LocalDate.parse(dataLimpa)
        } catch (DateTimeParseException ignored) {
            throw new RegraDeNegocioException("O campo 'dataNascimento' deve estar no formato AAAA-MM-DD.")
        }

        if (data.isAfter(LocalDate.now())) {
            throw new RegraDeNegocioException("O campo 'dataNascimento' não pode ser uma data futura.")
        }

        return data
    }

    static String validarSenha(String senha) {
        String senhaInformada = senha == null ? "" : senha

        if (senhaInformada.trim().isEmpty()) {
            throw new RegraDeNegocioException("O campo 'senha' é obrigatório.")
        }

        if (senhaInformada.length() < 6 || senhaInformada.length() > 50) {
            throw new RegraDeNegocioException("O campo 'senha' deve ter entre 6 e 50 caracteres.")
        }

        return senhaInformada
    }

    private static String limpar(String valor) {
        return valor == null ? "" : valor.trim()
    }
}
