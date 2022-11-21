package no.nav.helse.flex.bigquery

import com.google.cloud.bigquery.BigQuery
import com.google.cloud.bigquery.InsertAllRequest
import com.google.cloud.bigquery.TableId
import no.nav.helse.flex.logger
import org.springframework.stereotype.Component
import java.time.LocalDate
import kotlin.collections.HashMap

@Component
class VanligArbeidstidTable(val bq: BigQuery) {

    val log = logger()

    fun lagreVanligArbeidstid(ks: List<VanligArbeidstid>) {
        if (ks.isEmpty()) {
            return
        }

        val insertAll = bq.insertAll(
            InsertAllRequest.newBuilder(TableId.of(dataset, vanligArbeidstidTableName))
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

private fun VanligArbeidstid.tilMap(): Map<String, Any> {
    val data: MutableMap<String, Any> = HashMap()
    data["sykepengesoknadId"] = sykepengesoknadId
    data["fnr"] = fnr
    data["fom"] = fom.toString()
    data["tom"] = tom.toString()
    data["orgnr"] = orgnr
    data["orgnavn"] = orgnavn
    data["arbeidstid"] = arbeidstid

    return data.toMap()
}

data class VanligArbeidstid(
    val sykepengesoknadId: String,
    val fnr: String,
    val fom: LocalDate,
    val tom: LocalDate,
    val orgnr: String,
    val orgnavn: String,
    val arbeidstid: String,
)
