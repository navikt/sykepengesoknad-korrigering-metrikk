package no.nav.helse.flex.korrigeringer

import no.nav.helse.flex.bigquery.Endring
import no.nav.helse.flex.sykepengesoknad.kafka.SoknadsstatusDTO
import no.nav.helse.flex.sykepengesoknad.kafka.SoknadstypeDTO
import no.nav.helse.flex.sykepengesoknad.kafka.SporsmalDTO
import no.nav.helse.flex.sykepengesoknad.kafka.SvarDTO
import no.nav.helse.flex.sykepengesoknad.kafka.SykepengesoknadDTO
import org.amshove.kluent.`should be empty`
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.shouldHaveSize
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.*

class FinnKorrigerteSporsmalKtTest {

    @Test
    fun `test to tomme søknader`() {
        val soknadMedKorrigering = soknad()
        val søknadSomBleKorrigert = soknad()
        finnKorrigerteSporsmal(
            soknadMedKorrigering = soknadMedKorrigering,
            søknadSomBleKorrigert = søknadSomBleKorrigert
        ).`should be empty`()
    }

    @Test
    fun `test at spm bare er i korrigeringa`() {
        val soknadMedKorrigering =
            soknad(sporsmal = listOf(SporsmalDTO(tag = "213456", svar = listOf(SvarDTO(verdi = "JA")))))
        val søknadSomBleKorrigert = soknad()
        finnKorrigerteSporsmal(
            soknadMedKorrigering = soknadMedKorrigering,
            søknadSomBleKorrigert = søknadSomBleKorrigert
        ).`should be empty`()
    }

    @Test
    fun `test at spm bare er i den opprinnelige`() {
        val søknadSomBleKorrigert =
            soknad(sporsmal = listOf(SporsmalDTO(tag = "213456", svar = listOf(SvarDTO(verdi = "JA")))))
        val soknadMedKorrigering = soknad()
        finnKorrigerteSporsmal(
            soknadMedKorrigering = soknadMedKorrigering,
            søknadSomBleKorrigert = søknadSomBleKorrigert
        ).`should be empty`()
    }

    @Test
    fun `test at hovedspm endrer seg `() {
        val søknadSomBleKorrigert =
            soknad(sporsmal = listOf(SporsmalDTO(tag = "FERIE", svar = listOf(SvarDTO(verdi = "JA")))))
        val soknadMedKorrigering =
            soknad(sporsmal = listOf(SporsmalDTO(tag = "FERIE", svar = listOf(SvarDTO(verdi = "NEI")))))
        val korrigerteSporsmal = finnKorrigerteSporsmal(
            soknadMedKorrigering = soknadMedKorrigering,
            søknadSomBleKorrigert = søknadSomBleKorrigert
        )
        korrigerteSporsmal.shouldHaveSize(1)
        korrigerteSporsmal[0].sykepengesoknadId `should be equal to` soknadMedKorrigering.id
        korrigerteSporsmal[0].endring `should be equal to` Endring.HOVEDSPORSMAL
        korrigerteSporsmal[0].tag `should be equal to` "FERIE"
    }

    @Test
    fun `test at underspm endrer seg `() {
        val søknadSomBleKorrigert =
            soknad(
                sporsmal = listOf(
                    SporsmalDTO(
                        tag = "FERIE",
                        svar = listOf(SvarDTO(verdi = "JA")),
                        undersporsmal = listOf(SporsmalDTO(tag = "FERIESUB", svar = listOf(SvarDTO(verdi = "1"))))
                    )
                )
            )
        val soknadMedKorrigering =
            soknad(
                sporsmal = listOf(
                    SporsmalDTO(
                        tag = "FERIE",
                        svar = listOf(SvarDTO(verdi = "JA")),
                        undersporsmal = listOf(SporsmalDTO(tag = "FERIESUB", svar = listOf(SvarDTO(verdi = "2"))))
                    )
                )
            )
        val korrigerteSporsmal = finnKorrigerteSporsmal(
            soknadMedKorrigering = soknadMedKorrigering,
            søknadSomBleKorrigert = søknadSomBleKorrigert
        )
        korrigerteSporsmal.shouldHaveSize(1)
        korrigerteSporsmal[0].sykepengesoknadId `should be equal to` soknadMedKorrigering.id
        korrigerteSporsmal[0].endring `should be equal to` Endring.UNDERSPORSMAL
        korrigerteSporsmal[0].tag `should be equal to` "FERIE"
    }

    @Test
    fun `test at underspm og hovedspm endrer seg `() {
        val søknadSomBleKorrigert =
            soknad(
                sporsmal = listOf(
                    SporsmalDTO(
                        tag = "FERIE",
                        svar = listOf(SvarDTO(verdi = "JA")),
                        undersporsmal = listOf(SporsmalDTO(tag = "FERIESUB", svar = listOf(SvarDTO(verdi = "1"))))
                    ),
                    SporsmalDTO(
                        tag = "JOBBUNDERVEIS",
                        svar = listOf(SvarDTO(verdi = "JA")),
                    )
                )
            )
        val soknadMedKorrigering =
            soknad(
                sporsmal = listOf(
                    SporsmalDTO(
                        tag = "FERIE",
                        svar = listOf(SvarDTO(verdi = "JA")),
                        undersporsmal = listOf(SporsmalDTO(tag = "FERIESUB", svar = listOf(SvarDTO(verdi = "2"))))
                    ),
                    SporsmalDTO(
                        tag = "JOBBUNDERVEIS",
                        svar = listOf(SvarDTO(verdi = "NEI")),
                    )
                )
            )
        val korrigerteSporsmal = finnKorrigerteSporsmal(
            soknadMedKorrigering = soknadMedKorrigering,
            søknadSomBleKorrigert = søknadSomBleKorrigert
        )
        korrigerteSporsmal.shouldHaveSize(2)
        korrigerteSporsmal[0].sykepengesoknadId `should be equal to` soknadMedKorrigering.id
        korrigerteSporsmal[0].endring `should be equal to` Endring.UNDERSPORSMAL
        korrigerteSporsmal[0].tag `should be equal to` "FERIE"
        korrigerteSporsmal[1].sykepengesoknadId `should be equal to` soknadMedKorrigering.id
        korrigerteSporsmal[1].endring `should be equal to` Endring.HOVEDSPORSMAL
        korrigerteSporsmal[1].tag `should be equal to` "JOBBUNDERVEIS"
    }
    fun soknad(
        id: String = UUID.randomUUID().toString(),
        sporsmal: List<SporsmalDTO> = emptyList()
    ): SykepengesoknadDTO {
        return SykepengesoknadDTO(
            id = id,
            fnr = "12345",
            status = SoknadsstatusDTO.SENDT,
            type = SoknadstypeDTO.ARBEIDSTAKERE,
            sporsmal = sporsmal,
            sendtArbeidsgiver = LocalDateTime.now()
        )
    }
}
