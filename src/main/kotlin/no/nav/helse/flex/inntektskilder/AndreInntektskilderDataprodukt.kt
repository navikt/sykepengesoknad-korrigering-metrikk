package no.nav.helse.flex.inntektskilder

import no.nav.helse.flex.bigquery.AndreInntektskilderTable
import no.nav.helse.flex.logger
import no.nav.helse.flex.sykepengesoknad.kafka.AvsendertypeDTO
import no.nav.helse.flex.sykepengesoknad.kafka.SoknadsstatusDTO
import no.nav.helse.flex.sykepengesoknad.kafka.SoknadstypeDTO
import no.nav.helse.flex.sykepengesoknad.kafka.SykepengesoknadDTO
import org.springframework.stereotype.Component

@Component
class AndreInntektskilderDataprodukt(
    val andreInntektskilderTable: AndreInntektskilderTable,
) {

    val log = logger()

    val soknaderMedAndreInntektskilder = listOf(
        SoknadstypeDTO.ARBEIDSTAKERE,
        SoknadstypeDTO.SELVSTENDIGE_OG_FRILANSERE,
        SoknadstypeDTO.ARBEIDSLEDIG,
        SoknadstypeDTO.ANNET_ARBEIDSFORHOLD,
        SoknadstypeDTO.BEHANDLINGSDAGER,
        SoknadstypeDTO.GRADERT_REISETILSKUDD,
    )

    fun andreInntektskilder(soknad: SykepengesoknadDTO): Boolean {
        if (soknad.status == SoknadsstatusDTO.SENDT && soknad.type in soknaderMedAndreInntektskilder) {

            if (soknad.avsendertype == AvsendertypeDTO.SYSTEM) {
                return false
            }

            if (soknad.sporsmal!!.all { it.svar!!.isEmpty() }) {
                log.warn("Soknad ${soknad.id} inneholder ingen svar, veldig rart")
                return false
            }

            if (soknad.sporsmalOmAndreInntektskilder() != null && soknad.svarPaAndreInntektskilder() == null) {
                log.warn("Soknad ${soknad.id} inneholder ingen svar på andre inntektskilder spørsmålet, veldig rart")
                return false
            }

            andreInntektskilderTable.lagreAndreInntektskilderSporsmal(
                finnAndreInntektskilderSporsmal(soknad)
            )

            return true
        }

        return false
    }
}
