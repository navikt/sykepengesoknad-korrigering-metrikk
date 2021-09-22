package no.nav.helse.flex.kafka

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.helse.flex.logger
import no.nav.helse.flex.metrikker.JobbetUnderveisTimerProsent
import no.nav.helse.flex.metrikker.KorrigerteSporsmal
import no.nav.helse.flex.objectMapper
import no.nav.syfo.kafka.felles.SykepengesoknadDTO
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component

@Component
class SykepengesoknadListener(
    private val korrigerteSporsmal: KorrigerteSporsmal,
    private val jobbetUnderveisTimerProsent: JobbetUnderveisTimerProsent,
) {

    private val log = logger()

    @KafkaListener(topics = [FLEX_SYKEPENGESOKNAD_TOPIC])
    fun listen(cr: ConsumerRecord<String, String>, acknowledgment: Acknowledgment) {
        try {
            val soknad = cr.value().tilSykepengesoknadDTO()

            log.debug("Mottok soknad ${soknad.id} med status ${soknad.status}")

            korrigerteSporsmal.finnKorrigerteSporsmal(soknad)
            jobbetUnderveisTimerProsent.finnForetrukketSvarJobbetUnderveis(soknad)
            acknowledgment.acknowledge()
        } catch (e: Exception) {
            log.error("Feil ved mottak av record med key: ${cr.key()} offset: ${cr.offset()} partition: ${cr.partition()}", e)
            throw e
        }
    }

    fun String.tilSykepengesoknadDTO(): SykepengesoknadDTO = objectMapper.readValue(this)
}
