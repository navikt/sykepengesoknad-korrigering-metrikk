package no.nav.helse.flex.bigquery

import com.google.api.client.util.DateTime
import com.google.cloud.bigquery.BigQuery
import com.google.cloud.bigquery.InsertAllRequest
import com.google.cloud.bigquery.TableId
import no.nav.helse.flex.logger
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class AndreInntektskilderTable(val bq: BigQuery) {

    val log = logger()

    fun lagreAndreInntektskilderSporsmal(ai: List<AndreInntektskilder>) {
        if (ai.isEmpty()) {
            return
        }

        val insertAll = bq.insertAll(
            InsertAllRequest.newBuilder(TableId.of(dataset, andreInntektskilderTableName))
                .also { builder ->
                    ai.forEach {
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

private fun AndreInntektskilder.tilMap(): Map<String, Any> {
    val data: MutableMap<String, Any> = hashMapOf()

    data["sykepengesoknadId"] = sykepengesoknadId
    data["sendt"] = DateTime(sendt.toEpochMilli())
    korriggerer?.let { data["korriggerer"] = it }

    andreArbeidsforholdSykmeldt?.let { data["andreArbeidsforholdSykmeldt"] = andreArbeidsforholdSykmeldt }
    arbeidsforholdSykmeldt?.let { data["arbeidsforholdSykmeldt"] = arbeidsforholdSykmeldt }
    selvstendigNaeringsdrivendeSykmeldt?.let { data["selvstendigNaeringsdrivendeSykmeldt"] = selvstendigNaeringsdrivendeSykmeldt }
    dagmammaSykmeldt?.let { data["dagmammaSykmeldt"] = dagmammaSykmeldt }
    jordbrukFiskeReindriftSykmeldt?.let { data["jordbrukFiskeReindriftSykmeldt"] = jordbrukFiskeReindriftSykmeldt }
    frilanserSykmeldt?.let { data["frilanserSykmeldt"] = frilanserSykmeldt }
    frilanserSelvstendigSykmeldt?.let { data["frilanserSelvstendigSykmeldt"] = frilanserSelvstendigSykmeldt }
    fosterhjemgodtgjorelseSykmeldt?.let { data["fosterhjemgodtgjorelseSykmeldt"] = fosterhjemgodtgjorelseSykmeldt }
    omsorgslonnSykmeldt?.let { data["omsorgslonnSykmeldt"] = omsorgslonnSykmeldt }
    annet?.let { data["annet"] = annet }

    return data.toMap()
}

data class AndreInntektskilder(
    val sykepengesoknadId: String,
    val sendt: Instant,
    val korriggerer: String?,

    val andreArbeidsforholdSykmeldt: Boolean?,
    val arbeidsforholdSykmeldt: Boolean?,
    val selvstendigNaeringsdrivendeSykmeldt: Boolean?,
    val dagmammaSykmeldt: Boolean?,
    val jordbrukFiskeReindriftSykmeldt: Boolean?,
    val frilanserSykmeldt: Boolean?,
    val frilanserSelvstendigSykmeldt: Boolean?,
    val fosterhjemgodtgjorelseSykmeldt: Boolean?,
    val omsorgslonnSykmeldt: Boolean?,
    val annet: Boolean?,
)
