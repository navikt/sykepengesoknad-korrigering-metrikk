package no.nav.helse.flex.inntektskilder

import no.nav.helse.flex.korrigeringer.osloZone
import no.nav.helse.flex.sykepengesoknad.kafka.SoknadsstatusDTO
import no.nav.helse.flex.sykepengesoknad.kafka.SoknadstypeDTO
import no.nav.helse.flex.sykepengesoknad.kafka.SporsmalDTO
import no.nav.helse.flex.sykepengesoknad.kafka.SvarDTO
import no.nav.helse.flex.sykepengesoknad.kafka.SykepengesoknadDTO
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

class FinnAndreInntektskilderTest {

    @Test
    fun `Andre inntektskilder fra andre arbeidsforhold og annet`() {
        val soknad = soknad()

        finnAndreInntektskilderSporsmal(
            soknad = soknad
        ).let {
            it.sykepengesoknadId `should be equal to` soknad.id
            it.soknadstype `should be equal to` "arbeidstakere"
            it.sendt `should be equal to` soknad.sendtNav!!.atZone(osloZone).toInstant()
            it.korriggerer `should be equal to` null

            it.andreArbeidsforholdSykmeldt `should be equal to` true
            it.annet `should be equal to` true

            it.arbeidsforholdSykmeldt `should be equal to` null
            it.selvstendigNaeringsdrivendeSykmeldt `should be equal to` null
            it.dagmammaSykmeldt `should be equal to` null
            it.jordbrukFiskeReindriftSykmeldt `should be equal to` null
            it.frilanserSykmeldt `should be equal to` null
            it.frilanserSelvstendigSykmeldt `should be equal to` null
            it.fosterhjemgodtgjorelseSykmeldt `should be equal to` null
            it.omsorgslonnSykmeldt `should be equal to` null
        }
    }

    @Test
    fun `Har svart nei på hovedspørsmålet`() {
        val soknad = soknad(
            sporsmal = listOf(
                andreInntektskilderSpm.copy(
                    svar = listOf(SvarDTO(verdi = "NEI"))
                )
            )
        )

        finnAndreInntektskilderSporsmal(
            soknad = soknad
        ).let {
            it.sykepengesoknadId `should be equal to` soknad.id
            it.soknadstype `should be equal to` "arbeidstakere"
            it.sendt `should be equal to` soknad.sendtNav!!.atZone(osloZone).toInstant()
            it.korriggerer `should be equal to` null

            it.andreArbeidsforholdSykmeldt `should be equal to` null
            it.annet `should be equal to` null
            it.arbeidsforholdSykmeldt `should be equal to` null
            it.selvstendigNaeringsdrivendeSykmeldt `should be equal to` null
            it.dagmammaSykmeldt `should be equal to` null
            it.jordbrukFiskeReindriftSykmeldt `should be equal to` null
            it.frilanserSykmeldt `should be equal to` null
            it.frilanserSelvstendigSykmeldt `should be equal to` null
            it.fosterhjemgodtgjorelseSykmeldt `should be equal to` null
            it.omsorgslonnSykmeldt `should be equal to` null
        }
    }

    fun soknad(
        id: String = UUID.randomUUID().toString(),
        sporsmal: List<SporsmalDTO> = listOf(andreInntektskilderSpm),
    ): SykepengesoknadDTO {
        return SykepengesoknadDTO(
            id = id,
            fnr = "12345",
            fom = LocalDate.EPOCH,
            tom = LocalDate.EPOCH.plusDays(3),
            status = SoknadsstatusDTO.SENDT,
            type = SoknadstypeDTO.ARBEIDSTAKERE,
            sporsmal = sporsmal,
            sendtNav = LocalDateTime.now().minusHours(1),
            sendtArbeidsgiver = LocalDateTime.now(),
        )
    }
}
