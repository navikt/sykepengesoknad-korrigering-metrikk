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

    data["andreArbeidsforhold"] = andreArbeidsforhold
    andreArbeidsforholdSykmeldt?.let { data["andreArbeidsforholdSykmeldt"] = andreArbeidsforholdSykmeldt }

    data["selvstendigNaeringsdrivende"] = selvstendigNaeringsdrivende
    selvstendigNaeringsdrivendeSykmeldt?.let { data["selvstendigNaeringsdrivendeSykmeldt"] = selvstendigNaeringsdrivendeSykmeldt }

    data["dagmamma"] = dagmamma
    dagmammaSykmeldt?.let { data["dagmammaSykmeldt"] = dagmammaSykmeldt }

    data["jordbrukFiskeReindrift"] = jordbrukFiskeReindrift
    jordbrukFiskeReindriftSykmeldt?.let { data["jordbrukFiskeReindriftSykmeldt"] = jordbrukFiskeReindriftSykmeldt }

    data["frilanser"] = frilanser
    frilanserSykmeldt?.let { data["frilanserSykmeldt"] = frilanserSykmeldt }

    data["annet"] = annet

    return data.toMap()
}

data class AndreInntektskilder(
    val sykepengesoknadId: String,
    val sendt: Instant,
    val korriggerer: String?,

    val andreArbeidsforhold: Boolean,
    val andreArbeidsforholdSykmeldt: Boolean? = null,

    val arbeidsforhold: Boolean,
    val arbeidsforholdSykmeldt: Boolean? = null,

    val selvstendigNaeringsdrivende: Boolean,
    val selvstendigNaeringsdrivendeSykmeldt: Boolean? = null,

    val dagmamma: Boolean,
    val dagmammaSykmeldt: Boolean? = null,

    val jordbrukFiskeReindrift: Boolean,
    val jordbrukFiskeReindriftSykmeldt: Boolean? = null,

    val frilanser: Boolean,
    val frilanserSykmeldt: Boolean? = null,

    val frilanserSelvstendig: Boolean,
    val frilanserSelvstendigSykmeldt: Boolean? = null,

    val fosterhjemgodtgjorelse: Boolean,
    val fosterhjemgodtgjorelseSykmeldt: Boolean? = null,

    val omsorgslonn: Boolean,
    val omsorgslonnSykmeldt: Boolean? = null,

    val annet: Boolean,
)
