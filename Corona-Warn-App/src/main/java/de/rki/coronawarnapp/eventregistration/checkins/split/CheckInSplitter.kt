package de.rki.coronawarnapp.eventregistration.checkins.split

import de.rki.coronawarnapp.eventregistration.checkins.CheckIn

/**
 * Splits a [CheckIn] into multiple [CheckIn]s if it duration between its start and end times
 * are across many days.
 *
 * @return [List] of [CheckIn]s
 */
fun CheckIn.splitByMidnight(): List<CheckIn> {

    return emptyList()
}
