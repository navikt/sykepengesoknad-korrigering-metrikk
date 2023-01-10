package no.nav.helse.flex.bigquery

import com.google.api.client.util.DateTime
import com.google.cloud.bigquery.BigQuery
import com.google.cloud.bigquery.InsertAllRequest
import com.google.cloud.bigquery.TableId
import no.nav.helse.flex.arbeidunderveisgate.ArbeidUnderveisRad
import no.nav.helse.flex.logger
import org.springframework.stereotype.Component

@Component
class ArbeidUnderveisGateTable(val bq: BigQuery) {

    val log = logger()

    fun lagreRad(ks: List<ArbeidUnderveisRad>) {
        if (ks.isEmpty()) {
            return
        }

        val insertAll = bq.insertAll(
            InsertAllRequest.newBuilder(TableId.of(dataset, arbeidUnderveisGateTableName))
                .also { builder ->
                    ks.forEach {
                        builder.addRow(it.tilMap())
                    }
                }
                .build()
        )

        if (insertAll.hasErrors()) {
            insertAll.insertErrors.forEach { (t, u) -> log.error("$t - $u") }
            throw RuntimeException("Bigquery insert har errors")
        }
    }
}

private fun ArbeidUnderveisRad.tilMap(): Map<String, Any?> {
    val data: MutableMap<String, Any?> = HashMap()
    data["sykepengesoknadId"] = sykepengesoknadId
    data["sendt"] = DateTime(sendt.toEpochMilli())
    data["gammeltSpmHovedSvar"] = gammeltSpmHovedSvar
    data["gammeltSpmTimerPerUke"] = gammeltSpmTimerPerUke
    data["gammeltSpmProsent"] = gammeltSpmProsent
    data["nyttSpmHovedSvar"] = nyttSpmHovedSvar
    data["nyttSpmTimerPerUke"] = nyttSpmTimerPerUke
    data["nyttSpmProsent"] = nyttSpmProsent
    data["hovedsvarLikt"] = hovedsvarLikt
    data["undersvarLikt"] = undersvarLikt
    data["kafkaFaktiskGrad"] = kafkaFaktiskGrad
    data["kafkaFaktiskTimer"] = kafkaFaktiskTimer
    data["nyttSpmFaktiskGrad"] = nyttSpmFaktiskGrad
    data["nyttSpmFaktiskTimer"] = nyttSpmFaktiskTimer
    data["gammeltSpmFaktiskGrad"] = gammeltSpmFaktiskGrad
    data["gammeltSpmFaktiskTimer"] = gammeltSpmFaktiskTimer
    data["faktiskGradLikt"] = faktiskGradLikt
    data["faktiskTimerLikt"] = faktiskTimerLikt
    data["innenforArbeidsgiverperioden"] = innenforArbeidsgiverperioden
    return data
}
