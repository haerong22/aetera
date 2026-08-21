package io.aetera.model.renewal

import java.time.LocalDate

enum class RenewalCycle(
    private val months: Long,
) {
    NONE(0),
    MONTHLY(1),
    QUARTERLY(3),
    HALF_YEARLY(6),
    YEARLY(12),
    TWO_YEARS(24),
    FIVE_YEARS(60),
    TEN_YEARS(120),
    ;

    val repeats: Boolean get() = this != NONE

    fun nextFrom(date: LocalDate): LocalDate = date.plusMonths(months)
}
