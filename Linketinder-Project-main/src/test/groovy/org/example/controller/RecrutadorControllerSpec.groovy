package org.example.controller

import org.example.model.Empresa
import org.example.model.Recrutador
import org.example.model.Vaga
import org.example.service.RecrutadorService
import org.example.view.EntradaFalsa
import org.example.view.RecrutadorView
import spock.lang.Specification

import java.time.LocalDate

/**
 *
 * @author Guilherme Lima Conte
 */
class RecrutadorControllerSpec extends Specification {

    RecrutadorService recrutadorService = Mock()
    RecrutadorView recrutadorView = new RecrutadorView(EntradaFalsa.comRespostas([
            "Nome: "                            : "Ana Paula",
            "Área de recrutamento: "            : "TI",
            "Descrição: "                       : "Líder",
            "Email: "                           : "ana@example.com",
            "CPF: "                             : "120.120.120-12",
            "Data de nascimento (AAAA-MM-DD): " : "1990-07-12",
            "Senha: "                           : "senha-secreta",
            "ID do recrutador: "                : "9",
            "ID da vaga: "                      : "10"
    ]))
    EmpresaController empresaController = Mock()
    VagaController vagaController = Mock()
    RecrutadorController recrutadorController = new RecrutadorController(
            recrutadorService,
            recrutadorView,
            empresaController,
            vagaController
    )

    Empresa empresa = new Empresa("Alfa", "alfa@example.com", "11.111.111/1111-11", "Brasil", "01000-000", "Empresa", "123456")

    def setup() {
        empresa.id = 1
    }

    def "cadastra recrutador vinculado à empresa selecionada"() {
        given:
        empresaController.selecionar(_) >> empresa

        when:
        recrutadorController.cadastrar()

        then:
        1 * recrutadorService.cadastrar({
            Recrutador recrutador ->
                recrutador.nome == "Ana Paula" &&
                        recrutador.area == "TI" &&
                        recrutador.idEmpresa == 1
        })
    }

    def "lista vagas atribuídas para seleção sem consultar outras vagas"() {
        given:
        Vaga vaga = new Vaga("Dev Groovy", "Vaga", "SP", "São Paulo", empresa, [])
        vaga.id = 10

        when:
        Vaga selecionada = recrutadorController.selecionarVagaDoRecrutador(9)

        then:
        1 * recrutadorService.listarVagasDoRecrutador(9) >> [vaga]
        selecionada == vaga
    }

    def "recusa selecionar vaga que não está na lista de atribuições"() {
        given:
        RecrutadorView view = new RecrutadorView(EntradaFalsa.comRespostas([
                "ID da vaga: " : "11"
        ]))
        RecrutadorController controller = new RecrutadorController(
                recrutadorService,
                view,
                empresaController,
                vagaController
        )
        Vaga vagaAtribuida = new Vaga("Dev Groovy", "Vaga", "SP", "São Paulo", empresa, [])
        vagaAtribuida.id = 10

        when:
        Vaga selecionada = controller.selecionarVagaDoRecrutador(9)

        then:
        1 * recrutadorService.listarVagasDoRecrutador(9) >> [vagaAtribuida]
        selecionada == null
    }

    def "atribui somente vaga da empresa selecionada ao recrutador"() {
        given:
        empresaController.selecionar(_) >> empresa
        Recrutador recrutador = criarRecrutador()
        Vaga vaga = new Vaga("Dev Groovy", "Vaga", "SP", "São Paulo", empresa, [])
        vaga.id = 10
        vagaController.selecionarVagaDaEmpresa(empresa) >> vaga

        when:
        boolean atribuida = recrutadorController.atribuirVaga()

        then:
        1 * recrutadorService.listarPorEmpresa(1) >> [recrutador]
        1 * recrutadorService.atribuirVaga(9, 10) >> true
        atribuida
    }

    def "remove vaga previamente atribuída ao recrutador"() {
        given:
        empresaController.selecionar(_) >> empresa
        Recrutador recrutador = criarRecrutador()
        Vaga vaga = new Vaga("Dev Groovy", "Vaga", "SP", "São Paulo", empresa, [])
        vaga.id = 10

        when:
        boolean removida = recrutadorController.removerVaga()

        then:
        1 * recrutadorService.listarPorEmpresa(1) >> [recrutador]
        1 * recrutadorService.listarVagasDoRecrutador(9) >> [vaga]
        1 * recrutadorService.removerVaga(9, 10)
        removida
    }

    private static Recrutador criarRecrutador() {
        Recrutador recrutador = new Recrutador(
                "Ana Paula",
                "TI",
                "Líder",
                "ana@example.com",
                "12012012012",
                LocalDate.of(1990, 7, 12),
                "senha-secreta",
                1
        )
        recrutador.id = 9
        return recrutador
    }
}
