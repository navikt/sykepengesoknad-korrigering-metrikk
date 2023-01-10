package no.nav.helse.flex.arbeidunderveisgate

import no.nav.helse.flex.bigquery.ArbeidUnderveisGateTable
import no.nav.helse.flex.logger
import no.nav.helse.flex.sykepengesoknad.kafka.FravarstypeDTO
import no.nav.helse.flex.sykepengesoknad.kafka.SoknadsstatusDTO
import no.nav.helse.flex.sykepengesoknad.kafka.SporsmalDTO
import no.nav.helse.flex.sykepengesoknad.kafka.SykepengesoknadDTO
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.ZoneId

data class ArbeidUnderveisRad(
    val sykepengesoknadId: String,
    val sendt: Instant,
    val gammeltSpmHovedSvar: String,
    val gammeltSpmTimerPerUke: Double?,
    val gammeltSpmProsent: Double?,
    val nyttSpmHovedSvar: String,
    val nyttSpmTimerPerUke: Double?,
    val nyttSpmProsent: Double?,
    val hovedsvarLikt: Boolean,
    val undersvarLikt: Boolean,
    val kafkaFaktiskGrad: Int?,
    val kafkaFaktiskTimer: Double?,
    val nyttSpmFaktiskGrad: Int?,
    val nyttSpmFaktiskTimer: Double?,
    val gammeltSpmFaktiskGrad: Int?,
    val gammeltSpmFaktiskTimer: Double?,
    val faktiskGradLikt: Boolean,
    val faktiskTimerLikt: Boolean,
    val innenforArbeidsgiverperioden: Boolean,
)

