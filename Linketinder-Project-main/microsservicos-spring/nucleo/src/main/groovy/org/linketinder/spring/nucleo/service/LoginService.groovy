package org.linketinder.spring.nucleo.service

import org.linketinder.spring.nucleo.model.Candidato
import org.linketinder.spring.nucleo.model.Empresa
import org.linketinder.spring.nucleo.model.Recrutador
import org.linketinder.spring.nucleo.model.UsuarioLogado
import org.linketinder.spring.nucleo.repositorio.CandidatoRepositorio
import org.linketinder.spring.nucleo.repositorio.EmpresaRepositorio
import org.linketinder.spring.nucleo.repositorio.RecrutadorRepositorio
import org.springframework.stereotype.Service

/**
 *
 * @author Guilherme Lima Conte
 */

@Service
class LoginService {

    static final String CANDIDATO = "candidato"
    static final String EMPRESA = "empresa"
    static final String RECRUTADOR = "recrutador"

    private final CandidatoRepositorio candidatoRepositorio
    private final EmpresaRepositorio empresaRepositorio
    private final RecrutadorRepositorio recrutadorRepositorio

    LoginService(CandidatoRepositorio candidatoRepositorio, EmpresaRepositorio empresaRepositorio, RecrutadorRepositorio recrutadorRepositorio) {
        this.candidatoRepositorio = candidatoRepositorio
        this.empresaRepositorio = empresaRepositorio
        this.recrutadorRepositorio = recrutadorRepositorio
    }

    UsuarioLogado autenticar(Map<String, Object> dados) {
        String tipo = exigirTipo(dados)
        String email = CamposDoCorpo.exigirTexto(dados, "email")
        String senha = CamposDoCorpo.exigirTextoBruto(dados, "senha")

        UsuarioLogado usuario = buscarPorTipo(tipo, email, senha)
        if (usuario == null) {
            throw new CredenciaisInvalidasException()
        }
        return usuario
    }

    private UsuarioLogado buscarPorTipo(String tipo, String email, String senha) {
        if (tipo == CANDIDATO) {
            Candidato candidato = candidatoRepositorio.buscarPorEmail(email)
            return candidato?.senha == senha ? new UsuarioLogado(tipo: tipo, id: candidato.id, nome: candidato.nome) : null
        }
        if (tipo == EMPRESA) {
            Empresa empresa = empresaRepositorio.buscarPorEmail(email)
            return empresa?.senha == senha ? new UsuarioLogado(tipo: tipo, id: empresa.id, nome: empresa.nome) : null
        }

        Recrutador recrutador = recrutadorRepositorio.buscarPorEmail(email)
        return recrutador?.senha == senha ? new UsuarioLogado(tipo: tipo, id: recrutador.id, nome: recrutador.nome, idEmpresa: recrutador.idEmpresa) : null
    }

    private static String exigirTipo(Map<String, Object> dados) {
        String tipo = CamposDoCorpo.texto(dados, "tipo").trim()

        if (!(tipo in [CANDIDATO, EMPRESA, RECRUTADOR])) {
            throw new DadosInvalidosException("O campo 'tipo' deve ser '${CANDIDATO}', '${EMPRESA}' ou '${RECRUTADOR}'")
        }
        return tipo
    }
}
