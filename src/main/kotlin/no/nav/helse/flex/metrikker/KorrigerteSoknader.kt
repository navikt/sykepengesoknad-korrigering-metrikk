package no.nav.helse.flex.metrikker

import io.micrometer.core.instrument.MeterRegistry
import no.nav.helse.flex.client.SykepengesoknadBackendClient
import no.nav.helse.flex.logger
import no.nav.helse.flex.sykepengesoknad.kafka.*
import org.springframework.stereotype.Component

@Component
class KorrigerteSoknader(
    val sykepengesoknadBackendClient: SykepengesoknadBackendClient,
    val registry: MeterRegistry
) {

    val log = logger()

    fun finnKorrigerteSporsmal(soknad: SykepengesoknadDTO) {

        soknad.korrigerer?.let { korrigerer ->
            if (soknad.status == SoknadsstatusDTO.SENDT) {
                log.info("Behandler metrikk for søknad ${soknad.id} som korriger søknad $korrigerer")

                val søknadSomBleKorrigert = sykepengesoknadBackendClient.hentSoknad(korrigerer)
                if (soknad.erLikOriginal(søknadSomBleKorrigert)) {
                    log.info("Søknad ${soknad.id} er en korrigering uten endringer")
                    registry.counter("soknad_korrigert_uten_endringer").increment()
                }

                val opprinneligeSpm = søknadSomBleKorrigert.sporsmal!!.map {
                    it.tilSpørsmål()
                }.map { it.tag to it }.toMap()

                var endret = 0
                val endraTags = ArrayList<Pair<String, String>>()

                soknad.sporsmal!!
                    .map { it.tilSpørsmål() }
                    .forEach { nyttSpm ->

                        val opprinneligSpm = opprinneligeSpm[nyttSpm.tag]

                        opprinneligSpm?.let {
                            if (nyttSpm.svar != it.svar) {
                                endret += 1
                                endraTags.add(Pair(nyttSpm.tag, nyttSpm.svar.toString()))
                            }
                        }
                    }

                registry.counter("soknad_korrigert", "antall_endra_hovedsporsmal", endret.toString()).increment()
                log.debug("soknad_korrigert: antall_endra_hovedsporsmal=$endret ")

                endraTags.forEach {
                    registry.counter("hovedsporsmal_endra", "tag", it.first, "nytt_svar", it.second).increment()
                    log.debug("hovedsporsmal_endra: tag=${it.first} nytt_svar=${it.second}")
                }
            }
        }
    }

    fun SporsmalDTO.tilSpørsmål(): Spørsmål {
        return Spørsmål(
            tag = this.tag!!,
            svar = (this.svar ?: emptyList())
                .filter { it.verdi != null }
                .map { it.verdi!! }
                .sorted()
        )
    }

    data class Spørsmål(
        val svar: List<String>,
        val tag: String,
    )
}

private fun SykepengesoknadDTO.erLikOriginal(søknadSomBleKorrigert: SykepengesoknadDTO): Boolean {
    fun SporsmalDTO.tilIdlos(): SporsmalDTO = this.copy(
        id = null,
        undersporsmal = this.undersporsmal?.map { it.tilIdlos() }
    )

    return this.sporsmal!!.map { it.tilIdlos() } == søknadSomBleKorrigert.sporsmal!!.map { it.tilIdlos() }
}
