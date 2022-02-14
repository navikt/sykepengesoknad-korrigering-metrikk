package no.nav.helse.flex

import io.micrometer.core.instrument.MeterRegistry
import no.nav.helse.flex.metrikker.JobbetUnderveisTimerProsent
import no.nav.helse.flex.sykepengesoknad.kafka.*
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDateTime
import java.util.*

class JobbetUnderveisTimerProsentTest : FellesTestOppsett() {

    @Autowired
    lateinit var jobbetUnderveisTimerProsent: JobbetUnderveisTimerProsent

    @Autowired
    private lateinit var registry: MeterRegistry

    @Test
    fun testerValgteTimer() {
        val counter = registry.counter("jobbet_du_underveis_svart_ja", "timer_eller_prosent", "TIMER")
        val foer = counter.count()
        jobbetUnderveisTimerProsent.finnForetrukketSvarJobbetUnderveis(skapSoknad(velgTimer = true))

        val etter = counter.count()
        foer + 1 `should be equal to` etter
    }

    @Test
    fun testerValgteProsent() {
        val counter = registry.counter("jobbet_du_underveis_svart_ja", "timer_eller_prosent", "PROSENT")
        val foer = counter.count()
        jobbetUnderveisTimerProsent.finnForetrukketSvarJobbetUnderveis(skapSoknad(velgProsent = true))

        val etter = counter.count()
        foer + 1 `should be equal to` etter
    }

    @Test
    fun testerValgteIngen() {
        val counter = registry.counter("jobbet_du_underveis_svart_ja", "timer_eller_prosent", "PROSENT")
        val foer = counter.count()
        jobbetUnderveisTimerProsent.finnForetrukketSvarJobbetUnderveis(skapSoknad())

        val etter = counter.count()
        foer `should be equal to` etter
    }
}

fun skapSoknad(velgTimer: Boolean = false, velgProsent: Boolean = false): SykepengesoknadDTO {
    return SykepengesoknadDTO(
        fnr = "123",
        id = UUID.randomUUID().toString(),
        type = SoknadstypeDTO.ARBEIDSTAKERE,
        status = SoknadsstatusDTO.SENDT,
        opprettet = LocalDateTime.now(),
        sporsmal = listOf(
            SporsmalDTO(
                tag = "JOBBET_DU_100_PROSENT_0",
                sporsmalstekst = "Bla bla?",
                svar = listOf(SvarDTO(verdi = "JA")),
                svartype = SvartypeDTO.JA_NEI,
                kriterieForVisningAvUndersporsmal = VisningskriteriumDTO.JA,
                undersporsmal = jobbetDuUndersporsmal(velgTimer = velgTimer, velgProsent = velgProsent)
            )
        )
    )
}

fun jobbetDuUndersporsmal(
    minProsent: Int = 0,
    index: Int = 0,
    velgTimer: Boolean,
    velgProsent: Boolean,
): List<SporsmalDTO> {

    return listOf(
        SporsmalDTO(
            tag = "HVOR_MANGE_TIMER_PER_UKE_" + index,
            sporsmalstekst = "Hvor mange timer i uken jobber du vanligvis når du er frisk? Varierer det, kan du oppgi gjennomsnittet.",
            undertekst = "timer per uke",
            svartype = SvartypeDTO.TALL,
            min = "1",
            max = "150"
        ),
        SporsmalDTO(
            tag = "HVOR_MYE_HAR_DU_JOBBET_" + index,
            sporsmalstekst = "Hvor mye jobbet du tilsammen?",
            svartype = SvartypeDTO.RADIO_GRUPPE_TIMER_PROSENT,
            undertekst = "Velg timer eller prosent",
            undersporsmal = listOf(
                SporsmalDTO(
                    tag = "HVOR_MYE_TIMER_" + index,
                    sporsmalstekst = "Timer",
                    svartype = SvartypeDTO.RADIO,
                    svar = if (velgTimer) {
                        listOf(SvarDTO(verdi = "CHECKED"))
                    } else {
                        emptyList()
                    },
                    kriterieForVisningAvUndersporsmal = VisningskriteriumDTO.CHECKED,
                    undersporsmal = listOf(
                        SporsmalDTO(
                            tag = "HVOR_MYE_TIMER_VERDI_" + index,
                            undertekst = "timer totalt",
                            svartype = SvartypeDTO.TALL,
                            min = "1",
                            max = "100"
                        )
                    )
                ),
                SporsmalDTO(
                    tag = "HVOR_MYE_PROSENT_" + index,
                    sporsmalstekst = "Prosent",
                    svartype = SvartypeDTO.RADIO,
                    svar = if (velgProsent) {
                        listOf(SvarDTO(verdi = "CHECKED"))
                    } else {
                        emptyList()
                    },
                    kriterieForVisningAvUndersporsmal = VisningskriteriumDTO.CHECKED,
                    undersporsmal = listOf(
                        SporsmalDTO(
                            tag = "HVOR_MYE_PROSENT_VERDI_" + index,
                            undertekst = "prosent",
                            svartype = SvartypeDTO.TALL,
                            min = minProsent.toString(),
                            max = "99"
                        )
                    )
                ),
            )
        )
    )
}
