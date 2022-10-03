package no.nav.helse.flex.forfaaspm

import no.nav.helse.flex.kafka.FLEX_SYKEPENGESOKNAD_TOPIC
import no.nav.helse.flex.kafka.tilSykepengesoknadDTO
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.TopicPartition
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.listener.ConsumerSeekAware
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.ZoneOffset

@Component
class ForFaaSpmListener(
    private val forFaaSpm: ForFaaSpm,
) : ConsumerSeekAware {

    @KafkaListener(
        topics = [FLEX_SYKEPENGESOKNAD_TOPIC],
        id = "for-faa-spm-listener-3",
        idIsGroup = true,
        concurrency = "3",
        properties = ["auto.offset.reset = earliest"],
    )
    fun listen(cr: ConsumerRecord<String, String>, acknowledgment: Acknowledgment) {

        val soknad = cr.value().tilSykepengesoknadDTO()

        val behandlet = forFaaSpm.forFaaSpm(soknad, cr.timestamp())

        if (behandlet) {
            acknowledgment.acknowledge()
        }
    }

    override fun onPartitionsAssigned(
        assignments: MutableMap<TopicPartition, Long>,
        callback: ConsumerSeekAware.ConsumerSeekCallback
    ) {
        val startDateInEpochMilli = LocalDate.of(2022, 9, 1).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        callback.seekToTimestamp(assignments.map { it.key }, startDateInEpochMilli)
    }
}
