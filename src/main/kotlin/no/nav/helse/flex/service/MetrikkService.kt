package no.nav.helse.flex.service

import com.fasterxml.jackson.module.kotlin.readValue
import io.micrometer.core.instrument.MeterRegistry
import no.nav.helse.flex.client.SyfosoknadClient
import no.nav.helse.flex.logger
import no.nav.helse.flex.objectMapper
import no.nav.syfo.kafka.felles.SoknadsstatusDTO
import no.nav.syfo.kafka.felles.SporsmalDTO
import no.nav.syfo.kafka.felles.SykepengesoknadDTO
import org.springframework.stereotype.Service

@Service
class MetrikkService(
    val syfosoknadClient: SyfosoknadClient,
    val registry: MeterRegistry
) {

    val log = logger()

    fun behandleSoknad(soknadString: String) {
        val soknad = soknadString.tilSykepengesoknadDTO()
        log.debug("Mottok soknad ${soknad.id} med status ${soknad.status}")

        soknad.korrigerer?.let { korrigerer ->
            if (soknad.status == SoknadsstatusDTO.SENDT) {
                log.info("Behandler metrikk for søknad ${soknad.id} som korriger søknad $korrigerer")

                val søknadSomBleKorrigert = syfosoknadClient.hentSoknad(korrigerer)

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
                    if (it.first == "FERIE_V2") {
                        log.info("Til manuell analyse: ${soknad.id}")
                    }
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

    fun String.tilSykepengesoknadDTO(): SykepengesoknadDTO = objectMapper.readValue(this)
}
