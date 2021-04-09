package no.nav.helse.flex.client

import no.nav.helse.flex.logger
import no.nav.syfo.kafka.felles.*
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus.OK
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder

@Component
class SyfosoknadClient(
    private val syfosoknadRestTemplate: RestTemplate,
    @Value("\${flex.fss.proxy.url}") private val url: String
) {

    val log = logger()

    fun hentSoknad(soknadId: String): SykepengesoknadDTO {
        try {
            val uriBuilder = UriComponentsBuilder.fromHttpUrl("$url/syfosoknad/api/v3/soknader/$soknadId/kafkaformat")

            val headers = HttpHeaders()
            headers.contentType = MediaType.APPLICATION_JSON

            val result = syfosoknadRestTemplate
                .exchange(
                    uriBuilder.toUriString(),
                    HttpMethod.GET,
                    HttpEntity<Any>(headers),
                    SykepengesoknadDTO::class.java
                )

            if (result.statusCode != OK) {
                val message = "Kall mot syfosoknad feiler med HTTP-" + result.statusCode
                log.error(message)
                throw RuntimeException(message)
            }

            result.body?.let { return it }

            val message = "Kall mot syfosoknad returnerer ikke data"
            log.error(message)
            throw RuntimeException(message)
        } catch (ex: HttpClientErrorException.NotFound) {
            throw SøknadIkkeFunnetException("Fant ikke søknad: $soknadId")
        }
    }
}

class SøknadIkkeFunnetException(msg: String) : RuntimeException(msg)
