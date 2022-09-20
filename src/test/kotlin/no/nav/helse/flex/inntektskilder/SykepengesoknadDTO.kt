package no.nav.helse.flex.inntektskilder

import no.nav.helse.flex.sykepengesoknad.kafka.SporsmalDTO
import no.nav.helse.flex.sykepengesoknad.kafka.SvarDTO
import no.nav.helse.flex.sykepengesoknad.kafka.SvartypeDTO
import no.nav.helse.flex.sykepengesoknad.kafka.VisningskriteriumDTO.*

/**
 Sykmeldt fra INNTEKTSKILDE_ANDRE_ARBEIDSFORHOLD
 Andre inntektskilder fra INNTEKTSKILDE_ANNET
*/
val andreInntektskilderSpm = SporsmalDTO(
    id = "de4dabee-1ab0-31b5-93c2-4282a8839c36",
    tag = "ANDRE_INNTEKTSKILDER",
    sporsmalstekst = "Har du andre inntektskilder enn PENGELØS SPAREBANK?",
    svartype = SvartypeDTO.JA_NEI,
    kriterieForVisningAvUndersporsmal = JA,
    svar = listOf(SvarDTO(verdi = "JA")),
    undersporsmal = listOf(
        SporsmalDTO(
            id = "6a487a2c-af21-3a4e-b5c1-e7eb42fdb583",
            tag = "HVILKE_ANDRE_INNTEKTSKILDER",
            sporsmalstekst = "Hvilke andre inntektskilder har du?",
            svartype = SvartypeDTO.CHECKBOX_GRUPPE,
            svar = emptyList(),
            undersporsmal = listOf(
                SporsmalDTO(
                    id = "d98ffec4-25a1-390a-a921-321e6c3c156d",
                    tag = "INNTEKTSKILDE_ANDRE_ARBEIDSFORHOLD",
                    sporsmalstekst = "andre arbeidsforhold",
                    svartype = SvartypeDTO.CHECKBOX,
                    kriterieForVisningAvUndersporsmal = CHECKED,
                    svar = listOf(SvarDTO(verdi = "CHECKED")),
                    undersporsmal = listOf(
                        SporsmalDTO(
                            id = "8b8e0976-f00c-3781-8a47-e46c4a2db7ea",
                            tag = "INNTEKTSKILDE_ANDRE_ARBEIDSFORHOLD_ER_DU_SYKMELDT",
                            sporsmalstekst = "Er du sykmeldt fra dette?",
                            svartype = SvartypeDTO.JA_NEI,
                            svar = listOf(SvarDTO(verdi = "JA")),
                            undersporsmal = emptyList()
                        )
                    )
                ),
                SporsmalDTO(
                    id = "efcf19f8-de58-37c5-ab74-ec8ebcda03b8",
                    tag = "INNTEKTSKILDE_SELVSTENDIG",
                    sporsmalstekst = "selvstendig næringsdrivende",
                    svartype = SvartypeDTO.CHECKBOX,
                    kriterieForVisningAvUndersporsmal = CHECKED,
                    svar = emptyList(),
                    undersporsmal = listOf(
                        SporsmalDTO(
                            id = "e10970c7-3042-34a3-aef8-aed74478bb0f",
                            tag = "INNTEKTSKILDE_SELVSTENDIG_ER_DU_SYKMELDT",
                            sporsmalstekst = "Er du sykmeldt fra dette?",
                            svartype = SvartypeDTO.JA_NEI,
                            svar = emptyList(),
                            undersporsmal = emptyList()
                        )
                    )
                ),
                SporsmalDTO(
                    id = "e6cbdb96-4dc8-31c8-963b-39ff3f912cb9",
                    tag = "INNTEKTSKILDE_SELVSTENDIG_DAGMAMMA",
                    sporsmalstekst = "dagmamma",
                    svartype = SvartypeDTO.CHECKBOX,
                    kriterieForVisningAvUndersporsmal = CHECKED,
                    svar = emptyList(),
                    undersporsmal = listOf(
                        SporsmalDTO(
                            id = "7e0c1ec4-63cb-3b28-805e-e35482425408",
                            tag = "INNTEKTSKILDE_SELVSTENDIG_DAGMAMMA_ER_DU_SYKMELDT",
                            sporsmalstekst = "Er du sykmeldt fra dette?",
                            svartype = SvartypeDTO.JA_NEI,
                            svar = emptyList(),
                            undersporsmal = emptyList()
                        )
                    )
                ),
                SporsmalDTO(
                    id = "422905b8-bc7b-3b5b-8305-5b592411bceb",
                    tag = "INNTEKTSKILDE_JORDBRUKER",
                    sporsmalstekst = "jordbruk / fiske / reindrift",
                    svartype = SvartypeDTO.CHECKBOX,
                    kriterieForVisningAvUndersporsmal = CHECKED,
                    svar = emptyList(),
                    undersporsmal = listOf(
                        SporsmalDTO(
                            id = "7a7faa79-9fdb-30e3-95b3-e40dd057ea85",
                            tag = "INNTEKTSKILDE_JORDBRUKER_ER_DU_SYKMELDT",
                            sporsmalstekst = "Er du sykmeldt fra dette?",
                            svartype = SvartypeDTO.JA_NEI,
                            svar = emptyList(),
                            undersporsmal = emptyList()
                        )
                    )
                ),
                SporsmalDTO(
                    id = "a04f9037-59bb-3395-9bc0-4b8cb738cce3",
                    tag = "INNTEKTSKILDE_FRILANSER",
                    sporsmalstekst = "frilanser",
                    svartype = SvartypeDTO.CHECKBOX,
                    kriterieForVisningAvUndersporsmal = CHECKED,
                    svar = emptyList(),
                    undersporsmal = listOf(
                        SporsmalDTO(
                            id = "54071a77-2425-3a8a-87d4-529db6b3cf73",
                            tag = "INNTEKTSKILDE_FRILANSER_ER_DU_SYKMELDT",
                            sporsmalstekst = "Er du sykmeldt fra dette?",
                            svartype = SvartypeDTO.JA_NEI,
                            svar = emptyList(),
                            undersporsmal = emptyList()
                        )
                    )
                ),
                SporsmalDTO(
                    id = "984bc8b6-5b79-3fc0-a005-a5f487821ac6",
                    tag = "INNTEKTSKILDE_ANNET",
                    sporsmalstekst = "annet",
                    svartype = SvartypeDTO.CHECKBOX,
                    svar = listOf(SvarDTO(verdi = "CHECKED")),
                    undersporsmal = emptyList()
                )
            )
        )
    )
)