@Component
class ArbeidUnderveisGateDataprodukt(
    val arbeidUnderveisGateTable: ArbeidUnderveisGateTable,
) {

    val log = logger()

    fun behandleSoknad(soknad: SykepengesoknadDTO) {
        if (soknad.status != SoknadsstatusDTO.SENDT) {
            return
        }

        val gammeltSpm = soknad.sporsmal?.firstOrNull { it.tag == "JOBBET_DU_100_PROSENT_0" }
        val nyttSpm = soknad.sporsmal?.firstOrNull { it.tag == "ARBEID_UNDERVEIS_100_PROSENT_0" }
        if (nyttSpm == null || gammeltSpm == null) {
            // Har ikke to spm
            return
        }

        val gammmeltSpmHovedsvar = gammeltSpm.forsteSvar()!!
        val nyttSpmHovedsvar = nyttSpm.forsteSvar()!!

        val gammeltSpmFlat = listOf(gammeltSpm).flatten()

        val gammeltSpmTimerPerUke =
            gammeltSpmFlat.firstOrNull { it.tag == "HVOR_MANGE_TIMER_PER_UKE_0" }?.forsteSvar().tilDouble()
        val gammeltSpmTimer =
            gammeltSpmFlat.firstOrNull { it.tag == "HVOR_MYE_TIMER_VERDI_0" }?.forsteSvar().tilDouble()
        val gammeltSpmProsent =
            gammeltSpmFlat.firstOrNull { it.tag == "HVOR_MYE_PROSENT_VERDI_0" }?.forsteSvar().tilDouble()

        val nyttSpmFlat = listOf(nyttSpm).flatten()

        fun finnNyttSpmTimerPerUke(): String? {
            val normalArbeidsuke =
                nyttSpmFlat.firstOrNull { it.tag == "JOBBER_DU_NORMAL_ARBEIDSUKE_0" }?.forsteSvar() == "JA"
            if (normalArbeidsuke) {
                return "37.5"
            }
            return gammeltSpmFlat.firstOrNull { it.tag == "HVOR_MANGE_TIMER_PER_UKE_0" }?.forsteSvar()
        }

        val nyttSpmTimerPerUke = finnNyttSpmTimerPerUke().tilDouble()
        val nyttSpmTimer = nyttSpmFlat.firstOrNull { it.tag == "HVOR_MYE_TIMER_VERDI_0" }?.forsteSvar().tilDouble()
        val nyttSpmProsent = nyttSpmFlat.firstOrNull { it.tag == "HVOR_MYE_PROSENT_VERDI_0" }?.forsteSvar().tilDouble()

        val kafkaFaktiskGrad = soknad.soknadsperioder!!.first().faktiskGrad
        val kafkaFaktiskTimer = soknad.soknadsperioder!!.first().faktiskTimer

        fun faktiskGrad(faktiskTimer: Double?, avtaltTimer: Double?): Int? {
            val ferieOgPermisjonPerioder = soknad.fravar?.filter { (_, _, type) ->
                listOf(
                    FravarstypeDTO.FERIE, FravarstypeDTO.PERMISJON
                ).contains(type)
            } ?: emptyList()
            return getFaktiskGrad(
                faktiskTimer,
                avtaltTimer,
                soknad.soknadsperioder!!.first(),
                ferieOgPermisjonPerioder,
                soknad.arbeidGjenopptatt
            )
        }

        val nyttSpmFaktiskGrad = faktiskGrad(nyttSpmTimer, nyttSpmTimerPerUke)
        val gammeltSpmFaktiskGrad = faktiskGrad(gammeltSpmTimer, gammeltSpmTimerPerUke)
        val rad = ArbeidUnderveisRad(
            sykepengesoknadId = soknad.id,
            sendt = soknad.sendt(),
            gammeltSpmHovedSvar = gammmeltSpmHovedsvar,
            gammeltSpmTimerPerUke = gammeltSpmTimerPerUke,
            gammeltSpmProsent = gammeltSpmProsent,
            nyttSpmHovedSvar = nyttSpmHovedsvar,
            nyttSpmTimerPerUke = nyttSpmTimerPerUke,
            nyttSpmProsent = nyttSpmProsent,
            hovedsvarLikt = gammmeltSpmHovedsvar == nyttSpmHovedsvar,
            undersvarLikt = gammeltSpmTimerPerUke == nyttSpmTimerPerUke &&
                gammeltSpmTimer == nyttSpmTimer &&
                gammeltSpmProsent == nyttSpmProsent,
            kafkaFaktiskGrad = kafkaFaktiskGrad,
            kafkaFaktiskTimer = kafkaFaktiskTimer,
            nyttSpmFaktiskGrad = nyttSpmFaktiskGrad,
            nyttSpmFaktiskTimer = nyttSpmTimer,
            gammeltSpmFaktiskGrad = gammeltSpmFaktiskGrad,
            gammeltSpmFaktiskTimer = gammeltSpmTimer,
            faktiskGradLikt = gammeltSpmFaktiskGrad == kafkaFaktiskGrad && nyttSpmFaktiskGrad == kafkaFaktiskGrad,
            faktiskTimerLikt = gammeltSpmTimer == kafkaFaktiskTimer && nyttSpmTimer == kafkaFaktiskTimer,
            innenforArbeidsgiverperioden = soknad.erInnforArbeidsgiverperiode(),
        )
        arbeidUnderveisGateTable.lagreRad(listOf(rad))
    }
}

private fun SykepengesoknadDTO.erInnforArbeidsgiverperiode(): Boolean {
    return this.sendtArbeidsgiver != null && this.sendtNav == null
}

private fun String?.tilDouble(): Double? {
    if (this == null) {
        return null
    }
    return this.replace(",", ".").toDouble()
}

fun SykepengesoknadDTO.sendt(): Instant {
    val sendtDatoer = mutableListOf<Instant>()
    if (sendtArbeidsgiver != null) {
        sendtDatoer.add(sendtArbeidsgiver!!.atZone(ZoneId.systemDefault()).toInstant())
    }
    if (sendtNav != null) {
        sendtDatoer.add(sendtNav!!.atZone(ZoneId.systemDefault()).toInstant())
    }

    val firstDate = sendtDatoer.minByOrNull { it.toEpochMilli() }
    return firstDate!!
}

fun List<SporsmalDTO>.flatten(): List<SporsmalDTO> =
    flatMap {
        mutableListOf(it).apply {
            addAll((it.undersporsmal ?: emptyList()).flatten())
        }
    }

fun SporsmalDTO.forsteSvar(): String? {
    return svar?.firstOrNull()?.verdi
}
