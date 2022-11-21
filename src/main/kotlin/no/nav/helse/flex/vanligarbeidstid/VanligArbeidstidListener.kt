package no.nav.helse.flex.vanligarbeidstid

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.helse.flex.kafka.FLEX_SYKEPENGESOKNAD_TOPIC
import no.nav.helse.flex.logger
import no.nav.helse.flex.objectMapper
import no.nav.helse.flex.sykepengesoknad.kafka.SykepengesoknadDTO
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component

@Component
class VanligArbeidstidListener(
    private val vanligArbeidstidMetrikk: VanligArbeidstidMetrikk,

) {

    private val log = logger()

    @KafkaListener(
        topics = [FLEX_SYKEPENGESOKNAD_TOPIC],
        id = "vanlig-arbeidstid-listener-2",
        idIsGroup = true,
        concurrency = "3",
        properties = ["auto.offset.reset = earliest"],
    )
    fun listen(cr: ConsumerRecord<String, String>, acknowledgment: Acknowledgment) {

        val soknad = cr.value().tilSykepengesoknadDTO()
        vanligArbeidstidMetrikk.finnVanligArbeidstid(soknad)

        acknowledgment.acknowledge()
    }
}
fun String.tilSykepengesoknadDTO(): SykepengesoknadDTO = objectMapper.readValue(this)
