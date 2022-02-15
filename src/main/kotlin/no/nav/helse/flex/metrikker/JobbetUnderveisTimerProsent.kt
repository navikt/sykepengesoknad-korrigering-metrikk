package no.nav.helse.flex.metrikker

import io.micrometer.core.instrument.MeterRegistry
import no.nav.helse.flex.logger
import no.nav.helse.flex.sykepengesoknad.kafka.*
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class JobbetUnderveisTimerProsent(
    val registry: MeterRegistry
) {

    val log = logger()

    fun finnForetrukketSvarJobbetUnderveis(soknad: SykepengesoknadDTO) {
        val startDatoUtenForhåndsvalg = LocalDateTime.of(2021, 9, 22, 9, 0, 0)
        if (soknad.opprettet!!.isBefore(startDatoUtenForhåndsvalg)) {
            return
        }
        if (soknad.status != SoknadsstatusDTO.SENDT) {
            return
        }
        soknad.sporsmal!!
            .filter { it.tag?.startsWith("JOBBET_DU_") == true }
            .filter { it.svar?.firstOrNull()?.verdi == "JA" }
            .flatMap { it.undersporsmal!! }
            .filter { it.tag?.startsWith("HVOR_MYE_HAR_DU_JOBBET_") == true }
            .forEach { s ->
                fun valgtUndersporsmal(tag: String): Boolean {
                    return s.undersporsmal!!
                        .filter { it.tag!!.startsWith(tag) }
                        .any { spm -> spm.svar?.any { it.verdi == "CHECKED" } == true }
                }

                val valgtTimer = valgtUndersporsmal("HVOR_MYE_TIMER")
                val valgtProsent = valgtUndersporsmal("HVOR_MYE_PROSENT")

                if (valgtProsent == valgtTimer) {
                    log.warn("Veldig rart, forventer at valgtTimer og valgt prosent er forskjellig i søknad ${soknad.id}")
                    return
                }
                val svarformat = if (valgtTimer) {
                    "TIMER"
                } else {
                    "PROSENT"
                }
                registry.counter("jobbet_du_underveis_svart_ja", "timer_eller_prosent", svarformat).increment()
            }
    }
}
