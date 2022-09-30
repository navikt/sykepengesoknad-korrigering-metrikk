package no.nav.helse.flex.forfaaspm

import no.nav.helse.flex.kafka.FLEX_SYKEPENGESOKNAD_TOPIC
import no.nav.helse.flex.kafka.tilSykepengesoknadDTO
import no.nav.helse.flex.logger
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component

@Component
class ForFaaSpmListener(
    private val forFaaSpm: ForFaaSpm,
) {

    private val log = logger()

    @KafkaListener(
        topics = [FLEX_SYKEPENGESOKNAD_TOPIC],
        id = "for-faa-spm-listener",
        idIsGroup = true,
        concurrency = "3",
        properties = ["auto.offset.reset = earliest"],
    )
    fun listen(cr: ConsumerRecord<String, String>, acknowledgment: Acknowledgment) {

        val soknad = cr.value().tilSykepengesoknadDTO()

        val behandlet = forFaaSpm.forFaaSpm(soknad)

        if (behandlet) {
            acknowledgment.acknowledge()
        }
    }
}
