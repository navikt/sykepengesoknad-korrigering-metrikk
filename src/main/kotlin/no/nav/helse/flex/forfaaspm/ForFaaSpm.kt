package no.nav.helse.flex.forfaaspm

import no.nav.helse.flex.logger
import no.nav.helse.flex.sykepengesoknad.kafka.SoknadsstatusDTO
import no.nav.helse.flex.sykepengesoknad.kafka.SykepengesoknadDTO
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.ZoneOffset

@Component
class ForFaaSpm() {

    val log = logger()

    fun forFaaSpm(soknad: SykepengesoknadDTO, timestamp: Long): Boolean {
        val soknadsider = listOf(
            "34925c34-ca22-3ff6-9b55-d74c4526f9e1",
            "69822262-b75c-43bb-896b-3c56d033ac17",
            "0ebaf2f3-53fc-36ac-82ab-605bf82264eb",
            "f5ca821b-290f-4e63-ac0a-47292e9692a0",
            "5888afeb-7be8-3070-b836-c94970364250",
            "634236f8-754d-3684-b905-b43fa405766f",
        )

        if (soknad.status == SoknadsstatusDTO.NY) {

            if (soknadsider.contains(soknad.id)) {
                val antallspm = soknad.sporsmal?.size ?: 0
                val tags = soknad.sporsmal?.map { it.tag }?.joinToString(", ") ?: "ingen tags"
                log.info(
                    "NY Soknad ${soknad.id} har $antallspm spm med tagger $tags , $timestamp ${
                    Instant.ofEpochMilli(timestamp).atOffset(
                        ZoneOffset.UTC
                    )
                    }"
                )
            }

            return true
        }

        return false
    }
}
