package org.linketinder.usuarios.service

import jakarta.inject.Singleton
import org.linketinder.usuarios.model.Recrutador
import org.linketinder.usuarios.model.Vaga
import org.linketinder.usuarios.repositorio.EmpresaRepositorio
import org.linketinder.usuarios.repositorio.RecrutadorRepositorio
import org.linketinder.usuarios.repositorio.Transacao
import org.linketinder.usuarios.repositorio.VagaRepositorio

/**
 *
 * @author Guilherme Lima Conte
 */

@Singleton
class RecrutadorService {

    static final String RECRUTADOR_REPETIDO = "Já existe um recrutador com esses dados"
    static final String VAGA_DE_OUTRA_EMPRESA = "A vaga não pertence à empresa do recrutador"
    static final String VAGA_NAO_ATRIBUIDA = "A vaga não está atribuída ao recrutador"

    private final RecrutadorRepositorio recrutadorRepositorio
    private final EmpresaRepositorio empresaRepositorio
    private final VagaRepositorio vagaRepositorio
    private final Transacao transacao

    RecrutadorService(RecrutadorRepositorio recrutadorRepositorio, EmpresaRepositorio empresaRepositorio,
                      VagaRepositorio vagaRepositorio, Transacao transacao) {
        this.recrutadorRepositorio = recrutadorRepositorio
        this.empresaRepositorio = empresaRepositorio
        this.vagaRepositorio = vagaRepositorio
        this.transacao = transacao
    }

    List<Recrutador> listarPorEmpresa(Integer idEmpresa) {
        exigirEmpresa(idEmpresa)
        return recrutadorRepositorio.listarPorEmpresa(idEmpresa)
    }

    Recrutador cadastrar(Map<String, Object> dados, Integer idEmpresa) {
        Recrutador recrutador = validar(dados)
        exigirEmpresa(idEmpresa)
        recusarRepetido(recrutador, null)

        recrutador.idEmpresa = idEmpresa
        return recrutadorRepositorio.inserir(recrutador)
    }

    Recrutador atualizar(Integer id, Map<String, Object> dados) {
        Recrutador atual = buscar(id)
        Recrutador recrutador = validar(dados)
        recusarRepetido(recrutador, id)

        recrutador.id = id
        recrutador.idEmpresa = atual.idEmpresa
        recrutadorRepositorio.atualizar(recrutador)
        return buscar(id)
    }

    void excluir(Integer id) {
        buscar(id)
        transacao.executar {
            recrutadorRepositorio.excluir(id)
        }
    }

    List<Vaga> listarVagas(Integer idRecrutador) {
        buscar(idRecrutador)
        return vagaRepositorio.listarAtribuidasAoRecrutador(idRecrutador)
    }

    List<Vaga> atribuirVaga(Integer idRecrutador, Integer idVaga) {
        Recrutador recrutador = buscar(idRecrutador)
        exigirVagaDaEmpresa(recrutador, idVaga)

        recrutadorRepositorio.atribuirVaga(idRecrutador, idVaga)
        return vagaRepositorio.listarAtribuidasAoRecrutador(idRecrutador)
    }

    void removerVaga(Integer idRecrutador, Integer idVaga) {
        Recrutador recrutador = buscar(idRecrutador)
        exigirVagaDaEmpresa(recrutador, idVaga)

        if (!recrutadorRepositorio.vagaAtribuida(idRecrutador, idVaga)) {
            throw new RegraDeNegocioException(VAGA_NAO_ATRIBUIDA)
        }
        transacao.executar {
            recrutadorRepositorio.removerVaga(idRecrutador, idVaga)
        }
    }

    Recrutador buscar(Integer id) {
        Recrutador recrutador = recrutadorRepositorio.buscarPorId(id)

        if (recrutador == null) {
            throw new RecursoNaoEncontradoException("Recrutador ${id} não encontrado")
        }
        return recrutador
    }

    private void exigirEmpresa(Integer idEmpresa) {
        if (empresaRepositorio.buscarPorId(idEmpresa) == null) {
            throw new RecursoNaoEncontradoException("Empresa ${idEmpresa} não encontrada")
        }
    }

    private void exigirVagaDaEmpresa(Recrutador recrutador, Integer idVaga) {
        Vaga vaga = vagaRepositorio.buscarPorId(idVaga)

        if (vaga == null) {
            throw new RecursoNaoEncontradoException("Vaga ${idVaga} não encontrada")
        }
        if (vaga.idEmpresa != recrutador.idEmpresa) {
            throw new RegraDeNegocioException(VAGA_DE_OUTRA_EMPRESA)
        }
    }

    private void recusarRepetido(Recrutador recrutador, Integer idIgnorado) {
        Integer donoDoEmail = recrutadorRepositorio.buscarIdPorEmail(recrutador.email)
        Integer donoDoCpf = recrutadorRepositorio.buscarIdPorCpf(recrutador.cpf)

        if (pertenceAOutro(donoDoEmail, idIgnorado) || pertenceAOutro(donoDoCpf, idIgnorado)) {
            throw new RegraDeNegocioException(RECRUTADOR_REPETIDO)
        }
    }

    private static boolean pertenceAOutro(Integer dono, Integer idIgnorado) {
        return dono != null && dono != idIgnorado
    }

    private static Recrutador validar(Map<String, Object> dados) {
        return new Recrutador(
                nome: ValidadorDeRecrutador.validarNome(texto(dados, "nome")),
                area: ValidadorDeRecrutador.validarArea(texto(dados, "area")),
                descricao: ValidadorDeRecrutador.validarDescricao(texto(dados, "descricao")),
                email: ValidadorDeRecrutador.validarEmail(texto(dados, "email")),
                cpf: ValidadorDeRecrutador.validarCpf(texto(dados, "cpf")),
                dataNascimento: ValidadorDeRecrutador.validarDataNascimento(texto(dados, "dataNascimento")),
                senha: ValidadorDeRecrutador.validarSenha(texto(dados, "senha"))
        )
    }

    private static String texto(Map<String, Object> dados, String campo) {
        return dados[campo]?.toString()
    }
}
