package org.linketinder.spring.competencias.http

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import org.springframework.web.filter.CorsFilter

/**
 *
 * @author Guilherme Lima Conte
 */

@Configuration
class ConfiguracaoCors {

    static final List<String> METODOS_ACEITOS = ["GET", "POST", "PUT", "DELETE", "OPTIONS"]
    static final List<String> CABECALHOS_ACEITOS = ["Content-Type"]

    @Bean
    CorsFilter filtroCors(@Value('${linketinder.frontend-origem}') String origemDoFrontend) {
        return criarFiltro(origemDoFrontend)
    }

    static CorsFilter criarFiltro(String origemDoFrontend) {
        CorsConfiguration configuracao = new CorsConfiguration()
        configuracao.allowedOrigins = [origemDoFrontend]
        configuracao.allowedMethods = METODOS_ACEITOS
        configuracao.allowedHeaders = CABECALHOS_ACEITOS

        UrlBasedCorsConfigurationSource fonte = new UrlBasedCorsConfigurationSource()
        fonte.registerCorsConfiguration("/**", configuracao)
        return new CorsFilter(fonte)
    }
}
