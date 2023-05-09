package no.nav.helse.flex.kafka

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.helse.flex.logger
import no.nav.helse.flex.metrikker.JobbetUnderveisTimerProsent
import no.nav.helse.flex.metrikker.SoknadSendtForTom
import no.nav.helse.flex.metrikker.StudierEtterBegyntSykefravaer
import no.nav.helse.flex.objectMapper
import no.nav.helse.flex.sykepengesoknad.kafka.SykepengesoknadDTO
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component

@Component
class SykepengesoknadListener(
    private val jobbetUnderveisTimerProsent: JobbetUnderveisTimerProsent,
    private val studierEtterBegyntSykefravaer: StudierEtterBegyntSykefravaer,
    private val soknadSendtForTom: SoknadSendtForTom
) {

    private val log = logger()

    @KafkaListener(
        topics = [FLEX_SYKEPENGESOKNAD_TOPIC],
        id = "prometheus-metrikker-listener",
        idIsGroup = false
    )
    fun listen(cr: ConsumerRecord<String, String>, acknowledgment: Acknowledgment) {
        val soknad = cr.value().tilSykepengesoknadDTO()

        log.debug("Mottok soknad {} med status {}", soknad.id, soknad.status)

        jobbetUnderveisTimerProsent.finnForetrukketSvarJobbetUnderveis(soknad)
        studierEtterBegyntSykefravaer.finnBegyntStudierFoerSyk(soknad)
        soknadSendtForTom.finnSoknadSendtForTom(soknad)
        acknowledgment.acknowledge()
    }
}
fun String.tilSykepengesoknadDTO(): SykepengesoknadDTO = objectMapper.readValue(this)
