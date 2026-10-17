package org.linketinder.usuarios.service

import jakarta.inject.Singleton
import org.linketinder.usuarios.model.Candidato
import org.linketinder.usuarios.model.Empresa
import org.linketinder.usuarios.model.Recrutador
import org.linketinder.usuarios.repositorio.CandidatoRepositorio
import org.linketinder.usuarios.repositorio.EmpresaRepositorio
import org.linketinder.usuarios.repositorio.RecrutadorRepositorio

/**
 *
 * @author Guilherme Lima Conte
 */

@Singleton
class LoginService {

    static final String TIPO_CANDIDATO = "candidato"
    static final String TIPO_EMPRESA = "empresa"
    static final String TIPO_RECRUTADOR = "recrutador"

    private final CandidatoRepositorio candidatoRepositorio
    private final EmpresaRepositorio empresaRepositorio
    private final RecrutadorRepositorio recrutadorRepositorio

    LoginService(CandidatoRepositorio candidatoRepositorio, EmpresaRepositorio empresaRepositorio,
                 RecrutadorRepositorio recrutadorRepositorio) {
        this.candidatoRepositorio = candidatoRepositorio
        this.empresaRepositorio = empresaRepositorio
        this.recrutadorRepositorio = recrutadorRepositorio
    }

    Map<String, Object> autenticar(String tipo, String email, String senha) {
        switch (tipo) {
            case TIPO_CANDIDATO:
                return autenticarCandidato(email, senha)
            case TIPO_EMPRESA:
                return autenticarEmpresa(email, senha)
            case TIPO_RECRUTADOR:
                return autenticarRecrutador(email, senha)
            default:
                throw new RegraDeNegocioException(
                        "O campo 'tipo' deve ser '${TIPO_CANDIDATO}', '${TIPO_EMPRESA}' ou '${TIPO_RECRUTADOR}'")
        }
    }

    private Map<String, Object> autenticarCandidato(String email, String senha) {
        Candidato candidato = conferir(candidatoRepositorio.buscarParaLogin(email), senha)
        return [tipo: TIPO_CANDIDATO, id: candidato.id, nome: candidato.nome]
    }

    private Map<String, Object> autenticarEmpresa(String email, String senha) {
        Empresa empresa = conferir(empresaRepositorio.buscarParaLogin(email), senha)
        return [tipo: TIPO_EMPRESA, id: empresa.id, nome: empresa.nome]
    }

    private Map<String, Object> autenticarRecrutador(String email, String senha) {
        Recrutador recrutador = conferir(recrutadorRepositorio.buscarParaLogin(email), senha)
        return [tipo: TIPO_RECRUTADOR, id: recrutador.id, nome: recrutador.nome, idEmpresa: recrutador.idEmpresa]
    }

    private static <T> T conferir(T usuario, String senha) {
        if (usuario == null || usuario.senha != senha) {
            throw new CredenciaisInvalidasException()
        }
        return usuario
    }
}
