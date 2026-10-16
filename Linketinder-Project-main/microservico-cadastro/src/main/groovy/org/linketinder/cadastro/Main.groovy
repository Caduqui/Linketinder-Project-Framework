package org.linketinder.cadastro

import org.linketinder.cadastro.banco.ConexaoBanco
import org.linketinder.cadastro.banco.TransacaoJdbc
import org.linketinder.cadastro.dao.CandidatoDAO
import org.linketinder.cadastro.dao.CompetenciaDAO
import org.linketinder.cadastro.dao.EmpresaDAO
import org.linketinder.cadastro.http.RegistroDoServidor
import org.linketinder.cadastro.http.RotasDeCadastro
import org.linketinder.cadastro.http.Roteador
import org.linketinder.cadastro.http.ServidorCadastro
import org.linketinder.cadastro.repositorio.CandidatoRepositorio
import org.linketinder.cadastro.service.CadastroCandidatoService
import org.linketinder.cadastro.service.CadastroEmpresaService
import org.linketinder.cadastro.service.CompetenciaDoCandidatoService
import org.linketinder.cadastro.service.ValidadorDeCadastro

import java.time.Clock

/**
 *
 * @author Guilherme Lima Conte
 */

class Main {

    static final int SAIDA_COM_ERRO = 1

    static void main(String[] args) {
        try {
            subir(System.getenv(), new RegistroDoServidor(System.out))
        } catch (ConfiguracaoInvalidaException | IOException e) {
            System.err.println("O microsserviço de cadastro não subiu: ${e.message}")
            System.exit(SAIDA_COM_ERRO)
        }
    }

    static ServidorCadastro subir(Map<String, String> ambiente, RegistroDoServidor registro) {
        Configuracao configuracao = Configuracao.aPartirDe(ambiente)

        ServidorCadastro servidor = abrirServidor(configuracao.porta, montarRoteador(configuracao, registro))
        servidor.iniciar()
        registro.informar("Microsserviço de cadastro disponível na porta ${servidor.porta}")

        return servidor
    }

    private static Roteador montarRoteador(Configuracao configuracao, RegistroDoServidor registro) {
        ConexaoBanco conexaoBanco = new ConexaoBanco(configuracao)
        CandidatoRepositorio candidatoRepositorio = new CandidatoDAO(conexaoBanco)
        ValidadorDeCadastro validador = new ValidadorDeCadastro(Clock.systemDefaultZone())

        Roteador roteador = new Roteador(registro)
        RotasDeCadastro.registrar(
                roteador,
                new CadastroCandidatoService(validador, candidatoRepositorio),
                new CadastroEmpresaService(validador, new EmpresaDAO(conexaoBanco))
        )
        RotasDeCadastro.registrarCompetencias(
                roteador,
                new CompetenciaDoCandidatoService(candidatoRepositorio, new CompetenciaDAO(conexaoBanco), new TransacaoJdbc(conexaoBanco))
        )
        return roteador
    }

    private static ServidorCadastro abrirServidor(int porta, Roteador roteador) {
        try {
            return new ServidorCadastro(porta, roteador)
        } catch (BindException e) {
            throw new ConfiguracaoInvalidaException("A porta ${porta} já está em uso. Escolha outra na variável de ambiente ${Configuracao.VARIAVEL_PORTA}")
        }
    }
}
