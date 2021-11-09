package no.nav.helse.flex.metrikker

import io.micrometer.core.instrument.MeterRegistry
import no.nav.syfo.kafka.felles.SoknadsstatusDTO
import no.nav.syfo.kafka.felles.SykepengesoknadDTO
import org.springframework.stereotype.Component

@Component
class SoknadSendtForTom(
    val registry: MeterRegistry
) {

    // AKA julesøknader innsendt
    fun finnSoknadSendtForTom(soknad: SykepengesoknadDTO) {
        if (soknad.status != SoknadsstatusDTO.SENDT || soknad.tom == null) {
            return
        }

        val sendtDato = soknad.sendtNav ?: soknad.sendtArbeidsgiver
            ?: throw RuntimeException("En sendt søknad skal være sendt et sted!")

        if (sendtDato.isBefore(soknad.tom!!.atStartOfDay())) {
            registry.counter("sendt_foer_tom").increment()
        }
    }
}
