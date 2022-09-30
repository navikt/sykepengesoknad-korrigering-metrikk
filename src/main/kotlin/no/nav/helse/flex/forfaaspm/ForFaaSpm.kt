package no.nav.helse.flex.forfaaspm

import no.nav.helse.flex.korrigeringer.sendt
import no.nav.helse.flex.logger
import no.nav.helse.flex.sykepengesoknad.kafka.AvsendertypeDTO
import no.nav.helse.flex.sykepengesoknad.kafka.SoknadsstatusDTO
import no.nav.helse.flex.sykepengesoknad.kafka.SoknadstypeDTO
import no.nav.helse.flex.sykepengesoknad.kafka.SykepengesoknadDTO
import org.springframework.stereotype.Component
import java.time.ZoneOffset

@Component
class ForFaaSpm() {

    val log = logger()

    fun forFaaSpm(soknad: SykepengesoknadDTO): Boolean {
        if (soknad.status == SoknadsstatusDTO.SENDT && soknad.type == SoknadstypeDTO.ARBEIDSTAKERE) {

            if (soknad.avsendertype == AvsendertypeDTO.SYSTEM) {
                return false
            }
            val tilbakeIArbeid = soknad.sporsmal?.find { it.tag == "TILBAKE_I_ARBEID" } ?: throw RuntimeException("Tilbake i arbeid skal være der")

            val neiSvartTilbakeIArbeid = tilbakeIArbeid.svar?.any { it.verdi == "NEI" } ?: false

            val ferieFinnes = soknad.sporsmal?.any { it.tag == "FERIE_V2" } ?: false

            val sendtTid = soknad.sendt()
            if (neiSvartTilbakeIArbeid && !ferieFinnes) {
                log.info("Soknad ${soknad.id} sendt ${sendtTid.atOffset(ZoneOffset.UTC)} har nei svar TILBAKE_I_ARBEID og mangler FERIE_V2")
                return true
            }

            return false
        }

        return false
    }
}
