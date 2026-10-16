package org.example.api

import org.example.Banco.ConexaoBanco

import javax.servlet.ServletContextEvent
import javax.servlet.ServletContextListener
import javax.servlet.annotation.WebListener

/**
 *
 * @author Guilherme Lima Conte
 */

@WebListener
class EncerradorDeConexao implements ServletContextListener{

    @Override
    void contextDestroyed(ServletContextEvent evento) {
        ConexaoBanco.obterInstancia().fechar()
    }
}
