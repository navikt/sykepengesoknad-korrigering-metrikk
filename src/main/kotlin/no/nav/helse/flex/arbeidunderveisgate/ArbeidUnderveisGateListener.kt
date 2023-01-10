package no.nav.helse.flex.arbeidunderveisgate

import no.nav.helse.flex.kafka.FLEX_SYKEPENGESOKNAD_TOPIC
import no.nav.helse.flex.kafka.tilSykepengesoknadDTO
import no.nav.helse.flex.logger
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.TopicPartition
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.listener.ConsumerSeekAware
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component
import java.time.OffsetDateTime
import java.time.ZoneOffset

@Component
class ArbeidUnderveisGateListener(
    private val arbeidUnderveisGateDataprodukt: ArbeidUnderveisGateDataprodukt,
) : ConsumerSeekAware {

    private val log = logger()

    @KafkaListener(
        topics = [FLEX_SYKEPENGESOKNAD_TOPIC],
        id = "arbeidunderveisgate-listener",
        groupId = "arbeidunderveisgate-listener-1",
        properties = ["auto.offset.reset = earliest"],
    )
    fun listen(cr: ConsumerRecord<String, String>, acknowledgment: Acknowledgment) {

        val soknad = cr.value().tilSykepengesoknadDTO()

        arbeidUnderveisGateDataprodukt.behandleSoknad(soknad)
        acknowledgment.acknowledge()
    }

    override fun onPartitionsAssigned(
        assignments: MutableMap<TopicPartition, Long>,
        callback: ConsumerSeekAware.ConsumerSeekCallback
    ) {
        assignments.forEach { (topicPartition, _) ->
            callback.seekToTimestamp(
                FLEX_SYKEPENGESOKNAD_TOPIC, topicPartition.partition(),
                OffsetDateTime.of(
                    2022, 12, 28, 0, 0, 0, 0,
                    ZoneOffset.UTC
                ).toInstant().toEpochMilli()
            )
        }
    }
}
