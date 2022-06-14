package no.nav.helse.flex.korrigeringer

import no.nav.helse.flex.kafka.FLEX_SYKEPENGESOKNAD_TOPIC
import no.nav.helse.flex.kafka.tilSykepengesoknadDTO
import no.nav.helse.flex.logger
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.context.annotation.Profile
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component

@Component
@Profile("test")
class KorrigerteSoknaderDataproduktListener(
    private val korrigerteSoknaderDataprodukt: KorrigerteSoknaderDataprodukt,
) {

    private val log = logger()

    @KafkaListener(
        topics = [FLEX_SYKEPENGESOKNAD_TOPIC],
        groupId = "korrigerte-soknader-dataprodukt-listener-3",
        properties = ["auto.offset.reset = earliest"],
    )
    fun listen(cr: ConsumerRecord<String, String>, acknowledgment: Acknowledgment) {

        val soknad = cr.value().tilSykepengesoknadDTO()

        log.debug("Mottok soknad ${soknad.id} med status ${soknad.status}")

        korrigerteSoknaderDataprodukt.finnKorrigerteSporsmal(soknad)
        acknowledgment.acknowledge()
    }
}
