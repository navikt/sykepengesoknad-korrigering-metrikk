package no.nav.helse.flex.inntektskilder

import no.nav.helse.flex.sykepengesoknad.kafka.InntektskildeDTO
import no.nav.helse.flex.sykepengesoknad.kafka.InntektskildetypeDTO
import no.nav.helse.flex.sykepengesoknad.kafka.SporsmalDTO
import no.nav.helse.flex.sykepengesoknad.kafka.SykepengesoknadDTO
import java.util.*

fun hentInntektListe(soknad: SykepengesoknadDTO): List<InntektskildeDTO> {
    val andreinntektsporsmal = soknad.getSporsmalMedTagOrNull("ANDRE_INNTEKTSKILDER")
    return if ("JA" == andreinntektsporsmal?.forsteSvar())
        andreinntektsporsmal.undersporsmal!![0].undersporsmal!!
            .filter { it.svar!!.isNotEmpty() }
            .map {
                InntektskildeDTO(
                    mapSporsmalTilInntektskildetype(it),
                    if (it.undersporsmal!!.isEmpty())
                        null
                    else
                        "JA" == it.undersporsmal!![0].forsteSvar()
                )
            }
    else
        Collections.emptyList()
}

private fun SykepengesoknadDTO.getSporsmalMedTagOrNull(tag: String): SporsmalDTO? {
    return sporsmal!!.flatten().firstOrNull { s -> s.tag == tag }
}

private fun List<SporsmalDTO>.flatten(): List<SporsmalDTO> =
    flatMap {
        mutableListOf(it).apply {
            addAll(it.undersporsmal!!.flatten())
        }
    }

private fun SporsmalDTO.forsteSvar(): String? {
    return if (svar == null || svar!!.isEmpty())
        null
    else
        svar!![0].verdi
}

private fun mapSporsmalTilInntektskildetype(sporsmal: SporsmalDTO): InntektskildetypeDTO? {
    return when (sporsmal.tag) {
        "INNTEKTSKILDE_ANDRE_ARBEIDSFORHOLD" -> InntektskildetypeDTO.ANDRE_ARBEIDSFORHOLD
        "INNTEKTSKILDE_SELVSTENDIG" -> InntektskildetypeDTO.SELVSTENDIG_NARINGSDRIVENDE
        "INNTEKTSKILDE_SELVSTENDIG_DAGMAMMA" -> InntektskildetypeDTO.SELVSTENDIG_NARINGSDRIVENDE_DAGMAMMA
        "INNTEKTSKILDE_JORDBRUKER" -> InntektskildetypeDTO.JORDBRUKER_FISKER_REINDRIFTSUTOVER
        "INNTEKTSKILDE_FRILANSER" -> InntektskildetypeDTO.FRILANSER
        "INNTEKTSKILDE_ANNET" -> InntektskildetypeDTO.ANNET
        "INNTEKTSKILDE_FOSTERHJEM" -> InntektskildetypeDTO.FOSTERHJEMGODTGJORELSE
        "INNTEKTSKILDE_OMSORGSLONN" -> InntektskildetypeDTO.OMSORGSLONN
        "INNTEKTSKILDE_ARBEIDSFORHOLD" -> return InntektskildetypeDTO.ARBEIDSFORHOLD
        "INNTEKTSKILDE_FRILANSER_SELVSTENDIG" -> return InntektskildetypeDTO.FRILANSER_SELVSTENDIG
        else -> throw RuntimeException("Inntektskildetype " + sporsmal.tag + " finnes ikke i DTO")
    }
}
