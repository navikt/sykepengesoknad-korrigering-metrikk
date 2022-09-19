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
    val valgteInntektskilder = soknad.andreInntektskilder ?: hentInntektListe(soknad)
    val andreInntektskilder = AndreInntektskilder(
        sykepengesoknadId = soknad.id,
        soknadstype = soknad.type.toString().lowercase(),
        korriggerer = soknad.korrigerer,
        sendt = soknad.sendt(),
    )

    require(soknad.sporsmalOmAndreInntektskilder() != null) {
        "Soknad ${soknad.id} ${soknad.type} mangler spørsmål om andre inntektskilder, skal ikke skje"
    }

    require(valgteInntektskilder.filterNot { it.type == ANNET }.all { it.sykmeldt != null }) {
        "Soknad ${soknad.id} har et spørsmål der det ikke spørres om de er sykmeldt fra dette"
    }

    if (soknad.svarPaAndreInntektskilder() == "NEI") {
        return andreInntektskilder
    }

    return andreInntektskilder.copy(
        andreArbeidsforholdSykmeldt = valgteInntektskilder.erSykmeldtFra(ANDRE_ARBEIDSFORHOLD),
        arbeidsforholdSykmeldt = valgteInntektskilder.erSykmeldtFra(ARBEIDSFORHOLD),
        selvstendigNaeringsdrivendeSykmeldt = valgteInntektskilder.erSykmeldtFra(SELVSTENDIG_NARINGSDRIVENDE),
        dagmammaSykmeldt = valgteInntektskilder.erSykmeldtFra(SELVSTENDIG_NARINGSDRIVENDE_DAGMAMMA),
        jordbrukFiskeReindriftSykmeldt = valgteInntektskilder.erSykmeldtFra(JORDBRUKER_FISKER_REINDRIFTSUTOVER),
        frilanserSykmeldt = valgteInntektskilder.erSykmeldtFra(FRILANSER),
        frilanserSelvstendigSykmeldt = valgteInntektskilder.erSykmeldtFra(FRILANSER_SELVSTENDIG),
        fosterhjemgodtgjorelseSykmeldt = valgteInntektskilder.erSykmeldtFra(FOSTERHJEMGODTGJORELSE),
        omsorgslonnSykmeldt = valgteInntektskilder.erSykmeldtFra(OMSORGSLONN),
        annet = valgteInntektskilder.harInntektskilde(ANNET),
    )
}

private fun List<InntektskildeDTO>.erSykmeldtFra(inntektskilde: InntektskildetypeDTO): Boolean? {
    return find { it.type == inntektskilde }?.sykmeldt
}

private fun List<InntektskildeDTO>.harInntektskilde(inntektskilde: InntektskildetypeDTO): Boolean {
    return any { it.type == inntektskilde }
}

private fun SykepengesoknadDTO.sporsmalOmAndreInntektskilder() = sporsmal?.find { it.tag == "ANDRE_INNTEKTSKILDER" }
private fun SykepengesoknadDTO.svarPaAndreInntektskilder() = sporsmalOmAndreInntektskilder()?.svar?.get(0)?.verdi
