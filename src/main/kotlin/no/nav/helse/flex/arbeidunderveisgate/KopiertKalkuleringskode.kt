package no.nav.helse.flex.arbeidunderveisgate

import no.nav.helse.flex.sykepengesoknad.kafka.FravarDTO
import no.nav.helse.flex.sykepengesoknad.kafka.SoknadsperiodeDTO
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.HashSet

internal fun getFaktiskGrad(
    faktiskTimer: Double?,
    avtaltTimer: Double?,
    periode: SoknadsperiodeDTO,
    ferieOgPermisjonPerioder: List<FravarDTO>,
    arbeidgjenopptattDato: LocalDate?
): Int? {
    val antallVirkedagerPerUke = 5

    val virkedager = antallVirkedagerIPeriode(periode, arbeidgjenopptattDato) - antallVirkedagerIPerioder(ferieOgPermisjonPerioder, periode)

    return if (faktiskTimer == null || avtaltTimer == null || virkedager == 0) {
        null
    } else Math.toIntExact(Math.round(faktiskTimer / (avtaltTimer / antallVirkedagerPerUke * virkedager) * 100))
}

private fun antallVirkedagerIPeriode(periode: SoknadsperiodeDTO, arbeidgjenopptattDato: LocalDate?): Int {
    var virkedager = 0

    val slutt = if (arbeidgjenopptattDato == null)
        Math.toIntExact(ChronoUnit.DAYS.between(periode.fom, periode.tom) + 1)
    else
        Math.toIntExact(ChronoUnit.DAYS.between(periode.fom, arbeidgjenopptattDato))

    for (i in 0 until slutt) {
        if (erIkkeHelgedag(periode.fom!!.plusDays(i.toLong()))) {
            virkedager++
        }
    }

    return virkedager
}

private fun erIkkeHelgedag(dag: LocalDate): Boolean {
    return dag.dayOfWeek != DayOfWeek.SATURDAY && dag.dayOfWeek != DayOfWeek.SUNDAY
}

private fun antallVirkedagerIPerioder(ferieOgPermisjonPerioder: List<FravarDTO>, soknadsperiode: SoknadsperiodeDTO): Int {
    val virkedager = HashSet<LocalDate>()

    ferieOgPermisjonPerioder.forEach { (fom, tom) ->
        val slutt = Math.toIntExact(ChronoUnit.DAYS.between(fom!!, tom) + 1)

        for (i in 0 until slutt) {
            val current = fom.plusDays(i.toLong())
            if (erIkkeHelgedag(current) &&
                !current.isBefore(soknadsperiode.fom) &&
                !current.isAfter(soknadsperiode.tom)
            ) {
                virkedager.add(current)
            }
        }
    }

    return virkedager.size
}
