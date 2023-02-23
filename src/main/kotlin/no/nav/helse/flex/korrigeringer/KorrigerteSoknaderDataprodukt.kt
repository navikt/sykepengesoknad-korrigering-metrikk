package no.nav.helse.flex.korrigeringer

import no.nav.helse.flex.bigquery.KorrigertSporsmalTable
import no.nav.helse.flex.client.SykepengesoknadBackendClient
import no.nav.helse.flex.client.SøknadIkkeFunnetException
import no.nav.helse.flex.logger
import no.nav.helse.flex.sykepengesoknad.kafka.SoknadsstatusDTO
import no.nav.helse.flex.sykepengesoknad.kafka.SykepengesoknadDTO
import org.springframework.stereotype.Component

@Component
class KorrigerteSoknaderDataprodukt(
    val sykepengesoknadBackendClient: SykepengesoknadBackendClient,
    val korrigertSporsmalTable: KorrigertSporsmalTable
) {

    val log = logger()

    fun finnKorrigerteSporsmal(soknad: SykepengesoknadDTO) {
        try {
            soknad.korrigerer?.let { korrigerer ->
                if (soknad.status == SoknadsstatusDTO.SENDT) {
                    log.info("Behandler metrikk for søknad ${soknad.id} som korriger søknad $korrigerer")

                    val søknadSomBleKorrigert = sykepengesoknadBackendClient.hentSoknad(korrigerer)

                    korrigertSporsmalTable.lagreKorrigertSporsmal(
                        finnKorrigerteSporsmal(
                            soknadMedKorrigering = soknad,
                            søknadSomBleKorrigert = søknadSomBleKorrigert
                        )
                    )
                }
            }
        } catch (s: SøknadIkkeFunnetException) {
            log.warn("Fant ikke søknad ${soknad.korrigerer}")
        }
    }
}
