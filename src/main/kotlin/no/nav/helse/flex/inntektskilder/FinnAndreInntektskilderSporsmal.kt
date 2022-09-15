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
    val andreInntektskilder = soknad.andreInntektskilder

    require(andreInntektskilder != null) {
        "Soknad ${soknad.id} ${soknad.type} mangler spørsmål om andre inntektskilder"
    }

    return AndreInntektskilder(
        sykepengesoknadId = soknad.id,
        korriggerer = soknad.korrigerer,
        sendt = soknad.sendt(),
        andreArbeidsforholdSykmeldt = andreInntektskilder.erSykmeldtFra(ANDRE_ARBEIDSFORHOLD),
        arbeidsforholdSykmeldt = andreInntektskilder.erSykmeldtFra(ARBEIDSFORHOLD),
        selvstendigNaeringsdrivendeSykmeldt = andreInntektskilder.erSykmeldtFra(SELVSTENDIG_NARINGSDRIVENDE),
        dagmammaSykmeldt = andreInntektskilder.erSykmeldtFra(SELVSTENDIG_NARINGSDRIVENDE_DAGMAMMA),
        jordbrukFiskeReindriftSykmeldt = andreInntektskilder.erSykmeldtFra(JORDBRUKER_FISKER_REINDRIFTSUTOVER),
        frilanserSykmeldt = andreInntektskilder.erSykmeldtFra(FRILANSER),
        frilanserSelvstendigSykmeldt = andreInntektskilder.erSykmeldtFra(FRILANSER_SELVSTENDIG),
        fosterhjemgodtgjorelseSykmeldt = andreInntektskilder.erSykmeldtFra(FOSTERHJEMGODTGJORELSE),
        omsorgslonnSykmeldt = andreInntektskilder.erSykmeldtFra(OMSORGSLONN),
        annet = andreInntektskilder.erSykmeldtFra(ANNET),
    )
}

private fun List<InntektskildeDTO>.erSykmeldtFra(inntektskilde: InntektskildetypeDTO): Boolean? {
    return find { it.type == inntektskilde }?.sykmeldt
}
