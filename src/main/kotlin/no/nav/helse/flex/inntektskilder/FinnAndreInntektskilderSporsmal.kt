package no.nav.helse.flex.inntektskilder

import no.nav.helse.flex.bigquery.AndreInntektskilder
import no.nav.helse.flex.korrigeringer.sendt
import no.nav.helse.flex.sykepengesoknad.kafka.InntektskildeDTO
import no.nav.helse.flex.sykepengesoknad.kafka.InntektskildetypeDTO
import no.nav.helse.flex.sykepengesoknad.kafka.InntektskildetypeDTO.*
import no.nav.helse.flex.sykepengesoknad.kafka.SykepengesoknadDTO

fun finnAndreInntektskilderSporsmal(
    soknad: SykepengesoknadDTO,
): AndreInntektskilder {
    val inntektskilderDTO = soknad.andreInntektskilder
    val andreInntektskilder = AndreInntektskilder(
        sykepengesoknadId = soknad.id,
        soknadstype = soknad.type.toString().lowercase(),
        korriggerer = soknad.korrigerer,
        sendt = soknad.sendt(),
    )

    if (inntektskilderDTO == null) {
        require(soknad.sporsmalOmAndreInntektskilder() != null) {
            "Soknad ${soknad.id} ${soknad.type} mangler spørsmål om andre inntektskilder, skal ikke skje"
        }

        require(soknad.svarPaAndreInntektskilder() == "NEI") {
            "Soknad ${soknad.id} ${soknad.type} har ikke svart NEI på andre inntektskilder, og må hentes ut manuelt"
        }

        return andreInntektskilder // Har svart NEI
    }

    return andreInntektskilder.copy(
        andreArbeidsforholdSykmeldt = inntektskilderDTO.erSykmeldtFra(ANDRE_ARBEIDSFORHOLD),
        arbeidsforholdSykmeldt = inntektskilderDTO.erSykmeldtFra(ARBEIDSFORHOLD),
        selvstendigNaeringsdrivendeSykmeldt = inntektskilderDTO.erSykmeldtFra(SELVSTENDIG_NARINGSDRIVENDE),
        dagmammaSykmeldt = inntektskilderDTO.erSykmeldtFra(SELVSTENDIG_NARINGSDRIVENDE_DAGMAMMA),
        jordbrukFiskeReindriftSykmeldt = inntektskilderDTO.erSykmeldtFra(JORDBRUKER_FISKER_REINDRIFTSUTOVER),
        frilanserSykmeldt = inntektskilderDTO.erSykmeldtFra(FRILANSER),
        frilanserSelvstendigSykmeldt = inntektskilderDTO.erSykmeldtFra(FRILANSER_SELVSTENDIG),
        fosterhjemgodtgjorelseSykmeldt = inntektskilderDTO.erSykmeldtFra(FOSTERHJEMGODTGJORELSE),
        omsorgslonnSykmeldt = inntektskilderDTO.erSykmeldtFra(OMSORGSLONN),
        annet = inntektskilderDTO.erSykmeldtFra(ANNET),
    )
}

private fun List<InntektskildeDTO>.erSykmeldtFra(inntektskilde: InntektskildetypeDTO): Boolean? {
    return find { it.type == inntektskilde }?.sykmeldt
}

private fun SykepengesoknadDTO.sporsmalOmAndreInntektskilder() = sporsmal?.find { it.tag == "ANDRE_INNTEKTSKILDER" }
private fun SykepengesoknadDTO.svarPaAndreInntektskilder() = sporsmalOmAndreInntektskilder()?.svar?.get(0)?.verdi
