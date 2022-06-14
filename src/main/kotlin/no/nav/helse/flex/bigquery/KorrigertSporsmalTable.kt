package no.nav.helse.flex.bigquery

import com.google.api.client.util.DateTime
import com.google.cloud.bigquery.BigQuery
import com.google.cloud.bigquery.InsertAllRequest
import com.google.cloud.bigquery.TableId
import no.nav.helse.flex.logger
import org.springframework.stereotype.Component
import java.time.Instant
import kotlin.collections.HashMap

@Component
class KorrigertSporsmalTable(val bq: BigQuery) {

    val log = logger()

    fun lagreKorrigertSporsmal(ks: List<KorrigertSporsmal>) {
        if (ks.isEmpty()) {
            return
        }

        val insertAll = bq.insertAll(
            InsertAllRequest.newBuilder(TableId.of(dataset, "korrigeringer"))
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

private fun KorrigertSporsmal.tilMap(): Map<String, Any> {
    val data: MutableMap<String, Any> = HashMap()
    data["sykepengesoknadId"] = sykepengesoknadId
    data["opprettet"] = DateTime(Instant.now().toEpochMilli())
    data["korrigeringSendt"] = DateTime(korrigeringSendt.toEpochMilli())
    data["opprinneligSendt"] = DateTime(opprinneligSendt.toEpochMilli())
    data["endring"] = endring.toString()
    data["tag"] = tag

    return data.toMap()
}

data class KorrigertSporsmal(
    val sykepengesoknadId: String,
    val korrigeringSendt: Instant,
    val opprinneligSendt: Instant,
    val endring: Endring,
    val tag: String,
)

enum class Endring {
    HOVEDSPORSMAL, UNDERSPORSMAL
}
