package no.nav.helse.flex.vanligarbeidstid

import no.nav.helse.flex.bigquery.VanligArbeidstid
import no.nav.helse.flex.bigquery.VanligArbeidstidTable
import no.nav.helse.flex.logger
import no.nav.helse.flex.sykepengesoknad.kafka.*
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class VanligArbeidstidMetrikk(
    val table: VanligArbeidstidTable
) {

    val log = logger()

    fun finnVanligArbeidstid(soknad: SykepengesoknadDTO) {
        if (soknad.status != SoknadsstatusDTO.SENDT) {
            return
        }
        if (soknad.type != SoknadstypeDTO.ARBEIDSTAKERE) {
            return
        }
        if (soknad.fom!!.isBefore(LocalDate.of(2022, 10, 15))) {
            return
        }
        soknad.sporsmal!!
            .filter { it.tag?.startsWith("JOBBET_DU_") == true }
            .filter { it.svar?.firstOrNull()?.verdi == "JA" }
            .flatMap { it.undersporsmal!! }
            .filter { it.tag?.startsWith("HVOR_MANGE_TIMER_PER_UKE_") == true }
            .forEach { s ->

                s.svar?.firstOrNull()?.verdi?.let {

                    table.lagreVanligArbeidstid(
                        listOf(
                            VanligArbeidstid(
                                sykepengesoknadId = soknad.id,
                                fnr = soknad.fnr,
                                fom = soknad.fom!!,
                                tom = soknad.tom!!,
                                orgnr = soknad.arbeidsgiver?.orgnummer ?: "ukjent",
                                orgnavn = soknad.arbeidsgiver?.navn ?: "ukjent",
                                arbeidstid = it
                            )
                        )
                    )
                }
            }
    }
}
