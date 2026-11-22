package org.linketinder.vagas.mensageria

import jakarta.inject.Singleton
import org.linketinder.vagas.http.RegistroDoServidor
import org.linketinder.vagas.model.Vaga

import java.time.OffsetDateTime

/**
 *
 * @author Guilherme Lima Conte
 */

@Singleton
class PublicadorDeVagasKafka implements PublicadorDeVagas {

    private final ClienteKafkaDeVagas cliente
    private final RegistroDoServidor registro

    PublicadorDeVagasKafka(ClienteKafkaDeVagas cliente, RegistroDoServidor registro) {
        this.cliente = cliente
        this.registro = registro
    }

    @Override
    void publicarVagaCriada(Vaga vaga) {
        try {
            cliente.publicar(vaga.id.toString(), mensagemDe(vaga))
        } catch (Exception e) {
            registro.registrarFalhaDePublicacao(vaga.id, e.class.simpleName)
        }
    }

    private static Map<String, Object> mensagemDe(Vaga vaga) {
        return [
                id             : vaga.id,
                nome           : vaga.nome,
                descricao      : vaga.descricao,
                competencias   : vaga.competencias,
                estado         : vaga.estado,
                cidade         : vaga.cidade,
                dataHoraCriacao: OffsetDateTime.now().toString()
        ] as Map<String, Object>
    }
}
