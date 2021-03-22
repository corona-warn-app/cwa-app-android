package de.rki.coronawarnapp.eventregistration.checkins.riskcalculation

// converts number of 10min intervals into milliseconds
internal fun Int.tenMinIntervalToMillis() = this * MILLIS_IN_MIN

// converts number of 10min intervals into milliseconds
internal fun Long.millisToTenMinInterval() = this / MILLIS_IN_MIN

private const val MILLIS_IN_MIN = 600L * 1000L
