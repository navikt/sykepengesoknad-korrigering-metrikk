package no.nav.helse.flex.inntektskilder

import no.nav.helse.flex.bigquery.AndreInntektskilderTable
import no.nav.helse.flex.logger
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

    fun andreInntektskilder(soknad: SykepengesoknadDTO) {
        if (soknad.status == SoknadsstatusDTO.SENDT && soknad.type in soknaderMedAndreInntektskilder) {
            andreInntektskilderTable.lagreAndreInntektskilderSporsmal(
                finnAndreInntektskilderSporsmal(soknad)
            )
        }
    }
}
