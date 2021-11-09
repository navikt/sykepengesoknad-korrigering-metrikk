package no.nav.helse.flex

import io.micrometer.core.instrument.MeterRegistry
import no.nav.helse.flex.metrikker.SoknadSendtForTom
import no.nav.syfo.kafka.felles.*
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

class SendtForTomTest : FellesTestOppsett() {

    @Autowired
    lateinit var jobbetUnderveisTimerProsent: SoknadSendtForTom

    @Autowired
    private lateinit var registry: MeterRegistry

    @Test
    fun `tester sendt før tom`() {
        val counter = registry.counter("sendt_foer_tom")
        val foer = counter.count()
        jobbetUnderveisTimerProsent.finnSoknadSendtForTom(
            skapSendtSoknad(
                tom = LocalDate.now(),
                sendtNav = LocalDateTime.now().minusDays(1)
            )
        )
        jobbetUnderveisTimerProsent.finnSoknadSendtForTom(
            skapSendtSoknad(
                tom = LocalDate.now(),
                sendtNav = LocalDateTime.now().plusDays(1)
            )
        )
        jobbetUnderveisTimerProsent.finnSoknadSendtForTom(
            skapSendtSoknad(
                tom = LocalDate.now(),
                sendtNav = LocalDateTime.now()
            )
        )

        val etter = counter.count()
        foer + 1 `should be equal to` etter
    }
}

private fun skapSendtSoknad(tom: LocalDate?, sendtNav: LocalDateTime?): SykepengesoknadDTO {
    return SykepengesoknadDTO(
        fnr = "123",
        id = UUID.randomUUID().toString(),
        type = SoknadstypeDTO.ARBEIDSTAKERE,
        status = SoknadsstatusDTO.SENDT,
        opprettet = LocalDateTime.now(),
        tom = tom,
        sendtNav = sendtNav
    )
}
