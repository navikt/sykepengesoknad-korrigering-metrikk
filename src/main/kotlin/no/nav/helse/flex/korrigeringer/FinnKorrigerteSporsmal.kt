package no.nav.helse.flex.korrigeringer

import no.nav.helse.flex.bigquery.Endring
import no.nav.helse.flex.bigquery.KorrigertSporsmal
import no.nav.helse.flex.sykepengesoknad.kafka.SporsmalDTO
import no.nav.helse.flex.sykepengesoknad.kafka.SvartypeDTO
import no.nav.helse.flex.sykepengesoknad.kafka.SykepengesoknadDTO
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

fun finnKorrigerteSporsmal(
    soknadMedKorrigering: SykepengesoknadDTO,
    søknadSomBleKorrigert: SykepengesoknadDTO
): List<KorrigertSporsmal> {
    val opprinneligeSpm = søknadSomBleKorrigert.sporsmal!!.map {
        it.tilSpørsmål()
    }.map { it.tag to it }.toMap()
    val endringer = ArrayList<KorrigertSporsmal>()

    soknadMedKorrigering.sporsmal!!
        .map { it.tilSpørsmål() }
        .forEach { nyttSpm ->

            val opprinneligSpm = opprinneligeSpm[nyttSpm.tag]

            opprinneligSpm?.let {
                if (it != nyttSpm) {
                    endringer.add(
                        KorrigertSporsmal(
                            sykepengesoknadId = soknadMedKorrigering.id,
                            korrigeringSendt = soknadMedKorrigering.sendt(),
                            opprinneligSendt = søknadSomBleKorrigert.sendt(),
                            endring = if (it.svar != nyttSpm.svar) {
                                Endring.HOVEDSPORSMAL
                            } else
                                Endring.UNDERSPORSMAL,
                            tag = nyttSpm.tag,
                            fom = soknadMedKorrigering.fom,
                            tom = soknadMedKorrigering.tom,
                            hovedsvar = if (nyttSpm.svartype == SvartypeDTO.JA_NEI) {
                                nyttSpm.svar.firstOrNull()
                            } else {
                                null
                            }
                        )
                    )
                }
            }
        }
    return endringer
}

fun SporsmalDTO.taMedUnderspm(): Boolean {
    return kriterieForVisningAvUndersporsmal == null || kriterieForVisningAvUndersporsmal?.name == svar?.firstOrNull()?.verdi
}

fun SporsmalDTO.tilSpørsmål(): Spørsmål {
    return Spørsmål(
        tag = this.tag!!,
        svartype = this.svartype,
        svar = (this.svar ?: emptyList())
            .filter { it.verdi != null }
            .map { it.verdi!! }
            .sorted(),
        undersporsmal = if (taMedUnderspm())
            this.undersporsmal?.map { it.tilSpørsmål() } ?: emptyList() else emptyList()
    )
}

data class Spørsmål(
    val svar: List<String>,
    val tag: String,
    val undersporsmal: List<Spørsmål>,
    val svartype: SvartypeDTO?
)

fun SykepengesoknadDTO.sendt(): Instant {
    return min(sendtArbeidsgiver, sendtNav)
}

fun min(a: LocalDateTime?, b: LocalDateTime?): Instant {
    if (a == null) {
        return b?.instant() ?: throw RuntimeException("Skal ha minst en sendt dato satt")
    }
    if (b == null) {
        return a.instant()
    }
    if (a.isBefore(b)) {
        return a.instant()
    }
    return b.instant()
}

val osloZone = ZoneId.of("Europe/Oslo")

fun LocalDateTime.instant(): Instant {
    return this.atZone(osloZone).toInstant()
}
