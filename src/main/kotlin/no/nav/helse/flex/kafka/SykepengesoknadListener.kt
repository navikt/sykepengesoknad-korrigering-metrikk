package no.nav.helse.flex.kafka

import com.fasterxml.jackson.module.kotlin.readValue
import com.google.cloud.bigquery.BigQueryOptions
import com.google.cloud.bigquery.InsertAllRequest
import com.google.cloud.bigquery.TableId
import no.nav.helse.flex.logger
import no.nav.helse.flex.metrikker.JobbetUnderveisTimerProsent
import no.nav.helse.flex.metrikker.KorrigerteSoknader
import no.nav.helse.flex.metrikker.SoknadSendtForTom
import no.nav.helse.flex.metrikker.StudierEtterBegyntSykefravaer
import no.nav.helse.flex.objectMapper
import no.nav.helse.flex.sykepengesoknad.kafka.SykepengesoknadDTO
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.*
import kotlin.collections.HashMap

@Component
class SykepengesoknadListener(
    private val korrigerteSoknader: KorrigerteSoknader,
    private val jobbetUnderveisTimerProsent: JobbetUnderveisTimerProsent,
    private val studierEtterBegyntSykefravaer: StudierEtterBegyntSykefravaer,
    private val soknadSendtForTom: SoknadSendtForTom,
    @Value("\${GCP_TEAM_PROJECT_ID}")
    private val projectId: String
) {

    private val log = logger()
    val bigquery = BigQueryOptions.newBuilder().setProjectId(projectId).build().service

    @KafkaListener(topics = [FLEX_SYKEPENGESOKNAD_TOPIC])
    fun listen(cr: ConsumerRecord<String, String>, acknowledgment: Acknowledgment) {

        val soknad = cr.value().tilSykepengesoknadDTO()

        val row1Data: MutableMap<String, Any> = HashMap()
        row1Data["booleanField"] = true
        row1Data["soknadid"] = soknad.id
        row1Data["status"] = soknad.status
        row1Data["opprettet"] = Instant.now()
        bigquery.insertAll(
            InsertAllRequest.newBuilder(TableId.of("korrigering_metrikk", "soknadtest"))
                .addRow(UUID.randomUUID().toString(), row1Data)
                .build()
        )
        log.debug("Mottok soknad ${soknad.id} med status ${soknad.status}")

        korrigerteSoknader.finnKorrigerteSporsmal(soknad)
        jobbetUnderveisTimerProsent.finnForetrukketSvarJobbetUnderveis(soknad)
        studierEtterBegyntSykefravaer.finnBegyntStudierFoerSyk(soknad)
        soknadSendtForTom.finnSoknadSendtForTom(soknad)
        acknowledgment.acknowledge()
    }

    fun String.tilSykepengesoknadDTO(): SykepengesoknadDTO = objectMapper.readValue(this)
}
