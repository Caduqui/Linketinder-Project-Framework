package org.example.model

/**
 *
 * @author Guilherme Lima Conte
 */
enum AreaDeRecrutamento {
    TI('TI'),
    RH('RH'),
    MARKETING('Marketing'),
    SEGURANCA_DA_INFORMACAO('Segurança da Informação'),
    VENDAS('Vendas'),
    FINANCEIRO('Financeiro'),
    OUTROS('Outros')

    final String valor

    AreaDeRecrutamento(String valor) {
        this.valor = valor
    }

    static List<String> valores() {
        values()*.valor
    }

    static boolean ehValida(String valor) {
        valor in valores()
    }
}
