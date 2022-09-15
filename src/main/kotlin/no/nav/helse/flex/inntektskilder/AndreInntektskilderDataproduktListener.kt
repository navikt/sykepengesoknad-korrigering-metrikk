package no.nav.helse.flex.inntektskilder

import no.nav.helse.flex.kafka.FLEX_SYKEPENGESOKNAD_TOPIC
import no.nav.helse.flex.kafka.tilSykepengesoknadDTO
import no.nav.helse.flex.logger
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component

@Component
class AndreInntektskilderDataproduktListener(
    private val andreInntektskilderDataprodukt: AndreInntektskilderDataprodukt,
) {

    private val log = logger()

    @KafkaListener(
        topics = [FLEX_SYKEPENGESOKNAD_TOPIC],
        id = "andre-inntektskilder-dataprodukt-listener",
        idIsGroup = true,
        properties = ["auto.offset.reset = earliest"],
    )
    fun listen(cr: ConsumerRecord<String, String>, acknowledgment: Acknowledgment) {

        val soknad = cr.value().tilSykepengesoknadDTO()

        andreInntektskilderDataprodukt.andreInntektskilder(soknad)

        acknowledgment.acknowledge()
    }
}
