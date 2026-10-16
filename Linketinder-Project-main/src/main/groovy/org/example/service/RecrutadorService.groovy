package org.example.service

import org.example.model.Recrutador
import org.example.model.Vaga
import org.example.repositorio.RecrutadorRepositorio
import org.example.repositorio.VagaRepositorio

/**
 *
 * @author Guilherme Lima Conte
 */
class RecrutadorService {

    private final RecrutadorRepositorio recrutadorRepositorio
    private final VagaRepositorio vagaRepositorio

    RecrutadorService(RecrutadorRepositorio recrutadorRepositorio, VagaRepositorio vagaRepositorio) {
        this.recrutadorRepositorio = recrutadorRepositorio
        this.vagaRepositorio = vagaRepositorio
    }

    List<Recrutador> listarPorEmpresa(Integer idEmpresa) {
        return recrutadorRepositorio.listarPorEmpresa(idEmpresa)
    }

    Recrutador buscarPorId(Integer id) {
        return recrutadorRepositorio.buscarPorId(id)
    }

    void cadastrar(Recrutador recrutador) {
        validar(recrutador)
        validarUnicidade(recrutador, null)
        recrutadorRepositorio.inserir(recrutador)
    }

    Recrutador autenticar(String email, String senha) {
        Recrutador recrutador = recrutadorRepositorio.buscarPorEmail(email)

        if (recrutador == null || recrutador.senha != senha) {
            return null
        }

        return recrutador
    }

    void atualizar(Integer id, Recrutador dados) {
        validar(dados)
        dados.id = id
        validarUnicidade(dados, id)
        recrutadorRepositorio.atualizar(dados)
    }

    void excluir(Integer id) {
        recrutadorRepositorio.deletar(id)
    }

    boolean atribuirVaga(Integer idRecrutador, Integer idVaga) {
        Recrutador recrutador = recrutadorRepositorio.buscarPorId(idRecrutador)
        if (recrutador == null) {
            throw new RegraDeNegocioException("Recrutador não encontrado")
        }

        Vaga vaga = vagaRepositorio.buscarPorId(idVaga)
        if (vaga == null) {
            throw new RegraDeNegocioException("Vaga não encontrada")
        }

        if (vaga.empresa == null || vaga.empresa.id != recrutador.idEmpresa) {
            throw new RegraDeNegocioException("A vaga não pertence à empresa do recrutador")
        }

        if (recrutadorRepositorio.listarVagasDoRecrutador(idRecrutador)?.any { it.id == idVaga }) {
            return false
        }

        recrutadorRepositorio.atribuirVaga(idRecrutador, idVaga)
        return true
    }

    void removerVaga(Integer idRecrutador, Integer idVaga) {
        validarEscopo(idRecrutador, idVaga)
        recrutadorRepositorio.removerVaga(idRecrutador, idVaga)
    }

    List<Vaga> listarVagasDoRecrutador(Integer idRecrutador) {
        return recrutadorRepositorio.listarVagasDoRecrutador(idRecrutador)
    }

    void validarEscopo(Integer idRecrutador, Integer idVaga) {
        Recrutador recrutador = recrutadorRepositorio.buscarPorId(idRecrutador)
        if (recrutador == null) {
            throw new RegraDeNegocioException("Recrutador não encontrado")
        }

        Vaga vaga = vagaRepositorio.buscarPorId(idVaga)
        if (vaga == null) {
            throw new RegraDeNegocioException("Vaga não encontrada")
        }

        if (vaga.empresa == null || vaga.empresa.id != recrutador.idEmpresa) {
            throw new RegraDeNegocioException("A vaga não pertence à empresa do recrutador")
        }

        boolean atribuida = recrutadorRepositorio.listarVagasDoRecrutador(idRecrutador)?.any { it.id == idVaga }
        if (!atribuida) {
            throw new RegraDeNegocioException("A vaga não está atribuída ao recrutador")
        }
    }

    private void validar(Recrutador recrutador) {
        recrutador.nome = ValidadorDeRecrutador.validarNome(recrutador.nome)
        recrutador.area = ValidadorDeRecrutador.validarArea(recrutador.area)
        recrutador.descricao = ValidadorDeRecrutador.validarDescricao(recrutador.descricao)
        recrutador.email = ValidadorDeRecrutador.validarEmail(recrutador.email)
        recrutador.cpf = ValidadorDeRecrutador.validarCpf(recrutador.cpf)
        recrutador.dataNascimento = ValidadorDeRecrutador.validarDataNascimento(recrutador.dataNascimento?.toString())
        recrutador.senha = ValidadorDeRecrutador.validarSenha(recrutador.senha)
    }

    private void validarUnicidade(Recrutador recrutador, Integer idIgnorado) {
        Recrutador porEmail = recrutadorRepositorio.buscarPorEmail(recrutador.email)
        Recrutador porCpf = recrutadorRepositorio.buscarPorCpf(recrutador.cpf)

        boolean emailEmUso = porEmail != null && (idIgnorado == null || porEmail.id != idIgnorado)
        boolean cpfEmUso = porCpf != null && (idIgnorado == null || porCpf.id != idIgnorado)

        if (emailEmUso || cpfEmUso) {
            throw new RegraDeNegocioException("Já existe um recrutador com esses dados")
        }
    }
}
